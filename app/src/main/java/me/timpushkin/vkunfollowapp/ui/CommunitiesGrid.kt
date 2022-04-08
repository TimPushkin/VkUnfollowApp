package me.timpushkin.vkunfollowapp.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.timpushkin.vkunfollowapp.model.Community
import me.timpushkin.vkunfollowapp.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommunitiesGrid(
    communities: List<Community>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    onCellClick: (Community) -> Unit = {},
    onCellLongClick: (Community) -> Unit = {}
) {
    LazyVerticalGrid(
        cells = GridCells.Adaptive(dimensionResource(R.dimen.community_photo_size)),
        modifier = modifier,
        state = state,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (communities.isNotEmpty())
            items(communities) { community ->
                CommunityCell(
                    name = community.name,
                    photoUri = community.photoUri,
                    isSelected = community.isSelected,
                    modifier = Modifier
                        .padding(5.dp)
                        .combinedClickable(
                            onClick = { onCellClick(community) },
                            onLongClick = { onCellLongClick(community) }
                        )
                )
            }
        else
            item(span = { GridItemSpan(Int.MAX_VALUE) }) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(R.string.no_communities),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = dimensionResource(R.dimen.community_photo_size)),
                        color = MaterialTheme.colors.primaryVariant,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
    }
}
