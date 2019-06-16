package com.example.dev.cameraapp.ui.viewModels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel;
import android.view.View

/**
 * @author v.sibichenko on 06/16/2019.
 */

class StartFragmentViewModel : ViewModel() {

    var openCameraFragment: MutableLiveData<Boolean> = MutableLiveData()
        private set

    var videoDuration: Int = 0
        private  set

    var videoFrameRate: Int = 0
        private  set

    fun startCamera(v: View) {
        openCameraFragment.postValue(true)
    }

    fun afterVideoDurationChanged(s: CharSequence) {
        if (s.isNotEmpty()) {
            videoDuration = s.toString().toInt()
        }
    }

    fun afterVideoFrameRateChanged(s: CharSequence) {
        if (s.isNotEmpty()) {
            videoFrameRate = s.toString().toInt()
        }
    }
}
