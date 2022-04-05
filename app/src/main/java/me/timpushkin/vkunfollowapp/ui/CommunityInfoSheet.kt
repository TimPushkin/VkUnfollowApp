package me.timpushkin.vkunfollowapp.ui

import android.text.format.DateUtils
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
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
import me.timpushkin.vkunfollowapp.R
import me.timpushkin.vkunfollowapp.model.Community
import me.timpushkin.vkunfollowapp.ui.theme.VkUnsubAppTheme
import java.util.*

@Composable
fun CommunityInfoSheet(
    community: Community,
    onOpenClick: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    val res = LocalContext.current.resources

    Column(modifier = Modifier.padding(15.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = community.name,
                modifier = Modifier.weight(1f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.width(15.dp))

            Icon(
                painter = painterResource(
                    if (isSystemInDarkTheme()) R.drawable.ic_dismiss_dark_24
                    else R.drawable.ic_dismiss_24
                ),
                contentDescription = "Close community information",
                modifier = Modifier
                    .size(30.dp)
                    .clickable(onClick = onCloseClick),
                tint = MaterialTheme.colors.secondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (community.subscribersNum != null) {
            IconTextRow(
                resId = R.drawable.ic_rss_feed_outline_28,
                text = res.getQuantityString(
                    R.plurals.subscribers_num,
                    community.subscribersNum,
                    community.subscribersNum
                ) + if (community.friendsNum?.let { it > 0 } == true)
                    " • " + res.getQuantityString(
                        R.plurals.friends_num,
                        community.friendsNum,
                        community.friendsNum
                    ) else ""
            )
        }

        if (community.description?.isNotBlank() == true) {
            IconTextRow(
                resId = R.drawable.ic_article_outline_24,
                text = community.description,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        if (community.lastPost != null) {
            IconTextRow(
                resId = R.drawable.ic_newsfeed_outline_28,
                text = stringResource(
                    R.string.last_post,
                    DateUtils.formatDateTime(
                        LocalContext.current,
                        community.lastPost.toLong() * 1000,
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
fun IconTextRow(@DrawableRes resId: Int, text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Icon(
            painter = painterResource(resId),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 2.dp, end = 8.dp)
                .size(20.dp),
            tint = MaterialTheme.colors.secondary
        )

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
