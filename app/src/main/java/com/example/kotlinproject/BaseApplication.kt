package com.example.kotlinproject

import android.app.Application
import com.cloudinary.android.MediaManager
import com.example.kotlinproject.Utils.CloudinaryConfig

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Cloudinary
        val config = mapOf(
            "cloud_name" to CloudinaryConfig.CLOUD_NAME,
            "api_key" to CloudinaryConfig.API_KEY,
            "api_secret" to CloudinaryConfig.API_SECRET
        )
        MediaManager.init(this, config)
    }
}