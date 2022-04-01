package me.timpushkin.vkunsubapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
            }
        ) { contentPadding ->
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                CommunitiesGrid(
                    communities = applicationState.communities,
                    onCellClick = {
                        applicationState.displayedCommunity = it
                        scope.launch { sheetState.show() }
                    },
                    onCellLongClick = { applicationState.selectOrUnselect(it) }
                )

                val selectedNum = applicationState.selectedCommunities.size
                if (selectedNum > 0) {
                    ApplySelectedCommunitiesButton(
                        mode = applicationState.mode,
                        selectedNum = selectedNum,
                        onClick = onApplySelectedCommunities,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
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

@Composable
fun ApplySelectedCommunitiesButton(
    mode: ApplicationState.Mode,
    selectedNum: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .then(modifier),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary
        )
    ) {
        Row {
            Text(
                text =
                when (mode) {
                    ApplicationState.Mode.AUTH -> ""
                    ApplicationState.Mode.FOLLOWING -> stringResource(R.string.unfollow)
                    ApplicationState.Mode.UNFOLLOWED -> stringResource(R.string.follow)
                }
            )

            Spacer(modifier = Modifier.width(2.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colors.onPrimary,
                        shape = CircleShape
                    )
                    .size(5.dp)
            ) {
                Text(text = selectedNum.toString(10))
            }
        }
    }
}
