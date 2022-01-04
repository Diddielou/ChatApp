package fhnw.emoba.thatsapp.model

import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import java.time.format.DateTimeFormatter
import java.util.*

class ThatsAppModel(
    private val context: ComponentActivity,
    private val cameraAppConnector: CameraAppConnector,
    private val locator: GPSConnector
) {
    private val modelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /*  UI  */
    // Texts
    var drawerTitle = "Settings"

    // Screen
    var currentScreen by mutableStateOf(Screen.MAIN)

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
    /*
    http://www.hivemq.com/demos/websocket-client/
    Subscriptions:
    Get users: fhnw/emoba/thatsapp01cc/users/#
    Get messages: fhnw/emoba/thatsapp01cc/notification/users/097e25de-3079-4039-b4e0-a7fac9ff2fdc
    Send texts to me: fhnw/emoba/thatsapp01cc/notification/users/097e25de-3079-4039-b4e0-a7fac9ff2fdc

    {
      "messageID":  "$messageID",
      "timestamp":  1641296690,
      "senderID":  "0daffcae-dc4e-4ek9-87ec-9670874a406d",
      "receiverID": "ce060e4d-1c48-4ba3-a40f-3b961689797b",
      "payload":  { "body": "Helloo"},
      "messageType":  "TEXT"
    }
    */


    private val mqttConnector by lazy { MqttConnector(mqttBroker, mainTopic) }
    private val soundPlayer by lazy { MediaPlayer.create(context, R.raw.hangouts_message) }

    var isLoading by mutableStateOf(false)
    //var isLoaded by mutableStateOf(false)

    var message by mutableStateOf("")
    var messageImage by mutableStateOf<Bitmap?>(null)

    val allMessages = mutableStateListOf<ChatMessage>()
    var notificationMessage by mutableStateOf("")
    var MessagesPublished by mutableStateOf(0)

    /* CHAT PARTNERS */
    var currentChatPartner by mutableStateOf<ChatUser?>(null)

    /* USERS */
    val allUsers = mutableStateListOf<ChatUser>()

    /* PHOTO */
    var photo by mutableStateOf<Bitmap?>(null)

    /* LOCATION */
    val chatLocations = mutableStateListOf<GeoPosition>()

    // asynchroner Aufruf
    fun rememberCurrentPosition() {
        //modelScope.launch { // Code wird jetzt im UI /main Thread ausgeführt
        locator.getLocation(onSuccess = {
            chatLocations.add(it)
            notificationMessage = "neuer Wegpunkt"
            // open location in GoogleMaps...
        },
            onFailure = {}, //todo: was machen wir?
            onPermissionDenied = {
                notificationMessage = "Keine Berechtigung."
            }  //todo: was machen wir?
        )
        //}
    }

    /* FILE UPLOAD */
    var fileioURL by mutableStateOf<String?>(null)
    var uploadInProgress by mutableStateOf(false)

    var downloadedFile by mutableStateOf<Bitmap?>(null)
    var downloadInProgress by mutableStateOf(false)
    //var downloadMessage by mutableStateOf("")


    /* PROFILE */
    var profileId by mutableStateOf("")
    var profileName by mutableStateOf("")
    var profileBio by mutableStateOf("")
    var profileImagePath by mutableStateOf("")
    var profilePhoto by mutableStateOf("")

    // Preferences auslesen
    fun loadOrCreateProfile(): ChatUser {
        val prefs = context.getSharedPreferences("thatsAppPreferences", MODE_PRIVATE)
        profileId = prefs.getString("profileID", UUID.randomUUID().toString()).toString()
        profileName = prefs.getString("profileName", "yourName").toString()
        profileBio = prefs.getString("profileBio", "Your bio").toString()
        profileImagePath = prefs.getString(
            "profileImagePath",
            "https://pbs.twimg.com/profile_images/1140254399962521601/9hL6tDQj_400x400.jpg"
        ).toString()
        profilePhoto =
            if (profileImagePath != "") {
                loadImageFromStorage(profileImagePath)
            } else {
                profileImagePath
            }.toString()
        val profileVersion = prefs.getString("profileVersion", "1.0.0").toString()
        val profileLastOnline = System.currentTimeMillis() / 1000
        // User zusammenbauen
        val profile = ChatUser(
            userID = profileId,
            nickname = profileName,
            bio = profileBio,
            userImage = profilePhoto,
            userProfileImage = null,
            lastOnline = profileLastOnline,
            version = profileVersion
        )

        return profile
    }

    // Prefs speichern
    // TODO aufrufen im UI
    fun updatePrefs() {
        val prefs = context.getSharedPreferences("thatsAppPreferences", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("profileId", profileId)
        editor.putString("profileName", profileName)
        editor.putString("profileBio", profileBio)
        //editor.putString("profileImgUrl", profileImgUrl)
        //editor.putString("profilePicLocalPath", profilePicLocalPath)
        editor.apply()
    }



    /* USERS */
    fun addChatUser(user: ChatUser) {
        allUsers.add(user)
    }

    private fun addOrUpdateUserList(givenUser: ChatUser) {
        val existingUser = allUsers.find {it.userID == givenUser.userID}

        if(existingUser != null){
            allUsers.remove(existingUser)
        }

        allUsers.add(givenUser)
    }

    /* MESSAGES */
    fun processMessage(message: ChatMessage) {
        addChatUser(allUsers.first { user -> user.userID == message.senderID })

        if (message.messageType == ChatPayloadContents.TEXT.name) {
            addMessage(message)
        }
        playSound()
        // TODO: Process LOCATION, INFO, LIVE, IMAGE
    }

    fun addMessage(message: ChatMessage) {
        allMessages.add(message)
    }



    /*
    Notes
        // senderID : me  => message from me to someone 1
        // receiverID: foreign => message From me to someone 1

        // senderID : foreign => message to me from someone else 2
        // receiverID: me => message from someone else to me 2
     */
    // TODO use in UI
    fun filterMessagesPerConversation(givenUser: ChatUser) : List<ChatMessage> {
        val tempList = allMessages

        //tempList.filter { a -> a.senderID != profileId  } // keep all messages in list that are foreign

        // keep messages in list that are from me to the givenUser.id or from givenUser.id to me
        return tempList.filter { a -> a.senderID == givenUser.userID || a.receiverID == givenUser.userID }

    }

    fun filterConversationForMyMessages(givenUser: ChatUser) : List<ChatMessage> {
        val tempList = filterMessagesPerConversation(givenUser)

        return tempList.filter { a -> a.senderID == givenUser.userID }
    }
    fun filterConversationForForeignMessages(givenUser: ChatUser) : List<ChatMessage> {
        val tempList = filterMessagesPerConversation(givenUser)

        return tempList.filter { a -> a.receiverID == givenUser.userID }
    }





    /**
     * MQTT methods
     */
    fun connectAndSubscribe() {
        mqttConnector.connectAndSubscribe(
            subtopic = userTopic,
            onNewMessageUsers = {
                addOrUpdateUserList(it)
            },
            onError = { _, p ->
                notificationMessage = p
                playSound()
            },
            profileUser = loadOrCreateProfile(),
            profileUserTopic = "$userTopic$profileId",
            thisUserNotificationTopic = "$notificationTopic$profileId",
            onNewMessages = {
                processMessage(it)
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

    /* NOTIFICATION */
    private fun playSound() {
        soundPlayer.seekTo(0)
        soundPlayer.start()
    }


    /* PHOTO */
    fun takePhoto() {
        cameraAppConnector.getBitmap(onSuccess = { photo = it },
            onCanceled = { notificationMessage = "Kein neues Bild" })
    }

    fun rotatePhoto() {
        photo?.let { modelScope.launch { photo = photo!!.rotate(90f) } }
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }


    fun loadUserListImages() {
        allUsers.forEach {
            downloadUserImage(it)
        }
    }

    /* FILE UPLOAD */
    fun downloadUserImage(user: ChatUser) {
        modelScope.launch {
            if (user.userImage.isNotBlank()) {
                downloadBitmapFromFileIO(
                    url = user.userImage,
                    onSuccess = { user.userProfileImage = it },
                    onDeleted = { notificationMessage = "File is deleted" },
                    onError = { notificationMessage = "Connection failed" })
                downloadInProgress = false
            }
        }
    }

    fun getMessageImage() {

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

    fun getLocalDateTimeFromUTCtimestamp(utcTimestamp: Long): String {
        // Convert to LocalDateTime (GMT+1) in String
        val formatter = DateTimeFormatter.ofPattern("dd. MMMM HH:mm")
        return getLocalDateTime(utcTimestamp).format(formatter)
    }


    /* PROFILE */
    // TODO
    private fun loadImageFromStorage(profileImagePath: String): Any {
        return ""
    }


}