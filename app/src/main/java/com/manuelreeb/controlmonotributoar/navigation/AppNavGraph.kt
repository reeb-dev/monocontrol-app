package com.reeb.controlmonotributoar.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.reeb.controlmonotributoar.data.local.AppDatabase
import com.reeb.controlmonotributoar.data.repository.AlertaRepository
import com.reeb.controlmonotributoar.data.repository.AuthRepository
import com.reeb.controlmonotributoar.data.repository.ClienteRepository
import com.reeb.controlmonotributoar.data.repository.MovimientoRepository
import com.reeb.controlmonotributoar.data.repository.PerfilRepository
import com.reeb.controlmonotributoar.domain.usecase.AgregarMovimientoUseCase
import com.reeb.controlmonotributoar.domain.usecase.GenerarAlertasUseCase
import com.reeb.controlmonotributoar.domain.usecase.ObtenerResumenMensualUseCase
import com.reeb.controlmonotributoar.ui.login.AuthViewModel
import com.reeb.controlmonotributoar.ui.login.LoginScreen
import com.reeb.controlmonotributoar.ui.login.RegisterScreen
import com.reeb.controlmonotributoar.ui.onboarding.OnboardingScreen
import com.reeb.controlmonotributoar.ui.onboarding.SeleccionCategoriaScreen
import com.reeb.controlmonotributoar.ui.splash.SplashScreen
import com.reeb.controlmonotributoar.ui.alertas.AlertasScreen
import com.reeb.controlmonotributoar.ui.alertas.AlertasViewModel
import com.reeb.controlmonotributoar.ui.categoria.CategoriaScreen
import com.reeb.controlmonotributoar.ui.categoria.CategoriaViewModel
import com.reeb.controlmonotributoar.ui.clientes.ClienteDetalleScreen
import com.reeb.controlmonotributoar.ui.clientes.ClienteDetalleViewModel
import com.reeb.controlmonotributoar.ui.clientes.ClientesScreen
import com.reeb.controlmonotributoar.ui.clientes.ClientesViewModel
import com.reeb.controlmonotributoar.ui.clientes.ConfigurarEnvioScreen
import com.reeb.controlmonotributoar.ui.clientes.ConfigurarEnvioViewModel
import com.reeb.controlmonotributoar.ui.home.HomeScreen
import com.reeb.controlmonotributoar.ui.home.HomeViewModel
import com.reeb.controlmonotributoar.ui.movimientos.MovimientoScreen
import com.reeb.controlmonotributoar.ui.movimientos.MovimientoViewModel
import com.reeb.controlmonotributoar.ui.excel.ExcelScreen
import com.reeb.controlmonotributoar.ui.excel.ExcelViewModel
import com.reeb.controlmonotributoar.ui.simulacion.SimulacionScreen
import com.reeb.controlmonotributoar.ui.perfil.PerfilScreen
import com.reeb.controlmonotributoar.ui.perfil.PerfilViewModel
import com.reeb.controlmonotributoar.ui.reportes.ReportesScreen
import com.reeb.controlmonotributoar.ui.seguridad.Seguridad2FAScreen
import com.reeb.controlmonotributoar.ui.notificaciones.NotificacionesPushScreen
import com.reeb.controlmonotributoar.ui.panelclientes.PanelClientesScreen
import com.reeb.controlmonotributoar.ui.panelclientes.PanelClientesViewModel
import com.reeb.controlmonotributoar.ui.cobranzas.CobranzasScreen
import com.reeb.controlmonotributoar.ui.calendario.CalendarioFeriadosScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Routes {
    const val SPLASH             = "splash"
    const val LOGIN              = "login"
    const val REGISTER           = "register"
    const val SELECCION_CATEGORIA = "seleccion_categoria"
    const val ONBOARDING         = "onboarding"
    const val HOME               = "home"
    const val MOVIMIENTOS        = "movimientos"
    const val CATEGORIA          = "categoria"
    const val ALERTAS            = "alertas"
    const val EXCEL              = "excel"
    const val SIMULACION         = "simulacion"
    const val PERFIL             = "perfil"
    const val CLIENTES           = "clientes"
    const val CLIENTES_FILTRADO  = "clientes?filtro={filtro}"
    const val CLIENTE_DETALLE    = "cliente_detalle/{clienteId}"
    const val CLIENTE_ENVIO      = "cliente_envio/{clienteId}"
    // ── Nuevas rutas ────────────────────────────────────────────────
    const val REPORTES           = "reportes"
    const val SEGURIDAD_2FA      = "seguridad_2fa"
    const val NOTIFICACIONES_PUSH = "notificaciones_push"
    const val PANEL_CLIENTES     = "panel_clientes"
    const val COBRANZAS          = "cobranzas"
    const val CALENDARIO         = "calendario"

    fun clienteDetalle(clienteId: Long) = "cliente_detalle/$clienteId"
    fun clienteEnvio(clienteId: Long) = "cliente_envio/$clienteId"
    fun clientesFiltrados(filtro: String) = "clientes?filtro=$filtro"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onAbrirDonaciones: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val movimientoRepo = remember { MovimientoRepository(db.movimientoDao()) }
    val alertaRepo = remember { AlertaRepository(db.alertaDao()) }
    val perfilRepo = remember { PerfilRepository(db.perfilDao()) }
    val clienteRepo = remember { ClienteRepository(db.clienteDao()) }
    val authRepo = remember { AuthRepository() }
    val credenciales = remember { com.reeb.controlmonotributoar.data.local.CredencialesStore(context) }
    val agregarUseCase = remember { AgregarMovimientoUseCase(movimientoRepo) }
    val generarAlertasUseCase = remember {
        GenerarAlertasUseCase(movimientoRepo, alertaRepo, perfilRepo, context.applicationContext)
    }
    val obtenerResumenUseCase = remember { ObtenerResumenMensualUseCase(movimientoRepo) }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(onSplashFinished = {
                val destino = if (authRepo.estaLogueado) Routes.HOME else Routes.LOGIN
                navController.navigate(destino) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.LOGIN) {
            val authVm = remember { AuthViewModel(authRepo, perfilRepo, credenciales) }
            LoginScreen(
                viewModel = authVm,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onIrARegistro = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            val authVm = remember { AuthViewModel(authRepo, perfilRepo, credenciales) }
            RegisterScreen(
                viewModel = authVm,
                onRegistroExitoso = {
                    // Tras registrarse, mandamos al onboarding para precargar nombre/CUIT/rubro.
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onVolverALogin = { navController.popBackStack() }
            )
        }

        composable(Routes.ONBOARDING) {
            val vm = remember { PerfilViewModel(perfilRepo, clienteRepo) }
            OnboardingScreen(
                viewModel = vm,
                onListo = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SELECCION_CATEGORIA) {
            SeleccionCategoriaScreen(onCategoriaSeleccionada = { _ ->
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SELECCION_CATEGORIA) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) {
            val vm = remember {
                HomeViewModel(
                    obtenerResumenUseCase,
                    generarAlertasUseCase,
                    perfilRepo,
                    clienteRepo,
                    movimientoRepo,
                    alertaRepo
                )
            }
            // Helper: navegar a una ruta raíz reutilizando back stack (mismo comportamiento que la BottomNav)
            val navToRoot: (String) -> Unit = { route ->
                navController.navigate(route) {
                    popUpTo(Routes.HOME) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            HomeScreen(
                viewModel = vm,
                onAgregarIngreso = { navToRoot(Routes.MOVIMIENTOS) },
                onVerAlertas = { navToRoot(Routes.ALERTAS) },
                onAgregarGasto = { navToRoot(Routes.MOVIMIENTOS) },
                onAgregarCliente = { navController.navigate(Routes.CLIENTES) }, // detalle: permite back a Home
                onIrCategoria = { navToRoot(Routes.CATEGORIA) },
                onAbrirPanelClientes = { navController.navigate(Routes.PANEL_CLIENTES) },
                onAbrirCobranzas = { navController.navigate(Routes.COBRANZAS) },
                onIrClientesPorVencer = { navController.navigate(Routes.clientesFiltrados("por_vencer")) },
                onIrClientesSinEmail = { navController.navigate(Routes.clientesFiltrados("sin_email")) }
            )
        }

        composable(Routes.MOVIMIENTOS) {
            val vm = remember {
                MovimientoViewModel(agregarUseCase, generarAlertasUseCase, movimientoRepo, perfilRepo)
            }
            MovimientoScreen(viewModel = vm)
        }

        composable(Routes.CATEGORIA) {
            val vm = remember { CategoriaViewModel(movimientoRepo, perfilRepo, clienteRepo) }
            CategoriaScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.ALERTAS) {
            val vm = remember { AlertasViewModel(alertaRepo) }
            AlertasScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.EXCEL) {
            val vm = remember { ExcelViewModel(movimientoRepo, alertaRepo, generarAlertasUseCase) }
            ExcelScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.SIMULACION) {
            SimulacionScreen()
        }

        composable(Routes.PERFIL) {
            val vm = remember { PerfilViewModel(perfilRepo, clienteRepo) }
            val homeVm = remember { HomeViewModel(obtenerResumenUseCase, generarAlertasUseCase, perfilRepo, clienteRepo) }
            val resumen by homeVm.resumen.collectAsStateWithLifecycle()
            PerfilScreen(
                viewModel = vm,
                resumen = resumen,
                onIrAExcel = { navController.navigate(Routes.EXCEL) },
                onIrASimulacion = { navController.navigate(Routes.SIMULACION) },
                onIrAClientes = { navController.navigate(Routes.CLIENTES) },
                onIrCalendario = { navController.navigate(Routes.CALENDARIO) },
                onAbrirDonaciones = onAbrirDonaciones,
                onCerrarSesion = {
                    authRepo.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onEliminarCuenta = {
                    // Implementar eliminación completa de cuenta
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Eliminar datos locales
                            perfilRepo.eliminarTodo()
                            movimientoRepo.eliminarTodo()
                            clienteRepo.eliminarTodo()
                            alertaRepo.eliminarTodas()

                            // Cerrar sesión
                            authRepo.signOut()

                            // Redirigir al login
                            withContext(Dispatchers.Main) {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        } catch (e: Exception) {
                            // En caso de error, al menos cerrar sesión
                            authRepo.signOut()
                            withContext(Dispatchers.Main) {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            )
        }

        composable(
            route = Routes.CLIENTES_FILTRADO,
            arguments = listOf(navArgument("filtro") {
                type = NavType.StringType
                defaultValue = "todos"
            })
        ) { backStackEntry ->
            val filtroInicial = backStackEntry.arguments?.getString("filtro") ?: "todos"
            val vm = remember { ClientesViewModel(clienteRepo, perfilRepo, movimientoRepo) }
            ClientesScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onVerDetalle = { clienteId ->
                    navController.navigate(Routes.clienteDetalle(clienteId))
                },
                onAbrirPanel = { navController.navigate(Routes.PANEL_CLIENTES) },
                initialFiltro = filtroInicial
            )
        }

        composable(
            Routes.CLIENTE_DETALLE,
            arguments = listOf(navArgument("clienteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getLong("clienteId") ?: return@composable
            val vmDetalle = remember { ClienteDetalleViewModel(clienteRepo) }
            val vmClientes = remember { ClientesViewModel(clienteRepo, perfilRepo, movimientoRepo) }

            ClienteDetalleScreen(
                clienteId = clienteId,
                viewModel = vmDetalle,
                viewModelClientes = vmClientes,
                onBack = { navController.popBackStack() },
                onEnviarEmail = { /* callback opcional */ },
                onConfigurarEnvio = { id: Long ->
                    navController.navigate(Routes.clienteEnvio(id))
                }
            )
        }

        composable(
            Routes.CLIENTE_ENVIO,
            arguments = listOf(navArgument("clienteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getLong("clienteId") ?: return@composable
            val vmEnvio = remember { ConfigurarEnvioViewModel(clienteRepo) }
            val vmClientes = remember { ClientesViewModel(clienteRepo, perfilRepo, movimientoRepo) }

            ConfigurarEnvioScreen(
                clienteId = clienteId,
                viewModel = vmEnvio,
                viewModelClientes = vmClientes,
                onBack = { navController.popBackStack() }
            )
        }

        // ── NUEVAS RUTAS ──────────────────────────────────────────────

        composable(Routes.REPORTES) {
            ReportesScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SEGURIDAD_2FA) {
            Seguridad2FAScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.NOTIFICACIONES_PUSH) {
            NotificacionesPushScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PANEL_CLIENTES) {
            val vm = remember { PanelClientesViewModel(clienteRepo, movimientoRepo) }
            PanelClientesScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onAbrirCobranzas = { navController.navigate(Routes.COBRANZAS) }
            )
        }

        composable(Routes.COBRANZAS) {
            val vm = remember { ClientesViewModel(clienteRepo, perfilRepo, movimientoRepo) }
            LaunchedEffect(Unit) { vm.onFiltroChange("honorarios_pendientes") }
            CobranzasScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.CALENDARIO) {
            CalendarioFeriadosScreen(onBack = { navController.popBackStack() })
        }
    }
}
