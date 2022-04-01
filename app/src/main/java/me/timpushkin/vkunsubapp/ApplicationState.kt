package me.timpushkin.vkunsubapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import me.timpushkin.vkunsubapp.model.Community

class ApplicationState : ViewModel() {
    var mode by mutableStateOf(Mode.FOLLOWING)
    var displayedCommunity by mutableStateOf(Community.EMPTY)

    private var _communities by mutableStateOf(emptyList<Community>())
    val communities: List<Community>
        get() = _communities

    private var _selectedCommunities by mutableStateOf(emptySet<Community>())
    val selectedCommunities: Set<Community>
        get() = _selectedCommunities

    enum class Mode { FOLLOWING, UNFOLLOWED }

    fun switchMode() {
        mode = when (mode) {
            Mode.FOLLOWING -> Mode.UNFOLLOWED
            Mode.UNFOLLOWED -> Mode.FOLLOWING
        }
        displayedCommunity = Community.EMPTY
        _selectedCommunities = emptySet()
        _communities = TODO("Fetch appropriate communities")
    }

    fun selectOrUnselect(community: Community) {
        viewModelScope.launch {
            _communities =
                if (community in communities) communities - community
                else communities + community
        }
    }
}
