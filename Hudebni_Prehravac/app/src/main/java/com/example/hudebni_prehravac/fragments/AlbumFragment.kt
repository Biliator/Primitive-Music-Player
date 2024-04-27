package com.example.hudebni_prehravac.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hudebni_prehravac.MainActivity
import com.example.hudebni_prehravac.R
import com.example.hudebni_prehravac.adapters.AlbumAdapter

class AlbumFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_album, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomBarVisibility(true)
        navController = Navigation.findNavController(view)
        recyclerView = view.findViewById(R.id.recycler_view_album)
        displayAlbums(createAlbumList())
    }
    private fun createAlbumList(): ArrayList<String> {
        val list = (activity as MainActivity).musicList
        val albumList = ArrayList<String>()
        for (music in list) {
            if (music.album in albumList) continue
            albumList.add(music.album)
        }
        return albumList
    }

    private fun displayAlbums(albumList: ArrayList<String>) {
        if (albumList.isNotEmpty()) {
            val adapter = AlbumAdapter(requireContext(), albumList)
            recyclerView.layoutManager = GridLayoutManager(context, 2)
            recyclerView.adapter = adapter
            adapter.setOnItemClickListener(object : AlbumAdapter.OnItemClickListener {
                override fun playAlbum(album: String) {
                    val bundle = bundleOf("album" to album)
                    navController.navigate(
                            R.id.action_nav_album_to_albumDetailsFragment,
                            bundle
                    )
                }
            })
        }
    }
}