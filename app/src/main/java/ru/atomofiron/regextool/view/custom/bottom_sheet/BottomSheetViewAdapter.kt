package ru.atomofiron.regextool.view.custom.bottom_sheet

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.view.custom.menu.MenuImpl

class BottomSheetViewAdapter(context: Context) : RecyclerView.Adapter<BottomSheetViewAdapter.Holder>(), View.OnClickListener {
    val menu = MenuImpl(context)

    var menuItemClickListener: (id: Int) -> Unit = { }

    init {
        menu.setMenuChangedListener(::onMenuChanged)
    }

    private fun onMenuChanged() {
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_bottom_sheet_menu, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = menu.size()

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(menu.getItem(position))
        holder.itemView.setOnClickListener(this)
    }

    override fun onClick(v: View) = menuItemClickListener.invoke(v.id)

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.item_iv_icon)
        val title: TextView = view.findViewById(R.id.item_tv_label)

        fun bind(item: MenuItem) {
            itemView.id = item.itemId
            icon.setImageDrawable(item.icon)
            title.text = item.title
        }
    }
}