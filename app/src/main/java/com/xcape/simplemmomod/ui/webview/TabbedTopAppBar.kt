package com.xcape.simplemmomod.ui.webview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xcape.simplemmomod.ui.common.TabbedMenuItem

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabbedTopAppBar(
    header: String,
    progress: Float = 1F,
    selectedTab: Int,
    tabItem: TabbedMenuItem? = null,
    onWebViewReload: () -> Unit,
    onNavigationIconClick: () -> Unit,
    onTabChange: (Int, String) -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    text = header,
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
                IconButton(onClick = onWebViewReload) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            },
            modifier = Modifier.shadow(4.dp)
        )

        var color = MaterialTheme.colorScheme.primary
        if(tabItem != null) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp
            ) {
                if(tabItem is TabbedMenuItem.Custom) {
                    val listOfTabs = tabItem.tabs as Map<String, Pair<String, Int>>

                    for ((tabName, tabDetails) in listOfTabs) {
                        Tab(text = { Text(tabName) },
                            selected = selectedTab == tabDetails.second,
                            onClick = {
                                onTabChange(tabDetails.second, tabDetails.first)
                            }
                        )
                    }
                }
                else {
                    val listOfTabs = tabItem.tabs as List<String>
                    listOfTabs.forEachIndexed { index, title ->
                        Tab(text = { Text(title) },
                            selected = selectedTab == index,
                            onClick = {
                                onTabChange(index, title.lowercase())
                            }
                        )
                    }
                }
            }
            color = MaterialTheme.colorScheme.secondary
        }

        AnimatedVisibility(visible = progress < 1F) {
            LinearProgressIndicator(
                progress = progress,
                color = color,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}