package me.timpushkin.vkunfollowapp.model

import android.net.Uri

/**
 * Container that represents a VK community.
 */
data class Community(
    val id: Long,
    val name: String,
    val uri: Uri,
    val photoUri: Uri,
    val isSelected: Boolean = false,
    val subscribersNum: Int? = null,
    val friendsNum: Int? = null,
    val description: String? = null,
    val lastPost: Int? = null
) {
    companion object {
        val EMPTY = Community(-1, "", Uri.EMPTY, Uri.EMPTY)
    }

    /**
     * Determines if the community requires additional query for being displayed.
     */
    val isExtended: Boolean
        get() = subscribersNum != null || friendsNum != null || description != null || lastPost != null

    /**
     * Create a new community based on this one, but extended with the information from the provided
     * community.
     */
    fun extendedFrom(other: Community): Community =
        copy(
            subscribersNum = other.subscribersNum,
            friendsNum = other.friendsNum,
            description = other.description,
            lastPost = other.lastPost
        )
}
