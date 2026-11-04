package com.dreams.scanner.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dreams.scanner.AppContainer
import com.dreams.scanner.ui.document.DocumentScreen
import com.dreams.scanner.ui.document.DocumentViewModel
import com.dreams.scanner.ui.folder.FolderScreen
import com.dreams.scanner.ui.folder.FolderViewModel
import com.dreams.scanner.ui.home.HomeScreen
import com.dreams.scanner.ui.home.HomeViewModel
import com.dreams.scanner.ui.settings.SettingsScreen
import com.dreams.scanner.ui.settings.SettingsViewModel

private object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val FOLDER = "folder/{id}"
    const val DOCUMENT = "document/{id}"
    fun folder(id: Long) = "folder/$id"
    fun document(id: Long) = "document/$id"
}

@Composable
fun AppNavHost(container: AppContainer) {
    val nav = rememberNavController()
    val idArgs = listOf(navArgument("id") { type = NavType.LongType })

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            val vm = viewModel<HomeViewModel>(
                factory = factoryOf { HomeViewModel(container.repository, container.settings) }
            )
            HomeScreen(
                vm = vm,
                onOpenDocument = { nav.navigate(Routes.document(it)) },
                onOpenFolder = { nav.navigate(Routes.folder(it)) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.FOLDER, arguments = idArgs) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            val vm = viewModel<FolderViewModel>(
                key = "folder_$id",
                factory = factoryOf { FolderViewModel(container.repository, id) }
            )
            FolderScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onOpenDocument = { nav.navigate(Routes.document(it)) }
            )
        }

        composable(Routes.DOCUMENT, arguments = idArgs) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            val vm = viewModel<DocumentViewModel>(
                key = "document_$id",
                factory = factoryOf { DocumentViewModel(container.repository, id) }
            )
            DocumentScreen(vm = vm, onBack = { nav.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            val vm = viewModel<SettingsViewModel>(
                factory = factoryOf { SettingsViewModel(container.settings) }
            )
            SettingsScreen(vm = vm, onBack = { nav.popBackStack() })
        }
    }
}
