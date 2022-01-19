package fhnw.emoba.thatsapp.model

import android.graphics.Bitmap
import org.json.JSONObject

enum class ChatPayloadContents() {
    NONE,
    EMOJI,
    TEXT,
    LOCATION,
    IMAGE,
    PHOTO,
    INFO,
    LIVE
}

val DEFAULT_IMAGE: Bitmap = Bitmap.createBitmap(
    500,
    500,
    Bitmap.Config.ALPHA_8)

data class ChatText(
    val body: String // when sending a message
) {
    constructor(payload: JSONObject) : this(
        payload.getString("body") // when receiving
    )
    fun asJSON(): String {
        return """{
                 "body": "$body"
        }""".trimIndent()
    }
}

data class ChatImage(
    val url: String
){
    constructor(payload: JSONObject) : this(
        payload.getString("url")
    )
    fun asJSON(): String {
        return """
            {
            "url":  "$url"
            }
            """.trimIndent()
    }
}

data class ChatLocation(
    val latitude: String, val longitude : String){

    constructor(payload: JSONObject) : this(
        payload.getString("lat"),
        payload.getString("lon")
    )

    fun asJSON(): String {
        return """
            {
            "lat":  "$latitude", 
            "lon":  "$longitude"
            }
            """.trimIndent()
    }
}

/* not fully implemented */
data class ChatLive(val typing : String){

    constructor(payload: JSONObject) : this(
        payload.getString("typing")
    )

    fun asJSON(): String {
        return """
            {
            "typing":  "$typing"
            }
            """.trimIndent()
    }
}

/* not implemented */
data class ChatInfo(
    val messageID : String,
    val timestamp : String,
    val read : String,
    val delivered : String){

    constructor(payload: JSONObject) : this(
        payload.getString("messageID"),
        payload.getString("timestamp"),
        payload.getString("read"),
        payload.getString("delivered")
    )

    fun asJSON(): String {
        return """
            {
            "messageID":  "$messageID", 
            "timestamp":  "$timestamp", 
            "read":  "$read", 
            "delivered":  "$delivered"
            }
            """.trimIndent()
    }
}