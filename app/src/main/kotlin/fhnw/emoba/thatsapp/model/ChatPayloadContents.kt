package fhnw.emoba.thatsapp.model

import org.json.JSONObject

enum class ChatPayloadContents() {
    TEXT,
    LOCATION,
    IMAGE,
    INFO,
    LIVE
}

class ChatText(payload: JSONObject){
    val text = payload.getString("body")
}

class ChatImage(payload: JSONObject){
    val url = payload.getString("url")

}

class ChatLocation(payload: JSONObject){
    val latitude = payload.getString("lat")
    val longitude = payload.getString("lon")
}

class ChatInfo(payload: JSONObject){
    val messageID = payload.getString("messageID")
    val timestamp = payload.getString("timestamp")
    val read = payload.getString("read")
    val delivered = payload.getString("delivered")
}