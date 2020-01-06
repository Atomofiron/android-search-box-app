package ru.atomofiron.regextool.screens.root

import ru.atomofiron.regextool.R
import ru.atomofiron.regextool.common.base.BaseRouter
import ru.atomofiron.regextool.screens.explorer.ExplorerFragment
import ru.atomofiron.regextool.screens.finder.FinderFragment

class RootRouter : BaseRouter() {
    override var fragmentContainerId: Int = R.id.root_fl

    fun showMain() {
        startScreen(ExplorerFragment(), addToBackStack = false) {
            startScreen(FinderFragment(), addToBackStack = false)
        }
    }
}