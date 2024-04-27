package com.example.hudebni_prehravac.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hudebni_prehravac.MainActivity
import com.example.hudebni_prehravac.R
import com.example.hudebni_prehravac.adapters.AddToPlayListAdapter
import com.example.hudebni_prehravac.adapters.MusicAdapter
import com.example.hudebni_prehravac.models.Music
import com.example.hudebni_prehravac.room.FavoriteMusic
import com.example.hudebni_prehravac.room.MainViewModel
import com.example.hudebni_prehravac.room.PlayList
import com.example.hudebni_prehravac.utils.TimeLabel.createTimeLabel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mpatric.mp3agic.ID3v2
import com.mpatric.mp3agic.ID3v24Tag
import com.mpatric.mp3agic.Mp3File
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.dialog_rename.view.*
import kotlinx.android.synthetic.main.dialog_rename.view.btn_cancel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.lang.Exception

@Suppress("DEPRECATION")
class MusicItemMenu(
        val activity: Activity,
        val playList: List<PlayList>,
        val favList: List<FavoriteMusic>,
        val viewModel: MainViewModel,
        val music: Music,
        val adapter: MusicAdapter,
        val position: Int,
        var album: ArrayList<Music>? = null
) : BottomSheetDialogFragment(){
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_music_menu, container, false)

        val btnRename = view.findViewById<LinearLayout>(R.id.layout_rename)
        val btnDelete = view.findViewById<LinearLayout>(R.id.layout_delete)
        val btnShowDetails = view.findViewById<LinearLayout>(R.id.layout_details)
        val btnSetRingtone = view.findViewById<LinearLayout>(R.id.layout_ringtone)
        val btnAddToPlayList = view.findViewById<LinearLayout>(R.id.layout_playlist)
        val btnAddToFav = view.findViewById<LinearLayout>(R.id.layout_fav)
        val btnShare = view.findViewById<LinearLayout>(R.id.layout_share)
        val btnQueue = view.findViewById<LinearLayout>(R.id.layout_queue)
        val btnCover = view.findViewById<LinearLayout>(R.id.layout_change_cover)
        val txtFav = view.findViewById<TextView>(R.id.txt_fav)
        val txtQueue = view.findViewById<TextView>(R.id.txt_queue)

        if (music.fav) txtFav.text = "remove from favorite"
        if (music.id in (activity as MainActivity).queue) txtQueue.text = "add from queue"

        btnRename.setOnClickListener { rename() }
        btnDelete.setOnClickListener { delete() }
        btnShowDetails.setOnClickListener { showDetails() }
        btnSetRingtone.setOnClickListener { setRingtone() }
        btnAddToPlayList.setOnClickListener { addToPlayList() }
        btnAddToFav.setOnClickListener { addToFav() }
        btnShare.setOnClickListener { share() }
        btnQueue.setOnClickListener { addQueue() }
        btnCover.setOnClickListener { changeCover() }

        return view
    }

    private fun rename() {
        val builder = AlertDialog.Builder(context)
        val mView: View = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rename, null)
        builder.setView(mView)
        val dialog = builder.create()
        dialog.show()

        mView.btn_rename.setOnClickListener {
            val newName = mView.edit_txt_new_title.text.toString()
            val path = ArrayList(music.path.split('/'))
            path[path.size - 1] = newName
            var newPath = ""
            for (i in 0 until path.size - 1) newPath += "${path[i]}/"

            newPath += "$newName.mp3"

            val file = File(music.path)
            if (file.renameTo(File(newPath))) {
                updateMediaStore(newPath, "$newName.mp3")
                music.title = "$newName.mp3"
                music.path = newPath
                adapter.notifyItemChanged(position)
                Toast.makeText(requireContext(), "DONE", Toast.LENGTH_LONG).show()
            }
            else
                Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        mView.btn_cancel.setOnClickListener { dialog.dismiss() }
    }

    private fun delete() {
        val builder = AlertDialog.Builder(requireContext())
        val mView: View = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete, null)
        builder.setView(mView)
        val dialog = builder.create()
        dialog.show()

        mView.btn_delete.setOnClickListener {
            val file = File(music.path)
            val deleted = file.delete()
            if (deleted) {
                (activity as MainActivity).update = true
                if (album != null) album!!.remove(music)
                (activity as MainActivity).musicList.remove(music)

                val pos = checkIfInFav()
                if (pos != -1) viewModel.removeFavorite(FavoriteMusic(pos, music.id))

                for (pl in playList) {
                    for (song in pl.musics) {
                        if (song.id == music.id) {
                            Log.d("GGg", "nasel")
                            pl.musics.remove(music)
                            viewModel.updatePlaylist(pl)
                            continue
                        }
                    }
                }

                adapter.notifyItemRemoved(position)
                deleteMediaStore()
                Toast.makeText(requireContext(), "DONE", Toast.LENGTH_SHORT).show()
            } else
                Toast.makeText(requireContext(), "ERROR", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        mView.btn_cancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showDetails() {
        val builder = AlertDialog.Builder(requireContext())
        val mView: View = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_details, null)
        builder.setView(mView)
        val dialog = builder.create()
        dialog.show()

        val txtName = mView.findViewById<TextView>(R.id.txt_song_name)
        val txtAlbum = mView.findViewById<TextView>(R.id.txt_album)
        val txtDuration = mView.findViewById<TextView>(R.id.txt_duration)
        val txtSize = mView.findViewById<TextView>(R.id.txt_size)

        txtName.text = music.title
        txtAlbum.text = music.album
        txtDuration.text = createTimeLabel(music.duration.toInt())
        txtSize.text = """${"%.1f".format(music.size.toFloat() / 1_000_000f)} MB"""
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setRingtone() {
        try {
            if (!Settings.System.canWrite(requireContext())) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + requireContext().packageName)
                requireContext().startActivity(intent)
            } else {
                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DATA, music.path)
                values.put(MediaStore.MediaColumns.TITLE, music.title)
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
                values.put(MediaStore.MediaColumns.SIZE, music.size)
                values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name)
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true)

                val uri = MediaStore.Audio.Media.getContentUriForPath(music.path)
                requireContext().contentResolver.delete(uri!!, MediaStore.MediaColumns.DATA + "=\"" + music.path + "\"", null)
                val newUri: Uri = requireContext().contentResolver.insert(uri, values)!!

                RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri)
                Toast.makeText(context, "DONE", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "ERROR: ${e.message}", Toast.LENGTH_LONG).show()
        }
        dismiss()
    }

    private fun addToPlayList() {
        val builder = AlertDialog.Builder(context)
        val mView: View = LayoutInflater.from(context).inflate(R.layout.dialog_add_to_playlist, null)
        builder.setView(mView)
        val dialog = builder.create()
        dialog.show()

        val recyclerView = mView.findViewById<RecyclerView>(R.id.recycler_view_playlist)
        val txtNothing = mView.findViewById<TextView>(R.id.txt_no_playlist)
        val imgClose = mView.findViewById<ImageView>(R.id.img_close_dialog)

        if (playList.isNotEmpty()) {
            recyclerView.visibility = View.VISIBLE
            txtNothing.visibility = View.INVISIBLE
            recyclerView.layoutManager = LinearLayoutManager(context)
            val ad = AddToPlayListAdapter(requireContext(), playList, music)
            recyclerView.adapter = ad
            ad.setOnItemClickListener(object : AddToPlayListAdapter.OnItemClickListener {
                override fun addToPlayList(data: PlayList) {
                    var found = false
                    for (i in data.musics)
                        if (i.id == music.id) {
                            found = true
                            break
                        }
                    (activity as MainActivity).update = true
                    if (found) data.musics.remove(music)
                    else data.musics.add(music)
                    viewModel.updatePlaylist(data)
                    dialog.dismiss()
                }
            })
        } else {
            recyclerView.visibility = View.INVISIBLE
            txtNothing.visibility = View.VISIBLE
        }

        imgClose.setOnClickListener { dialog.dismiss() }
    }

    private fun addToFav() {
        if (favList.isNotEmpty()) {
            val position = checkIfInFav()
            if (position != -1) {
                (activity as MainActivity).update = true
                viewModel.removeFavorite(FavoriteMusic(position, music.id))
                music.fav = false
            } else {
                (activity as MainActivity).update = true
                viewModel.insertFavorite(FavoriteMusic(0, music.id))
                music.fav = true
            }
        } else {
            (activity as MainActivity).update = true
            viewModel.insertFavorite(FavoriteMusic(0, music.id))
            music.fav = true
        }
        dismiss()
        Toast.makeText(context, "DONE", Toast.LENGTH_LONG).show()
    }

    private fun checkIfInFav(): Int {
        for (fav in favList) if (fav.musicId == music.id) return fav.id
        return -1
    }

   private fun share() {
        val intent = Intent(Intent.ACTION_SEND)
        val file = File(music.path)
        val uri = FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName + ".provider", file)
        if (file.exists()) {
            intent.type = "application/mp3"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Share Song")
            intent.putExtra(Intent.EXTRA_TEXT, "Share Song")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            requireContext().startActivity(Intent.createChooser(intent, "Share Song"))
        }
    }

    private fun addQueue() {
        if (music.id in (activity as MainActivity).queue)
            (activity as MainActivity).queue.remove(music.id)
        else (activity as MainActivity).queue.add(music.id)
        dismiss()
    }

    private fun changeCover() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { intent ->
            intent.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivityForResult(intent, 0)
        }
    }




    private fun updateMediaStore(newPath: String, newName: String) {
        val values = ContentValues()
        values.put(MediaStore.Audio.Media.DATA, newPath)
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, newName)
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music.id.toLong())
        requireContext().contentResolver.update(uri, values, null, null)
    }

    private fun deleteMediaStore() {
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music.id.toLong())
        requireContext().contentResolver.delete(uri, null, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {0 -> { try {
                    if (resultCode == Activity.RESULT_OK)
                        data?.data?.let { uri ->
                            val imageStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
                            val bitmap = BitmapFactory.decodeStream(imageStream)
                            val baos = ByteArrayOutputStream()
                            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val imageBytes = baos.toByteArray()
                            val newName = "Copy${(10000000..90000000).random()}"
                            val path = ArrayList(music.path.split('/'))
                            path[path.size - 1] = newName
                            var newPath = ""
                            for (i in 0 until path.size - 1) newPath += "${path[i]}/"
                            newPath += "$newName.mp3"
                            val file = File(music.path)
                            val mp3file = Mp3File(file)
                            val id3v2Tag: ID3v2
                            if (mp3file.hasId3v2Tag()) {
                                id3v2Tag = mp3file.id3v2Tag
                            } else {
                                id3v2Tag = ID3v24Tag()
                                mp3file.id3v2Tag = id3v2Tag
                            }
                            id3v2Tag.setAlbumImage(imageBytes, "image/jpeg")
                            mp3file.save(newPath)
                            file.delete()
                            deleteMediaStore()
                            val newFile = File(newPath)
                            newFile.renameTo(File(music.path))
                            val values = ContentValues()
                            values.put(MediaStore.Audio.Media.DATA, music.path)
                            MediaScannerConnection.scanFile(requireContext(), arrayOf(File(music.path).absolutePath),
                                    null) { _: String?, u: Uri ->
                                requireContext().contentResolver.update(u, values, null, null)
                            }
                            music.art = imageBytes
                            adapter.notifyItemChanged(position)
                        }
                    else Toast.makeText(requireContext(), "You haven't picked Image", Toast.LENGTH_LONG).show()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "File not found", Toast.LENGTH_LONG).show()
                } } } }
}