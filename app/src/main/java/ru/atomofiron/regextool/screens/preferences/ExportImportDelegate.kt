package ru.atomofiron.regextool.screens.preferences

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import ru.atomofiron.regextool.App
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.utils.Shell
import ru.atomofiron.regextool.view.custom.bottom_sheet.BottomSheetView

class ExportImportDelegate(
        private val rootView: View,
        private val anchorView: View
) {
    companion object {
        @SuppressLint("InlinedApi")
        private const val LIST_CONTAINER_ID = android.R.id.list_container

        val isAvailable: Boolean get() = App.context.getExternalFilesDir(null) != null
    }

    private val context = rootView.context
    private val exportSheet = BottomSheetView(context).apply {
        setView(R.layout.layout_export_import)
    }
    private val tvPath = exportSheet.findViewById<TextView>(R.id.lei_tv_path)
    private val rgTarget = exportSheet.findViewById<RadioGroup>(R.id.lei_rg_target)
    private val rgAction = exportSheet.findViewById<RadioGroup>(R.id.lei_rg_action)
    private val button = exportSheet.findViewById<Button>(R.id.lei_btn)

    lateinit var onImportPreferencesListener: () -> Unit
    lateinit var onImportHistoryListener: () -> Unit

    init {
        rootView.findViewById<ViewGroup>(LIST_CONTAINER_ID).addView(exportSheet)

        rgAction.setOnCheckedChangeListener { _, checkedId ->
            val id = when (checkedId) {
                R.id.lei_rb_export -> R.string.export_btn
                R.id.lei_rb_import -> R.string.import_btn
                else -> throw IllegalArgumentException()
            }
            button.setText(id)
        }
        button.setOnClickListener { onButtonClick() }
    }

    private fun onButtonClick() {
        val packageName = context.packageName
        val toybox = App.pathToybox

        val output = when (rgAction.checkedRadioButtonId) {
            R.id.lei_rb_export -> {
                val srcPath = context.applicationInfo.dataDir
                val dstPath = context.getExternalFilesDir(null)!!.absolutePath
                when (rgTarget.checkedRadioButtonId) {
                    R.id.lei_rb_preferences -> exportPreferences(toybox, srcPath, dstPath, packageName)
                    R.id.lei_rb_history -> exportHistory(toybox, srcPath, dstPath)
                    else -> throw IllegalArgumentException()
                }
            }
            R.id.lei_rb_import -> {
                val srcPath = context.getExternalFilesDir(null)!!.absolutePath
                val dstPath = context.applicationInfo.dataDir
                when (rgTarget.checkedRadioButtonId) {
                    R.id.lei_rb_preferences -> importPreferences(toybox, srcPath, dstPath, packageName).apply {
                        if (success) {
                            onImportPreferencesListener.invoke()
                        }
                    }
                    R.id.lei_rb_history -> importHistory(toybox, srcPath, dstPath).apply {
                        if (success) {
                            onImportHistoryListener.invoke()
                        }
                    }
                    else -> throw IllegalArgumentException()
                }
            }
            else -> throw IllegalArgumentException()
        }
        exportSheet.hide()
        showOutput(output)
    }

    fun show() {
        tvPath.text = context.getExternalFilesDir(null)!!.absolutePath
        exportSheet.show()
    }

    private fun exportPreferences(toybox: String, srcPath: String, dstPath: String, packageName: String): Shell.Output {
        return Shell.exec("$toybox cp -Rf $srcPath/shared_prefs/${packageName}_preferences.xml $dstPath/")
    }

    private fun exportHistory(toybox: String, srcPath: String, dstPath: String): Shell.Output {
        return Shell.exec("$toybox cp -Rf $srcPath/databases/history* $dstPath/")
    }

    private fun importPreferences(toybox: String, srcPath: String, dstPath: String, packageName: String): Shell.Output {
        return Shell.exec("$toybox cp -Rf $srcPath/${packageName}_preferences.xml $dstPath/shared_prefs/")
    }

    private fun importHistory(toybox: String, srcPath: String, dstPath: String): Shell.Output {
        return Shell.exec("$toybox cp -Rf $srcPath/history* $dstPath/databases/")
    }

    private fun showOutput(output: Shell.Output) {
        if (output.success) {
            Snackbar.make(rootView, R.string.successful, Snackbar.LENGTH_SHORT)
                    .setAnchorView(anchorView)
                    .show()
        } else {
            Snackbar.make(rootView, R.string.error, Snackbar.LENGTH_SHORT)
                    .setAnchorView(anchorView)
                    .apply {
                        if (output.error.isNotEmpty()) {
                            setAction(R.string.more) {
                                AlertDialog.Builder(context)
                                        .setMessage(output.error)
                                        .show()
                            }
                        }
                    }
                    .show()
        }
    }
}