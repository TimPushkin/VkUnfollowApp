package me.timpushkin.vkunfollowapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.timpushkin.vkunfollowapp.model.Community

class ApplicationState : ViewModel() {
    var isWaitingManageResponse by mutableStateOf(false)
    var communities by mutableStateOf(emptyList<Community>())
    var displayedCommunity by mutableStateOf(Community.EMPTY)

    private var _selectedCommunities by mutableStateOf(emptyList<Community>())
    val selectedCommunities: List<Community>
        get() = _selectedCommunities

    private var _mode by mutableStateOf(Mode.FOLLOWING)
    var mode: Mode
        get() = _mode
        set(value) {
            if (_mode == value) return
            communities = emptyList()
            displayedCommunity = Community.EMPTY
            _selectedCommunities = emptyList()
            _mode = value
        }

    enum class Mode { FOLLOWING, UNFOLLOWED }

    fun switchSelectionOf(community: Community) {
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
