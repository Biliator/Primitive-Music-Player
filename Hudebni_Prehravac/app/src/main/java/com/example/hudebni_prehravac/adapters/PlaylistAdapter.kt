package com.example.hudebni_prehravac.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.hudebni_prehravac.R
import com.example.hudebni_prehravac.room.PlayList

class PlaylistAdapter(private val context: Context, private val data: List<PlayList>): RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {
    private var mListener: OnItemClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.playlist_item, parent, false), mListener!!)
    }
    override fun getItemCount(): Int = data.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class ViewHolder(itemView: View, private val listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        private val layout: ConstraintLayout = itemView.findViewById(R.id.layout_playlist)
        private val txtTitle: TextView = itemView.findViewById(R.id.txt_playlist_name)
        private val imgMenu: ImageView = itemView.findViewById(R.id.img_more)

        fun bind(music: PlayList) {
            val position = adapterPosition
            txtTitle.text = music.playListName
            layout.setOnClickListener {
                if (position != RecyclerView.NO_POSITION)
                    listener.goToDetails(position)
            }
            imgMenu.setOnClickListener {
                if (position != RecyclerView.NO_POSITION)
                    listener.playlistMenu(position, imgMenu)
            }
        }
    }

    interface OnItemClickListener {
        fun goToDetails(position: Int)

        fun playlistMenu(position: Int, view: View)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }
}