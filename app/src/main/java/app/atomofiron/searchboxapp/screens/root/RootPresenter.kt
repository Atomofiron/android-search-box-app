package app.atomofiron.searchboxapp.screens.root

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.root.fragment.SnackbarCallbackFragmentDelegate
import app.atomofiron.searchboxapp.screens.root.util.tasks.XTask
import app.atomofiron.searchboxapp.utils.Shell

class RootPresenter(
        viewModel: RootViewModel,
        router: RootRouter,
        preferenceStore: PreferenceStore
) : BasePresenter<RootViewModel, RootRouter>(viewModel, router),
        SnackbarCallbackFragmentDelegate.SnackbarCallbackOutput
{
    override var isExitSnackbarShown: Boolean = false

    init {
        router.showMainIfEmpty()

        preferenceStore.appTheme.addObserver(onClearedCallback) {
            viewModel.setTheme.invoke(it)
            router.reattachFragments()
        }
        preferenceStore.appOrientation.addObserver(onClearedCallback) {
            viewModel.setOrientation.invoke(it)
        }
        preferenceStore.joystickComposition.addObserver(onClearedCallback) {
            viewModel.setJoystick.value = it
        }
        preferenceStore.toyboxVariant.addObserver(onClearedCallback) {
            Shell.toyboxPath = it.toyboxPath
        }
        viewModel.tasks.value = Array(16) { XTask() }.toList()
    }

    fun onJoystickClick() {
        when {
            router.onBack() -> Unit
            else -> viewModel.showExitSnackbar()
        }
    }

    fun onExitClick() = router.closeApp()

    fun onBackButtonClick() {
        when {
            router.onBack() -> Unit
            isExitSnackbarShown -> router.closeApp()
            else -> viewModel.showExitSnackbar()
        }
    }
}