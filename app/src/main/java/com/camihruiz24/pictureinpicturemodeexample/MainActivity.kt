package com.camihruiz24.pictureinpicturemodeexample

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.util.Rational
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.viewinterop.AndroidView
import com.camihruiz24.pictureinpicturemodeexample.ui.theme.PictureInPictureModeExampleTheme

class MainActivity : ComponentActivity() {

    class MyReceiver : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            println("Clicked on PIP action")
        }
    }

    // Mirar si PIP está habilitado en el dispositivo
    private val isPipSupported by lazy {
        packageManager.hasSystemFeature(
            PackageManager.FEATURE_PICTURE_IN_PICTURE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PictureInPictureModeExampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AndroidView(factory = {
                        VideoView(it, null).apply {
                            setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.mi_video}")) // poner aquí el nombre del vídeo si estuviera en la carpeta raw
                            start()
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned {
                            videoViewBounds = it
                                .boundsInWindow()
                                .toAndroidRect()
                        })
                }
            }
        }
    }

    private var videoViewBounds = Rect()

    private fun updatedPipParams(): PictureInPictureParams? {
        return PictureInPictureParams.Builder()
            .setSourceRectHint(videoViewBounds)
            .setAspectRatio(Rational(16, 9))
            .setActions(
                listOf(
                    RemoteAction(
                        Icon.createWithResource(
                            applicationContext,
                            R.drawable.baseline_play_circle_outline_24,
                        ),
                        "Play",
                        "Play",
                        PendingIntent.getBroadcast(
                            applicationContext,
                            0,
                            Intent(
                                applicationContext,
                                MyReceiver::class.java
                            ),
                            PendingIntent.FLAG_IMMUTABLE,
                        )
                    )
                )
            )
            .build()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (!isPipSupported) {
            return
        }
        updatedPipParams()?.let {
            enterPictureInPictureMode(it)
        }
    }
}