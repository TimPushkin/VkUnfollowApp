package me.timpushkin.vkunsubapp.utils

import android.net.Uri
import android.util.Log
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.requests.VKRequest
import com.vk.dto.common.id.UserId
import com.vk.sdk.api.base.dto.BaseOkResponse
import com.vk.sdk.api.groups.GroupsService
import com.vk.sdk.api.groups.dto.*
import com.vk.sdk.api.users.dto.UsersFields
import com.vk.sdk.api.wall.WallService
import kotlinx.coroutines.*
import me.timpushkin.vkunsubapp.model.Community

private const val TAG = "VkRequests"

private val scope = CoroutineScope(Dispatchers.IO)

fun getFollowingCommunities(callback: (List<Community>) -> Unit) {
    val userId = VK.getUserId()
    Log.d(TAG, "Getting communities followed by ${userId.value}")

    VK.execute(
        GroupsService().groupsGetExtended(userId = userId),
        object : VKApiCallback<GroupsGetObjectExtendedResponse> {
            override fun success(result: GroupsGetObjectExtendedResponse) {
                val communities = mutableListOf<Community>()

                for (group in result.items) communities += Community(
                    id = group.id.value,
                    name = group.name ?: "",
                    uri = group.screenName?.let { Uri.parse("https://vk.com/$it") } ?: Uri.EMPTY,
                    photoUri = group.photo200?.let { Uri.parse(it) } ?: Uri.EMPTY
                )

                Log.i(TAG, "Got ${communities.size} communities followed by ${userId.value}")

                callback(communities)
            }

            override fun fail(error: Exception) {
                Log.e(TAG, "Failed to get communities followed by ${userId.value}", error)
            }
        }
    )
}

fun getExtendedCommunityInfo(community: Community, callback: (Community) -> Unit) {
    Log.d(TAG, "Getting extended information about ${community.name}")

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
                    filter = GroupsGetMembersFilter.FRIENDS,
                    fields = listOf(UsersFields.SEX) // any field is required -- API crashes otherwise
                )
            )
        }
        val wallGet = async(Dispatchers.IO) {
            VK.executeSync(
                WallService().wallGet(
                    ownerId = UserId(-groupId.value),
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
            Log.i(TAG, "Got extended information about ${community.name}")
            Log.v(TAG, "Extended information: $extendedCommunity")
            launch(Dispatchers.Main) { callback(extendedCommunity) }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to get extended information about ${community.name}, id ${community.id}",
                e
            )
        }
    }
}

enum class CommunityAction { FOLLOW, UNFOLLOW }

fun manageCommunities(
    communities: List<Community>,
    action: CommunityAction,
    callback: (List<Community>) -> Unit
) {
    Log.d(TAG, "Performing $action on ${communities.map { it.name }}")

    val service = GroupsService()
    val command: (groupId: UserId) -> VKRequest<BaseOkResponse> = when (action) {
        CommunityAction.FOLLOW -> service::groupsJoin
        CommunityAction.UNFOLLOW -> service::groupsLeave
    }

    scope.launch {
        val results = mutableListOf<Deferred<BaseOkResponse>>()
        for (community in communities) {
            results += async(Dispatchers.IO) { VK.executeSync(command(UserId(community.id))) }
        }

        val successful = mutableListOf<Community>()
        results.forEachIndexed { i, result ->
            val community = communities[i]
            try {
                when (result.await()) {
                    BaseOkResponse.OK -> successful += community
                    else -> { // There are no other values in the current API, but in case some are added
                        Log.e(
                            TAG,
                            "Failed to perform $action on ${community.name}, id ${community.id}: " +
                                    "API returned $result"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Failed to to perform $action on ${community.name}, id ${community.id}",
                    e
                )
            }
        }

        Log.i(
            TAG,
            "Successfully performed $action on ${successful.size} out of ${communities.size} communities"
        )

        launch(Dispatchers.Main) { callback(successful) }
    }
}
