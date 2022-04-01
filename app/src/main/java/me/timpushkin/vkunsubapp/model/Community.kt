package me.timpushkin.vkunsubapp.model

import androidx.compose.ui.graphics.ImageBitmap

data class Community(
    val id: Int,
    val name: String,
    val image: ImageBitmap,
    val subscribersNum: Int = 0,
    val friendsNum: Int = 0,
    val description: String = "",
    val lastPost: Long = 0
) {
    companion object {
        val EMPTY = Community(-1, "", ImageBitmap(0, 0))
    }
}
