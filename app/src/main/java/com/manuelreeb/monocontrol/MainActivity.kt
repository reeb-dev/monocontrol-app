package com.manuelreeb.monocontrol

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.Info
import com.manuelreeb.monocontrol.data.repository.PerfilRepository
import com.manuelreeb.monocontrol.domain.model.UserRole
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.lifecycleScope
import com.manuelreeb.monocontrol.data.local.AppDatabase
import com.manuelreeb.monocontrol.data.remote.RemoteConfigHolder
import com.manuelreeb.monocontrol.data.repository.AlertaRepository
import com.manuelreeb.monocontrol.data.repository.AuthRepository
import com.manuelreeb.monocontrol.data.work.RecordatorioScheduler
import com.manuelreeb.monocontrol.data.work.ClienteSyncWorker
import com.manuelreeb.monocontrol.navigation.AppNavGraph
import com.manuelreeb.monocontrol.navigation.Routes
import com.manuelreeb.monocontrol.ui.components.DonacionesDialog
import com.manuelreeb.monocontrol.ui.theme.LímiteMonotributoTheme
import com.manuelreeb.monocontrol.utils.NotificacionHelper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Celeste     = Color(0xFF75AADB)
private val TextoOscuro = Color(0xFF0D2A4A)

class MainActivity : ComponentActivity() {

    private val pedirPermisoNotif =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Notificaciones
        NotificacionHelper.crearCanal(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pedirPermisoNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Programa el worker periódico que avisa al contador cuándo enviar
        // los recordatorios automáticos a sus clientes.
        RecordatorioScheduler.schedule(this)

        // Sincroniza clientes con Firestore (corre cuando hay red).
        ClienteSyncWorker.encolar(this)

        // Inicializa el RemoteConfig de forma síncrona para que las tarifas
        // del JSON estén cargadas antes de que cualquier pantalla pida un
        // valor de CategoriaMonotributo.limiteAnual / cuotaMensual.
        val configRepo = RemoteConfigHolder.get(this)

        // Intento de actualizar el config remoto (tarifas + donaciones) en background.
        // Si falla por red o por JSON inválido, la app sigue usando el caché o assets.
        lifecycleScope.launch {
            configRepo.refrescar()
        }

        setContent {
            LímiteMonotributoTheme {
                MainApp()
            }
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    // Observamos alertas no leídas para mostrar el badge en la campanita global
    val context = LocalContext.current
    val alertaRepo = remember { AlertaRepository(AppDatabase.getInstance(context).alertaDao()) }
    val noLeidasFlow = remember(alertaRepo) {
        alertaRepo.obtenerTodas().map { lista -> lista.count { !it.leida } }
    }
    val noLeidasCount by noLeidasFlow.collectAsStateWithLifecycle(initialValue = 0)

    // Observar el rol del perfil para mostrar "Clientes" solo a CONTADORs
    val perfilRepo = remember { PerfilRepository(AppDatabase.getInstance(context).perfilDao()) }
    val rolFlow = remember { perfilRepo.observar().map { UserRole.fromName(it?.rol) } }
    val rolActual by rolFlow.collectAsStateWithLifecycle(initialValue = UserRole.PERSONAL)

    val navItems = remember(rolActual) {
        buildList {
            add(NavItem("Inicio",    Icons.Default.Home,                 Routes.HOME))
            add(NavItem("Ingresos",  Icons.AutoMirrored.Filled.List,     Routes.MOVIMIENTOS))
            add(NavItem("Categoría", Icons.Default.Star,                 Routes.CATEGORIA))
            if (rolActual == UserRole.CONTADOR) {
                add(NavItem("Clientes", Icons.Default.Group, Routes.CLIENTES))
            }
            add(NavItem("Perfil",    Icons.Default.Person,               Routes.PERFIL))
        }
    }

    val hiddenRoutes  = setOf(Routes.SPLASH, Routes.LOGIN, Routes.SELECCION_CATEGORIA)
    val showChrome    = currentRoute !in hiddenRoutes

    // Rutas raíz (las de la BottomNav) → no muestran flecha back
    val rootRoutes = navItems.map { it.route }.toSet() + Routes.CLIENTES
    val showBack   = showChrome && currentRoute != null && currentRoute !in rootRoutes

    // Título dinámico
    val screenTitle: String? = when (currentRoute) {
        Routes.HOME         -> null // mostramos el logo
        Routes.MOVIMIENTOS  -> "Ingresos y gastos"
        Routes.CATEGORIA    -> "Categoría"
        Routes.ALERTAS      -> "Alertas"
        Routes.PERFIL       -> "Perfil"
        Routes.CLIENTES     -> "Clientes"
        Routes.EXCEL        -> "Importar / Exportar"
        Routes.SIMULACION   -> "Simulación"
        else                -> null
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDonaciones by remember { mutableStateOf(false) }

    val configRepo = remember { RemoteConfigHolder.get(context) }
    val remoteConfig by configRepo.config.collectAsStateWithLifecycle()

    if (showDonaciones) {
        DonacionesDialog(
            onDismiss = { showDonaciones = false },
            alias = remoteConfig.donaciones.aliasMercadoPago,
            cafecitoUrl = remoteConfig.donaciones.cafecitoUrl,
            cbu = remoteConfig.donaciones.cbu,
            mensaje = remoteConfig.donaciones.mensaje
        )
    }

    // Diálogo de confirmación
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon             = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Celeste) },
            title            = { Text("Cerrar sesión", fontWeight = FontWeight.Bold, color = TextoOscuro) },
            text             = { Text("¿Seguro que querés cerrar sesión?") },
            confirmButton    = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        runCatching { AuthRepository().signOut() }
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Celeste)
                ) { Text("Cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton    = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = TextoOscuro)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (showChrome) {
                TopAppBar(
                    title = {
                        if (screenTitle == null) {
                            Image(
                                painter            = painterResource(id = R.drawable.logo1),
                                contentDescription = "Logo",
                                modifier           = Modifier.size(42.dp)
                            )
                        } else {
                            Text(
                                screenTitle,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        if (showBack) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    actions = {
                        // 🔔 Campanita con contador de alertas no leídas
                        IconButton(onClick = {
                            if (currentRoute != Routes.ALERTAS) {
                                navController.navigate(Routes.ALERTAS) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }) {
                            BadgedBox(
                                badge = {
                                    if (noLeidasCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFFE53935),
                                            contentColor   = Color.White
                                        ) {
                                            Text(
                                                if (noLeidasCount > 99) "99+" else noLeidasCount.toString(),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notificaciones",
                                    tint = Color.White
                                )
                            }
                        }
                        IconButton(onClick = { showDonaciones = true }) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = "Acerca de / Donaciones",
                                tint = Color(0xFFFBB81C)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor       = Celeste,
                        titleContentColor    = Color.White,
                        actionIconContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            if (showChrome) {
                NavigationBar(
                    containerColor = Color(0xFF75AADB),
                    contentColor   = Color.White
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick  = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                }
                            },
                            icon  = { Icon(item.icon, contentDescription = item.label, tint = if (currentRoute == item.route) Color(0xFFFBB81C) else Color.White) },
                            label = { Text(item.label, color = if (currentRoute == item.route) Color(0xFFFBB81C) else Color.White) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = Color(0xFFFBB81C),
                                unselectedIconColor = Color.White,
                                selectedTextColor   = Color(0xFFFBB81C),
                                unselectedTextColor = Color.White,
                                indicatorColor      = Color(0xFF4A86C8)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AppNavGraph(navController)
        }
    }
}
