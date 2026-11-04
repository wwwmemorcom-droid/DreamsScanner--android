package com.dreams.scanner

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.dreams.scanner.data.DocumentRepository
import com.dreams.scanner.data.FileStore
import com.dreams.scanner.data.SettingsRepository
import com.dreams.scanner.data.db.AppDatabase

/** Manual dependency container (kept simple on purpose: no DI framework needed for this app). */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val fileStore = FileStore(appContext)
    val database: AppDatabase = Room.databaseBuilder(appContext, AppDatabase::class.java, "dreams_scanner.db").build()
    val repository = DocumentRepository(appContext, database, fileStore)
    val settings = SettingsRepository(appContext)
}

class DreamsApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
