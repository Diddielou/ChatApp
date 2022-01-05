package fhnw.emoba.thatsapp.model

import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asAndroidBitmap
import fhnw.emoba.R
import fhnw.emoba.thatsapp.data.ChatMessage
import fhnw.emoba.thatsapp.data.ChatUser
import fhnw.emoba.thatsapp.data.GeoPosition
import fhnw.emoba.thatsapp.data.connectors.CameraAppConnector
import fhnw.emoba.thatsapp.data.connectors.GPSConnector
import fhnw.emoba.thatsapp.data.connectors.MqttConnector
import fhnw.emoba.thatsapp.data.downloadBitmapFromURL
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
    var currentScreen by mutableStateOf(Screen.PROFILE)

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
    Get messages: fhnw/emoba/thatsapp01cc/notification/users/229abb4a-4fe9-4dfa-a013-594a8fba5bab
    Send texts to me: fhnw/emoba/thatsapp01cc/notification/users/229abb4a-4fe9-4dfa-a013-594a8fba5bab

    Publish users: fhnw/emoba/thatsapp01cc/users/
    {
      "userID": "0daffcae-dc4e-4ek9-87ec-9670874a458d",
      "nickname": "Hansli",
      "bio": "some interesting text about me",
      "userImage": "https://oneoffixx.com/wp-content/uploads/logo.png.webp",
      "lastOnline": 1640095599,
      "version": "1.0.0"
    }
     Receiver: me
     Sender: Hansli

    {
    "timestamp": 1641296690,
    "messageID": "c7f29c84-2449-4780-becd-b23ad2991234",
    "senderID": "0daffcae-dc4e-4ek9-87ec-9670874a458d",
    "receiverID": "229abb4a-4fe9-4dfa-a013-594a8fba5bab",
    "payload": {
      "body":"go go Gadgeto send Text"
    },
    "messageType": "TEXT"
    }
    */

    private val mqttConnector by lazy { MqttConnector(mqttBroker, mainTopic) }
    private val soundPlayer by lazy { MediaPlayer.create(context, R.raw.hangouts_message) }

    var isLoading by mutableStateOf(false)

    var messageToSend by mutableStateOf("")
    var messageImageToSend by mutableStateOf<Bitmap?>(null) // bleibt nur bei mir
    var currentMessageOptionSend by mutableStateOf(ChatPayloadContents.NONE)

    val allMessages = mutableStateListOf<ChatMessage>()
    var notificationMessage by mutableStateOf("")
    var MessagesPublished by mutableStateOf(0)

    /* CHAT PARTNERS */
    var currentChatPartner by mutableStateOf<ChatUser?>(null)

    /* USERS */
    val allUsers = mutableStateListOf<ChatUser>()
    val selectedUsers = mutableStateListOf<ChatUser>()

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
    var profileImageURL by mutableStateOf("")
    var profileImagePath by mutableStateOf("")
    var profileImage by mutableStateOf<Bitmap?>(null)

    // Preferences auslesen
    fun loadOrCreateProfile(): ChatUser {
        val prefs = context.getSharedPreferences("thatsAppPreferences", MODE_PRIVATE)
        profileId = prefs.getString("profileId", UUID.randomUUID().toString()).toString()
        profileName = prefs.getString("profileName", "yourName").toString()
        profileBio = prefs.getString("profileBio", "Your bio").toString()
        profileImageURL = prefs.getString("profileImageURL", "https://pbs.twimg.com/profile_images/1140254399962521601/9hL6tDQj_400x400.jpg").toString()
        profileImagePath = prefs.getString("profileImagePath", "").toString()
        profileImage = if (profileImagePath.isNotEmpty()) {
                            loadImageFromStorage(profileImagePath)
                        } else {
                            downloadProfileImageFromURL()
                        }
        val profileVersion = prefs.getString("profileVersion", "1.0.0").toString()
        val profileLastOnline = System.currentTimeMillis() / 1000
        // User zusammenbauen
        val profile = ChatUser(
            userID = profileId,
            nickname = profileName,
            bio = profileBio,
            userImage = profileImageURL,
            userProfileImage = null,
            lastOnline = profileLastOnline,
            version = profileVersion
        )
        updatePrefs();
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
        editor.putString("profileImageURL", profileImageURL)
        editor.putString("profileImagePath", profileImagePath)
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


    /* PAYLOADS */


    /* MESSAGES */
    // received message
    fun processMessage(message: ChatMessage) {
        if (message.messageType == ChatPayloadContents.TEXT.name) {
            addMessage(message)
        }

        // When picture: load bitmap from this url > load into ChatMessage.bitmap

        // Loc: create geoLocation
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
                processMessage(it) // TODO get payload to receive
            }
        )
    }

    fun buildPayload(){
    }
    // TODO change payload to what I actually send
    fun publish() {
        // build Payload
        var payloadToSend = JSONObject()
        var messageTypeToSend = ""

        when (currentMessageOptionSend){
            ChatPayloadContents.EMOJI, ChatPayloadContents.TEXT, ChatPayloadContents.NONE -> {
                payloadToSend = JSONObject(ChatText(messageToSend).asJSON())
                messageTypeToSend = ChatPayloadContents.TEXT.name
                messageToSend = "" // nur bei text msg
            }
            ChatPayloadContents.LOCATION -> {  }
            ChatPayloadContents.IMAGE -> {  } // show bitmap
            ChatPayloadContents.INFO -> {  }
            ChatPayloadContents.LIVE -> {  }
            else -> { notificationMessage = "Publish failed."  } // TODO (when enough time) set NONE to Text (in GUI and set it here on TEXT)
        }

        val chatMessage = currentChatPartner?.userID?.let {
            ChatMessage(
                timestamp = getUTCTimestamp(),
                messageID = UUID.randomUUID().toString(),
                senderID = profileId,
                receiverID = it,
                payload = payloadToSend,
                messageType = messageTypeToSend,
                bitmap = messageImageToSend,
                read = false,
                delivered = false
            )
        }
        if (chatMessage != null) {
            // dann addMessage hier.
            mqttConnector.publish(
                message = chatMessage,
                subtopic = "$notificationTopic${currentChatPartner?.userID}",
                onPublished = { addMessage(chatMessage) }, // setMessageSent boolean true (dann im GUI anzeigen)
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
            downloadUserImageFromURL(it)
        }
    }

    /* UP AND DOWNLOAD FILES */

    /* PROFILE */

    // TODO
    private fun loadImageFromStorage(profileImagePath: String): Bitmap {
        return DEFAULT_IMAGE.asAndroidBitmap()
    }

    fun downloadProfileImageFromURL(): Bitmap? {
        modelScope.launch {
            if (profileImageURL.isNotEmpty()) {
                downloadBitmapFromURL(
                    url = profileImageURL,
                    onSuccess = { profileImage = it },
                    onDeleted = { notificationMessage = "File is deleted" },
                    onError = { notificationMessage = "Connection failed" })
                downloadInProgress = false
            }
        }
        return profileImage
    }

    fun downloadUserImageFromURL(user: ChatUser) {
        modelScope.launch {
            if (user.userImage.isNotBlank()) {
                downloadBitmapFromURL(
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


}