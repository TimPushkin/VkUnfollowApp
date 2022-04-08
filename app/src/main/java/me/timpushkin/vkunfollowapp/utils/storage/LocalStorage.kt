package me.timpushkin.vkunfollowapp.utils.storage

import android.content.Context
import android.util.Log
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Repository implementation that uses internal storage to store data.
 *
 * It is intended to be used instead of a heavy database to persistently store simple data such as
 * IDs of communities.
 */
object LocalStorage : Repository {
    private const val TAG = "Storage"
    private const val STORAGE_FILENAME = "storage"

    private lateinit var storageFile: File
    private lateinit var unfollowedCommunitiesIds: MutableSet<Long>

    /**
     * Initializes the storage. Must be called before using it.
     */
    @Suppress("UNCHECKED_CAST")
    @Synchronized
    fun initialize(context: Context) {
        storageFile = File(context.filesDir, STORAGE_FILENAME)

        unfollowedCommunitiesIds =
            if (storageFile.exists()) {
                ObjectInputStream(storageFile.inputStream()).use { ois ->
                    try {
                        (ois.readObject() as MutableSet<Long>).also { ids ->
                            Log.d(TAG, "Successfully read ${ids.size} IDs")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to read unfollowed communities IDs from $storageFile", e)
                        mutableSetOf()
                    }
                }
            } else mutableSetOf<Long>().also { Log.d(TAG, "Storage $storageFile is empty") }

        Log.i(TAG, "Loaded ${unfollowedCommunitiesIds.size} IDs from local storage")
    }

    @Synchronized
    override fun commit() {
        ObjectOutputStream(storageFile.outputStream()).use { oos ->
            try {
                oos.writeObject(unfollowedCommunitiesIds)
                Log.d(
                    TAG,
                    "Successfully wrote ${unfollowedCommunitiesIds.size} IDs to $storageFile"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write $unfollowedCommunitiesIds IDs from $storageFile", e)
            }
        }
    }

    override fun getUnfollowedCommunitiesIds() = unfollowedCommunitiesIds

    override fun putUnfollowedCommunitiesIds(ids: Set<Long>) {
        val newIds = (ids + unfollowedCommunitiesIds) as MutableSet<Long>
        synchronized(unfollowedCommunitiesIds) { unfollowedCommunitiesIds = newIds }
    }

    override fun removeUnfollowedCommunitiesIds(ids: Set<Long>) {
        synchronized(unfollowedCommunitiesIds) { unfollowedCommunitiesIds -= ids }
    }
}
