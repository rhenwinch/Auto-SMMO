package com.xcape.simplemmomod.ui.main

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xcape.simplemmomod.ui.common.MenuItem

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainBottomAppBar(
    menuItems: List<MenuItem>,
    selectedItem: String,
    onFabClick: () -> Unit,
    onViewChange: (String) -> Unit
) {
    BottomAppBar(
        modifier = Modifier.height(75.dp),
        actions = {
            menuItems.forEach { item ->
                val isSelected = item.route == selectedItem
                val iconColor: Color by animateColorAsState(
                    if (isSelected)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        Color.Unspecified
                )

                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(
                            color = iconColor,
                            shape = RoundedCornerShape(size = 10.dp)
                        )
                        .padding(8.dp)
                        .clickable {
                            onViewChange(item.route)
                            item.action()
                        }
                ) {
                    Icon(
                        painter = item.icon.asPainterResource(),
                        contentDescription = item.route,
                    )

                    AnimatedVisibility(
                        visible = isSelected,
                        enter = scaleIn(),
                        exit = scaleOut()
                    ) {
                        Text(
                            text = item.route,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(5.dp))
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedItem == "Travel",
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(onClick = onFabClick) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Run Auto-Travel")
                }
            }
        }
    )
}


