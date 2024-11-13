package com.example.lab7

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class PhotoGalleryFragment : Fragment() {
    private var mPhotoRecyclerView: RecyclerView? = null
    private var mItems: List<Lab7GalleryItem.GalleryItem> = ArrayList<Lab7GalleryItem.GalleryItem>()
    private var mThumbnailDownloader: ThumbnailDownloader<PhotoHolder>? = null

    private inner class FetchItemsTask :
        AsyncTask<Void?, Void?, List<Lab7GalleryItem.GalleryItem>>() {
        protected override fun doInBackground(vararg p0: Void?): List<Lab7GalleryItem> {
            val query = "robot" // for testing purposes
            return if (query == null) FlickrFetcher().fetchRecentPhotos()
            else FlickrFetcher().searchPhotos(query)
        }

        override fun onPostExecute(items: List<Lab7GalleryItem.GalleryItem>) {
            mItems = items
            setupAdapter()
        }

        override fun doInBackground(vararg p0: Void?): List<Lab7GalleryItem.GalleryItem> {
            TODO("Not yet implemented")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //frees the fragment instance from the activity lifecycle. Setting to
        //true retains the fragment across activity re-creation
        retainInstance = true
        //starts AsyncTask which starts the background thread and calls
        //doInBackground()
        FetchItemsTask().execute()
        //turn on the menu
        setHasOptionsMenu(true)
        val responseHandler = Handler()
        mThumbnailDownloader = ThumbnailDownloader(responseHandler)
        mThumbnailDownloader.setThumbnailDownloadListener(
            object : ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder?>() {
                fun onThumbnailDownloaded(photoHolder: PhotoHolder, bitmap: Bitmap?) {
                    val drawable: Drawable = BitmapDrawable(resources, bitmap)
                    photoHolder.bindDrawable(drawable)
                }
            }
        )
        mThumbnailDownloader.start()
        mThumbnailDownloader.getLooper()
        Log.i(TAG, "Background thread started")
    }

    override fun onDestroy() {
        super.onDestroy()
        mThumbnailDownloader.quit()
        Log.i(TAG, "Background thread destroyed")
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.fragment_photo_gallery_menu, menu)
        // use this so we can get a ref to the SearchView
        val searchItem = menu.findItem(R.id.menu_item_search)

        val searchView = searchItem.actionView as SearchView?

        searchView!!.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(s: String): Boolean {
                    // executes anytime the user submits a query
                    // Launch FetchItemTask to query for new results
                    Log.d(TAG, "QueryTextSubmit: $s")
                    updateItem()
                    return true
                }


                override fun onQueryTextChange(s: String): Boolean {
                    Log.d(TAG, "QueryTextChange: $s")
                    return false
                }
            }
        )
    }

    // wrapper for calling FetchItemTask.
    private fun updateItem() {
        FetchItemsTask().execute()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mThumbnailDownloader.clearQueue()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_photo_gallery, container, false)
        mPhotoRecyclerView = v.findViewById<View>(R.id.photo_recycler_view) as RecyclerView
        //Recyclerview will crash without a layout manager.
        //3 = the number of columns in the grid. (static)
        mPhotoRecyclerView!!.layoutManager = GridLayoutManager(context, 3)
        setupAdapter()

        return v
    }

    private fun setupAdapter() {
        if (isAdded) {
            mPhotoRecyclerView!!.adapter = PhotoAdapter(mItems)
        }
    }

    private inner class PhotoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mItemImageView = itemView.findViewById<View>(R.id.item_image_view) as ImageView

        fun bindDrawable(drawable: Drawable?) {
            mItemImageView.setImageDrawable(drawable)
        }
    }

    private inner class PhotoAdapter(galleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {
        private val mGalleryItems: List<GalleryItem> = galleryItems

        override fun onCreateViewHolder(group: ViewGroup, viewType: Int): PhotoHolder {
            val inflater = LayoutInflater.from(activity)
            val view: View = inflater.inflate(R.layout.list_item_gallery, group, false)
            return PhotoHolder(view)
        }

        override fun onBindViewHolder(photoHolder: PhotoHolder, position: Int) {
            val galleryItem: GalleryItem = mGalleryItems[position]
            val placeholder = resources.getDrawable(R.drawable.ic_launcher_foreground)
            photoHolder.bindDrawable(placeholder)
            mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getmUrl())
        }

        override fun getItemCount(): Int {
            return mGalleryItems.size
        }
    }

    companion object {
        private const val TAG = "PhotoGalleryFragment"
        fun newInstance(): PhotoGalleryFragment {
            return PhotoGalleryFragment()
        }
    }
}