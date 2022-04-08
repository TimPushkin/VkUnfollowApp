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

        updateCommunities() // Also requests authorization when needed

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

    private fun handleAuth() {
        if (isAuthLaunched) Log.d(TAG, "Authorization is already launched")
        else {
            Log.i(TAG, "Launching authorization")
            isAuthLaunched = true
            authLauncher.launch(PERMISSIONS)
        }
    }

    private fun switchMode() {
        Log.d(TAG, "Switching application mode (current is ${appState.mode})")

        when (appState.mode) {
            Mode.FOLLOWING -> appState.mode = Mode.UNFOLLOWED
            Mode.UNFOLLOWED -> appState.mode = Mode.FOLLOWING
        }
        Log.i(TAG, "Switched to ${appState.mode}")

        updateCommunities()
    }

    private fun updateCommunities() {
        Log.d(TAG, "Updating displayed communities")

        when (appState.mode) {
            Mode.FOLLOWING -> getFollowingCommunities(
                onAuthError = this::handleAuth
            ) { appState.communities = it }
            Mode.UNFOLLOWED -> {
                ioScope.launch {
                    val unfollowedCommunitiesIds = repository.getUnfollowedCommunitiesIds()
                    launch(Dispatchers.Main) {
                        getCommunitiesById(
                            ids = unfollowedCommunitiesIds,
                            onAuthError = this@MainActivity::handleAuth
                        ) { appState.communities = it }
                    }
                }
            }
        }
    }

    private fun displayCommunity(community: Community) {
        appState.displayedCommunity = community
        if (community.isExtended()) return

        getExtendedCommunityInfo(
            community = community,
            onAuthError = this::handleAuth
        ) { extendedCommunity ->
            appState.displayedCommunity = extendedCommunity
            ioScope.launch {
                val withExtended =
                    appState.communities.map { if (it.id == community.id) extendedCommunity else it }
                launch(Dispatchers.Main) { appState.communities = withExtended }
            }
        }
    }

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

    private fun performCommunityAction(action: CommunityAction) {
        appState.isWaitingManageResponse = true
        manageCommunities(
            communities = appState.selectedCommunities,
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
                    appState.unselectAll()
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
