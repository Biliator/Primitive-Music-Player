package com.example.hudebni_prehravac

import android.Manifest
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.example.hudebni_prehravac.models.Music
import com.example.hudebni_prehravac.room.MainViewModel
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mpatric.mp3agic.Mp3File
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel
    lateinit var musicList: ArrayList<Music>
    lateinit var queue: ArrayList<String>
    var update = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        bottom_navigation.let {
            NavigationUI.setupWithNavController(it, navController)
        }
        askPermissions()
    }

    private fun askPermissions() {
        Dexter.withActivity(this)
            .withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                @RequiresApi(Build.VERSION_CODES.Q)
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        musicList = getAllAudio()
                        queue = ArrayList()
                    }

                    if (report.isAnyPermissionPermanentlyDenied)
                        Toast.makeText(this@MainActivity, "Go" +
                                " to setting and grant permissions", Toast.LENGTH_LONG).show()
                }
                override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                ) { token!!.continuePermissionRequest() }
            })
            .onSameThread()
            .check()
    }
    private fun getImage(path: String): ByteArray? {
        try {
            val mp3file = Mp3File(File(path))
            if (mp3file.hasId3v2Tag()) {
                val id3v2Tag = mp3file.id3v2Tag
                val imageData = id3v2Tag.albumImage
                if (imageData != null)
                    return imageData
                return null
            }
        } catch (e: Exception) {
            Log.e("EXCEPTION", e.message.toString())
        }
        return null
    }
    fun setBottomBarVisibility(visible: Boolean) {
        bottom_navigation.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getAllAudio(): ArrayList<Music> {
        val tempAudioList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID,
        )
        val cursor = this.contentResolver.query(
                uri,
                projection,
                selection,
                null,
                null
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val path = cursor.getString(0)
                val title = cursor.getString(1)
                val artist = cursor.getString(2)
                val album = cursor.getString(3)
                val duration = cursor.getString(4)
                val size = cursor.getString(5)
                val id = cursor.getString(6)

                val file = File(path)
                if (file.exists()) {
                    val art = getImage(path)
                    val musicFiles = Music(path, title, artist, album, duration, size, id, art)
                    tempAudioList.add(musicFiles)
                }
            }
            cursor.close()
        }
        tempAudioList.sortBy { it.title.toLowerCase() }
        return tempAudioList
    }
}