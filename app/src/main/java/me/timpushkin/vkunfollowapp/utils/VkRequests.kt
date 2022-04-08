package me.timpushkin.vkunfollowapp.utils

import android.net.Uri
import android.util.Log
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.exceptions.VKApiCodes
import com.vk.api.sdk.exceptions.VKApiExecutionException
import com.vk.api.sdk.requests.VKRequest
import com.vk.dto.common.id.UserId
import com.vk.sdk.api.base.dto.BaseBoolInt
import com.vk.sdk.api.base.dto.BaseOkResponse
import com.vk.sdk.api.groups.GroupsService
import com.vk.sdk.api.groups.dto.*
import com.vk.sdk.api.users.dto.UsersFields
import com.vk.sdk.api.wall.WallService
import com.vk.sdk.api.wall.dto.WallGetResponse
import kotlinx.coroutines.*
import me.timpushkin.vkunfollowapp.model.Community

private const val TAG = "VkRequests"

private val scope = CoroutineScope(Dispatchers.IO)

private fun Exception.isAuthError(): Boolean =
    this is VKApiExecutionException && code == VKApiCodes.CODE_AUTHORIZATION_FAILED

private fun groupScreenNameToUri(screenName: String?): Uri =
    screenName?.let { Uri.parse("https://vk.com/$it") } ?: Uri.EMPTY

private fun photoAddressToUri(address: String?): Uri = address?.let { Uri.parse(it) } ?: Uri.EMPTY

fun getFollowingCommunities(onAuthError: () -> Unit = {}, callback: (List<Community>) -> Unit) {
    if (!VK.isLoggedIn()) {
        Log.i(TAG, "Cannot get followed communities: not authorized")
        onAuthError()
        return
    }

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
                if (error.isAuthError()) onAuthError()
            }
        }
    )
}

fun getCommunitiesById(
    ids: Iterable<Long>,
    onAuthError: () -> Unit = {},
    callback: (List<Community>) -> Unit
) {
    Log.d(TAG, "Getting information about communities $ids")

    val groupIds = ids.map { UserId(it) }
    if (groupIds.isEmpty()) {
        Log.d(TAG, "No IDs provided")
        callback(emptyList())
        return
    }

    if (!VK.isLoggedIn()) {
        Log.i(TAG, "Cannot information about the communities: not authorized")
        onAuthError()
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
                if (error.isAuthError()) onAuthError()
            }
        }
    )
}

fun getExtendedCommunityInfo(
    community: Community,
    onAuthError: () -> Unit = {},
    callback: (Community) -> Unit
) {
    Log.d(TAG, "Getting extended information about ${community.name}")

    if (!VK.isLoggedIn()) {
        Log.i(TAG, "Cannot get extended community information: not authorized")
        onAuthError()
        return
    }

    scope.launch {
        val groupId = UserId(community.id)

        val groupsGetById: Deferred<List<GroupsGroupFull>>
        val groupsGetMembers: Deferred<GroupsGetMembersFieldsResponse>
        val wallGet: Deferred<WallGetResponse>

        supervisorScope {
            groupsGetById = async(Dispatchers.IO) {
                VK.executeSync(
                    GroupsService().groupsGetById(
                        groupId = groupId,
                        fields = listOf(GroupsFields.DESCRIPTION, GroupsFields.MEMBERS_COUNT)
                    )
                )
            }
            groupsGetMembers = async(Dispatchers.IO) {
                VK.executeSync(
                    GroupsService().groupsGetMembers(
                        groupId = groupId.value.toString(10),
                        filter = GroupsGetMembersFilter.FRIENDS,
                        fields = listOf(UsersFields.SEX) // any field is required -- API crashes otherwise
                    )
                )
            }
            wallGet = async(Dispatchers.IO) {
                VK.executeSync(
                    WallService().wallGet(
                        ownerId = UserId(-groupId.value),
                        count = 1
                    )
                )
            }
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
            if (e.isAuthError()) launch(Dispatchers.Main) { onAuthError() }
        }
    }
}

enum class CommunityAction { FOLLOW, UNFOLLOW }

fun manageCommunities(
    communities: List<Community>,
    action: CommunityAction,
    onAuthError: () -> Unit = {},
    callback: (List<Community>) -> Unit
) {
    Log.d(TAG, "Performing $action on ${communities.map { it.name }}")

    if (communities.isEmpty()) {
        Log.d(TAG, "No communities provided")
        callback(emptyList())
    }

    if (!VK.isLoggedIn()) {
        Log.i(TAG, "Cannot manage the communities: not authorized")
        onAuthError()
        return
    }

    val service = GroupsService()
    val command: (groupId: UserId) -> VKRequest<BaseOkResponse> = when (action) {
        CommunityAction.FOLLOW -> service::groupsJoin
        CommunityAction.UNFOLLOW -> service::groupsLeave
    }

    scope.launch {
        val results = mutableListOf<Deferred<BaseOkResponse>>()
        for (community in communities) supervisorScope {
            results += async(Dispatchers.IO) { VK.executeSync(command(UserId(community.id))) }
        }

        val successful = mutableListOf<Community>()
        var authErrorOccurred = false

        coroutineScope {
            results.forEachIndexed { i, result ->
                val community = communities[i]
                try {
                    if (result.await() == BaseOkResponse.OK) successful += community
                    else // There are no other values in the current API, but in case some are added
                        Log.e(
                            TAG,
                            "Failed to perform $action on ${community.name}, id ${community.id}: " +
                                    "API returned ${result.await()}"
                        )
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Failed to to perform $action on ${community.name}, id ${community.id}",
                        e
                    )

                    if (e.isAuthError()) authErrorOccurred = true
                    else
                        launch(Dispatchers.IO) {
                            val isFollowing = isFollowing(community)
                            if (isFollowing && action == CommunityAction.FOLLOW) {
                                Log.i(TAG, "Already following ${community.name}")
                                successful += community
                            } else if (!isFollowing && action == CommunityAction.UNFOLLOW) {
                                Log.i(TAG, "Already unfollowed ${community.name}")
                                successful += community
                            }
                        }
                }
            }
        }

        Log.i(
            TAG,
            "Successfully performed $action on ${successful.size} out of ${communities.size} communities"
        )

        launch(Dispatchers.Main) {
            if (authErrorOccurred) onAuthError()
            callback(successful)
        }
    }
}

private fun isFollowing(community: Community): Boolean {
    val userId = VK.getUserId()

    val isMember =
        try {
            VK.executeSync(
                GroupsService().groupsIsMember(
                    groupId = community.id.toString(),
                    userId = userId
                )
            )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Failed to determine membership of $userId in ${community.name}, id ${community.id}",
                e
            )
            BaseBoolInt.NO
        }

    return isMember == BaseBoolInt.YES
}
