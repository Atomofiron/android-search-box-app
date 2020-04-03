package ru.atomofiron.regextool.di.module

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.iss.store.ExplorerStore
import ru.atomofiron.regextool.iss.store.SettingsStore
import javax.inject.Singleton

@Module
open class StoreModule {

    @Provides
    @Singleton
    open fun provideExplorerStore(): ExplorerStore = ExplorerStore()

    @Provides
    @Singleton
    open fun provideSettingsStore(context: Context): SettingsStore {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        return SettingsStore(sp)
    }
}