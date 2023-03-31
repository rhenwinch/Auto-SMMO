package com.xcape.simplemmomod.ui.autotravelui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xcape.simplemmomod.R
import com.xcape.simplemmomod.ui.common.IconResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoTravelTopAppBar(
    isUserTravelling: Boolean = false,
    onClickPlay: (isPausing: Boolean) -> Unit,
    onNavigationIconClick: () -> Unit
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
                IconButton(onClick = onNavigationIconClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Go back"
                    )
                }
            },
            actions = {
                val icon = if(isUserTravelling) {
                    IconResource.fromDrawableResource(R.drawable.ic_pause)
                } else {
                    IconResource.fromImageVector(Icons.Default.PlayArrow)
                }

                IconButton(onClick = {
                    onClickPlay(isUserTravelling)
                }) {
                    Icon(
                        painter = icon.asPainterResource(),
                        contentDescription = "Play Button"
                    )
                }
            },
            modifier = Modifier.shadow(4.dp)
        )

        AnimatedVisibility(visible = isUserTravelling) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}