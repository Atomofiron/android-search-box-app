package app.atomofiron.searchboxapp.di.module

import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import app.atomofiron.searchboxapp.injectable.channel.ResultChannel
import app.atomofiron.searchboxapp.injectable.service.FinderService
import app.atomofiron.searchboxapp.injectable.service.ResultService
import app.atomofiron.searchboxapp.injectable.service.explorer.ExplorerService
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.injectable.store.ResultStore
import javax.inject.Singleton

@Module
open class ServiceModule {

    @Provides
    @Singleton
    fun explorerService(
            context: Context,
            assets: AssetManager,
            preferences: SharedPreferences,
            explorerStore: ExplorerStore,
            preferenceStore: PreferenceStore
    ): ExplorerService = ExplorerService(context, assets, preferences, explorerStore, preferenceStore)

    @Provides
    @Singleton
    fun finderService(
            workManager: WorkManager,
            notificationManager: NotificationManager,
            finderStore: FinderStore,
            preferenceStore: PreferenceStore
    ): FinderService = FinderService(workManager, notificationManager, finderStore, preferenceStore)

    @Provides
    @Singleton
    fun resultService(
            workManager: WorkManager,
            resultChannel: ResultChannel,
            resultStore: ResultStore,
            finderStore: FinderStore,
            preferenceStore: PreferenceStore,
            clipboardManager: ClipboardManager
    ): ResultService = ResultService(workManager, resultChannel, resultStore , finderStore, preferenceStore, clipboardManager)
}
