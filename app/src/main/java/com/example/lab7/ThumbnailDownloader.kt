package com.example.lab7

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap


class ThumbnailDownloader<T>(private val mResponseHandler: Handler) : HandlerThread(TAG) {
    private var mHasQuit = false
    private var mRequestHandler: Handler? = null
    private val mRequestMap = ConcurrentHashMap<T, String>()
    private var mThumbnailDownloadListener: ThumbnailDownloadListener<T>? = null

    interface ThumbnailDownloadListener<T> {
        //will be called when an image is download and
        //needs to be added to the UI. Separates responsibility. Code re-use
        fun onThumbnailDownloaded(Target: T, thumbnail: Bitmap?)
    }

    fun setThumbnailDownloadListener(listener: ThumbnailDownloadListener<*>?) {
        mThumbnailDownloadListener = listener
    }

    override fun quit(): Boolean {
        mHasQuit = true
        return super.quit()
    }

    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        mRequestHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL: " + mRequestMap[target])
                    handleRequest(target)
                }
            }
        }
    }

    fun clearQueue() {
        mRequestHandler!!.removeMessages(MESSAGE_DOWNLOAD)
        mRequestMap.clear()
    }

    private fun handleRequest(target: T) {
        try {
            val url = mRequestMap[target] ?: return
            val bitmapBytes: ByteArray = FlickrFetcher().getUrlBytes(url)
            val bitmap = BitmapFactory.decodeByteArray(
                bitmapBytes,
                0,
                bitmapBytes.size
            )
            Log.i(TAG, "Bitmap created")
            mResponseHandler.post(Runnable {
                if (mRequestMap[target] !== url || mHasQuit) return@Runnable
                mRequestMap.remove(target)
                mThumbnailDownloadListener!!.onThumbnailDownloaded(target, bitmap)
            })
        } catch (ioe: IOException) {
            Log.e(TAG, "Error downloading image", ioe)
        }
    }

    fun queueThumbnail(target: T, url: String?) {
        Log.i(TAG, "Got a URL: $url")
        if (url == null) mRequestMap.remove(target)
        else mRequestMap[target] = url
        mRequestHandler!!.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }

    companion object {
        private const val TAG = "ThumbnailDownloader"
        private const val MESSAGE_DOWNLOAD = 0
    }
}