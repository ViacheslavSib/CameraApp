package com.example.dev.cameraapp.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.databinding.DataBindingUtil
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.example.dev.cameraapp.*
import com.example.dev.cameraapp.databinding.CameraFragmentBinding
import android.support.v4.content.FileProvider
import android.os.Build
import com.example.dev.cameraapp.customCamera.*
import com.example.dev.cameraapp.customCamera.dialogs.ConfirmationDialog
import com.example.dev.cameraapp.customCamera.dialogs.ErrorDialog
import com.example.dev.cameraapp.ui.viewModels.CameraFragmentViewModel
import java.io.File

/**
 * @author v.sibichenko on 06/16/2019.
 */

class CameraFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        const val VIDEO_FRAME_RATE_KEY = "video_frame_rate"
        const val VIDEO_DURATION_KEY = "video_duration"
        fun newInstance(): CameraFragment = CameraFragment()
    }

    private val FRAGMENT_DIALOG = "dialog"
    private val TAG = "CameraFragment"

    private lateinit var viewModel: CameraFragmentViewModel

    /**
     * [TextureView.SurfaceTextureListener] handles several lifecycle events on a
     * [TextureView].
     */
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            cameraHandler?.configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture) = true

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit

    }

    /**
     * An [AutoFitTextureView] for camera preview.
     */
    private lateinit var textureView: AutoFitTextureView

    private var cameraHandler: CameraHandler? = null
    private var binding: CameraFragmentBinding? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.camera_fragment, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textureView = view.findViewById(R.id.texture)

        val videoFrameRate = arguments?.getInt(VIDEO_FRAME_RATE_KEY) ?: 30
        val videoDuration = arguments?.getInt(VIDEO_DURATION_KEY) ?: 3000

        activity?.let {
            cameraHandler = CameraHandler.newInstance(it, textureView, videoFrameRate, videoDuration, object : CameraHandler.CameraHandlerCallBack {
                override fun onError(error:String) {
                    showToast(error)
                }

                override fun onVideoCaptured(path: String?) {
                    binding?.recordBtn?.setText(R.string.record)
                    context?.let { c ->
                        val videoFile = File(path)
                        val videoURI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            FileProvider.getUriForFile(c, "com.example.dev.fileprovider", videoFile)
                        else
                            Uri.fromFile(videoFile)
                        val shareIntent: Intent = Intent().apply {
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, videoURI)
                            type = "video/mp4"
                        }
                        startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
                        activity?.onBackPressed()
                    }
                }
            })
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CameraFragmentViewModel::class.java)
        binding?.viewmodel = viewModel

        viewModel.recordVideo.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                if (it) {
                    viewModel.recordVideo.postValue(false)
                    if (cameraHandler?.isRecordingVideo == true) {
                        binding?.recordBtn?.setText(R.string.record)
                        cameraHandler?.stopRecordingVideo()
                    }
                    else {
                        binding?.recordBtn?.setText(R.string.stop)
                        cameraHandler?.startRecordingVideo()
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        cameraHandler?.onResume()

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onPause() {
        cameraHandler?.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        cameraHandler = null
        super.onDestroyView()
    }

    /**
     * Gets whether you should show UI with rationale for requesting permissions.
     *
     * @param permissions The permissions your app wants to request.
     * @return Whether you can show permission rationale UI.
     */
    private fun shouldShowRequestPermissionRationale(permissions: Array<String>) =
        permissions.any { shouldShowRequestPermissionRationale(it) }

    /**
     * Requests permissions needed for recording video.
     */
    private fun requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
            ConfirmationDialog()
                .show(childFragmentManager, FRAGMENT_DIALOG)
        } else {
            requestPermissions(
                VIDEO_PERMISSIONS,
                REQUEST_VIDEO_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.size == VIDEO_PERMISSIONS.size) {
                for (result in grantResults) {
                    if (result != PERMISSION_GRANTED) {
                        ErrorDialog.newInstance(getString(R.string.permission_request))
                            .show(childFragmentManager, FRAGMENT_DIALOG)
                        break
                    }
                }
            } else {
                ErrorDialog.newInstance(getString(R.string.permission_request))
                    .show(childFragmentManager, FRAGMENT_DIALOG)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun hasPermissionsGranted(permissions: Array<String>) =
        permissions.none {
            checkSelfPermission((activity as FragmentActivity), it) != PERMISSION_GRANTED
        }

    /**
     * Tries to open a [CameraDevice]. The result is listened by [CameraHandler.stateCallback].
     *
     * Lint suppression - permission is checked in [hasPermissionsGranted]
     */
    @SuppressLint("MissingPermission")
    private fun openCamera(width: Int, height: Int) {
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestVideoPermissions()
            return
        }

        cameraHandler?.openCamera(width, height)
    }

    private fun showToast(message : String) {
        activity?.let {
            Toast.makeText(it, message, LENGTH_SHORT).show()
        }
    }
}

