package com.example.hudebni_prehravac.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hudebni_prehravac.R
import com.example.hudebni_prehravac.adapters.SongsLibraryAdapter
import com.example.hudebni_prehravac.models.Song
import kotlinx.android.synthetic.main.fragment_library.view.*
import java.io.ByteArrayOutputStream
import java.io.File


class LibraryFragment : Fragment() {
    private lateinit var musicFilesList: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            val permission: Array<String> = Array(1) { Manifest.permission.READ_EXTERNAL_STORAGE }
            ActivityCompat.requestPermissions(requireActivity(), permission, 1)
        }
        else
        {
            /*
            val songList = getAllAudioFromDevice(requireContext())
            if (songList != null) {
                buildRecyclerView(view, songList)
            }

             */
            musicFilesList = ArrayList()
            fillMusicList()
            if (musicFilesList.isNotEmpty()) {
                buildRecyclerView(view, musicFilesList)
            }
        }
    }

    private fun getAllAudioFromDevice(context: Context): ArrayList<Song>? {
        val tempAudioList = ArrayList<Song>()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.ArtistColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.ALBUM_ART
        )

        val c = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            null
        )

        if (c != null) {
            while (c.moveToNext()) {
                val id: String = c.getString(0)
                val album: String = c.getString(1)
                val artist: String = c.getString(2)
                val photo: String? = c.getString(3)
                val audioModel = Song(id, album, artist, photo)

                tempAudioList.add(audioModel)
            }
            c.close()
        }
        return tempAudioList
    }

    private fun bitMapToString(bitmap: Bitmap?): String? {
        if (bitmap != null) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val b: ByteArray = baos.toByteArray()
            return Base64.encodeToString(b, Base64.DEFAULT)
        }
        return null
    }

    private fun addMusicFileFrom(dirPath: String) {
        val musicDir = File(dirPath)
        if (!musicDir.exists()) {
            musicDir.mkdir()
            return
        }
        val files = musicDir.listFiles()
        for (file in files) {
            val path = file.absolutePath
            if (path.endsWith(".mp3"))
                musicFilesList.add(path)
        }
    }

    private fun fillMusicList() {
        musicFilesList.clear()
        addMusicFileFrom(requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC).toString())
        addMusicFileFrom(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString())
    }

    private fun buildRecyclerView(view: View, data: ArrayList<String>) {
        view.recyclerview_songs.layoutManager = LinearLayoutManager(context)
        val adapter = SongsLibraryAdapter(requireContext(), data)
        view.recyclerview_songs.adapter = adapter
        adapter.setOnItemClickListener(object : SongsLibraryAdapter.OnItemClickListener {
            override fun onClickListener(position: Int) {

            }
        })
    }
}