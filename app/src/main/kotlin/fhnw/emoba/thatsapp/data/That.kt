package fhnw.emoba.thatsapp.data

import org.json.JSONObject


data class That(val sender: String, val message: String) {

    constructor(json : JSONObject): this(json.getString("sender"),
                                         json.getString("message"))

    fun asJSON(): String {
        return """
            {"sender":  "$sender", 
             "message": "$message" 
            }
            """
    }
}