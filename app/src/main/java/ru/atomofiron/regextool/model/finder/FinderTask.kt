package ru.atomofiron.regextool.model.finder

import java.util.*

interface FinderTask {
    val id: Long
    val uuid: UUID
    val results: List<FinderResult>
    val count: Int
    val inProgress: Boolean
    val isDone: Boolean

    fun copyTask(): FinderTask
    fun areContentsTheSame(other: FinderTask): Boolean
}