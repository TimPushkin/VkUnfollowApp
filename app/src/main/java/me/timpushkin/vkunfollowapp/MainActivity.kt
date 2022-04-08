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
import me.timpushkin.vkunfollowapp.model.Community
import me.timpushkin.vkunfollowapp.ui.ApplicationState
import me.timpushkin.vkunfollowapp.ui.ApplicationState.Mode
import me.timpushkin.vkunfollowapp.ui.MainScreen
import me.timpushkin.vkunfollowapp.ui.theme.VkFollowAppTheme
import me.timpushkin.vkunfollowapp.utils.*
import me.timpushkin.vkunfollowapp.utils.storage.LocalStorage
import me.timpushkin.vkunfollowapp.utils.storage.Repository

private const val TAG = "MainActivity"

private val PERMISSIONS = listOf(VKScope.GROUPS)

/**
 * Main activity of the application.
 */
class MainActivity : ComponentActivity() {
    private val appState: ApplicationState by viewModels()
    private val repository: Repository = LocalStorage
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var isAuthLaunched = false
    private val authLauncher = VK.login(this) { result ->
        isAuthLaunched = false
        when (result) {
            is VKAuthenticationResult.Success -> {
                Log.i(TAG, "Authorization succeeded")
                appState.mode = Mode.FOLLOWING
                updateCommunities()
            }
            is VKAuthenticationResult.Failed -> Log.i(TAG, "Authorization failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (appState.isClear) updateCommunities() // Also requests authorization when needed

        setContent {
            VkFollowAppTheme {
                window.statusBarColor = MaterialTheme.colors.background.toArgb()
                window.navigationBarColor = MaterialTheme.colors.surface.toArgb()

                if (!isSystemInDarkTheme()) {
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = true
                        isAppearanceLightNavigationBars = true
                    }
                }

                MainScreen(
                    appState = appState,
                    onModeSwitch = this::switchMode,
                    onReloadCommunities = this::updateCommunities,
                    onDisplayCommunity = this::displayCommunity,
                    onManageSelectedCommunities = this::manageSelectedCommunities
                )
            }
        }
    }

    /**
     * Launches VK authorization if it isn't already launched.
     */
    private fun handleAuth() {
        if (isAuthLaunched) Log.d(TAG, "Authorization is already launched")
        else {
            Log.i(TAG, "Launching authorization")
            isAuthLaunched = true
            authLauncher.launch(PERMISSIONS)
        }
    }

    /**
     * Switches the application mode in [ApplicationState].
     */
    private fun switchMode() {
        Log.d(TAG, "Switching application mode (current is ${appState.mode})")

        when (appState.mode) {
            Mode.FOLLOWING -> appState.mode = Mode.UNFOLLOWED
            Mode.UNFOLLOWED -> appState.mode = Mode.FOLLOWING
        }
        Log.i(TAG, "Switched to ${appState.mode}")

        updateCommunities()
    }

    /**
     * Updates communities that are currently shown in the main screen grid.
     */
    private fun updateCommunities() {
        Log.d(TAG, "Updating displayed communities")

        when (appState.mode) {
            Mode.FOLLOWING -> getFollowingCommunities(
                onAuthError = this::handleAuth,
                callback = appState::setCommunities
            )
            Mode.UNFOLLOWED -> {
                ioScope.launch {
                    val unfollowedCommunitiesIds = repository.getUnfollowedCommunitiesIds()
                    launch(Dispatchers.Main) {
                        getCommunitiesById(
                            ids = unfollowedCommunitiesIds,
                            onAuthError = this@MainActivity::handleAuth,
                            callback = appState::setCommunities
                        )
                    }
                }
            }
        }
    }

    /**
     * Displays a community, showing its extended information on a main screen. Only one community
     * is displayed at any given moment.
     */
    private fun displayCommunity(community: Community) {
        appState.displayedCommunity = community
        if (community.isExtended) return

        getExtendedCommunityInfo(
            community = community,
            onAuthError = this::handleAuth
        ) { extendedCommunity ->
            appState.displayedCommunity = extendedCommunity
            ioScope.launch {
                val withExtended = appState.communities.map {
                    if (it.id == community.id) it.extendedFrom(extendedCommunity) else it
                }
                launch(Dispatchers.Main) { appState.setCommunities(withExtended, false) }
            }
        }
    }

    /**
     * Sends a query to follow or unfollow the currently selected communities.
     */
    private fun manageSelectedCommunities() {
        if (appState.isWaitingManageResponse) {
            Log.i(TAG, "Already waiting for a community management response")
            return
        }

        Log.i(TAG, "Managing selected communities")

        when (appState.mode) {
            Mode.FOLLOWING -> performCommunityAction(CommunityAction.UNFOLLOW)
            Mode.UNFOLLOWED -> performCommunityAction(CommunityAction.FOLLOW)
        }
    }

    /**
     * Helper function to send a follow or unfollow query and update the main screen with the
     * received result..
     */
    private fun performCommunityAction(action: CommunityAction) {
        appState.isWaitingManageResponse = true
        manageCommunities(
            communities = appState.communities.filter { it.isSelected },
            action = action,
            onAuthError = this::handleAuth
        ) { managed ->
            ioScope.launch {
                val ids = mutableSetOf<Long>().apply { managed.forEach { add(it.id) } }
                when (action) {
                    CommunityAction.FOLLOW -> repository.removeUnfollowedCommunitiesIds(ids)
                    CommunityAction.UNFOLLOW -> repository.putUnfollowedCommunitiesIds(ids)
                }
                launch(Dispatchers.Main) {
                    updateCommunities()
                    appState.isWaitingManageResponse = false
                }
            }
        }
    }

    override fun onPause() {
        repository.commit() // Not inside a coroutine scope as not to be killed committing
        super.onPause()
    }
}
