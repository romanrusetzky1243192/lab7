package com.example.lab7

import androidx.fragment.app.Fragment


class PhotoGalleryActivity : SingleFragmentActivity() {
    protected fun createFragment(): Fragment {
        return PhotoGalleryFragment.newInstance()
    }
}