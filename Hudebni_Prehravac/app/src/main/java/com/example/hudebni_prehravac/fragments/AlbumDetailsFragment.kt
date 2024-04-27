package com.example.hudebni_prehravac.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
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

class AlbumDetailsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel
    private lateinit var playlist: List<PlayList>
    private lateinit var favList: List<FavoriteMusic>
    private lateinit var albumSongsList: ArrayList<Music>
    private lateinit var album: String

    override fun onCreate(savedInstanceState: Bundle?) {
        album = requireArguments().getString("album")!!
        (activity as MainActivity).update = false
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomBarVisibility(false)
        navController = Navigation.findNavController(view)
        recyclerView = view.findViewById(R.id.recycler_view_album_songs)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.getFavorite.observe(requireActivity(), { fav ->
            favList = fav
            viewModel.getPlayList.observe(requireActivity(), {
                playlist = it
                albumSongsList = createAlbumList()
                if (!(activity as MainActivity).update) displayAlbumSong(albumSongsList)
            })
        })
    }

    override fun onPause() {
        viewModel.getPlayList.removeObservers(requireActivity())
        viewModel.getFavorite.removeObservers(requireActivity())
        super.onPause()
    }

    private fun createAlbumList(): ArrayList<Music> {
        val albumSongsList = ArrayList<Music>()
        for (music in (activity as MainActivity).musicList)
            if (music.album == album)
                albumSongsList.add(music)
        return albumSongsList
    }

    private fun displayAlbumSong(albumSongList: ArrayList<Music>) {
        if (albumSongList.isNotEmpty()) {
            val adapter = MusicAdapter(requireContext(), albumSongList)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            adapter.setOnItemClickListener(object : MusicAdapter.OnItemClickListener {
                override fun playSong(id: String) {
                    val bundle = bundleOf("id" to id, "album" to album)
                    navController.navigate(
                            R.id.action_albumDetailsFragment_to_playerFragment,
                            bundle
                    )
                }

                override fun musicMenu(music: Music, view: View, position: Int) {
                    val menuItem = MusicItemMenu(requireActivity(), playlist, favList, viewModel, music, adapter, position, albumSongsList)
                    menuItem.show(requireActivity().supportFragmentManager, "Music Options")
                }
            })
        } else recyclerView.visibility = View.GONE
    }
}