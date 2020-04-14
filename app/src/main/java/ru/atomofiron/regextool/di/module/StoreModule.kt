package ru.atomofiron.regextool.di.module

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import ru.atomofiron.regextool.injectable.store.ExplorerStore
import ru.atomofiron.regextool.injectable.store.SettingsStore
import javax.inject.Singleton

@Module
open class StoreModule {

    @Provides
    @Singleton
    open fun provideExplorerStore(): ExplorerStore = ExplorerStore()

    @Provides
    @Singleton
    open fun provideSettingsStore(context: Context, sp: SharedPreferences): SettingsStore = SettingsStore(context, sp)
}
