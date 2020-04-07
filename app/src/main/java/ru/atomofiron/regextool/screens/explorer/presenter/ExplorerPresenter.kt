package ru.atomofiron.regextool.screens.explorer.presenter

import app.atomofiron.common.base.BasePresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.injectable.interactor.ExplorerInteractor
import ru.atomofiron.regextool.injectable.service.explorer.model.Change
import ru.atomofiron.regextool.injectable.service.explorer.model.XFile
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.SettingsStore
import ru.atomofiron.regextool.screens.explorer.ExplorerRouter
import ru.atomofiron.regextool.screens.explorer.ExplorerViewModel
import ru.atomofiron.regextool.screens.explorer.adapter.ExplorerItemActionListener
import ru.atomofiron.regextool.screens.explorer.places.PlacesAdapter
import ru.atomofiron.regextool.screens.explorer.places.XPlace
import ru.atomofiron.regextool.screens.explorer.sheet.BottomSheetMenuWithTitle
import ru.atomofiron.regextool.view.custom.bottom_sheet_menu.BottomSheetMenuListener

class ExplorerPresenter(
        viewModel: ExplorerViewModel,
        override val router: ExplorerRouter,
        private val explorerStore: ExplorerStore,
        private val settingsStore: SettingsStore,
        private val explorerInteractor: ExplorerInteractor,
        private val itemListener: ExplorerItemActionListenerDelegate,
        private val placesListener: PlacesActionListenerDelegate,
        private val menuListener: BottomSheetMenuListenerDelegate
) : BasePresenter<ExplorerViewModel, ExplorerRouter>(viewModel),
        ExplorerItemActionListener by itemListener,
        PlacesAdapter.ItemActionListener by placesListener,
        BottomSheetMenuListener by menuListener {

    init {
        settingsStore.dockGravity.addObserver(onClearedCallback, ::onDockGravityChanged)
        settingsStore.storagePath.addObserver(onClearedCallback, ::onStoragePathChanged)
        settingsStore.explorerItem.addObserver(onClearedCallback, viewModel.itemComposition::setValue)

        val items = ArrayList<XPlace>()
        items.add(XPlace.InternalStorage(context.getString(R.string.internal_storage), visible = true))
        items.add(XPlace.ExternalStorage(context.getString(R.string.external_storage), visible = true))
        items.add(XPlace.AnotherPlace("Another Place 0"))
        items.add(XPlace.AnotherPlace("Another Place 1"))
        items.add(XPlace.AnotherPlace("Another Place 2"))
        viewModel.places.value = items

        onSubscribeData()
    }

    override fun onSubscribeData() {
        explorerStore.store.addObserver(onClearedCallback) {
            GlobalScope.launch(Dispatchers.Main) {
                viewModel.items.value = it
            }
        }
        explorerStore.updates.addObserver(onClearedCallback) {
            GlobalScope.launch(Dispatchers.Main) {
                when (it) {
                    is Change.Update -> viewModel.notifyUpdate(it.item)
                    is Change.Remove -> viewModel.notifyRemove(it.item)
                    is Change.Insert -> viewModel.notifyInsert(Pair(it.previous, it.item))
                    is Change.UpdateRange -> viewModel.notifyUpdateRange(it.items)
                    is Change.RemoveRange -> viewModel.notifyRemoveRange(it.items)
                    is Change.InsertRange -> viewModel.notifyInsertRange(Pair(it.previous, it.items))
                }
            }
        }
        explorerStore.current.addObserver(onClearedCallback) {
            GlobalScope.launch(Dispatchers.Main) {
                viewModel.current.value = it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        explorerInteractor.scope.cancel("${this.javaClass.simpleName}.onCleared()")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        itemListener.permissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun onDockGravityChanged(gravity: Int) {
        viewModel.historyDrawerGravity.value = gravity
    }

    private fun onStoragePathChanged(path: String) = explorerInteractor.setRoot(path)

    fun onSearchOptionSelected() = router.showFinder()

    fun onOptionsOptionSelected() {
        val current = explorerStore.current.value
        val files = when {
            explorerStore.checked.isNotEmpty() -> ArrayList(explorerStore.checked)
            current != null -> arrayListOf(current)
            else -> return
        }
        val ids = when {
            files.size > 1 -> viewModel.manyFilesOptions
            files[0].isChecked -> viewModel.manyFilesOptions
            files[0].isDirectory -> viewModel.directoryOptions
            else -> viewModel.oneFileOptions
        }
        viewModel.showOptions.invoke(BottomSheetMenuWithTitle.ExplorerItemOptions(ids, files, viewModel.itemComposition.value))
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onDockGravityChange(gravity: Int) = settingsStore.dockGravity.push(gravity)

    fun onCreateClick(dir: XFile, name: String, directory: Boolean) {
        // todo next
    }

    fun onRenameClick(item: XFile, name: String) {
        // todo next
    }

    fun onAllowStorageClick() = router.showSystemPermissionsAppSettings()

    fun onVolumeUp() {
        explorerInteractor.openParent()
        viewModel.scrollToCurrentDir.invoke()
    }
}