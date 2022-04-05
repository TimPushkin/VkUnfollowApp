package me.timpushkin.vkunsubapp.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import me.timpushkin.vkunsubapp.model.Community
import me.timpushkin.vkunsubapp.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommunitiesGrid(
    communities: List<Community>,
    selectedCommunities: Iterable<Community>,
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
        items(communities) { community ->
            CommunityCell(
                name = community.name,
                photoUri = community.photoUri,
                isSelected = community in selectedCommunities,
                modifier = Modifier
                    .padding(5.dp)
                    .combinedClickable(
                        onClick = { onCellClick(community) },
                        onLongClick = { onCellLongClick(community) }
                    )
            )
        }
    }
}
