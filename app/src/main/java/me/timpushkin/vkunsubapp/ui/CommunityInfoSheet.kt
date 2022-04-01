package me.timpushkin.vkunsubapp.ui

import android.text.format.DateUtils
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.timpushkin.vkunsubapp.R
import me.timpushkin.vkunsubapp.model.Community

@Composable
fun CommunityInfoSheet(
    community: Community,
    onOpenClick: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    val res = LocalContext.current.resources

    Column(modifier = Modifier.padding(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
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

        Spacer(modifier = Modifier.height(5.dp))

        IconRow(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_rss_feed_outline_28),
                    contentDescription = "Subscriber statistics"
                )
            },
            text = res.getQuantityString(R.plurals.subscribers_num, community.subscribersNum)
                    + " • " + res.getQuantityString(R.plurals.friends_num, community.friendsNum)
        )

        IconRow(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_article_outline_24),
                    contentDescription = "Community description"
                )
            },
            text = community.description
        )

        IconRow(
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_newsfeed_outline_28),
                    contentDescription = "Last post date"
                )
            },
            text = stringResource(
                R.string.last_post,
                DateUtils.formatDateTime(
                    LocalContext.current,
                    community.lastPost,
                    DateUtils.FORMAT_SHOW_DATE
                )
            )
        )

        Spacer(modifier = Modifier.height(5.dp))

        Button(
            onClick = onOpenClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.open))
        }
    }
}

@Composable
fun IconRow(icon: @Composable () -> Unit, text: String) {
    Row {
        icon()

        Spacer(modifier = Modifier.width(2.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.body1
        )
    }
}
