package app.atomofiron.searchboxapp.screens.result

import android.content.Intent
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.model.finder.FinderQueryParams
import app.atomofiron.searchboxapp.screens.viewer.TextViewerFragment
import app.atomofiron.searchboxapp.utils.Const

class ResultRouter(property: WeakProperty<ResultFragment>) : BaseRouter(property) {
    fun shareFile(title: String, data: String): Boolean {
        val intent = Intent(Intent.ACTION_SEND)
                .setType(Const.MIME_TYPE_TEXT)
                .putExtra(Intent.EXTRA_SUBJECT, title)
                .putExtra(Intent.EXTRA_TITLE, title)
                .putExtra(Intent.EXTRA_TITLE, title)
                .putExtra(Intent.EXTRA_TEXT, data)

        val activity = activity!!
        val success = intent.resolveActivity(activity.packageManager) != null
        if (success) {
            val chooser = Intent.createChooser(intent, "")
            activity.startActivity(chooser)
        }
        return success
    }

    fun openFile(path: String, params: FinderQueryParams?) = startScreen(TextViewerFragment.openTextFile(path, params))
}