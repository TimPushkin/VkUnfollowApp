package me.timpushkin.vkunfollowapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.timpushkin.vkunfollowapp.model.Community

/**
 * A [ViewModel] that stores the current UI state of the application.
 */
class ApplicationState : ViewModel() {
    /**
     * Current application mode.
     */
    var mode by mutableStateOf(Mode.FOLLOWING)

    /**
     * Whether the application is currently waiting for a response from VK API for a community
     * managing query.
     */
    var isWaitingManageResponse by mutableStateOf(false)

    /**
     * The community that is displayed in the bottom drawer.
     */
    var displayedCommunity by mutableStateOf(Community.EMPTY)

    private var _communities by mutableStateOf(emptyList<Community>())

    /**
     * Communities that are shown on the main screen grid.
     */
    val communities: List<Community>
        get() = _communities

    private var _selectedNum by mutableStateOf(0)

    /**
     * The number of the communities currently selected.
     */
    val selectedNum: Int
        get() = _selectedNum

    /**
     * Whether the state stores any information inputted by user.
     */
    val isClear: Boolean
        get() = displayedCommunity == Community.EMPTY && selectedNum == 0

    /**
     * Application mode that determines what communities to show and how to manage them.
     */
    enum class Mode { FOLLOWING, UNFOLLOWED }

    /**
     * Set the communities that are shown on the main screen grid.
     * @param communities list of communities to be set.
     * @param clearInteractions whether to clear the stored user input.
     */
    fun setCommunities(communities: List<Community>, clearInteractions: Boolean = true) {
        if (clearInteractions) {
            displayedCommunity = Community.EMPTY
            _selectedNum = 0
            _communities = communities.map { it.copy(isSelected = false) }
        } else _communities = communities
    }

    /**
     * Switch the selection state of a community shown on the main screen grid.
     * @param community the community the state of which will be changed.
     */
    fun switchSelectionOf(community: Community) {
        viewModelScope.launch(Dispatchers.IO) {
            val newCommunities =
                communities.map {
                    if (it.id == community.id) it.copy(isSelected = !it.isSelected).also { new ->
                        if (new.isSelected) _selectedNum++ else _selectedNum--
                    }
                    else it
                }
            launch(Dispatchers.Main) { _communities = newCommunities }
        }
    }
}
