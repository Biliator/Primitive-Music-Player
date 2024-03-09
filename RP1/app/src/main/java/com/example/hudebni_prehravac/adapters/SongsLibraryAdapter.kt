package com.example.hudebni_prehravac.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hudebni_prehravac.R
import com.example.hudebni_prehravac.models.Song
import kotlinx.android.synthetic.main.song_row.view.*
import java.io.File


class SongsLibraryAdapter(private val context: Context, private val data: ArrayList<String>): RecyclerView.Adapter<SongsLibraryAdapter.ViewHolder>() {
    private var mListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.song_row, parent, false), mListener!!)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: SongsLibraryAdapter.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class ViewHolder(itemView: View, private val listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        private val imgSongPhoto: ImageView = itemView.img_song_photo
        private val txtName: TextView = itemView.txt_song_name
        private val txtAuthor: TextView = itemView.txt_author

        fun bind(song: String) {
            //txtName.text = song.album
            txtAuthor.text = song
            //Log.d("GGG", song.photo.toString())
            //imgSongPhoto.setImageBitmap(stringToBitMap(song.photo))
        }
    }

    fun stringToBitMap(path: String?): Bitmap? {
        return try {
            BitmapFactory.decodeFile(File(path!!).absolutePath)
        } catch (e: Exception) {
            e.message
            null
        }
    }

    interface OnItemClickListener {
        fun onClickListener(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }
}