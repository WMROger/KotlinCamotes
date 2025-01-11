package com.example.kotlinactivities.adminPage.upload

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

fun uploadImageAndSaveToRealtimeDB(
    imageUri: Uri,
    context: Context,
    roomDetails: Map<String, Any?>
) {
    // Get the file from Uri
    val file = getFileFromUri(context, imageUri)

    if (file == null || !file.exists()) {
        Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
        return
    }

    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

    // Upload to server
    val api = RetrofitClient.instance
    api.uploadFile(body).enqueue(object : Callback<UploadResponse> {
        override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
            val uploadResponse = response.body()
            if (response.isSuccessful && uploadResponse?.status == "success") {
                val imageUrl = uploadResponse.url ?: ""
                saveToRealtimeDatabase(imageUrl, roomDetails, context)
            } else {
                Toast.makeText(context, "Upload failed: ${uploadResponse?.message}", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
            Toast.makeText(context, "Upload error: ${t.message}", Toast.LENGTH_SHORT).show()
        }
    })
}

private fun saveToRealtimeDatabase(imageUrl: String, roomDetails: Map<String, Any?>, context: Context) {
    val database = FirebaseDatabase.getInstance().reference
    val roomId = database.child("rooms").push().key // Generate a unique room ID

    if (roomId != null) {
        val roomData = roomDetails.toMutableMap()
        roomData["image_url"] = imageUrl // Add the image URL to the room details

        database.child("rooms").child(roomId).setValue(roomData)
            .addOnSuccessListener {
                Toast.makeText(context, "Room details saved to database!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to save room: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

// Utility function to copy Uri content to a local file
private fun getFileFromUri(context: Context, uri: Uri): File? {
    val contentResolver: ContentResolver = context.contentResolver
    val fileName = getFileNameFromUri(contentResolver, uri) ?: return null

    val file = File(context.cacheDir, fileName)
    try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    } catch (e: Exception) {
        Log.e("FileError", "Error copying file from URI: ${e.message}", e)
        return null
    }
    return file
}


// Utility function to get file name from Uri
private fun getFileNameFromUri(contentResolver: ContentResolver, uri: Uri): String? {
    var fileName: String? = null
    val cursor = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                fileName = it.getString(nameIndex)
            }
        }
    }
    return fileName
}
