package com.xcape.simplemmomod.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.xcape.simplemmomod.R
import com.xcape.simplemmomod.common.Endpoints.BOT_VERIFICATION_URL
import com.xcape.simplemmomod.common.Functions
import com.xcape.simplemmomod.common.OnTravellerStateChange
import com.xcape.simplemmomod.common.SkillType.Companion.toSkillType
import com.xcape.simplemmomod.domain.repository.UserRepository
import com.xcape.simplemmomod.domain.smmo_tasks.AutoSMMOLogger
import com.xcape.simplemmomod.domain.smmo_tasks.Traveller
import com.xcape.simplemmomod.ui.autotravelui.AutoTravelActivity
import com.xcape.simplemmomod.ui.autotravelui.AutoTravelUiState
import com.xcape.simplemmomod.ui.autotravelui.TravellingStatus
import com.xcape.simplemmomod.ui.theme.md_theme_light_error
import com.xcape.simplemmomod.ui.theme.md_theme_light_onSurface
import com.xcape.simplemmomod.ui.webview.WebViewActivity
import com.xcape.simplemmomod.ui.webview.WebViewActivity.Companion.URL_INTENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

const val SKILL_TYPE = "skill_type"

const val BROADCAST_ID = "broadcast_message"
const val BROADCAST_MSG = "broadcast_message"

const val TRAVELLING_NOTIFICATION_REQUEST_CODE = 0
const val PLAY_REQUEST_CODE = 0
const val PAUSE_REQUEST_CODE = 1
const val VERIFY_REQUEST_CODE = 1
const val STOP_REQUEST_CODE = 2

const val ACTION_STOP_TRAVELLING = "stop_travelling"
const val ACTION_PLAY_TRAVELLING = "play_travelling"
const val ACTION_PAUSE_TRAVELLING = "pause_travelling"
const val ACTION_START_TRAVELLING = "start_travelling"
const val ACTION_CONSUME_ERROR_TRAVELLING = "consume_error"
const val ACTION_CLEAN_TRAVEL_LOGS = "clean_travel_logs"
const val ACTION_TICK_CHANGE_UPGRADE_TRAVELLING = "tick_change_upgrade"
const val ACTION_TICK_AUTO_EQUIP_TRAVELLING = "tick_auto_equip"
const val ACTION_TICK_IGNORE_NPC_TRAVELLING = "tick_ignore_npc"

const val TRAVELLING_NOTIFICATION_CHANNEL_ID = "auto_traveller_channel"
const val TRAVELLING_NOTIFICATION_CHANNEL_NAME = "Auto Traveller"
const val TRAVELLING_NOTIFICATION_ID = 1
const val VERIFICATION_NOTIFICATION_CHANNEL_ID = "verification_channel"
const val VERIFICATION_NOTIFICATION_CHANNEL_NAME = "AutoSMMO Verification"
const val VERIFICATION_NOTIFICATION_ID = 2
const val ERROR_NOTIFICATION_CHANNEL_ID = "error_notification"
const val ERROR_NOTIFICATION_CHANNEL_NAME = "Script Error"
const val ERROR_NOTIFICATION_ID = 3

@AndroidEntryPoint
class TravellerForegroundService : LifecycleService(), OnTravellerStateChange {
    private var travellingJob: Job? = null
    private var isFirstRun = true
    private val travellerBroadcastReceiver by lazy { TravellerBroadcastReceiver() }
    private val user by lazy { userRepository.getFlowLoggedInUser() }

    @Inject
    lateinit var traveller: Traveller

    @Inject
    lateinit var autoSMMOLogger: AutoSMMOLogger

    @Inject
    lateinit var userRepository: UserRepository

    companion object {
        private val state = MutableStateFlow(AutoTravelUiState())
        val exposedState: StateFlow<AutoTravelUiState> = state.asStateFlow()

        private fun resetTravellerState() {
            state.update { AutoTravelUiState() }
        }
    }

    inner class TravellerBroadcastReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getStringExtra(BROADCAST_MSG)?.let {
                onActionChange(it, intent)
            }
        }
    }

    private sealed class NotificationButtons(val name: String) {
        object Stop: NotificationButtons("Stop")
        object Play: NotificationButtons("Play")
        object Verify: NotificationButtons("Verify")
        object Pause: NotificationButtons("Pause")
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(travellerBroadcastReceiver, IntentFilter(BROADCAST_ID))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(travellerBroadcastReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            onActionChange(it, intent)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun onActionChange(action: String, intent: Intent? = null) {
        var toLog: String? = null

        when (action) {
            ACTION_START_TRAVELLING -> {
                if(isFirstRun) {
                    toLog = "[SERVICE]> Starting service..."
                    isFirstRun = false

                    resetTravellerState()
                    bindTravellingStatusToNotificationTitle()
                    bindUserVerification()
                    startTravellerService()
                }
            }
            ACTION_STOP_TRAVELLING -> {
                toLog = "[SERVICE]> Stopping service..."
                state.update { it.copy(isServiceRunning = false) }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
            ACTION_PLAY_TRAVELLING -> start()
            ACTION_PAUSE_TRAVELLING -> {
                toLog = "[SERVICE]> Pausing service..."
                pause()
            }
            ACTION_CONSUME_ERROR_TRAVELLING -> onConsumeError()
            ACTION_CLEAN_TRAVEL_LOGS -> clearTravelLogs()
            ACTION_TICK_CHANGE_UPGRADE_TRAVELLING -> {
                val skillType = intent!!.getStringExtra(SKILL_TYPE)!!.toSkillType()
                state.update { it.copy(skillToUpgrade = skillType) }
            }
            ACTION_TICK_AUTO_EQUIP_TRAVELLING -> {
                state.update { it.copy(shouldAutoEquipItem = !it.shouldAutoEquipItem) }
            }
            ACTION_TICK_IGNORE_NPC_TRAVELLING -> {
                state.update { it.copy(shouldSkipNpc = !it.shouldSkipNpc) }
            }
            else -> throw IllegalStateException("Invalid action given!")
        }

        lifecycleScope.launch {
            if (toLog != null) {
                autoSMMOLogger.log(message = toLog)
            }
        }
    }

    override fun start() {
        state.update {
            it.copy(isTravelling = true)
        }

        travellerJob()
    }

    override fun pause() {
        state.update {
            it.copy(
                isTravelling = false,
                header = TravellingStatus.NotStarted
            )
        }

        travellingJob?.cancel()
    }

    override fun onError(message: String) {
        state.update {
            it.copy(
                isTravelling = false,
                errors = message,
                header = TravellingStatus.Error
            )
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val notificationBuilder = createErrorNotification(message)

        notificationManager.notify(ERROR_NOTIFICATION_ID, notificationBuilder.build())
    }

    override fun onConsumeError() {
        state.update {
            it.copy(
                errors = "",
                header = TravellingStatus.NotStarted
            )
        }
    }

    private fun startTravellerService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = createNotificationBuilder(state.value.header.toString())

        startForeground(TRAVELLING_NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun travellerJob() {
        travellingJob?.cancel()

        travellingJob = lifecycleScope.launch {
            onStatusChange(TravellingStatus.Initializing)
            delay(1500L) // for effect

            try {
                while (state.value.isTravelling) {
                    yield()
                    val currentTime = Functions.getTimeInMilliseconds()
                    var waitTime = 0L

                    if(currentTime >= traveller.energyTimer) {
                        onStatusChange(
                            status = TravellingStatus.UpgradeSkill(
                                skillType = state.value.skillToUpgrade
                            )
                        )
                        traveller.upgradeSkill()

                        onStatusChange(TravellingStatus.Questing)
                        waitTime += traveller.doQuest()

                        onStatusChange(TravellingStatus.Battling)
                        val arenaWaitTime = traveller.doArena(
                            shouldAutoEquip = state.value.shouldAutoEquipItem,
                            shouldSkipNPCs = state.value.shouldSkipNpc
                        )
                        val shouldSkipNPCs = arenaWaitTime == -1L
                        waitTime += if(shouldSkipNPCs) (800L..1500L).random() else arenaWaitTime


                        onStatusChange(
                            status = TravellingStatus.UpgradeSkill(
                                skillType = state.value.skillToUpgrade
                            )
                        )
                        traveller.upgradeSkill()
                        traveller.resetEnergyTimer()
                    }
                    else {
                        onStatusChange(TravellingStatus.Stepping)
                        waitTime += traveller.takeStep(
                            shouldAutoEquip = state.value.shouldAutoEquipItem,
                            shouldSkipNPCs = state.value.shouldSkipNpc,
                        )
                    }

                    onStatusChange(TravellingStatus.Idle)
                    delay(waitTime)
                }
            }
            catch (_: CancellationException) {
                return@launch
            }
            catch (e: Exception) {
                e.printStackTrace()
                onError(e.stackTraceToString())
            }
        }

        travellingJob?.start()
    }

    private fun clearTravelLogs() {
        lifecycleScope.launch {
            userRepository.getLoggedInUser()?.let {
                userRepository.updateUser(user = it.copy(travelLog = ""))
            }
        }
    }

    private fun bindTravellingStatusToNotificationTitle() {
        lifecycleScope.launch {
            state.collectLatest {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

                val notificationBuilder = createNotificationBuilder(it.header.toString())

                notificationManager.notify(TRAVELLING_NOTIFICATION_ID, notificationBuilder.build())
            }
        }
    }

    private fun bindUserVerification() {
        val needsVerification = user.map {
            it?.needsVerification ?: false
        }.distinctUntilChanged()

        lifecycleScope.launch {
            needsVerification.collect { verify ->
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

                if (verify) {
                    onStatusChange(TravellingStatus.Verify)

                    val notificationBuilder = createVerificationNotification()

                    notificationManager.notify(VERIFICATION_NOTIFICATION_ID, notificationBuilder.build())
                } else {
                    if(!state.value.isTravelling)
                        onStatusChange(TravellingStatus.NotStarted)

                    notificationManager.cancel(VERIFICATION_NOTIFICATION_ID)
                }
            }
        }
    }

    private fun createVerificationNotification(): NotificationCompat.Builder {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationSound = Settings.System.DEFAULT_ALARM_ALERT_URI
        val vibrationEffect = LongArray(10) { 1000 }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                notificationManager = notificationManager,
                channelId = VERIFICATION_NOTIFICATION_CHANNEL_ID,
                channelName = VERIFICATION_NOTIFICATION_CHANNEL_NAME,
                importance = IMPORTANCE_HIGH,
                isVerifying = true,
                notificationSound = notificationSound,
                vibrationEffects = vibrationEffect
            )
        }

        val pendingIntent =
            getNotificationsButtonPendingActivity(NotificationButtons.Verify.name)

        return NotificationCompat.Builder(this@TravellerForegroundService, VERIFICATION_NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_travel)
            .setContentTitle("Verify!")
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(notificationSound)
            .setVibrate(vibrationEffect)
            .addAction(
                NotificationCompat.Action(
                    null,
                    NotificationButtons.Verify.name,
                    pendingIntent
                )
            )
    }

    private fun createErrorNotification(errorMessage: String): NotificationCompat.Builder {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationSound = Settings.System.DEFAULT_NOTIFICATION_URI

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                notificationManager = notificationManager,
                channelId = ERROR_NOTIFICATION_CHANNEL_ID,
                channelName = ERROR_NOTIFICATION_CHANNEL_NAME,
                importance = IMPORTANCE_HIGH,
                notificationSound = notificationSound
            )
        }

        return NotificationCompat.Builder(this@TravellerForegroundService, ERROR_NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
            .setOngoing(false)
            .setSmallIcon(R.drawable.ic_travel)
            .setContentTitle("Error Running the Traveller!")
            .setContentText(errorMessage)
            .setContentIntent(getAutoTravelActivityPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setSound(notificationSound)
    }

    private fun createNotificationBuilder(title: String): NotificationCompat.Builder {
        val startOrPause = when(state.value.isTravelling) {
            true -> NotificationButtons.Pause.name
            false -> NotificationButtons.Play.name
        }

        var builder = NotificationCompat.Builder(this@TravellerForegroundService, TRAVELLING_NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_travel)
            .setContentTitle(title)
            .setContentIntent(getAutoTravelActivityPendingIntent())
            .addAction(
                NotificationCompat.Action(
                    null,
                    startOrPause,
                    getNotificationsButtonPendingActivity(startOrPause)
                )
            )
            .addAction(
                NotificationCompat.Action(
                    null,
                    NotificationButtons.Stop.name,
                    getNotificationsButtonPendingActivity(NotificationButtons.Stop.name)
                )
            )

        if(state.value.header == TravellingStatus.Error) {
            builder = builder.setContentText(state.value.errors)
        }

        return builder
    }

    private fun onStatusChange(status: TravellingStatus)
        = state.update { it.copy(header = status) }

    private fun getNotificationsButtonPendingActivity(button: String): PendingIntent {
        return when(button.lowercase()) {
            "stop" -> {
                val stopIntent = Intent(BROADCAST_ID)
                stopIntent.putExtra(BROADCAST_MSG, ACTION_STOP_TRAVELLING)
                PendingIntent.getBroadcast(
                    this,
                    STOP_REQUEST_CODE,
                    stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            "play" -> {
                val playIntent = Intent(BROADCAST_ID)
                playIntent.putExtra(BROADCAST_MSG, ACTION_PLAY_TRAVELLING)
                PendingIntent.getBroadcast(
                    this,
                    PLAY_REQUEST_CODE,
                    playIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            "pause" -> {
                val pauseIntent = Intent(BROADCAST_ID)
                pauseIntent.putExtra(BROADCAST_MSG, ACTION_PAUSE_TRAVELLING)
                PendingIntent.getBroadcast(
                    this,
                    PAUSE_REQUEST_CODE,
                    pauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            "verify" -> {
                val intent = Intent(this, WebViewActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(URL_INTENT, BOT_VERIFICATION_URL)
                }
                PendingIntent.getActivity(
                    this,
                    VERIFY_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            else -> throw Exception("Invalid button!")
        }
    }

    private fun getAutoTravelActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, AutoTravelActivity::class.java)

        return TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)

            getPendingIntent(TRAVELLING_NOTIFICATION_REQUEST_CODE,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        notificationManager: NotificationManager,
        channelId: String = TRAVELLING_NOTIFICATION_CHANNEL_ID,
        channelName: String = TRAVELLING_NOTIFICATION_CHANNEL_NAME,
        importance: Int = IMPORTANCE_LOW,
        isVerifying: Boolean = false,
        notificationSound: Uri? = null,
        vibrationEffects: LongArray? = null,
    ) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            importance
        )

        channel.enableLights(true)
        channel.lightColor = md_theme_light_onSurface.toArgb()

        notificationSound?.let {
            channel.lightColor = md_theme_light_error.toArgb()

            val attr = AudioAttributes.Builder()
                .setUsage(if(isVerifying) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            channel.setSound(it, attr)
        }

        vibrationEffects?.let {
            channel.enableVibration(true)
            channel.vibrationPattern = vibrationEffects
        }

        notificationManager.createNotificationChannel(channel)
    }
}