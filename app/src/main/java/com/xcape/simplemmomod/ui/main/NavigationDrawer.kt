package com.xcape.simplemmomod.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.ui.common.IconResource
import com.xcape.simplemmomod.ui.common.MenuItem
import com.xcape.simplemmomod.ui.theme.SimpleMMOModTheme

@Composable
fun NavigationDrawer(
    isUserLoggedIn: Boolean,
    user: User?,
    headerMenuItems: Map<String, MenuItem>,
    bodyMenuItems: Map<String, List<MenuItem>>,
    selectedItem: String,
    onViewChange: (String) -> Unit,
) {
    ModalDrawerSheet(
        drawerShape = MaterialTheme.shapes.small,
        drawerTonalElevation = 4.dp,
        modifier = Modifier.width(300.dp)
    ) {
        DrawerBody(
            isUserLoggedIn = isUserLoggedIn,
            user = user,
            headerMenuItems = headerMenuItems,
            menuItems = bodyMenuItems,
            selectedItem = selectedItem,
            onViewChange = onViewChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerHeader(
    user: User,
    menuItems: Map<String, MenuItem>,
    onViewChange: (String) -> Unit,
) {
    val messages = menuItems["messages"]!!
    val notifications = menuItems["notifications"]!!
    val character = menuItems["character"]!!

    Row {
        ConstraintLayout(
            modifier = Modifier.fillMaxWidth()
        ) {
            val (image, column, messagesButton, notificationsButton) = createRefs()

            AsyncImage(
                model = user.avatar,
                contentDescription = user.username,
                placeholder = IconResource.fromImageVector(Icons.Default.Person).asPainterResource(),
                modifier = Modifier
                    .padding(15.dp)
                    .clip(CircleShape)
                    .size(45.dp)
                    .constrainAs(image) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )

            Column(
                modifier = Modifier
                    .constrainAs(column) {
                        start.linkTo(image.end, margin = 5.dp)
                        end.linkTo(messagesButton.start, margin = 15.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }
                    .clickable {
                        onViewChange(character.route)
                        character.action()
                    }
            ) {
                BadgedBox(badge = {
                    if(user.characterUpgrades > 0) {
                        Badge { Text(text = user.characterUpgrades.toString()) }
                    }
                }) {
                    Text(
                        text = user.username,
                        textAlign = TextAlign.Start,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(vertical = 2.dp),
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "#${user.id}",
                    textAlign = TextAlign.Left,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Thin,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }


            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .constrainAs(messagesButton) {
                        end.linkTo(notificationsButton.start, margin = 15.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .clickable {
                        onViewChange(messages.route)
                        messages.action()
                    }
            ) {
                Icon(
                    painter = messages.icon.asPainterResource(),
                    contentDescription = messages.route,
                )

                AnimatedVisibility(
                    visible = user.messages > 0,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    Text(
                        text = user.messages.toString(),
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape
                            )
                            .padding(2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(end = 5.dp)
                    .constrainAs(notificationsButton) {
                        end.linkTo(parent.end, margin = 15.dp)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .clickable {
                        onViewChange(notifications.route)
                        notifications.action()
                    }
            ) {
                Icon(
                    painter = notifications.icon.asPainterResource(),
                    contentDescription = notifications.route
                )

                AnimatedVisibility(
                    visible = user.notifications > 0,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    Text(
                        text = user.notifications.toString(),
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = CircleShape
                            )
                            .padding(2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerBody(
    isUserLoggedIn: Boolean,
    user: User?,
    headerMenuItems: Map<String, MenuItem>,
    menuItems: Map<String, List<MenuItem>>,
    selectedItem: String,
    onViewChange: (String) -> Unit,
) {
    val itemsToUse = if(isUserLoggedIn && user != null) "loggedIn" else "loggedOut"

    if(isUserLoggedIn && user?.id != 0 && user != null) {
        DrawerHeader(
            user = user,
            menuItems = headerMenuItems,
            onViewChange = onViewChange
        )

        Divider(
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 15.dp)
        )
    }

    LazyColumn {
        items(menuItems[itemsToUse]!!) { item ->
            val isSelected = selectedItem == item.route
            val iconColor: Color by animateColorAsState(
                if (isSelected)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    Color.Unspecified
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onViewChange(item.route)
                        item.action()
                    }
                    .background(
                        color = iconColor,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(16.dp)
            ) {
                Icon(
                    painter = item.icon.asPainterResource(),
                    contentDescription = item.route,
                    modifier = Modifier.scale(1.7F),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = item.route,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    SimpleMMOModTheme {
        Box(Modifier.width(300.dp)) {
            val drawerHeaderMenuItems = mapOf(
                "messages" to MenuItem.Messages {  },
                "notifications" to MenuItem.Notifications {  },
                "character" to MenuItem.Character {  }
            )
            DrawerHeader(user = User().copy(username = "eeateat asi", id = 0), menuItems = drawerHeaderMenuItems, onViewChange = {})
        }
    }
}