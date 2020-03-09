package app.atomofiron.common.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import app.atomofiron.common.util.KObservable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import ru.atomofiron.regextool.App

abstract class BaseViewModel<R : BaseRouter>(app: Application) : AndroidViewModel(app) {
    protected abstract val router: R
    protected var provider: ViewModelProvider? = null
    val screenScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.Main.immediate)

    protected val app: App get() = getApplication()

    protected val onClearedCallback = KObservable.RemoveObserverCallback()

    open fun onFragmentAttach(fragment: Fragment) {
        router.onFragmentAttach(fragment)
        provider = ViewModelProvider(fragment.activity!!)
    }

    open fun onActivityAttach(activity: Activity) {
        router.onActivityAttach(activity as AppCompatActivity)
        provider = ViewModelProvider(activity)
    }

    fun onCreate(context: Context, arguments: Bundle?) {
        onCreate(context, Intent().putExtras(arguments ?: Bundle()))
    }

    open fun onCreate(context: Context, intent: Intent) = Unit

    open fun onVisibleChanged(visible: Boolean) = Unit

    open fun onViewDestroy() = router.onViewDestroy()

    override fun onCleared() {
        screenScope.cancel()
        provider = null
        onClearedCallback.invoke()
    }

    fun onAttachChildFragment(childFragment: Fragment) = router.onAttachChildFragment(childFragment)

    open fun onBackButtonClick(): Boolean = router.onBack()

    open fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) = Unit
}