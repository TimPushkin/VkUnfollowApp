package me.timpushkin.vkunfollowapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.timpushkin.vkunfollowapp.model.Community
import me.timpushkin.vkunfollowapp.utils.*
import me.timpushkin.vkunfollowapp.utils.storage.Repository

class ApplicationState(private val repository: Repository) : ViewModel() {
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
            Mode.FOLLOWING -> getFollowingCommunities { _communities = it }
            Mode.UNFOLLOWED -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val unfollowedCommunitiesIds = repository.getUnfollowedCommunitiesIds()
                    launch(Dispatchers.Main) {
                        getCommunitiesById(unfollowedCommunitiesIds) { _communities = it }
                    }
                }
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

    class Factory(private val repository: Repository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            modelClass.getConstructor(Repository::class.java).newInstance(repository)
    }
}
