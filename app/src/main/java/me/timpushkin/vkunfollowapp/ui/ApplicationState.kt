package me.timpushkin.vkunfollowapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.timpushkin.vkunfollowapp.model.Community

class ApplicationState : ViewModel() {
    var mode by mutableStateOf(Mode.FOLLOWING)
    var isWaitingManageResponse by mutableStateOf(false)
    var displayedCommunity by mutableStateOf(Community.EMPTY)

    private var _communities by mutableStateOf(emptyList<Community>())
    val communities: List<Community>
        get() = _communities

    private var _selectedNum by mutableStateOf(0)
    val selectedNum: Int
        get() = _selectedNum

    val isClear: Boolean
        get() = displayedCommunity == Community.EMPTY && selectedNum == 0

    enum class Mode { FOLLOWING, UNFOLLOWED }

    fun setCommunities(communities: List<Community>, clearInteractions: Boolean = true) {
        if (clearInteractions) {
            displayedCommunity = Community.EMPTY
            _selectedNum = 0
            _communities = communities.map { it.copy(isSelected = false) }
        } else _communities = communities
    }

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
