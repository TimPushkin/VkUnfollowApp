package me.timpushkin.vkunfollowapp.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.timpushkin.vkunfollowapp.R
import me.timpushkin.vkunfollowapp.model.Community

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    appState: ApplicationState,
    onModeSwitch: () -> Unit,
    onDisplayCommunity: (Community) -> Unit,
    onManageSelectedCommunities: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scaffoldState = with(LocalDensity.current) {
        rememberCollapsibleTopScaffoldState(
            minTopBarHeightPx = 56.dp.toPx(),
            maxTopBarHeightPx = 168.dp.toPx()
        )
    }

    val uriHandler = LocalUriHandler.current

    ModalBottomSheetLayout(
        sheetContent = {
            CommunityInfoSheet(
                community = appState.displayedCommunity,
                onOpenClick = { uriHandler.openUri(appState.displayedCommunity.uri.toString()) },
                onCloseClick = { scope.launch { sheetState.hide() } }
            )
        },
        modifier = Modifier.fillMaxSize(),
        sheetState = sheetState,
        sheetShape = MaterialTheme.shapes.large.copy(
            bottomStart = CornerSize(0),
            bottomEnd = CornerSize(0)
        ),
        sheetElevation = 8.dp,
        sheetBackgroundColor = MaterialTheme.colors.background
    ) {
        CollapsibleTopScaffold(
            modifier = Modifier.fillMaxSize(),
            state = scaffoldState,
            expandedTopBar = {
                BigTopBar(
                    title = when (appState.mode) {
                        ApplicationState.Mode.FOLLOWING -> stringResource(R.string.unfollow_communities)
                        ApplicationState.Mode.UNFOLLOWED -> stringResource(R.string.follow_communities)
                    },
                    description = stringResource(R.string.hold_to_see_more),
                    actions = {
                        ModeSwitchButton(
                            mode = appState.mode,
                            onClick = {
                                onModeSwitch()
                                scaffoldState.expand()
                            }
                        )
                    }
                )
            },
            collapsedTopBar = {
                SmallTopBar(
                    actions = {
                        ModeSwitchButton(
                            mode = appState.mode,
                            onClick = {
                                onModeSwitch()
                                scaffoldState.expand()
                            }
                        )
                    }
                )
            },
            bottomBar = {
                BottomBar(
                    mode = appState.mode,
                    showButton = !appState.isWaitingManageResponse,
                    selectedNum = appState.selectedCommunities.size,
                    onButtonClick = onManageSelectedCommunities
                )
            }
        ) {
            CommunitiesGrid(
                communities = appState.communities,
                selectedCommunities = appState.selectedCommunities,
                onCellClick = { appState.switchSelectionOf(it) },
                onCellLongClick = {
                    onDisplayCommunity(it)
                    scope.launch { sheetState.show() }
                }
            )
        }
    }
}

@Composable
fun BigTopBar(
    title: String,
    description: String,
    actions: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(start = 4.dp, top = 4.dp, end = 4.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h6
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                modifier = Modifier.fillMaxWidth(0.63f),
                color = MaterialTheme.colors.primaryVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1
            )
        }
    }
}

@Composable
fun SmallTopBar(
    actions: @Composable RowScope.() -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.communities),
                style = MaterialTheme.typography.h6
            )
        },
        actions = actions,
        backgroundColor = MaterialTheme.colors.background
    )
}

@Composable
fun ModeSwitchButton(mode: ApplicationState.Mode, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        when (mode) {
            ApplicationState.Mode.FOLLOWING ->
                Icon(
                    painter = painterResource(R.drawable.ic_clock_outline_28),
                    contentDescription = "See unfollowed",
                    tint = MaterialTheme.colors.secondary
                )
            ApplicationState.Mode.UNFOLLOWED ->
                Icon(
                    painter = painterResource(R.drawable.ic_users_3_outline_28),
                    contentDescription = "See following",
                    tint = MaterialTheme.colors.secondary
                )
        }
    }
}

@Composable
fun BottomBar(
    mode: ApplicationState.Mode,
    showButton: Boolean,
    selectedNum: Int,
    onButtonClick: () -> Unit
) {
    AnimatedVisibility(
        visible = selectedNum > 0,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colors.background
        ) {
            Crossfade(targetState = showButton) { showButton ->
                if (showButton) {
                    CounterButton(
                        number = selectedNum,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        onClick = onButtonClick
                    ) {
                        Text(
                            text = when (mode) {
                                ApplicationState.Mode.FOLLOWING -> stringResource(R.string.unfollow)
                                ApplicationState.Mode.UNFOLLOWED -> stringResource(R.string.follow)
                            }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.request_in_progress),
                            color = MaterialTheme.colors.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
