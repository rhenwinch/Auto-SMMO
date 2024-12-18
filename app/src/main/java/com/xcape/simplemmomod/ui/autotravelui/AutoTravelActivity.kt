package com.xcape.simplemmomod.ui.autotravelui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.xcape.simplemmomod.R
import com.xcape.simplemmomod.common.Functions.isUserWayPastDailyResetTime
import com.xcape.simplemmomod.common.SkillType
import com.xcape.simplemmomod.domain.model.User
import com.xcape.simplemmomod.services.*
import com.xcape.simplemmomod.ui.common.IconResource
import com.xcape.simplemmomod.ui.common.showToast
import com.xcape.simplemmomod.ui.theme.SimpleMMOModTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class AutoTravelActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val foregroundIntent = Intent(this@AutoTravelActivity, TravellerForegroundService::class.java).also {
            it.action = ACTION_START_TRAVELLING
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(foregroundIntent)
        } else {
            startService(foregroundIntent)
        }

        setContent {
            val closeActivity = {
                this.finish()
            }

            SimpleMMOModTheme {
                Surface {
                    AutoTravelUi(activityKiller = closeActivity)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AutoTravelUi(
    activityKiller: () -> Unit,
    viewModel: AutoTravelViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun Context.startTravellerService(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    val serviceIntent = Intent(context, TravellerForegroundService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationsPermissionState = rememberPermissionState(
            android.Manifest.permission.POST_NOTIFICATIONS
        )

        val textToShow = if (notificationsPermissionState.status.shouldShowRationale) {
            "It seems that you've blocked notification permissions. Please grant this permission."
        } else {
            "Notification permission is required for this feature to be available. Please grant this permission."
        }

        if(!notificationsPermissionState.status.isGranted) {
            AutoTravelDialog(errorMessage = textToShow) {
                notificationsPermissionState.launchPermissionRequest()
            }

            if(notificationsPermissionState.status.isGranted) {
                serviceIntent.also {
                    it.action = ACTION_STOP_TRAVELLING
                    context.startTravellerService(it)
                }
            }
        }
    }

    val screenState by viewModel.state.collectAsState(initial = AutoTravelUiState())
    val user by viewModel.user.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val (selectedOption, onOptionSelected) = remember { mutableStateOf(screenState.skillToUpgrade) }

    LaunchedEffect(screenState.isServiceRunning) {
        if(!screenState.isServiceRunning)
            activityKiller()
    }

    val onExit = {
        if(screenState.isTravelling) {
            context.showToast("Pause the script to leave!")
        } else {
            serviceIntent.also {
                it.action = ACTION_STOP_TRAVELLING
                context.startTravellerService(it)
            }
            activityKiller()
        }
    }

    BackHandler(onBack = onExit)

    Scaffold(
        topBar = {
            AutoTravelTopAppBar(
                isUserTravelling = screenState.isTravelling,
                onNavigationIconClick = onExit,
                onClickPlay = { isPausing ->
                    scope.launch {
                        if(!isPausing) {
                            user?.let {
                                if(isUserWayPastDailyResetTime(resetTime = it.dailyResetTime)) {
                                    scope.launch {
                                        viewModel.resetUserDailies()
                                        snackbarHostState.showSnackbar(
                                            message = "Dailies have been reset!",
                                            withDismissAction = true,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            }

                            serviceIntent.also {
                                it.action = ACTION_PLAY_TRAVELLING
                                context.startTravellerService(it)
                            }
                        }
                        else {
                            serviceIntent.also {
                                it.action = ACTION_PAUSE_TRAVELLING
                                context.startTravellerService(it)
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { scaffoldPadding ->
        if(screenState.errors.isNotEmpty()) {
            AutoTravelDialog(
                errorMessage = screenState.errors,
                onConsumeError = {
                    serviceIntent.also {
                        it.action = ACTION_CONSUME_ERROR_TRAVELLING
                        context.startTravellerService(it)
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(scaffoldPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Status:",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Thin,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(bottom = 15.dp, top = 68.dp)
            )

            val header = remember(screenState.header) {
                screenState.header.toString()
            }

            Text(
                text = header,
                fontSize = 30.sp,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 68.dp)
            )

            Text(
                text = "Script Config:",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Thin,
                    textAlign = TextAlign.Center
                )
            )

            Row(modifier = Modifier.padding(bottom = 12.dp)) {
                SwitchWithLabel(
                    label = "Ignore NPCs",
                    isChecked = screenState.shouldSkipNpc,
                    onStateChange = {
                        serviceIntent.also {
                            it.action = ACTION_TICK_IGNORE_NPC_TRAVELLING
                            context.startTravellerService(it)
                        }
                    }
                )

                Spacer(modifier = Modifier.width(20.dp))

                SwitchWithLabel(
                    label = "Auto-equip",
                    isChecked = screenState.shouldAutoEquipItem,
                    onStateChange = {
                        serviceIntent.also {
                            it.action = ACTION_TICK_AUTO_EQUIP_TRAVELLING
                            context.startTravellerService(it)
                        }
                    }
                )
            }

            Text(
                text = "Skill to Upgrade:",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Thin,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SkillTypeRadioButtons(
                selectedOption = selectedOption,
                onOptionChange = { skill ->
                    onOptionSelected(skill)
                    serviceIntent.also {
                        it.action = ACTION_TICK_CHANGE_UPGRADE_TRAVELLING
                        it.putExtra(SKILL_TYPE, skill.toString())
                        context.startTravellerService(it)
                    }
                }
            )

            Text(
                text = "Travel Logs:",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Thin,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(top = 22.dp, bottom = 22.dp)
            )

            val listState = rememberLazyListState()
            val logs by remember {
                derivedStateOf { 
                    user?.travelLog
                        ?.split("\n") 
                        ?: emptyList()
                }
            }

            LaunchedEffect(logs) {
                listState.scrollToItem(logs.size)
            }

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp)
                    .height(140.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        shape = MaterialTheme.shapes.small
                    ),
            ) {
                items(logs) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Thin
                        )
                    )
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        serviceIntent.also {
                            it.action = ACTION_CLEAN_TRAVEL_LOGS
                            context.startTravellerService(it)
                        }

                        snackbarHostState.showSnackbar(
                            message = "Logs cleaned!",
                            withDismissAction = true,
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                modifier = Modifier.padding(top = 17.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Clean Logs",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 5.dp)
                    )

                    Text(
                        text = "(click if lagging)",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Divider(
                thickness = 2.dp,
                modifier = Modifier.padding(
                    start = 25.dp,
                    end = 25.dp,
                    top = 17.dp
                )
            )

            AsyncImage(
                model = user?.avatar,
                contentDescription = user?.username,
                placeholder = IconResource.fromImageVector(Icons.Default.Person).asPainterResource(),
                modifier = Modifier
                    .padding(top = 38.dp)
                    .size(68.dp)
            )
            
            val username = remember {
                String.format(Locale.getDefault(), "%s #%d", user?.username, user?.id)
            }

            Text(
                text = username,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(top = 14.dp)
            )

            Row(
                modifier = Modifier.padding(
                    bottom = 20.dp,
                    top = 35.dp
                )
            ) {
                Text(
                    text = "Level ",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Thin,
                        textAlign = TextAlign.Center
                    )
                )
                
                val level = remember(user?.level) {
                    String.format(Locale.getDefault(), "%,d", user?.level)
                }

                Text(
                    text = level,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(end = 35.dp)
                )

                Icon(
                    painter = IconResource.fromDrawableResource(R.drawable.gold).asPainterResource(),
                    contentDescription = "Gold",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )
                
                val gold = remember(user?.gold) {
                    String.format(Locale.getDefault(), "%,d", user?.gold)
                }
                
                Text(
                    text = gold,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

            StatsComponent(
                headline = "Daily Stats:",
                statsList = {
                    listOf(
                        GameStat.Steps(user!!.dailySteps),
                        GameStat.Quests(user!!.dailyQuests),
                        GameStat.Battles(user!!.dailyBattles),
                        GameStat.ItemsFound(user!!.dailyItemsFound),
                        GameStat.MaterialsFound(user!!.dailyMaterialsFound)
                    )
                }
            )

            StatsComponent(
                headline = "Total Stats:",
                statsList = {
                    listOf(
                        GameStat.Steps(user!!.totalSteps),
                        GameStat.Quests(user!!.totalQuests),
                        GameStat.Battles(user!!.totalBattles),
                        GameStat.ItemsFound(user!!.totalItemsFound),
                        GameStat.MaterialsFound(user!!.totalMaterialsFound)
                    )
                }
            )
        }
    }
}

@Composable
fun SkillTypeRadioButtons(
    selectedOption: SkillType,
    onOptionChange: (SkillType) -> Unit
) {
    val radioOptions = listOf(
        SkillType.DEX,
        SkillType.DEF,
        SkillType.STR
    )

    Row {
        radioOptions.forEach { skill ->
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .selectable(
                        selected = (skill == selectedOption),
                        onClick = { onOptionChange(skill) }
                    )
                    .padding(horizontal = 10.dp)
            ) {
                RadioButton(
                    selected = (skill == selectedOption),
                    onClick = { onOptionChange(skill) }
                )

                Text(
                    text = skill.toString().uppercase(),
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
    }
}

@Composable
fun StatsComponent(
    headline: String,
    statsList: () -> List<GameStat>
) {
    Text(
        text = headline,
        style = TextStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.Thin,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.padding(top = 15.dp, bottom = 15.dp)
    )

    StatsCardItem(statsList = statsList)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatsCardItem(statsList: () -> List<GameStat>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(
            space = 15.dp,
            alignment = Alignment.CenterHorizontally
        ),
        maxItemsInEachRow = 3,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .wrapContentWidth(Alignment.CenterHorizontally)
            .wrapContentHeight(Alignment.CenterVertically)
    ) {
        statsList().forEach { stat ->
            Card(
                shape = RoundedCornerShape(5.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                modifier = Modifier
                    .widthIn(min = 101.dp)
                    .wrapContentHeight(Alignment.CenterVertically)
                    .padding(bottom = 25.dp)
                    .clickable { }
            ) {
                Text(
                    text = stat.name,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(start = 10.dp, top = 4.dp)
                )
                
                val count = remember(stat.count) {
                    String.format(Locale.getDefault(), "%,d", stat.count)
                }

                Text(
                    text = count,
                    style = TextStyle(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewUI() {
    SimpleMMOModTheme(useDarkTheme = true) {
        AutoTravelUi(activityKiller = {})
    }
}