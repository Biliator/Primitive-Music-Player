package com.example.hudebni_prehravac.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hudebni_prehravac.R
import com.example.hudebni_prehravac.models.Music
import com.example.hudebni_prehravac.room.PlayList

class AddToPlayListAdapter(private val context: Context, private val data: List<PlayList>, private val currentMusic: Music): RecyclerView.Adapter<AddToPlayListAdapter.ViewHolder>() {
    private var mListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.add_to_playlist_item, parent, false), mListener!!)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class ViewHolder(itemView: View, private val listener: OnItemClickListener): RecyclerView.ViewHolder(itemView) {
        private val playlistName: TextView = itemView.findViewById(R.id.txt_playlist_name)

        fun bind(playlist: PlayList) {
            var found = false
            for (musics in playlist.musics)
                if (musics.id == currentMusic.id)
                    found = true

            if (found) playlistName.text = "${playlist.playListName} (already in)"
            else playlistName.text = playlist.playListName

            playlistName.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION)
                    listener.addToPlayList(playlist)
            }
        }
    }

    interface OnItemClickListener {
        fun addToPlayList(data: PlayList)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }
}