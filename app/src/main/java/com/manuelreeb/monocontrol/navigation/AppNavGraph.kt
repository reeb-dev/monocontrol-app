package com.manuelreeb.monocontrol.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.manuelreeb.monocontrol.data.local.AppDatabase
import com.manuelreeb.monocontrol.data.repository.AlertaRepository
import com.manuelreeb.monocontrol.data.repository.AuthRepository
import com.manuelreeb.monocontrol.data.repository.ClienteRepository
import com.manuelreeb.monocontrol.data.repository.MovimientoRepository
import com.manuelreeb.monocontrol.data.repository.PerfilRepository
import com.manuelreeb.monocontrol.domain.usecase.AgregarMovimientoUseCase
import com.manuelreeb.monocontrol.domain.usecase.GenerarAlertasUseCase
import com.manuelreeb.monocontrol.domain.usecase.ObtenerResumenMensualUseCase
import com.manuelreeb.monocontrol.ui.login.AuthViewModel
import com.manuelreeb.monocontrol.ui.login.LoginScreen
import com.manuelreeb.monocontrol.ui.login.RegisterScreen
import com.manuelreeb.monocontrol.ui.onboarding.OnboardingScreen
import com.manuelreeb.monocontrol.ui.onboarding.SeleccionCategoriaScreen
import com.manuelreeb.monocontrol.ui.splash.SplashScreen
import com.manuelreeb.monocontrol.ui.alertas.AlertasScreen
import com.manuelreeb.monocontrol.ui.alertas.AlertasViewModel
import com.manuelreeb.monocontrol.ui.categoria.CategoriaScreen
import com.manuelreeb.monocontrol.ui.categoria.CategoriaViewModel
import com.manuelreeb.monocontrol.ui.clientes.ClienteDetalleScreen
import com.manuelreeb.monocontrol.ui.clientes.ClienteDetalleViewModel
import com.manuelreeb.monocontrol.ui.clientes.ClientesScreen
import com.manuelreeb.monocontrol.ui.clientes.ClientesViewModel
import com.manuelreeb.monocontrol.ui.clientes.ConfigurarEnvioScreen
import com.manuelreeb.monocontrol.ui.clientes.ConfigurarEnvioViewModel
import com.manuelreeb.monocontrol.ui.home.HomeScreen
import com.manuelreeb.monocontrol.ui.home.HomeViewModel
import com.manuelreeb.monocontrol.ui.movimientos.MovimientoScreen
import com.manuelreeb.monocontrol.ui.movimientos.MovimientoViewModel
import com.manuelreeb.monocontrol.ui.excel.ExcelScreen
import com.manuelreeb.monocontrol.ui.excel.ExcelViewModel
import com.manuelreeb.monocontrol.ui.simulacion.SimulacionScreen
import com.manuelreeb.monocontrol.ui.perfil.PerfilScreen
import com.manuelreeb.monocontrol.ui.perfil.PerfilViewModel
import com.manuelreeb.monocontrol.ui.reportes.ReportesScreen
import com.manuelreeb.monocontrol.ui.seguridad.Seguridad2FAScreen
import com.manuelreeb.monocontrol.ui.notificaciones.NotificacionesPushScreen
import com.manuelreeb.monocontrol.ui.panelclientes.PanelClientesScreen
import com.manuelreeb.monocontrol.ui.panelclientes.PanelClientesViewModel

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
    const val CLIENTE_DETALLE    = "cliente_detalle/{clienteId}"
    const val CLIENTE_ENVIO      = "cliente_envio/{clienteId}"
    // ── Nuevas rutas ────────────────────────────────────────────────
    const val REPORTES           = "reportes"
    const val SEGURIDAD_2FA      = "seguridad_2fa"
    const val NOTIFICACIONES_PUSH = "notificaciones_push"
    const val PANEL_CLIENTES     = "panel_clientes"

    fun clienteDetalle(clienteId: Long) = "cliente_detalle/$clienteId"
    fun clienteEnvio(clienteId: Long) = "cliente_envio/$clienteId"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val movimientoRepo = remember { MovimientoRepository(db.movimientoDao()) }
    val alertaRepo = remember { AlertaRepository(db.alertaDao()) }
    val perfilRepo = remember { PerfilRepository(db.perfilDao()) }
    val clienteRepo = remember { ClienteRepository(db.clienteDao()) }
    val authRepo = remember { AuthRepository() }
    val credenciales = remember { com.manuelreeb.monocontrol.data.local.CredencialesStore(context) }
    val agregarUseCase = remember { AgregarMovimientoUseCase(movimientoRepo) }
    val generarAlertasUseCase = remember {
        GenerarAlertasUseCase(movimientoRepo, alertaRepo, context.applicationContext)
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
                HomeViewModel(obtenerResumenUseCase, generarAlertasUseCase, perfilRepo, movimientoRepo, alertaRepo)
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
                onIrCategoria = { navToRoot(Routes.CATEGORIA) }
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
            val homeVm = remember { HomeViewModel(obtenerResumenUseCase, generarAlertasUseCase, perfilRepo) }
            val resumen by homeVm.resumen.collectAsStateWithLifecycle()
            PerfilScreen(
                viewModel = vm,
                resumen = resumen,
                onIrAExcel = { navController.navigate(Routes.EXCEL) },
                onIrASimulacion = { navController.navigate(Routes.SIMULACION) },
                onIrAClientes = { navController.navigate(Routes.CLIENTES) },
                onCerrarSesion = {
                    authRepo.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CLIENTES) {
            val vm = remember { ClientesViewModel(clienteRepo, perfilRepo, movimientoRepo) }
            ClientesScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onVerDetalle = { clienteId ->
                    navController.navigate(Routes.clienteDetalle(clienteId))
                }
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
            PanelClientesScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
