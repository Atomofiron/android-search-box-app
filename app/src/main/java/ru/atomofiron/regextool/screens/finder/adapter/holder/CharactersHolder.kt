package ru.atomofiron.regextool.screens.finder.adapter.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.recycler.GeneralHolder
import ru.atomofiron.regextool.screens.finder.model.FinderStateItem

class CharactersHolder(
        parent: ViewGroup,
        id: Int,
        private val listener: OnActionListener
) : GeneralHolder<FinderStateItem>(parent, id), View.OnClickListener {

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.SpecialCharactersItem
        itemView as ViewGroup
        itemView.removeAllViews()

        if (item.characters.isNotEmpty() && item.characters[0].isNotEmpty()) {
            for (c in item.characters) {
                val inflater = LayoutInflater.from(itemView.context)
                val view = inflater.inflate(R.layout.button_character, itemView, false) as Button
                view.text = c
                view.setOnClickListener(this)
                itemView.addView(view)
            }
        }
    }

    override fun onClick(v: View) {
        v as Button
        listener.onCharacterClick(v.text.toString())
    }

    interface OnActionListener {
        fun onCharacterClick(value: String)
    }
}