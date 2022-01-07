package fhnw.emoba.thatsapp.data

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fhnw.emoba.thatsapp.model.DEFAULT_IMAGE
import org.json.JSONObject

data class ChatUser(
    val userID: String,
    val nickname: String,
    val bio: String,
    val userImage: String,
    var userProfileImage: Bitmap?,
    val lastOnline: Long,
    val version: String,
    var isLive: Boolean
    ) {

    constructor(json : JSONObject): this(
        json.getString("userID"),
        json.getString("nickname"),
        json.getString("bio"),
        json.getString("userImage"),
        null,
        json.getLong("lastOnline"),
        json.getString("version"),
        false
    )

    fun asJSON(): String {
        return """
            {
            "userID":  "$userID",
            "nickname":  "$nickname",
            "bio":  "$bio",
            "userImage":  "$userImage",
            "lastOnline":  $lastOnline,
            "version": "$version"
            }
            """
    }
}
