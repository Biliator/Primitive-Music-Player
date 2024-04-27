package com.example.hudebni_prehravac.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.hudebni_prehravac.R
import com.example.hudebni_prehravac.models.Artists

class ArtistFilterAdapter(private val context: Context, private val data: ArrayList<Artists>): RecyclerView.Adapter<ArtistFilterAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.artist_item, parent, false))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val artistName: CheckBox = itemView.findViewById(R.id.check_box_artist_name)

        fun bind(artist: Artists) {
            artistName.text = artist.name

            artistName.setOnClickListener {
                artist.selected = !artist.selected
                notifyDataSetChanged()
            }

            if (artist.selected) {
                artistName.isChecked = true
                artistName.setTextColor(ContextCompat.getColor(context, R.color.yellow_59))
            } else {
                artistName.isChecked = false
                artistName.setTextColor(ContextCompat.getColor(context, R.color.grey_37))
            }
        }
    }
}