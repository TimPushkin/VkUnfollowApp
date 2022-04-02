package me.timpushkin.vkunsubapp.ui

import android.text.format.DateUtils
import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.timpushkin.vkunsubapp.R
import me.timpushkin.vkunsubapp.model.Community
import me.timpushkin.vkunsubapp.ui.theme.VkUnsubAppTheme

@Composable
fun CommunityInfoSheet(
    community: Community,
    onOpenClick: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    val res = LocalContext.current.resources

    Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = community.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.h6
            )

            IconButton(onClick = onCloseClick) {
                Icon(
                    painter = painterResource(
                        if (isSystemInDarkTheme()) R.drawable.ic_dismiss_dark_24
                        else R.drawable.ic_dismiss_24
                    ),
                    contentDescription = "Close community info",
                    tint = MaterialTheme.colors.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        IconTextRow(
            resId = R.drawable.ic_rss_feed_outline_28,
            text = res.getQuantityString(
                R.plurals.subscribers_num,
                community.subscribersNum,
                community.subscribersNum
            ) + if (community.friendsNum > 0) " • " + res.getQuantityString(
                R.plurals.friends_num,
                community.friendsNum,
                community.friendsNum
            ) else ""
        )

        if (community.description.isNotBlank()) {
            IconTextRow(
                resId = R.drawable.ic_article_outline_28,
                text = community.description
            )
        }

        if (community.lastPost != null) {
            IconTextRow(
                resId = R.drawable.ic_newsfeed_outline_28,
                text = stringResource(
                    R.string.last_post,
                    DateUtils.formatDateTime(
                        LocalContext.current,
                        community.lastPost.toLong(),
                        DateUtils.FORMAT_SHOW_DATE
                    )
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onOpenClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.open),
                modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
            )
        }
    }
}

@Composable
fun IconTextRow(@DrawableRes resId: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(resId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colors.secondary
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.body1
        )
    }
}

@Preview
@Composable
fun CommunityInfoSheetPreview() {
    VkUnsubAppTheme {
        CommunityInfoSheet(
            community = Community.EMPTY.copy(
                name = "Unnamed community",
                subscribersNum = 100,
                friendsNum = 5,
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                lastPost = 1648922998
            )
        )
    }
}
