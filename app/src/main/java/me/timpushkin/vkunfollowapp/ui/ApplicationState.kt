package me.timpushkin.vkunfollowapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.timpushkin.vkunfollowapp.model.Community

class ApplicationState : ViewModel() {
    var mode by mutableStateOf(Mode.FOLLOWING)
    var isWaitingManageResponse by mutableStateOf(false)
    var displayedCommunity by mutableStateOf(Community.EMPTY)

    private var _selectedCommunities by mutableStateOf(emptyList<Community>())
    val selectedCommunities: List<Community>
        get() = _selectedCommunities

    private var _communities by mutableStateOf(emptyList<Community>())
    val communities: List<Community>
        get() = _communities

    enum class Mode { FOLLOWING, UNFOLLOWED }

    fun setCommunities(communities: List<Community>, clearInteractions: Boolean = true) {
        if (clearInteractions) {
            displayedCommunity = Community.EMPTY
            _selectedCommunities = emptyList()
        }
        _communities = communities
    }

    fun switchSelectionOf(community: Community) {
        viewModelScope.launch {
            _selectedCommunities =
                if (community in selectedCommunities) selectedCommunities - community
                else selectedCommunities + community
        }
    }
}
