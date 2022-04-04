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
    private var _communities by mutableStateOf(emptyList<Community>())
    val communities: List<Community>
        get() = _communities

    private var _displayedCommunity by mutableStateOf(Community.EMPTY)
    val displayedCommunity: Community
        get() = _displayedCommunity

    private var _selectedCommunities by mutableStateOf(emptySet<Community>())
    val selectedCommunities: Set<Community>
        get() = _selectedCommunities

    private var _mode by mutableStateOf(Mode.AUTH)
    val mode: Mode
        get() = _mode

    enum class Mode { AUTH, FOLLOWING, UNFOLLOWED }

    fun setMode(newMode: Mode) {
        if (mode == newMode) return

        when (newMode) {
            Mode.AUTH -> {}
            Mode.FOLLOWING -> {
                getFollowingCommunities { communities -> _communities = communities }
            }
            Mode.UNFOLLOWED -> {
                _communities = emptyList() /* TODO: fetch unfollowed from database */
            }
        }

        _communities = emptyList()
        _displayedCommunity = Community.EMPTY
        _selectedCommunities = emptySet()
        _mode = newMode
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

    fun selectOrUnselect(community: Community) {
        viewModelScope.launch {
            _selectedCommunities =
                if (community in selectedCommunities) selectedCommunities - community
                else selectedCommunities + community
        }
    }
}
