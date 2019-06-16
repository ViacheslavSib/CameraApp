package com.example.dev.cameraapp.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.Navigation

import com.example.dev.cameraapp.R
import com.example.dev.cameraapp.databinding.StartFragmentBinding
import com.example.dev.cameraapp.ui.CameraFragment.Companion.VIDEO_DURATION_KEY
import com.example.dev.cameraapp.ui.CameraFragment.Companion.VIDEO_FRAME_RATE_KEY
import com.example.dev.cameraapp.ui.viewModels.StartFragmentViewModel

/**
 * @author v.sibichenko on 06/16/2019.
 */

class StartFragment : Fragment() {

    companion object {
        fun newInstance() = StartFragment()
    }

    private lateinit var viewModel: StartFragmentViewModel
    private var binding: StartFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.start_fragment, container, false)
        return binding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(StartFragmentViewModel::class.java)
        binding?.viewmodel = viewModel

        viewModel.openCameraFragment.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                if (it) {
                    viewModel.openCameraFragment.postValue(false)
                    view?.let { v ->
                        val durationInSeconds = viewModel.videoDuration
                        val frameRate = viewModel.videoFrameRate
                        val bundle = Bundle()
                        bundle.putInt(VIDEO_DURATION_KEY, durationInSeconds * 1000) // in ms
                        bundle.putInt(VIDEO_FRAME_RATE_KEY, frameRate)

                        Navigation.findNavController(v)
                            .navigate(R.id.cameraFragment, bundle, NavOptions.Builder().build())
                    }
                }
            }
        })
    }

}
