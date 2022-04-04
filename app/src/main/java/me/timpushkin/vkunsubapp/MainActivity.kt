package me.timpushkin.vkunsubapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import me.timpushkin.vkunsubapp.ui.MainScreen
import me.timpushkin.vkunsubapp.ui.theme.VkUnsubAppTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private val applicationState: ApplicationState by viewModels()

    private val authLauncher = VK.login(this) { result ->
        when (result) {
            is VKAuthenticationResult.Success -> {
                Log.i(TAG, "Authorization succeeded")
                applicationState.setMode(ApplicationState.Mode.FOLLOWING)
            }
            is VKAuthenticationResult.Failed -> Log.i(TAG, "Authorization failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!VK.isLoggedIn()) {
            Log.i(TAG, "Launching authorization")
            authLauncher.launch(listOf(VKScope.GROUPS))
        } else {
            Log.i(TAG, "Already authorized")
            if (applicationState.mode == ApplicationState.Mode.AUTH)
                applicationState.setMode(ApplicationState.Mode.FOLLOWING)
        }

        setContent {
            VkUnsubAppTheme {
                window.statusBarColor = MaterialTheme.colors.background.toArgb()
                window.navigationBarColor = MaterialTheme.colors.surface.toArgb()

                if (!isSystemInDarkTheme()) {
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = true
                        isAppearanceLightNavigationBars = true
                    }
                }

                MainScreen(
                    applicationState = applicationState,
                    onApplySelectedCommunities = this::applySelectedCommunities
                )
            }
        }
    }

    private fun applySelectedCommunities() {
        when (applicationState.mode) {
            ApplicationState.Mode.AUTH ->
                Log.e(TAG, "Cannot apply selected communities when unauthorized")
            ApplicationState.Mode.FOLLOWING ->
                Log.i(TAG, "Unfollowing ${applicationState.selectedCommunities}")
            ApplicationState.Mode.UNFOLLOWED ->
                Log.i(TAG, "Starting to follow ${applicationState.selectedCommunities}")
        }
        // TODO
    }
}
