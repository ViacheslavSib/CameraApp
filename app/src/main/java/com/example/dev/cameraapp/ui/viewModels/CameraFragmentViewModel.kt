package com.example.dev.cameraapp.ui.viewModels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.view.View

/**
 * @author v.sibichenko on 06/16/2019.
 */

class CameraFragmentViewModel : ViewModel() {
    var recordVideo: MutableLiveData<Boolean> = MutableLiveData()
        private set

    fun recordVideo(v: View) {
        recordVideo.postValue(true)
    }
}
