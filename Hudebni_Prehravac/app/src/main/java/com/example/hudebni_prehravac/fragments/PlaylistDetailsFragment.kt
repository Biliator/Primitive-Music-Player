package com.example.hudebni_prehravac.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hudebni_prehravac.MainActivity
import com.example.hudebni_prehravac.R
import com.example.hudebni_prehravac.adapters.MusicAdapter
import com.example.hudebni_prehravac.models.Music
import com.example.hudebni_prehravac.room.FavoriteMusic
import com.example.hudebni_prehravac.room.MainViewModel
import com.example.hudebni_prehravac.room.PlayList
import com.example.hudebni_prehravac.utils.MusicItemMenu

class PlaylistDetailsFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var recyclerView: RecyclerView
    private var position: Int = -1
    private lateinit var playlist: PlayList
    private lateinit var playlists: List<PlayList>
    private lateinit var viewModel: MainViewModel
    private lateinit var favlist: List<FavoriteMusic>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = requireArguments().getInt("position")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playlist_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomBarVisibility(false)
        navController = Navigation.findNavController(view)
        recyclerView = view.findViewById(R.id.recycler_view_playlist_songs)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.getFavorite.observe(requireActivity(), Observer {
            favlist = it
            viewModel.getPlayList.observe(requireActivity(), Observer { pl ->
                playlist = pl[position]
                playlists = pl
                if (playlist.musics.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    for (music in playlist.musics) {
                        music.fav = false
                        for (fav in favlist) if (fav.musicId == music.id) music.fav = true
                    }
                } else recyclerView.visibility = View.GONE
                displayPlaylistSong(playlist.musics)
            })
        })

    }

    override fun onDestroy() {
        viewModel.getPlayList.removeObservers(requireActivity())
        viewModel.getFavorite.removeObservers(requireActivity())
        super.onDestroy()
    }

    private fun displayPlaylistSong(playlistSongList: ArrayList<Music>) {
        if (playlistSongList.size != 0) {
            val adapter = MusicAdapter(requireContext(), playlistSongList)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            adapter.setOnItemClickListener(object : MusicAdapter.OnItemClickListener {
                override fun playSong(id: String) {
                    val bundle = bundleOf("id" to id, "playlist" to position.toString())
                    navController.navigate(
                        R.id.action_playlistDetailsFragment_to_playerFragment,
                        bundle
                    )
                }

                @RequiresApi(Build.VERSION_CODES.M)
                override fun musicMenu(music: Music, view: View, position: Int) {
                    val menuItem = MusicItemMenu(requireActivity(), playlists, favlist, viewModel, music, adapter, position)
                    menuItem.show(requireActivity().supportFragmentManager, "Music Options")
                }
            })
        }
    }
}