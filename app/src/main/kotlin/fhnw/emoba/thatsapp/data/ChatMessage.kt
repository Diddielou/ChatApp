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
    var position: GeoPosition?,
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
        null,
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


}