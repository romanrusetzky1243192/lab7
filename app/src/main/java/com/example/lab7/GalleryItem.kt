package com.example.lab7

class Lab7GalleryItem {
    fun setmCaption(string: String) {

    }

    fun setmId(string: String) {

    }

    fun setmUrl(string: String) {
        TODO("Not yet implemented")
    }
    package com.bignerdranch.android.lab7;

    class GalleryItem {
        private var mCaption: String? = null
        private var mId: String? = null
        private var mUrl: String? = null

        fun getmId(): String? {
            return mId
        }

        fun setmId(mId: String?) {
            this.mId = mId
        }

        fun getmUrl(): String? {
            return mUrl
        }

        fun setmUrl(mUrl: String?) {
            this.mUrl = mUrl
        }

        fun getmCaption(): String? {
            return mCaption
        }

        fun setmCaption(mCaption: String?) {
            this.mCaption = mCaption
        }

        override fun toString(): String {
            return mCaption!!
        }
    }
}