package com.example.cimed

import android.app.Application
import com.example.cimed.data.repository.MedicalRepository

class CimedApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MedicalRepository.init(this)
    }
}
