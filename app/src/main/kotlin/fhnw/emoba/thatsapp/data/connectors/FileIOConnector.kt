package fhnw.emoba.thatsapp.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

fun uploadBitmapToFileIO(bitmap:    Bitmap,
                         onSuccess: (link: String) -> Unit,
                         onError:   (responseCode: Int, json: String) -> Unit = {_, _ -> }) {
    val fileName   = "photo.jpg"
    val crlf       = "\r\n"
    val twoHyphens = "--"
    val boundary   = "*****Boundary*****"

    with(URL("https://file.io?expires=1d").openConnection() as HttpsURLConnection){
        //Request
        requestMethod = "POST"
        setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)

        val requestStringStart = crlf + crlf +
                twoHyphens + boundary + crlf +
                "Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"" + crlf + crlf
        val requestStringEnd = crlf + twoHyphens + boundary + twoHyphens + crlf + crlf

        val request = DataOutputStream(getOutputStream())

        request.writeBytes(requestStringStart)
        request.write(bitmap.asByteArray())
        request.writeBytes(requestStringEnd)

        request.flush()
        request.close()

        //Response
        if(responseCode == 200){
            val jsonString = message()
            val jsonObject = JSONObject(jsonString)
            if(jsonObject.getBoolean("success")){
                onSuccess.invoke(jsonObject.getString("link"))
            }
            else {
                onError.invoke(responseCode, jsonString)
            }
        }
        else {
            return onError.invoke(responseCode, "")
        }
    }
}

fun downloadBitmapFromURL(url:       String,
                          onSuccess: (bitmap: Bitmap) -> Unit,
                          onDeleted: () -> Unit = {},
                          onError:   (exception: Exception) -> Unit) {

    with(URL(url).openConnection() as HttpsURLConnection) {
        setRequestProperty("User-Agent", "emoba_Thats_App") //vermeidet den Redirect wie im Browser
        try {
            connect()
        } catch (e: Exception) {
            onError.invoke(e)
        }
        try {
            onSuccess.invoke(bitmap())
        } catch (e: Exception) {

            onDeleted.invoke()
        }
    }
}


// ein paar hilfreiche Extension Functions

private fun HttpsURLConnection.message(): String {
    val reader = BufferedReader(InputStreamReader(this.inputStream, StandardCharsets.UTF_8))
    val message = reader.readText()
    reader.close()

    return message
}

private fun HttpsURLConnection.bitmap(): Bitmap {
    val bitmapAsBytes = inputStream.readBytes()
    inputStream.close()

    return BitmapFactory.decodeByteArray(bitmapAsBytes, 0, bitmapAsBytes.size)
}

private fun Bitmap.asByteArray(): ByteArray {
    val baos = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val byteArray = baos.toByteArray()
    baos.close()
    return byteArray
}