package me.timpushkin.vkunfollowapp.utils

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
import me.timpushkin.vkunfollowapp.model.Community

private const val TAG = "VkRequests"

private val scope = CoroutineScope(Dispatchers.IO)

private fun groupScreenNameToUri(screenName: String?): Uri =
    screenName?.let { Uri.parse("https://vk.com/$it") } ?: Uri.EMPTY

private fun photoAddressToUri(address: String?): Uri = address?.let { Uri.parse(it) } ?: Uri.EMPTY

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
                    uri = groupScreenNameToUri(group.screenName),
                    photoUri = photoAddressToUri(group.photo200)
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

fun getCommunitiesById(ids: Iterable<Long>, callback: (List<Community>) -> Unit) {
    Log.d(TAG, "Getting information about communities $ids")

    val groupIds = ids.map { UserId(it) }
    if (groupIds.isEmpty()) {
        Log.d(TAG, "No IDs provided")
        callback(emptyList())
        return
    }

    VK.execute(
        GroupsService().groupsGetById(groupIds = groupIds),
        object : VKApiCallback<List<GroupsGroupFull>> {
            override fun success(result: List<GroupsGroupFull>) {
                val communities = mutableListOf<Community>()

                for (group in result) communities += Community(
                    id = group.id.value,
                    name = group.name ?: "",
                    uri = groupScreenNameToUri(group.screenName),
                    photoUri = photoAddressToUri(group.photo200)
                )

                Log.i(TAG, "Got ${communities.size} communities from ids $ids")

                callback(communities)
            }

            override fun fail(error: Exception) {
                Log.e(TAG, "Failed to get communities by ids $ids", error)
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

    if (communities.isEmpty()) {
        Log.d(TAG, "No communities provided")
        callback(emptyList())
    }

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
