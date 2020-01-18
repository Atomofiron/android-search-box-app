package ru.atomofiron.regextool.common.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

open class GeneralHolder<D : Any>(view: View) : RecyclerView.ViewHolder(view) {
    protected lateinit var item: D

    constructor(parent: ViewGroup, id: Int)
            : this(LayoutInflater.from(parent.context).inflate(id, parent, false))

    fun bind(item: D, position: Int) {
        this.item = item
        onBind(item, position)
    }

    open fun onBind(item: D, position: Int) = Unit
}