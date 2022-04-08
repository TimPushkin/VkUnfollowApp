package me.timpushkin.vkunfollowapp.utils.storage

/**
 * Encapsulates interaction with persistent application data.
 */
interface Repository {
    /**
     * Returns IDs of the communities that were unfollowed using the application.
     */
    fun getUnfollowedCommunitiesIds(): Set<Long>

    /**
     * Adds the given IDs of unfollowed communities into the repository.
     * @param ids IDs to be added.
     */
    fun putUnfollowedCommunitiesIds(ids: Set<Long>)

    /**
     * Removes the given IDs of unfollowed communities into the repository.
     * @param ids IDs to be removed.
     */
    fun removeUnfollowedCommunitiesIds(ids: Set<Long>)

    /**
     * Commit a transaction, saving the previously added changes to the repository.
     */
    fun commit()
}
