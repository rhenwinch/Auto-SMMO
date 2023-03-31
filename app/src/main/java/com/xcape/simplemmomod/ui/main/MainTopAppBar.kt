package com.xcape.simplemmomod.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.xcape.simplemmomod.R
import com.xcape.simplemmomod.ui.common.IconResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    isUserLoggedIn: Boolean,
    progress: Float,
    onNavigationDrawerClick: () -> Unit,
    onWebViewReload: () -> Unit,
    onFriendsMenuClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontWeight = FontWeight.Black
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigationDrawerClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Toggle drawer"
                    )
                }
            },
            actions = {
                if (isUserLoggedIn) {
                    IconButton(onClick = onFriendsMenuClick) {
                        Icon(
                            painter = IconResource
                                .fromDrawableResource(R.drawable.ic_people)
                                .asPainterResource(),
                            contentDescription = "Friends"
                        )
                    }
                }

                IconButton(onClick = onWebViewReload) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        )

        AnimatedVisibility(visible = progress < 1F) {
            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
        }
    }
}