package me.timpushkin.vkunsubapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CollapsibleTopScaffold(
    modifier: Modifier = Modifier,
    maxTopBarHeight: Dp = 0.dp,
    minTopBarHeight: Dp = 0.dp,
    expandedTopBar: @Composable BoxScope.() -> Unit = {},
    shrunkTopBar: @Composable BoxScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = bottomBar
    ) { contentPadding ->
        val minTopBarHeightPx = with(LocalDensity.current) { minTopBarHeight.roundToPx().toFloat() }
        val maxTopBarHeightPx = with(LocalDensity.current) { maxTopBarHeight.roundToPx().toFloat() }

        var topBarHeightPx by remember { mutableStateOf(maxTopBarHeightPx) }
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    topBarHeightPx = (topBarHeightPx + available.y / 1.5f)
                        .coerceIn(minTopBarHeightPx, maxTopBarHeightPx)
                    return Offset.Zero
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(contentPadding)
                .nestedScroll(nestedScrollConnection)
                .then(modifier)
        ) {
            val topBarHeight = with(LocalDensity.current) { topBarHeightPx.toDp() }
            val isExpanded = topBarHeightPx > minTopBarHeightPx

            Box(
                modifier = Modifier
                    .height(topBarHeight)
                    .align(Alignment.TopStart),
                content = shrunkTopBar
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .height(topBarHeight)
                        .align(Alignment.TopStart),
                    content = expandedTopBar
                )
            }

            Box(
                modifier = Modifier.offset(y = with(LocalDensity.current) { topBarHeightPx.toDp() }),
                content = content
            )
        }
    }
}
