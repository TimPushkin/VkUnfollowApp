package me.timpushkin.vkunsubapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAuthenticationResult
import com.vk.api.sdk.auth.VKScope
import me.timpushkin.vkunsubapp.ui.MainScreen
import me.timpushkin.vkunsubapp.ui.theme.VkUnsubAppTheme

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private val applicationState = ApplicationState()

    private val authLauncher = VK.login(this) { result ->
        when (result) {
            is VKAuthenticationResult.Success -> Log.i(TAG, "Login succeeded")
            is VKAuthenticationResult.Failed -> Log.i(TAG, "Login failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authLauncher.launch(listOf(VKScope.GROUPS))

        setContent {
            VkUnsubAppTheme {
                MainScreen(
                    applicationState = applicationState,
                    onOpenCommunity = this::openCommunity,
                    onApplySelectedCommunities = this::applySelectedCommunities
                )
            }
        }
    }

    private fun openCommunity() {
        Log.i(TAG, "Opening ${applicationState.displayedCommunity}")
        // TODO
    }

    private fun applySelectedCommunities() {
        when (applicationState.mode) {
            ApplicationState.Mode.FOLLOWING ->
                Log.i(TAG, "Unfollowing ${applicationState.selectedCommunities}")
            ApplicationState.Mode.UNFOLLOWED ->
                Log.i(TAG, "Starting to follow ${applicationState.selectedCommunities}")
        }
        // TODO
    }
}
