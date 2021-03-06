package app.atomofiron.searchboxapp.screens.result

import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.util.LateinitLiveData
import app.atomofiron.common.util.SingleLiveEvent
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.logI
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.finder.FinderTaskChange
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.result.adapter.FinderResultItem

class ResultViewModel : BaseViewModel<ResultComponent, ResultFragment>() {
    val checked = ArrayList<XFile>()

    val oneFileOptions = listOf(R.id.menu_copy_path, R.id.menu_remove)
    val manyFilesOptions = listOf(R.id.menu_remove)

    val task = LateinitLiveData<FinderTask>()
    val composition = LateinitLiveData<ExplorerItemComposition>()
    val enableOptions = LateinitLiveData(false)
    val showOptions = SingleLiveEvent<ExplorerItemOptions>()
    val notifyTaskHasChanged = SingleLiveEvent<Unit>()
    val notifyItemChanged = SingleLiveEvent<FinderResultItem.Item>()

    override val component = DaggerResultComponent
            .builder()
            .bind(this)
            .bind(viewProperty)
            .dependencies(DaggerInjector.appComponent)
            .build()

    override fun inject(view: ResultFragment) {
        super.inject(view)
        component.inject(this)
        component.inject(view)
    }

    fun updateState(update: FinderTaskChange? = null) {
        when (update) {
            null -> notifyTaskHasChanged.invoke()
            is FinderTaskChange.Update -> {
                val task = task.value
                val newTask = update.tasks.find { it.id == task.id }
                when {
                    newTask == null -> logI("[ERROR] newTask == null")
                    !task.areContentsTheSame(newTask) -> this.task.value = newTask.copyTask()
                }
            }
        }
    }
}