package fhnw.emoba.thatsapp.model

import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import fhnw.emoba.R
import fhnw.emoba.thatsapp.data.MqttConnector
import fhnw.emoba.thatsapp.data.That
import java.time.LocalDateTime
import java.time.ZoneId

class ThatsAppModel(private val context: ComponentActivity) {

    // Texts
    var drawerTitle = "Settings"

    // Screen
    var currentScreen by mutableStateOf(Screen.CHAT)

    // Theme
    var darkTheme by mutableStateOf(false)
        private set

    fun toggleTheme() {
        darkTheme = !darkTheme
    }
    fun toggleThemeText(): String {
        return if (darkTheme) {
            "Go light"
        } else {
            "Go dark"
        }
    }


    /**
     * MQTT variables
     */
    val mqttBroker = "broker.hivemq.com"
    val mainTopic = "/fhnw/emoba/thatsapp01cc"
    val subTopic = "/users/Lou"
    val clientId = "emobaLou"
    var message by mutableStateOf("")

    val allThats = mutableStateListOf<That>()
    var notificationMessage by mutableStateOf("")
    var thatsPublished by mutableStateOf(0)

    private val mqttConnector by lazy { MqttConnector(mqttBroker, mainTopic + subTopic) }
    private val soundPlayer by lazy { MediaPlayer.create(context, R.raw.hangouts_message) }



    /**
     * MQTT methods
     */
    fun connectAndSubscribe() {
        mqttConnector.connectAndSubscribe(
            onNewMessage = {
                allThats.add(it)
                playSound()
            },
            onError = { _, p ->
                notificationMessage = p
                playSound()
            }
        )
    }

    fun publish() {
        mqttConnector.publish(
            That(
            sender = "Lou",
            message = "Ciaociao ($thatsPublished)" + message
        ),
            onPublished = { thatsPublished++ }
        )
    }

    private fun playSound() {
        soundPlayer.seekTo(0)
        soundPlayer.start()
    }




    /* Help methods: Unix Timestamps */
    /**
     * Takes a LocalDateTime and converts it to an UTC-normalized unix timestamp. If [dateTime]
     * is not provided, the current unix timestamp is returned (UTC)
     *
     * @author Contributors of https://github.com/MichaelJob/ThatsAppSpecEmobaHS21
     */
    fun getUTCTimestamp(dateTime: LocalDateTime? = null): Long {
        return if (dateTime == null) {
            System.currentTimeMillis() / 1000
        } else {
            val now = LocalDateTime.now()
            val zone = ZoneId.systemDefault()
            val zoneOffSet = zone.rules.getOffset(now)
            dateTime.toEpochSecond(zoneOffSet)
        }
    }

    /**
     * Takes an unix timestamp (assumed to be UTC) and returns a LocalDateTime. If [timestamp] is
     * null, the current, timezone-aware datetime is returned
     *
     * @author Contributors of https://github.com/MichaelJob/ThatsAppSpecEmobaHS21
     */
    fun getLocalDateTime(timestamp: Long? = null): LocalDateTime {
        return if (timestamp == null) {
            LocalDateTime.now()
        } else {
            val now = LocalDateTime.now()
            val zone = ZoneId.systemDefault()
            val zoneOffSet = zone.rules.getOffset(now)
            LocalDateTime.ofEpochSecond(timestamp, 0, zoneOffSet)
        }
    }



}