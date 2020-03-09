package app.atomofiron.common.base

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.common.util.hideKeyboard
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.log2
import kotlin.reflect.KClass

abstract class BaseFragment<M : BaseViewModel<*>> : Fragment(), Backable {
    protected abstract val viewModelClass: KClass<M>
    protected lateinit var viewModel: M
    protected val dataProvider: M get() = viewModel

    protected abstract val layoutId: Int
    protected open val systemBarsColorId: Int = R.color.transparent
    protected open val systemBarsLights: Boolean get() = !context.findBooleanByAttr(R.attr.isDarkTheme)

    protected val theContext get() = requireContext()
    protected val theActivity get() = requireActivity()
    protected val theView get() = requireView()

    protected val anchorView: View get() = activity!!.findViewById(R.id.root_iv_joystick)
    private val visibilityWatcher = VisibilityWatcher()

    init {
        log2("init")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(activity!!).get(viewModelClass.java)
        viewModel.onFragmentAttach(this)
        viewModel.onCreate(context!!, arguments)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = LayoutInflater.from(context).inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onSubscribeData(viewLifecycleOwner)
    }

    override fun onBack(): Boolean {
        val viewWithFocus = view?.findFocus()
        return viewWithFocus?.hideKeyboard() != null
    }

    override fun onStart() {
        super.onStart()

        // todo bad idea do this in onStart()
        setStatusBarColor(systemBarsColorId)
        fixSystemBars(systemBarsLights)
    }

    override fun onResume() {
        super.onResume()
        visibilityWatcher.resumed = true
    }

    override fun onPause() {
        super.onPause()
        visibilityWatcher.resumed = false
    }

    override fun onAttachFragment(childFragment: Fragment) = viewModel.onAttachChildFragment(childFragment)

    open fun onSubscribeData(owner: LifecycleOwner) = Unit

    open fun onVisibleChanged(visible: Boolean) = viewModel.onVisibleChanged(visible)

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {
            setStatusBarColor(systemBarsColorId)
            fixSystemBars(systemBarsLights)
        }
        visibilityWatcher.hidden = hidden
    }

    override fun onDestroy() {
        viewModel.onViewDestroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        viewModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setStatusBarColor(colorId: Int) {
        val activity = activity as AppCompatActivity
        val color = ContextCompat.getColor(activity, colorId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = color
            activity.window.navigationBarColor = color
        }
    }

    private fun fixSystemBars(windowLightBars: Boolean) {
        // fix of the bug, when the flag is not applied by the system
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity!!.window.decorView.apply {
                systemUiVisibility = when {
                    windowLightBars -> systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    else -> systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    systemUiVisibility = when {
                        windowLightBars -> systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        else -> systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                    }
                }
            }
        }
    }

    private inner class VisibilityWatcher {
        private val visible: Boolean get() = !hidden && resumed

        var hidden = false
            set(value) {
                if (field == value) return
                val state = visible
                field = value
                if (state != visible) onVisibleChanged(visible)
            }
        var resumed = false
            set(value) {
                if (field == value) return
                val state = visible
                field = value
                if (state != visible) onVisibleChanged(visible)
            }
    }
}