package com.example.lab7

import android.net.Uri
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class FlickrFetcher {
    //for Logcat
    private static
    val TAG: String = "FlickrFetcher"

    private static
    val API_KEY: String = "2d1920a1392f42e06c9e4cc451fcdb44"

    private static
    val FETCH_RECENTS_METHOD: String = "flickr.photos.getRecent"
    private static
    val SEARCH_METHOD: String = "flickr.photos.search"

    //method that builds a URL used to do some fetching
    private static
    val ENDPOINT: Uri = Uri.parse("https://api.flickr.com/services/rest")
        .buildUpon()
        .appendQueryParameter("api_key", API_KEY)
        .appendQueryParameter("format", "json")
        .appendQueryParameter("nojsoncallback", "1")
        .appendQueryParameter("extras", "url_s")
        .build()

    private fun downloadGalleryItems(url: String): List<Lab7GalleryItem> {
        val items: MutableList<Lab7GalleryItem> = ArrayList<Lab7GalleryItem>()
        try {
            val jsonString = getUrlString(url)
            Log.i(TAG, "Received JSON: $jsonString")
            val jsonBody = JSONObject(jsonString)
            parseItems(items, jsonBody)
        } catch (ioe: IOException) {
            Log.e(TAG, "Failed to fetch items", ioe)
        } catch (je: JSONException) {
            Log.e(TAG, "Failed to parse JSON", je)
        }
        return items
    }

    fun fetchRecentPhotos(): List<Lab7GalleryItem> {
        val url = buildUrl(FETCH_RECENTS_METHOD, null)
        return downloadGalleryItems(url)
    }

    fun searchPhotos(query: String?): List<Lab7GalleryItem> {
        val url = buildUrl(SEARCH_METHOD, query)
        return downloadGalleryItems(url)
    }

    private fun buildUrl(method: String, query: String?): String {
        val uriBuilder = ENDPOINT.buildUpon().appendQueryParameter("method", method)
        if (method == SEARCH_METHOD) uriBuilder.appendQueryParameter("text", query)

        return uriBuilder.build().toString()
    }

    @Throws(IOException::class, JSONException::class)
    private fun parseItems(items: MutableList<Lab7GalleryItem>, jsonBody: JSONObject) {
        val photosJsonObject = jsonBody.getJSONObject("photos")
        val photoJsonArray = photosJsonObject.getJSONArray("photo")
        for (i in 0 until photoJsonArray.length()) {
            val photoJsonObject = photoJsonArray.getJSONObject(i)
            val item: Lab7GalleryItem = Lab7GalleryItem()
            item.setmId(photoJsonObject.getString("id"))
            item.setmCaption(photoJsonObject.getString("title"))
            if (!photoJsonObject.has("url_s")) continue
            item.setmUrl(photoJsonObject.getString("url_s"))
            items.add(item)
        }
    }


    @Throws(IOException::class)
    fun getUrlBytes(urlSpec: String): ByteArray {
        val url = URL(urlSpec)
        val connection = url.openConnection() as HttpURLConnection

        try {
            val out = ByteArrayOutputStream()
            val `in` = connection.inputStream

            if (connection.responseCode != HttpURLConnection.HTTP_OK) throw IOException(connection.responseMessage + ": with" + urlSpec)

            var bytesRead = 0
            val buffer = ByteArray(1024)
            while ((`in`.read(buffer).also { bytesRead = it }) > 0) out.write(buffer, 0, bytesRead)

            out.close()
            return out.toByteArray()
        } finally {
            connection.disconnect()
        }
    }

    @Throws(IOException::class)
    fun getUrlString(urlSpec: String): String {
        return String(getUrlBytes(urlSpec))
    }
}
