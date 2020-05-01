package ru.atomofiron.regextool.injectable.interactor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.atomofiron.regextool.injectable.service.ResultService
import ru.atomofiron.regextool.model.explorer.XFile
import ru.atomofiron.regextool.model.finder.FinderResult
import java.util.*

class ResultInteractor(
        private val scope: CoroutineScope,
        private val resultService: ResultService
) {
    private val context = Dispatchers.IO

    fun stop(uuid: UUID) = resultService.stop(uuid)

    fun copyToClipboard(finderResult: FinderResult) = resultService.copyToClipboard(finderResult)

    fun deleteItems(items: List<XFile>, uuid: UUID) {
        scope.launch(context) {
            resultService.deleteItems(items, uuid)
        }
    }
}