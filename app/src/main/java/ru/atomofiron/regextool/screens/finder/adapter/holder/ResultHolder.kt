package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class ResultHolder(parent: ViewGroup, id: Int, listener: OnActionListener) : CardViewHolder(parent, id) {
    private val ivIcon = itemView.findViewById<ImageView>(R.id.item_iv_icon)
    private val tvTitle = itemView.findViewById<TextView>(R.id.item_tv_title)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(item as FinderStateItem.ResultItem)
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.ResultItem
        tvTitle.text = item.target.completedPath
    }

    interface OnActionListener {
        fun onItemClick(item: FinderStateItem.ResultItem)
    }
}