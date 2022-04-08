package me.timpushkin.vkunfollowapp

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import me.timpushkin.vkunfollowapp.utils.storage.LocalStorage

/**
 * The application class used to perform initialization.
 */
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
        LocalStorage.initialize(this)
    }
}
