package me.timpushkin.vkunsubapp.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
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
    modifier: Modifier = Modifier,
    onCellClick: (Community) -> Unit = {},
    onCellLongClick: (Community) -> Unit = {}
) {
    LazyVerticalGrid(
        cells = GridCells.Adaptive(dimensionResource(R.dimen.community_photo_size)),
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items(communities) { community ->
            var isSelected by remember { mutableStateOf(false) }

            CommunityCell(
                name = community.name,
                photoUri = community.photoUri,
                isSelected = isSelected,
                modifier = Modifier
                    .padding(5.dp)
                    .combinedClickable(
                        onClick = { onCellClick(community) },
                        onLongClick = {
                            isSelected = !isSelected
                            onCellLongClick(community)
                        }
                    )
            )
        }
    }
}
