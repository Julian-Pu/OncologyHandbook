package com.oncology.handbook

import android.app.Application
import com.oncology.handbook.data.AppDatabase
import com.oncology.handbook.util.StorageHelper

class App : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        StorageHelper.init(this)
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
