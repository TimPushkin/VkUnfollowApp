package me.timpushkin.vkunsubapp.utils

import android.net.Uri
import android.util.Log
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.dto.common.id.UserId
import com.vk.sdk.api.groups.GroupsService
import com.vk.sdk.api.groups.dto.*
import com.vk.sdk.api.wall.WallService
import kotlinx.coroutines.*
import me.timpushkin.vkunsubapp.model.Community
import java.io.IOException

private const val TAG = "VkRequests"

private val scope = CoroutineScope(Dispatchers.IO)

fun getFollowingCommunities(callback: (List<Community>) -> Unit) {
    val userId = VK.getUserId()
    Log.d(TAG, "Getting communities followed by ${userId.value}")

    VK.execute(GroupsService().groupsGetExtended(
        userId = userId
    ), object : VKApiCallback<GroupsGetObjectExtendedResponse> {
        override fun success(result: GroupsGetObjectExtendedResponse) {
            val communities = mutableListOf<Community>()

            for (group in result.items) communities += Community(
                id = group.id.value,
                name = group.name ?: "",
                photoUri = group.photo200?.let { Uri.parse(it) } ?: Uri.EMPTY
            )

            Log.i(TAG, "Got ${communities.size} communities followed by ${userId.value}")

            callback(communities)
        }

        override fun fail(error: Exception) {
            Log.e(TAG, "Failed to get communities followed by ${userId.value}", error)
        }
    })
}

fun getExtendedCommunityInfo(community: Community, callback: (Community) -> Unit) {
    scope.launch {
        val groupId = UserId(community.id)

        val groupsGetById = async(Dispatchers.IO) {
            VK.executeSync(
                GroupsService().groupsGetById(
                    groupId = groupId,
                    fields = listOf(GroupsFields.DESCRIPTION, GroupsFields.MEMBERS_COUNT)
                )
            )
        }
        val groupsGetMembers = async(Dispatchers.IO) {
            VK.executeSync(
                GroupsService().groupsGetMembers(
                    groupId = groupId.value.toString(10),
                    filter = GroupsGetMembersFilter.FRIENDS
                )
            )
        }
        val wallGet = async(Dispatchers.IO) {
            VK.executeSync(
                WallService().wallGet(
                    ownerId = groupId,
                    count = 1
                )
            )
        }

        try {
            val extendedCommunity = community.copy(
                subscribersNum = groupsGetById.await().firstOrNull()?.membersCount ?: 0,
                friendsNum = groupsGetMembers.await().count,
                description = groupsGetById.await().firstOrNull()?.description ?: "",
                lastPost = wallGet.await().items.firstOrNull()?.date
            )
            launch(Dispatchers.Main) { callback(extendedCommunity) }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to get extended information about ${community.id}", e)
        }
    }
}
