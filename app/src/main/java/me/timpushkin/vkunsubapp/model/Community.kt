package me.timpushkin.vkunsubapp.model

import android.net.Uri

data class Community(
    val id: Long,
    val name: String,
    val photoUri: Uri,
    val subscribersNum: Int = 0,
    val friendsNum: Int = 0,
    val description: String = "",
    val lastPost: Int? = null
) {
    companion object {
        val EMPTY = Community(-1, "", Uri.EMPTY)
    }
}
