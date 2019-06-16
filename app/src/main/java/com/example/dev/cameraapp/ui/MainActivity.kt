package com.example.dev.cameraapp.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.example.dev.cameraapp.R

/**
 * @author v.sibichenko on 06/16/2019.
 */

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(
            R.id.startFragment, null,
            NavOptions.Builder()
                .setPopUpTo(R.id.startFragment, true)
                .build()
        )
    }

}
