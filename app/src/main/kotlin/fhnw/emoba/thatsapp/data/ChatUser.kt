package fhnw.emoba.thatsapp.data

import org.json.JSONObject

data class ChatUser(
    val userID: String,
    val nickname: String,
    val bio: String,
    val userImage: String,
    val lastOnline: String,
    val version: String
    ) {
    constructor(json : JSONObject): this(
        json.getString("userID"),
        json.getString("nickname"),
        json.getString("bio"),
        json.getString("userImage"),
        json.getString("lastOnline"),
        json.getString("version")
    )

    fun asJSON(): String {
        return """
            {
            "userID":  "$userID",
            "nickname":  "$nickname",
            "bio":  "$bio",
            "userImage":  "$userImage",
            "lastOnline":  "$lastOnline",
            "lastOnline":  "$lastOnline",
             "version": "$version"
            }
            """
    }
}