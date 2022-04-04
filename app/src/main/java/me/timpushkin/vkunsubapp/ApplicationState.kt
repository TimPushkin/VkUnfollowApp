package me.timpushkin.vkunsubapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.timpushkin.vkunsubapp.model.Community
import me.timpushkin.vkunsubapp.utils.getExtendedCommunityInfo
import me.timpushkin.vkunsubapp.utils.getFollowingCommunities

class ApplicationState : ViewModel() {
    var isWaitingManageResponse by mutableStateOf(false)

    private var _communities by mutableStateOf(emptyList<Community>())
    val communities: List<Community>
        get() = _communities

    private var _displayedCommunity by mutableStateOf(Community.EMPTY)
    val displayedCommunity: Community
        get() = _displayedCommunity

    private var _selectedCommunities by mutableStateOf(emptyList<Community>())
    val selectedCommunities: List<Community>
        get() = _selectedCommunities

    private var _mode by mutableStateOf(Mode.AUTH)
    val mode: Mode
        get() = _mode

    enum class Mode { AUTH, FOLLOWING, UNFOLLOWED }

    fun setMode(newMode: Mode) {
        if (mode == newMode) return

        _communities = emptyList()
        _displayedCommunity = Community.EMPTY
        _selectedCommunities = emptyList()
        _mode = newMode

        updateCommunities()
    }

    fun updateCommunities() {
        when (mode) {
            Mode.AUTH -> {}
            Mode.FOLLOWING -> {
                getFollowingCommunities { communities -> _communities = communities }
            }
            Mode.UNFOLLOWED -> {
                _communities = emptyList() /* TODO: fetch unfollowed from database */
            }
        }
    }

    fun display(community: Community) {
        _displayedCommunity = community
        if (community.isExtended()) return

        getExtendedCommunityInfo(community) { extendedCommunity ->
            _displayedCommunity = extendedCommunity
            viewModelScope.launch {
                _communities =
                    _communities.map { if (it.id == community.id) extendedCommunity else it }
            }
        }
    }

    fun switchSelection(community: Community) {
        viewModelScope.launch {
            _selectedCommunities =
                if (community in selectedCommunities) selectedCommunities - community
                else selectedCommunities + community
        }
    }

    fun unselectAll() {
        _selectedCommunities = emptyList()
    }
}
