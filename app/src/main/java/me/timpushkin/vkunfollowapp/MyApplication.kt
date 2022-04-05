package me.timpushkin.vkunfollowapp

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import me.timpushkin.vkunfollowapp.utils.storage.LocalStorage

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
        LocalStorage.initialize(this)
    }
}
