package com.xcape.simplemmomod.ui.main

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.web.rememberWebViewNavigator
import com.google.accompanist.web.rememberWebViewState
import com.xcape.simplemmomod.common.Endpoints.ABOUT_URL
import com.xcape.simplemmomod.common.Endpoints.BATTLE_URL
import com.xcape.simplemmomod.common.Endpoints.CHARACTER_URL
import com.xcape.simplemmomod.common.Endpoints.COLLECTION_URL
import com.xcape.simplemmomod.common.Endpoints.CRAFTING_URL
import com.xcape.simplemmomod.common.Endpoints.DIAMONDS_STORE_URL
import com.xcape.simplemmomod.common.Endpoints.DISCUSSION_BOARD_URL
import com.xcape.simplemmomod.common.Endpoints.EARN_URL
import com.xcape.simplemmomod.common.Endpoints.EVENTS_URL
import com.xcape.simplemmomod.common.Endpoints.GUILDS_URL
import com.xcape.simplemmomod.common.Endpoints.HOME_URL
import com.xcape.simplemmomod.common.Endpoints.INVENTORY_URL
import com.xcape.simplemmomod.common.Endpoints.JOBS_URL
import com.xcape.simplemmomod.common.Endpoints.LEADERBOARDS_URL
import com.xcape.simplemmomod.common.Endpoints.LOGIN_URL
import com.xcape.simplemmomod.common.Endpoints.LOGOUT_URL
import com.xcape.simplemmomod.common.Endpoints.MESSAGES_URL
import com.xcape.simplemmomod.common.Endpoints.NOTIFICATIONS_URL
import com.xcape.simplemmomod.common.Endpoints.QUEST_URL
import com.xcape.simplemmomod.common.Endpoints.REGISTER_URL
import com.xcape.simplemmomod.common.Endpoints.SETTINGS_URL
import com.xcape.simplemmomod.common.Endpoints.SOCIAL_MEDIA_URL
import com.xcape.simplemmomod.common.Endpoints.SUPPORT_URL
import com.xcape.simplemmomod.common.Endpoints.TASKS_URL
import com.xcape.simplemmomod.common.Endpoints.TOWN_URL
import com.xcape.simplemmomod.common.Endpoints.TRAVEL_URL
import com.xcape.simplemmomod.common.JavascriptFunctions.SHOW_FRIENDS
import com.xcape.simplemmomod.common.JavascriptFunctions.SHOW_PLAYER_INFO
import com.xcape.simplemmomod.domain.model.AppPreferences
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.ui.account_picker.AccountPickerActivity
import com.xcape.simplemmomod.ui.account_picker.USER_DATA
import com.xcape.simplemmomod.ui.autotravelui.AutoTravelActivity
import com.xcape.simplemmomod.ui.common.ExitBackPressHandler
import com.xcape.simplemmomod.ui.common.MenuItem
import com.xcape.simplemmomod.ui.theme.SimpleMMOModTheme
import com.xcape.simplemmomod.ui.webview.WebViewActivity.Companion.openSeparateActivity
import com.xcape.simplemmomod.ui.webview.WebViewContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainApp()
        }
    }
}

@Composable
fun MainApp(
    viewModel: MainViewModel = viewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val appState by viewModel.appState.collectAsState(initial = AppPreferences())
    val user by viewModel.user.collectAsState(initial = User())
    val userAgent = appState.userAgent
    val isUserLoggedIn = appState.userIdToUse != 0

    var url by rememberSaveable(isUserLoggedIn) {
        mutableStateOf(if(isUserLoggedIn) HOME_URL else LOGIN_URL)
    }
    var selectedMenuItem by rememberSaveable {
        mutableStateOf(if(isUserLoggedIn) "Home" else "Login")
    }

    val webViewState = rememberWebViewState(url = url, additionalHttpHeaders = emptyMap())
    val webViewNavigator = rememberWebViewNavigator()

    val onWebStateChange = { newUrl: String ->
        url = newUrl
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val accountPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        scope.launch {
            selectedMenuItem = "Login"

            if(it.resultCode != RESULT_OK)
                return@launch

            it.data?.getStringExtra(USER_DATA)?.let { cookie ->
                val cookieManager = CookieManager.getInstance()

                cookieManager.removeAllCookies(null)
                cookie.split("; ").forEach { key ->
                    cookieManager.setCookie(HOME_URL, key)
                }

                viewModel.login(cookie = cookie)

                viewModel.updateUserAgentPreference()

                selectedMenuItem = "Home"
                onWebStateChange(HOME_URL)
            }
        }
    }

    val onFabClick = {
        context.startActivity(
            Intent(context, AutoTravelActivity::class.java)
        )
    }

    val onAccountPickerClick = {
        accountPickerLauncher.launch(
            Intent(context, AccountPickerActivity::class.java)
        )
    }

    var progress by remember { mutableStateOf(0F) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    val bottomNavItems = listOf(
        MenuItem.Home { onWebStateChange(HOME_URL) },
        MenuItem.Quest { onWebStateChange(QUEST_URL) },
        MenuItem.Travel { onWebStateChange(TRAVEL_URL) },
        MenuItem.Battle { onWebStateChange(BATTLE_URL) },
        MenuItem.Profile { webViewNavigator.loadUrl(SHOW_PLAYER_INFO) }
    )

    val drawerHeaderMenuItems = mapOf(
        "messages" to MenuItem.Messages { onWebStateChange(MESSAGES_URL) },
        "notifications" to MenuItem.Notifications { onWebStateChange(NOTIFICATIONS_URL) },
        "character" to MenuItem.Character { onWebStateChange(CHARACTER_URL) }
    )

    val drawerBodyLoggedInMenuItems = listOf(
        MenuItem.Town { onWebStateChange(TOWN_URL) },
        MenuItem.Inventory { context.openSeparateActivity(url = INVENTORY_URL) },

        MenuItem.Jobs { onWebStateChange(JOBS_URL) },
        MenuItem.Tasks { context.openSeparateActivity(url = TASKS_URL) },
        MenuItem.Collections { onWebStateChange(COLLECTION_URL) },
        MenuItem.Crafting { onWebStateChange(CRAFTING_URL) },

        MenuItem.Guilds { onWebStateChange(GUILDS_URL) },
        MenuItem.Leaderboards { onWebStateChange(LEADERBOARDS_URL) },
        MenuItem.DiscussionBoards { onWebStateChange(DISCUSSION_BOARD_URL) },
        MenuItem.SocialMedia { onWebStateChange(SOCIAL_MEDIA_URL) },
        MenuItem.Events { onWebStateChange(EVENTS_URL) },

        MenuItem.DiamondStore { onWebStateChange(DIAMONDS_STORE_URL) },
        MenuItem.Earn { onWebStateChange(EARN_URL) },
        MenuItem.Settings { onWebStateChange(SETTINGS_URL) },
        MenuItem.About { onWebStateChange(ABOUT_URL) },
        MenuItem.Support { onWebStateChange(SUPPORT_URL) },
        MenuItem.Logout {
            onWebStateChange(LOGOUT_URL)
            viewModel.onEvent(MainUiEvent.ClickedLogoutButton)
        },
    )

    val drawerBodyLoggedOutMenuItems = listOf(
        MenuItem.Login { onWebStateChange(LOGIN_URL) },
        MenuItem.Register { onWebStateChange(REGISTER_URL) },
        MenuItem.AccountPicker(onAccountPickerClick)
    )

    ExitBackPressHandler()

    SimpleMMOModTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    NavigationDrawer(
                        isUserLoggedIn = isUserLoggedIn,
                        userProvider = {
                            user ?:
                                throw NullPointerException("No logged in user was found")
                        },
                        headerMenuItems = drawerHeaderMenuItems,
                        bodyMenuItems = mapOf(
                            "loggedIn" to drawerBodyLoggedInMenuItems,
                            "loggedOut" to drawerBodyLoggedOutMenuItems
                        ),
                        selectedItem = selectedMenuItem,
                        onViewChange = {
                            scope.launch {
                                drawerState.close()
                                selectedMenuItem = it
                            }
                        }
                    )
                },
                gesturesEnabled = drawerState.isOpen
            ) {
                Scaffold(
                    topBar = {
                        MainTopAppBar(
                            isUserLoggedIn = isUserLoggedIn,
                            progress = { animatedProgress },
                            onNavigationDrawerClick = {
                                scope.launch {
                                    drawerState.open()
                                    viewModel.onEvent(MainUiEvent.ClickedNavigationDrawer(isOpen = true))
                                }
                            },
                            onWebViewReload = {
                                webViewNavigator.reload()
                            },
                            onFriendsMenuClick = {
                                webViewNavigator.loadUrl(SHOW_FRIENDS)
                            }
                        )
                    },
                    bottomBar = {
                        if(isUserLoggedIn) {
                            MainBottomAppBar(
                                menuItems = bottomNavItems,
                                selectedItem = selectedMenuItem,
                                onFabClick = onFabClick,
                                onViewChange = { selectedMenuItem = it }
                            )
                        }
                    },
                    content = {
                        if(userAgent.isNotEmpty()) {
                            WebViewContent(
                                loggedInUserAgent = userAgent,
                                webViewState = webViewState,
                                webViewNavigator = webViewNavigator,
                                activityKiller = { _, _ -> },
                                onWebViewLoading = { newProgress ->
                                    progress = newProgress
                                },
                                onCookieChange = { newCookie ->
                                    newCookie?.let {
                                        viewModel.onEvent(
                                            MainUiEvent.ChangedWebsite(newCookie = newCookie)
                                        )
                                    }
                                },
                                onLoginChange = { cookie ->
                                    cookie?.let {
                                        if(!isUserLoggedIn) {
                                            viewModel.login(cookie = cookie)
                                        }
                                    }
                                },
                                modifier = Modifier.padding(it)
                            )
                        }
                    },
                )
            }
        }
    }
}