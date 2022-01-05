package fhnw.emoba.thatsapp.data

import android.graphics.Bitmap
import fhnw.emoba.thatsapp.model.*
import org.json.JSONObject


data class ChatMessage (
    val messageType: String,
    val timestamp: Long,
    val messageID: String,
    val senderID: String,
    val receiverID: String,
    val payload: JSONObject,
    var bitmap: Bitmap?,
    var read: Boolean,
    var delivered: Boolean
) {

    constructor(json : JSONObject): this(
        json.getString("messageType"),
        json.getLong("timestamp"),
        json.getString("messageID"),
        json.getString("senderID"),
        json.getString("receiverID"),
        json.getJSONObject("payload"),
        Bitmap.createBitmap(30, 30, Bitmap.Config.ALPHA_8),
        false,
        false
    )

    fun asJSON(): String {
        return """
            {
            "messageID":  "$messageID", 
            "timestamp":  $timestamp, 
            "senderID":  "$senderID", 
            "receiverID":  "$receiverID", 
            "payload":  $payload, 
            "messageType":  "$messageType"
            }
            """.trimIndent()
    }

    /*
    // TODO what is this for?
    fun createPayloadObject() {
        try {
            when (messageType){
                ChatPayloadContents.TEXT.name -> { ChatText(payload) }
                ChatPayloadContents.LOCATION.name -> { ChatLocation(payload) }
                ChatPayloadContents.IMAGE.name -> { ChatImage(payload) }
                ChatPayloadContents.INFO.name -> { ChatInfo(payload) }
                ChatPayloadContents.LIVE.name -> { ChatLive(payload) }
            }
        } catch (e: Exception){
            System.out.println(e.stackTrace.toString())
            // TODO catch exception
        }
    }

     */

    /*
    fun getPayloadTextData(): ChatText {
        try {
            if(messageType == ChatPayloadContents.TEXT.name){
                ChatText(payload)
            }
        } catch (e: Exception){

        }
    }
    fun getPayloadLocationData(): ChatLocation {
        return if(messageType == ChatPayloadContents.LOCATION.name){
            ChatLocation(payload)
        } else {
            ChatLocation("System: Error")
        }
    }
    fun getGeoLocationData(): GeoPosition{
        val location = getPayloadLocationData()
        return GeoPosition(location.longitude.toDouble(), location.latitude.toDouble(), 0.0)
    }
    fun getPayloadImageData(): ChatImage{
        return if(messageType == ChatPayloadContents.IMAGE.name){
            ChatImage(payload)
        } else {
            ChatImage("System: Error")
        }
    }

     */


}