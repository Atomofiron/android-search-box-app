package ru.atomofiron.regextool

import android.app.Application
import android.content.Context
import ru.atomofiron.regextool.di.DaggerInjector

class App : Application() {
    companion object {
        private lateinit var appContext: Context
        val context: Context get() = appContext
        val pathToybox: String get() = "${context.filesDir}/toybox"
    }

    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext

        DaggerInjector.init(this)
    }
}