package me.timpushkin.vkunsubapp.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.timpushkin.vkunsubapp.ApplicationState
import me.timpushkin.vkunsubapp.R

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    applicationState: ApplicationState,
    onOpenCommunity: () -> Unit = {},
    onApplySelectedCommunities: () -> Unit = {}
) {
    if (applicationState.mode == ApplicationState.Mode.AUTH) return

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    ModalBottomSheetLayout(
        sheetContent = {
            CommunityInfoSheet(
                community = applicationState.displayedCommunity,
                onOpenClick = onOpenCommunity,
                onCloseClick = { scope.launch { sheetState.hide() } }
            )
        },
        modifier = Modifier.fillMaxSize(),
        sheetElevation = 8.dp,
        sheetBackgroundColor = MaterialTheme.colors.background
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBar(
                    mode = applicationState.mode,
                    onButtonClick = {
                        when (applicationState.mode) {
                            ApplicationState.Mode.AUTH -> {}
                            ApplicationState.Mode.FOLLOWING ->
                                applicationState.setMode(ApplicationState.Mode.UNFOLLOWED)
                            ApplicationState.Mode.UNFOLLOWED ->
                                applicationState.setMode(ApplicationState.Mode.FOLLOWING)
                        }
                    }
                )
            },
            bottomBar = {
                val selectedNum = applicationState.selectedCommunities.size

                AnimatedVisibility(
                    visible = selectedNum > 0,
                    enter = slideInVertically { fullHeight -> fullHeight / 2 },
                    exit = slideOutVertically { fullHeight -> fullHeight / 2 }
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colors.background
                    ) {
                        CounterButton(
                            number = selectedNum,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            onClick = onApplySelectedCommunities
                        ) {
                            Text(
                                text =
                                when (applicationState.mode) {
                                    ApplicationState.Mode.AUTH -> ""
                                    ApplicationState.Mode.FOLLOWING -> stringResource(R.string.unfollow)
                                    ApplicationState.Mode.UNFOLLOWED -> stringResource(R.string.follow)
                                }
                            )
                        }
                    }
                }
            }
        ) { contentPadding ->
            CommunitiesGrid(
                communities = applicationState.communities,
                selectedCommunities = applicationState.selectedCommunities,
                modifier = Modifier.padding(contentPadding),
                onCellClick = {
                    applicationState.displayedCommunity = it
                    scope.launch { sheetState.show() }
                },
                onCellLongClick = { applicationState.selectOrUnselect(it) }
            )
        }
    }
}

@Composable
fun TopBar(mode: ApplicationState.Mode, onButtonClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.communities),
                style = MaterialTheme.typography.h6
            )
        },
        actions = {
            IconButton(onClick = onButtonClick) {
                when (mode) {
                    ApplicationState.Mode.AUTH -> {}
                    ApplicationState.Mode.FOLLOWING ->
                        Icon(
                            painter = painterResource(R.drawable.ic_clock_outline_28),
                            contentDescription = "See unfollowed"
                        )
                    ApplicationState.Mode.UNFOLLOWED ->
                        Icon(
                            painter = painterResource(R.drawable.ic_users_3_outline_28),
                            contentDescription = "See following"
                        )
                }
            }
        },
        backgroundColor = MaterialTheme.colors.background
    )
}
