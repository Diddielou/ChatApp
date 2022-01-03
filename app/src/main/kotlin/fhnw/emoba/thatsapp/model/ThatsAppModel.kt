package fhnw.emoba.thatsapp.model

import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import fhnw.emoba.R
import fhnw.emoba.thatsapp.data.ChatMessage
import fhnw.emoba.thatsapp.data.ChatUser
import fhnw.emoba.thatsapp.data.GeoPosition
import fhnw.emoba.thatsapp.data.connectors.CameraAppConnector
import fhnw.emoba.thatsapp.data.connectors.GPSConnector
import fhnw.emoba.thatsapp.data.connectors.MqttConnector
import fhnw.emoba.thatsapp.data.downloadBitmapFromFileIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class ThatsAppModel(private val context: ComponentActivity,
                    private val cameraAppConnector: CameraAppConnector,
                    private val locator: GPSConnector
) {
    private val modelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /*  UI  */
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
    val mainTopic = "fhnw/emoba/thatsapp01cc"
    val notificationTopic = "/notifications/users/"
    val userTopic = "/users/"

    private val mqttConnector by lazy { MqttConnector(mqttBroker, mainTopic) }
    private val soundPlayer by lazy { MediaPlayer.create(context, R.raw.hangouts_message) }

    var message by mutableStateOf("")
    var messageImage by mutableStateOf<Bitmap?>(null)

    val allMessages = mutableStateListOf<ChatMessage>()
    var notificationMessage by mutableStateOf("")
    var MessagesPublished by mutableStateOf(0)

    /* PHOTO */
    var photo by mutableStateOf<Bitmap?>(null)

    /* LOCATION */
    val chatLocations = mutableStateListOf<GeoPosition>()
    // asynchroner Aufruf
    fun rememberCurrentPosition(){
        //modelScope.launch { // Code wird jetzt im UI /main Thread ausgeführt
        locator.getLocation(onSuccess          = { chatLocations.add(it)
            notificationMessage = "neuer Wegpunkt"
            // open location in GoogleMaps...
        },
            onFailure          = {}, //todo: was machen wir?
            onPermissionDenied = { notificationMessage = "Keine Berechtigung." }  //todo: was machen wir?
        )
        //}
    }

    /* FILE UPLOAD */
    var fileioURL          by mutableStateOf<String?>(null)
    var uploadInProgress   by mutableStateOf(false)

    var downloadedFile     by mutableStateOf<Bitmap?>(null)
    var downloadInProgress by mutableStateOf(false)
    var downloadMessage    by mutableStateOf("")


    /* PROFILE */
    //var profileUser  by  mutableStateOf(loadOrCreateProfile())
    var profileId = ""
    var profileName = ""
    var profileBio = ""
    var profileImagePath = ""
    var profilePhoto = ""
    var profileLastOnline = ""
    var profileVersion = ""


    fun loadOrCreateProfile(): ChatUser {
        val prefs = context.getSharedPreferences("thatsAppPreferences", MODE_PRIVATE)
        profileId = prefs.getString("profileID", UUID.randomUUID().toString()).toString()
        profileName = prefs.getString("profileName", "yourName").toString()
        profileBio = prefs.getString("profileBio", "Your bio").toString()
        profileImagePath = prefs.getString("profileImagePath", "").toString()
        profilePhoto =
            if (profileImagePath != "") { loadImageFromStorage(profileImagePath) }
            else { profileImagePath }.toString()
        profileLastOnline = prefs.getString("profileLastOnline", "").toString()
        profileVersion = prefs.getString("profileVersion", "").toString()

        // User zusammenbauen
        val profile = ChatUser(
            userID = profileId,
            nickname = profileName,
            bio = profileBio,
            userImage = profilePhoto,
            lastOnline = profileLastOnline,
            version = profileVersion)

        return profile
    }



    /* CHAT PARTNERS */
    var currentChatPartner by mutableStateOf<ChatUser?>(null)

    /* USERS */
    var allUsers = mutableStateListOf<ChatUser>()


    /* CONTACTS */
    var contacts = mutableStateListOf<ChatUser>()

    /* MESSAGES */
    fun processMessage(message: ChatMessage){
        addChatUser(contacts.first { user -> user.userID == message.senderID })

        if (message.messageType == ChatPayloadContents.TEXT.name ||
            message.messageType == ChatPayloadContents.LOCATION.name){
            addMessage(message)
        }
        else if(message.messageType == ChatPayloadContents.IMAGE.name) {
            downloadBitmapFromFileIO(
                onError = { },
                onDeleted = { },
                onSuccess = { },
                url = "URL" // TODO
            )
        }
        // TODO: Process INFO, LIVE
    }

    fun addMessage(message: ChatMessage){
        allMessages.add(message)
    }

    /* USERS */
    fun addChatUser(user: ChatUser){
        contacts.add(user)
    }

    /**
     * MQTT methods
     */
    fun connectAndSubscribe() {
        mqttConnector.connectAndSubscribe(
            onNewMessageUsers = {
                allUsers.add(it)
            },
            onError = { _, p ->
                notificationMessage = p
                playSound()
            },
            thisUserTopic = userTopic,
            thisUser = loadOrCreateProfile(),
            thisUserNotificationTopic = notificationTopic,
            onNewMessages = {
                addMessage(it)
                playSound()
            }
        )
    }

    fun publish() {
        val chatMessage = currentChatPartner?.userID?.let {
            ChatMessage(
                timestamp = getUTCTimestamp(),
                messageID = UUID.randomUUID().toString(),
                senderID = profileId,
                receiverID = it,
                payload = JSONObject("payload"),
                messageType = "",
                bitmap = messageImage,
                read = false,
                delivered = false
            )
        }
        if (chatMessage != null) {
            mqttConnector.publish(
                message = chatMessage,
                subtopic = "$notificationTopic${currentChatPartner?.userID}",
                onPublished = { MessagesPublished++ },
                onError = { notificationMessage = "Couldn't publish: ${chatMessage.messageID}" }
            )
        }
    }


    /*
    private fun buildPayload(Triple<String, String, Bitmap>){
        return getPayLoadData()
    }

    */

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



    /* PROFILE */
    // TODO
    private fun loadImageFromStorage(profileImagePath: String): Any {
        return ""
    }


    /* PHOTO */
    fun takePhoto() {
        cameraAppConnector.getBitmap(onSuccess  = { photo = it },
            onCanceled = { notificationMessage = "Kein neues Bild" })
    }

    fun rotatePhoto() {
        photo?.let { modelScope.launch { photo = photo!!.rotate(90f) } }
    }

    private fun Bitmap.rotate(degrees: Float) : Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    /* FILE UPLOAD */
    fun downloadFromFileIO(){
        if(fileioURL != null){
            downloadedFile = null
            downloadInProgress = true
            modelScope.launch {
                downloadBitmapFromFileIO(url       = fileioURL!!,
                    onSuccess = { downloadedFile = it},
                    onDeleted = { downloadMessage = "File is deleted"},
                    onError   = { downloadMessage = "Connection failed"})
                downloadInProgress = false
            }
        }
    }




}