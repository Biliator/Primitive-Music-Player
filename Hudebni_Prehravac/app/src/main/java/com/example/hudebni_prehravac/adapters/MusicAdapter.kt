package com.example.hudebni_prehravac.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hudebni_prehravac.R
import com.example.hudebni_prehravac.models.Music

class MusicAdapter(private val context: Context, private val data: ArrayList<Music>): RecyclerView.Adapter<MusicAdapter.ViewHolder>() {
    private var mListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.music_item, parent, false), mListener!!)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class ViewHolder(itemView: View, private val listener: OnItemClickListener): RecyclerView.ViewHolder(itemView) {
        private val layout: ConstraintLayout = itemView.findViewById(R.id.music_layout)
        private val txtTitle: TextView = itemView.findViewById(R.id.txt_title)
        private val txtArtist: TextView = itemView.findViewById(R.id.txt_artist)
        private val imgPhoto: ImageView = itemView.findViewById(R.id.img_music_photo)
        private val imgMenu: ImageView = itemView.findViewById(R.id.img_more)

        fun bind(music: Music) {
            if (music.art != null) {
                val bitmap = BitmapFactory.decodeByteArray(music.art, 0, music.art!!.size)
                imgPhoto.setImageBitmap(bitmap)
            } else imgPhoto.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_music))

            txtTitle.text = music.title
            txtArtist.text = music.artist

            layout.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION)
                    listener.playSong(music.id)
            }

            imgMenu.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION)
                    listener.musicMenu(music, imgMenu, position)
            }
        }
    }

    interface OnItemClickListener {
        fun playSong(id: String)

        fun musicMenu(music: Music, view: View, position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }
}