package me.timpushkin.vkunfollowapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity

@Composable
fun CollapsibleTopScaffold(
    modifier: Modifier = Modifier,
    state: CollapsibleTopScaffoldState = rememberCollapsibleTopScaffoldState(),
    expandedTopBar: @Composable BoxScope.() -> Unit = {},
    collapsedTopBar: @Composable BoxScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = bottomBar
    ) { contentPadding ->

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    state.apply {
                        if (isCollapsible && contentOffsetPx >= minTopBarHeightPx)
                            topBarHeightPx = (topBarHeightPx + available.y / 1.5f)
                                .coerceIn(minTopBarHeightPx, maxTopBarHeightPx)
                        contentOffsetPx = (contentOffsetPx + available.y / 1.5f)
                            .coerceIn(0f, maxTopBarHeightPx)
                    }

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
            val topBarHeight = with(LocalDensity.current) { state.topBarHeightPx.toDp() }
            val isExpanded = state.topBarHeightPx > state.minTopBarHeightPx

            Box(
                modifier = Modifier.offset(y = with(LocalDensity.current) { state.contentOffsetPx.toDp() }),
                content = content
            )

            Box(
                modifier = Modifier
                    .height(topBarHeight)
                    .align(Alignment.TopStart),
                content = collapsedTopBar
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
        }
    }
}

class CollapsibleTopScaffoldState(
    val minTopBarHeightPx: Float = 0f,
    val maxTopBarHeightPx: Float = 0f
) {
    var topBarHeightPx by mutableStateOf(maxTopBarHeightPx)
    var contentOffsetPx by mutableStateOf(maxTopBarHeightPx)
    var isCollapsible by mutableStateOf(true)

    companion object {
        val Saver: Saver<CollapsibleTopScaffoldState, *> = listSaver(
            save = { state ->
                state.run {
                    listOf(
                        minTopBarHeightPx,
                        maxTopBarHeightPx,
                        topBarHeightPx,
                        contentOffsetPx,
                        if (isCollapsible) 1f else 0f
                    )
                }
            },
            restore = { saved ->
                CollapsibleTopScaffoldState(
                    minTopBarHeightPx = saved[0],
                    maxTopBarHeightPx = saved[1]
                ).apply {
                    topBarHeightPx = saved[2]
                    contentOffsetPx = saved[3]
                    isCollapsible = saved[4] == 1f
                }
            }
        )
    }

    fun expand() {
        topBarHeightPx = maxTopBarHeightPx
        contentOffsetPx = maxTopBarHeightPx
    }

    fun collapse() {
        topBarHeightPx = minTopBarHeightPx
        contentOffsetPx = minTopBarHeightPx
    }
}

@Composable
fun rememberCollapsibleTopScaffoldState(
    minTopBarHeightPx: Float = 0f,
    maxTopBarHeightPx: Float = 0f
) = rememberSaveable(saver = CollapsibleTopScaffoldState.Saver) {
    CollapsibleTopScaffoldState(minTopBarHeightPx, maxTopBarHeightPx)
}
