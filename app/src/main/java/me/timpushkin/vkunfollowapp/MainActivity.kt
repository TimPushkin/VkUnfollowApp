package me.timpushkin.vkunfollowapp

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.timpushkin.vkunfollowapp.ui.ApplicationState
import me.timpushkin.vkunfollowapp.ui.MainScreen
import me.timpushkin.vkunfollowapp.ui.theme.VkUnsubAppTheme
import me.timpushkin.vkunfollowapp.utils.CommunityAction
import me.timpushkin.vkunfollowapp.utils.storage.LocalStorage
import me.timpushkin.vkunfollowapp.utils.storage.Repository
import me.timpushkin.vkunfollowapp.utils.manageCommunities

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private val repository: Repository = LocalStorage
    private val applicationState: ApplicationState by viewModels {
        ApplicationState.Factory(repository)
    }
    private val ioScope = CoroutineScope(Dispatchers.IO)

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

        if (VK.isLoggedIn()) {
            Log.i(TAG, "Already authorized")
            if (applicationState.mode == ApplicationState.Mode.AUTH)
                applicationState.setMode(ApplicationState.Mode.FOLLOWING)
        } else {
            Log.i(TAG, "Launching authorization")
            authLauncher.launch(listOf(VKScope.GROUPS))
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
                    onManageSelectedCommunities = this::manageSelectedCommunities
                )
            }
        }
    }

    private fun manageSelectedCommunities() {
        if (applicationState.isWaitingManageResponse) {
            Log.i(TAG, "Already waiting for a community management response")
            return
        }

        Log.i(TAG, "Managing selected communities")

        when (applicationState.mode) {
            ApplicationState.Mode.AUTH ->
                Log.e(TAG, "Cannot apply selected communities when unauthorized")
            ApplicationState.Mode.FOLLOWING -> performCommunityAction(CommunityAction.UNFOLLOW)
            ApplicationState.Mode.UNFOLLOWED -> performCommunityAction(CommunityAction.FOLLOW)
        }
    }

    private fun performCommunityAction(action: CommunityAction) {
        applicationState.isWaitingManageResponse = true
        manageCommunities(
            applicationState.selectedCommunities,
            action
        ) { managed ->
            ioScope.launch {
                val ids = mutableSetOf<Long>().apply { managed.forEach { add(it.id) } }
                when (action) {
                    CommunityAction.FOLLOW -> repository.removeUnfollowedCommunitiesIds(ids)
                    CommunityAction.UNFOLLOW -> repository.putUnfollowedCommunitiesIds(ids)
                }
                launch(Dispatchers.Main) {
                    applicationState.unselectAll()
                    applicationState.updateCommunities()
                    applicationState.isWaitingManageResponse = false
                }
            }
        }
    }

    override fun onPause() {
        repository.commit() // Not inside a coroutine scope as not to be killed committing
        super.onPause()
    }
}
