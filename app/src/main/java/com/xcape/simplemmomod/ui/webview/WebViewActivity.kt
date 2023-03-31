package com.xcape.simplemmomod.ui.webview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.web.*
import com.xcape.simplemmomod.common.Constants.APP_TAG
import com.xcape.simplemmomod.common.Endpoints
import com.xcape.simplemmomod.common.Endpoints.BOT_VERIFICATION_URL
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.ui.common.TabbedMenuItem
import com.xcape.simplemmomod.ui.common.TabbedMenuItem.Companion.toTabbedMenuItem
import com.xcape.simplemmomod.ui.theme.SimpleMMOModTheme
import com.xcape.simplemmomod.ui.webview.WebViewActivity.Companion.listOfSitesToRunOnSeparateActivity
import com.xcape.simplemmomod.ui.webview.WebViewActivity.Companion.openSeparateActivity
import dagger.hilt.android.AndroidEntryPoint

const val FINISHED_LOADING = 1F
const val START_LOADING = 0F

@AndroidEntryPoint
class WebViewActivity : ComponentActivity() {

    companion object {
        val listOfSitesToRunOnSeparateActivity = listOf(
            "new_page",
            "/user/view/"
        )
        const val URL_INTENT = "url"

        fun Context.openSeparateActivity(url: String) {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(URL_INTENT, url)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: WebViewViewModel = viewModel()
            val user by viewModel.user.collectAsState(initial = User())

            val originalUrl = intent.getStringExtra(URL_INTENT)!!
            var url by remember { mutableStateOf(originalUrl) }

            var selectedTab by remember { mutableStateOf(0) }
            val tabItem = originalUrl.toTabbedMenuItem()

            val webViewState = rememberWebViewState(url = url, additionalHttpHeaders = emptyMap())
            val webViewNavigator = rememberWebViewNavigator()

            val activityKiller = { isVerified: Boolean ->
                if(isVerified) {
                    viewModel.onEvent(WebViewUiEvent.UserVerified)
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
                                        tabItem == TabbedMenuItem.PROFILE && index == 0 -> originalUrl
                                        tabItem == TabbedMenuItem.TASKS -> String.format("${Endpoints.BASE_URL}/tasks/%s", tab)
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
                            activityKiller = { activityKiller(it) },
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
                            onLoginChange = { _, _, _ -> },
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewContent(
    loggedInUserAgent: String,
    webViewState: WebViewState,
    webViewNavigator: WebViewNavigator,
    activityKiller: (isVerified: Boolean) -> Unit,
    onTitleChange: (String) -> Unit = { },
    onWebViewLoading: (Float) -> Unit,
    onCookieChange: (String?) -> Unit,
    onLoginChange: (WebView?, String?, CookieManager) -> Unit,
    modifier: Modifier
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

                onLoginChange(view, cookie, cookieManager)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
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
                        activityKiller(webViewState.lastLoadedUrl == BOT_VERIFICATION_URL)
                    }
                }, "ok")
            }
        },
        client = webClient,
        chromeClient = webChromeClient,
        captureBackPresses = false,
        modifier = modifier.fillMaxHeight()
    )
}