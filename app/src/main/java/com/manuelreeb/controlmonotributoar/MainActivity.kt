package com.reeb.controlmonotributoar

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.Info
import com.reeb.controlmonotributoar.data.repository.PerfilRepository
import com.reeb.controlmonotributoar.data.repository.ClienteRepository
import com.reeb.controlmonotributoar.domain.model.UserRole
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.lifecycleScope
import com.reeb.controlmonotributoar.data.local.AppDatabase
import com.reeb.controlmonotributoar.data.remote.RemoteConfigHolder
import com.reeb.controlmonotributoar.data.repository.AlertaRepository
import com.reeb.controlmonotributoar.data.repository.AuthRepository
import com.reeb.controlmonotributoar.data.work.RecordatorioScheduler
import com.reeb.controlmonotributoar.data.work.ClienteSyncWorker
import com.reeb.controlmonotributoar.data.work.UpdateCheckWorker
import com.reeb.controlmonotributoar.navigation.AppNavGraph
import com.reeb.controlmonotributoar.navigation.Routes
import com.reeb.controlmonotributoar.ui.components.DonacionesDialog
import com.reeb.controlmonotributoar.ui.theme.LímiteMonotributoTheme
import com.reeb.controlmonotributoar.utils.NotificacionHelper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Celeste     = Color(0xFF75AADB)
private val TextoOscuro = Color(0xFF0D2A4A)

class MainActivity : ComponentActivity() {

    private val pedirPermisoNotif =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge compatible con Android 15+ sin usar APIs de color de barras obsoletas.
        WindowCompat.setDecorFitsSystemWindows(window, false)

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

        // Programa chequeo periódico de actualizaciones de tarifas (cada 24 horas).
        UpdateCheckWorker.schedulePeriodicCheck(this)

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
    val clienteRepo = remember { ClienteRepository(AppDatabase.getInstance(context).clienteDao()) }
    val rolFlow = remember { perfilRepo.observar().map { UserRole.fromName(it?.rol) } }
    val rolActual by rolFlow.collectAsStateWithLifecycle(initialValue = UserRole.PERSONAL)
    val porVencerFlow = remember(clienteRepo) {
        clienteRepo.observarTodos().map { lista ->
            val ahora = System.currentTimeMillis()
            val sieteDias = ahora + 7L * 24 * 60 * 60 * 1000
            lista.count { it.proximoVencimientoEpoch in (ahora + 1)..sieteDias }
        }
    }
    val clientesPorVencerCount by porVencerFlow.collectAsStateWithLifecycle(initialValue = 0)

    val navItems = remember(rolActual) {
        buildList {
            add(NavItem("Inicio",    Icons.Default.Home,                 Routes.HOME))
            add(NavItem("Ingresos",  Icons.AutoMirrored.Filled.List,     Routes.MOVIMIENTOS))
            add(NavItem("Categoría", Icons.Default.Star,                 Routes.CATEGORIA))
            if (rolActual == UserRole.CONTADOR) {
                add(NavItem("Clientes", Icons.Default.Group, Routes.CLIENTES))
                add(NavItem("Cobranzas", Icons.Default.AttachMoney, Routes.COBRANZAS))
            }
            add(NavItem("Perfil",    Icons.Default.Person,               Routes.PERFIL))
        }
    }

    val hiddenRoutes  = setOf(Routes.SPLASH, Routes.LOGIN, Routes.SELECCION_CATEGORIA)
    val showChrome    = currentRoute !in hiddenRoutes
    val showGlobalTopBar = showChrome && currentRoute != Routes.HOME
    val isClientesRoute = currentRoute?.startsWith(Routes.CLIENTES) == true

    // Rutas raíz (las de la BottomNav) → no muestran flecha back
    val rootRoutes = navItems.map { it.route }.toSet() + Routes.CLIENTES
    val showBack   = showChrome && currentRoute != null && currentRoute !in rootRoutes && !isClientesRoute

    // Título dinámico
    val screenTitle: String? = when (currentRoute) {
        Routes.HOME         -> null // en Home usamos app bar propia
        Routes.MOVIMIENTOS  -> "Ingresos y gastos"
        Routes.CATEGORIA    -> "Categoría"
        Routes.ALERTAS      -> "Alertas"
        Routes.PERFIL       -> "Perfil"
        Routes.CLIENTES     -> "Clientes"
        Routes.COBRANZAS    -> "Cobranzas"
        Routes.CALENDARIO   -> "Calendario"
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
            if (showGlobalTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            screenTitle ?: "Inicio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
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
                    containerColor = Color(0xFFF6FAFF),
                    contentColor   = Color(0xFF1B3552),
                    tonalElevation = 6.dp
                ) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = (currentRoute == item.route) || (item.route == Routes.CLIENTES && isClientesRoute),
                            onClick  = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                }
                            },
                            icon  = {
                                if (item.route == Routes.CLIENTES && clientesPorVencerCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = Color(0xFFE53935),
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    if (clientesPorVencerCount > 99) "99+" else clientesPorVencerCount.toString(),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(item.icon, contentDescription = item.label, tint = if (currentRoute == item.route) Color(0xFF0B6CC4) else Color(0xFF4E6178))
                                    }
                                } else {
                                    Icon(item.icon, contentDescription = item.label, tint = if (currentRoute == item.route) Color(0xFF0B6CC4) else Color(0xFF4E6178))
                                }
                            },
                            label = { Text(item.label, color = if (currentRoute == item.route) Color(0xFF0B6CC4) else Color(0xFF4E6178)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = Color(0xFF0B6CC4),
                                unselectedIconColor = Color(0xFF4E6178),
                                selectedTextColor   = Color(0xFF0B6CC4),
                                unselectedTextColor = Color(0xFF4E6178),
                                indicatorColor      = Color(0xFFD9ECFF)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220)))
                        .togetherWith(fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 1.01f, animationSpec = tween(150)))
                },
                label = "route_shared_axis"
            ) {
                AppNavGraph(
                    navController = navController,
                    onAbrirDonaciones = { showDonaciones = true }
                )
            }
        }
    }
}
