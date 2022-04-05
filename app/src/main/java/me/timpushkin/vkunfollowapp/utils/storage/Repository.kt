package me.timpushkin.vkunfollowapp.utils.storage

interface Repository {
    fun getUnfollowedCommunitiesIds(): Set<Long>

    fun putUnfollowedCommunitiesIds(ids: Set<Long>)

    fun removeUnfollowedCommunitiesIds(ids: Set<Long>)

    fun commit()
}
