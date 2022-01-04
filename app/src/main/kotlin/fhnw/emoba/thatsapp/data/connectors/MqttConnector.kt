package fhnw.emoba.thatsapp.data.connectors

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import fhnw.emoba.thatsapp.data.ChatMessage
import fhnw.emoba.thatsapp.data.ChatUser
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * siehe die Doku:
 * https://hivemq.github.io/hivemq-mqtt-client/
 * https://github.com/hivemq/hivemq-mqtt-client
 *
 * Ein generischer Mqtt-Client (gut um Messages zu kontrollieren)
 * http://www.hivemq.com/demos/websocket-client/
 *
 * TODO: check sessionExpiryInterval(<seconds>) / Check uuid (in model?) / Check persistent sessions
 *
 */
class MqttConnector (val mqttBroker: String, val maintopic: String,
                     val qos: MqttQos = MqttQos.AT_LEAST_ONCE) {

    private val client = Mqtt5Client.builder()
        .serverHost(mqttBroker)
        .identifier(UUID.randomUUID().toString())
        .buildAsync()


    fun connectAndSubscribe(
        subtopic: String = "",
        onNewMessageUsers: (ChatUser) -> Unit,
        onError: (exception: Exception, payload: String) -> Unit = { e, _ -> e.printStackTrace() },
        onConnectionFailed: () -> Unit = {},
        profileUserTopic: String,
        profileUser: ChatUser,
        thisUserNotificationTopic: String,
        onNewMessages: (ChatMessage) -> Unit,
    ) {
        client.connectWith()
            .cleanStart(false)
            .keepAlive(600)
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    onConnectionFailed.invoke()
                } else { //erst wenn die Connection aufgebaut ist, kann subscribed werden
                    //subscribe to users/#
                    subscribe(subtopic, onNewMessageUsers, onError)
                    //publish yourself as user
                    publishUser(profileUser, profileUserTopic)
                    //subscribe to your notifications
                    subscribeToMyself(thisUserNotificationTopic, onNewMessages, onError)
                }
            }
    }


    private fun publishUser(profileUser: ChatUser,
                            profileUserTopic: String,
                            onError: () -> Unit = {  },
                            onPublished: () -> Unit = { }){
        client.publishWith()
            .topic(maintopic+profileUserTopic)
            .payload(profileUser.asJSON().asPayload())
            .qos(qos)
            .retain(true)
            .messageExpiryInterval(600)
            .send()
            .whenComplete{_, throwable ->
                if(throwable != null){
                    onError.invoke()
                }
                else {
                    onPublished.invoke()
                }
            }
    }

    // auf alle User subscriben
    fun subscribe(subtopic:          String = "",
                  onNewMessageUsers: (ChatUser) -> Unit,
                  onError:           (exception: Exception,
                                     payload: String) -> Unit = { e, _ -> e.printStackTrace() } ){

        client.subscribeWith()
            .topicFilter("$maintopic$subtopic#") // main + /users/ + #
            .qos(qos)
            .noLocal(true)
            .callback {
                try {
                    // aus was zurückkommt, wird in ein User-Objekt gemacht
                    onNewMessageUsers.invoke(ChatUser(JSONObject(it.payloadAsString())))
                }
                catch (e: Exception){
                    onError.invoke(e, it.payloadAsString())
                }
            }
            .send()
    }

    fun subscribeToMyself(thisUserNotificationTopic: String,
                          onNewMessages: (ChatMessage) -> Unit,
                          onError: (exception: Exception, payload: String) -> Unit) {
        client.subscribeWith()
            .topicFilter(thisUserNotificationTopic)
            .qos(qos)
            .noLocal(true)
            .callback {
                try {
                    onNewMessages.invoke(ChatMessage(JSONObject(it.payloadAsString())))
                }
                catch (e: Exception){
                    onError.invoke(e, it.payloadAsString())
                }
            }
            .send()

    }



    fun publish(message: ChatMessage,
                subtopic: String = "",
                onPublished: () -> Unit = {}, // Was soll geschehen, nachdem gepublished wurde?
                onError: () -> Unit = {}) {

        client.publishWith()
            .topic(maintopic + subtopic)
            .payload(message.asJSON().asPayload())
            .qos(qos)
            .retain(false)
            .messageExpiryInterval(60)
            .send()
            .whenComplete{_, throwable ->
                if(throwable != null){
                    onError.invoke()
                }
                else {
                    onPublished.invoke()
                }
            }
    }

    fun disconnect() {
        client.disconnectWith()
            .sessionExpiryInterval(0)
            .send()
    }

}

// praktische Extension Functions
private fun String.asPayload() : ByteArray = toByteArray(StandardCharsets.UTF_8)
private fun Mqtt5Publish.payloadAsString() : String = String(payloadAsBytes, StandardCharsets.UTF_8)