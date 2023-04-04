package com.xcape.simplemmomod.ui.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.web.*
import com.xcape.simplemmomod.common.Constants.APP_TAG
import com.xcape.simplemmomod.common.Endpoints.BASE_URL
import com.xcape.simplemmomod.common.Endpoints.BOT_VERIFICATION_URL
import com.xcape.simplemmomod.common.JavascriptFunctions.CHECK_IF_ALREADY_VERIFIED
import com.xcape.simplemmomod.data.smmo_tasks.BOT_RESPONSE
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.ui.common.TabbedMenuItem
import com.xcape.simplemmomod.ui.common.TabbedMenuItem.Companion.toTabbedMenuItem
import com.xcape.simplemmomod.ui.theme.SimpleMMOModTheme
import com.xcape.simplemmomod.ui.webview.WebViewActivity.Companion.listOfSitesToRunOnSeparateActivity
import com.xcape.simplemmomod.ui.webview.WebViewActivity.Companion.openSeparateActivity
import dagger.hilt.android.AndroidEntryPoint


const val FINISHED_LOADING = 1F
const val START_LOADING = 0F

@Suppress("UNCHECKED_CAST")
@AndroidEntryPoint
class WebViewActivity : ComponentActivity() {

    companion object {
        val listOfSitesToRunOnSeparateActivity = listOf(
            "new_page",
            "/user/view/"
        )
        const val URL_INTENT = "url"
        const val CUSTOM_TAB_INTENT = "custom_tabbed_view"

        fun Context.openSeparateActivity(url: String, tabLinks: String? = null) {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(URL_INTENT, url)
            intent.putExtra(CUSTOM_TAB_INTENT, tabLinks)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: WebViewViewModel = viewModel()
            val user by viewModel.user.collectAsState(initial = User())

            var originalUrl = intent.getStringExtra(URL_INTENT)!!
            val customTabs = intent.getStringExtra(CUSTOM_TAB_INTENT)
            val tabItem = originalUrl.toTabbedMenuItem(customTabs)
            var selectedTab by remember { mutableStateOf(0) }

            var url by remember {
                if(tabItem is TabbedMenuItem.Custom) {
                    val firstKey = (tabItem.tabs as Map<String, Pair<String, Int>>).keys.first()
                    mutableStateOf(originalUrl + tabItem.tabs[firstKey]!!.first)
                } else {
                    mutableStateOf(originalUrl)
                }
            }

            if(tabItem is TabbedMenuItem.Custom) {
                val firstKey = (tabItem.tabs as Map<String, Pair<String, Int>>).keys.first()
                originalUrl += tabItem.tabs[firstKey]!!.first
            }

            val webViewState = rememberWebViewState(url = url, additionalHttpHeaders = emptyMap())
            val webViewNavigator = rememberWebViewNavigator()

            fun activityKiller(
                isVerified: Boolean,
                isForced: Boolean = false
            ) {
                if(isForced) {
                    this.finish()
                    return
                }

                if(isVerified) {
                    viewModel.onEvent(WebViewUiEvent.UserVerified)
                }

                if(originalUrl.contains(BOT_RESPONSE) && !isVerified) {
                    webViewNavigator.loadUrl(CHECK_IF_ALREADY_VERIFIED)
                    return
                }

                this.finish()
            }

            BackHandler {
                if(originalUrl.contains(BOT_RESPONSE)) {
                    webViewNavigator.loadUrl(CHECK_IF_ALREADY_VERIFIED)
                    return@BackHandler
                }

                this.finish()
            }

            var toolbarTitle by remember { mutableStateOf("") }
            var progress by remember { mutableStateOf(0.1F) }
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
            )

            SimpleMMOModTheme {
                Surface {
                    Scaffold(
                        topBar = {
                            TabbedTopAppBar(
                                header = toolbarTitle,
                                tabItem = tabItem,
                                selectedTab = selectedTab,
                                onTabChange = { index, tab ->
                                    selectedTab = index

                                    val newUrl = when {
                                        tabItem is TabbedMenuItem.Profile && index == 0 -> originalUrl
                                        tabItem is TabbedMenuItem.Tasks -> String.format("$BASE_URL/tasks/%s", tab)
                                        tabItem is TabbedMenuItem.Custom -> BASE_URL + tab
                                        else -> String.format("%s/%s", originalUrl, tab)
                                    }
                                    url = newUrl
                                },
                                progress = animatedProgress,
                                onWebViewReload = webViewNavigator::reload,
                                onNavigationIconClick = { activityKiller(false) },
                            )
                        }
                    ) { padding ->
                        WebViewContent(
                            loggedInUserAgent = user!!.userAgent,
                            webViewState = webViewState,
                            webViewNavigator = webViewNavigator,
                            activityKiller = { isVerified, isForced ->
                                activityKiller(isVerified, isForced)
                            },
                            onTitleChange = {
                                toolbarTitle = it
                            },
                            onWebViewLoading = { newProgress ->
                                progress = newProgress
                            },
                            onCookieChange = { cookie ->
                                cookie?.let {
                                    viewModel.onEvent(WebViewUiEvent.ChangedWebsite(it))
                                }
                            },
                            onLoginChange = { },
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    }
}

@Suppress("unused", "UNUSED_PARAMETER")
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContent(
    loggedInUserAgent: String,
    webViewState: WebViewState,
    webViewNavigator: WebViewNavigator,
    activityKiller: (isVerified: Boolean, isForced: Boolean) -> Unit,
    onTitleChange: (String) -> Unit = { },
    onWebViewLoading: (Float) -> Unit,
    onCookieChange: (String?) -> Unit,
    onLoginChange: (String?) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current

    val webClient = remember {
        object : AccompanistWebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onWebViewLoading(START_LOADING)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onWebViewLoading(FINISHED_LOADING)

                onTitleChange(view?.title ?: "")

                val cookieManager = CookieManager.getInstance()
                val cookie = cookieManager.getCookie(url)
                onCookieChange(cookie)
                onLoginChange(cookie)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?,
            ): Boolean {
                val url = request!!.url.toString()

                val shouldRunOnASeparateActivity = listOfSitesToRunOnSeparateActivity.any { url.contains(it) }
                if(shouldRunOnASeparateActivity) {
                    onWebViewLoading(FINISHED_LOADING)
                    context.openSeparateActivity(url = url)
                    return true
                }

                return super.shouldOverrideUrlLoading(view, request)
            }
        }
    }

    val webChromeClient = remember {
        object : AccompanistWebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                onWebViewLoading(newProgress / 100F)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d(APP_TAG, "[${consoleMessage?.messageLevel() ?: "Unknown"}] > ${consoleMessage?.message() ?: "No message provided"}")
                return super.onConsoleMessage(consoleMessage)
            }
        }
    }

    WebView(
        state = webViewState,
        navigator = webViewNavigator,
        onCreated = { webView ->
            webView.apply {
                val transparent = 0x00000000
                setBackgroundColor(transparent)

                settings.javaScriptEnabled = true
                settings.userAgentString = loggedInUserAgent
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun close() {
                        activityKiller(webViewState.lastLoadedUrl == BOT_VERIFICATION_URL, false)
                    }
                }, "ok")

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun openFixedTabPage(title: String, links: String) {
                        context.openSeparateActivity(BASE_URL, links)
                    }

                    @JavascriptInterface
                    fun openScrollableTabPage(title: String, links: String) {
                        context.openSeparateActivity(BASE_URL, links)
                    }
                }, "app")

                @Suppress("SpellCheckingInspection")
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun showAd() {
                        post {
                            val fingerprint =
                                "samsung/a54ks/a54:13/TP1A.220624.014/A545NKSU2DWB6:user/release-keys"
                            loadUrl("javascript:redeemedReward(\"${fingerprint}\")")
                        }
                    }
                }, "oktwo")

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun check(content: String) {
                        if(content.contains("You have passed")) {
                            activityKiller(true, false)
                            return
                        }

                        activityKiller(false, true)
                    }
                }, "verificationCheck")
            }
        },
        client = webClient,
        chromeClient = webChromeClient,
        captureBackPresses = false,
        modifier = modifier.fillMaxHeight()
    )
}