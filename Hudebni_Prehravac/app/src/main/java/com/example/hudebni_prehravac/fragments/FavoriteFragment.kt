package com.example.hudebni_prehravac.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class FavoriteFragment : Fragment() {
    private lateinit var navController: NavController
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: MainViewModel
    private lateinit var playlist: List<PlayList>
    private lateinit var favlist: List<FavoriteMusic>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomBarVisibility(true)
        navController = Navigation.findNavController(view)
        recyclerView = view.findViewById(R.id.recycler_view_fav)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.getPlayList.observe(requireActivity(), Observer { pl ->
            playlist = pl
            viewModel.getFavorite.observe(requireActivity(), Observer {
                favlist = it
                if (favlist.isNotEmpty()) {
                    recyclerView.visibility = View.VISIBLE
                    val favList = ArrayList<Music>()
                    for (music in (activity as MainActivity).musicList) {
                        music.fav = false
                        for (fav in favlist)
                            if (fav.musicId == music.id) {
                                music.fav = true
                                favList.add(music)
                            }
                    }
                    buildRecycleView(favList)
                } else recyclerView.visibility = View.GONE
            })
        })
    }

    override fun onPause() {
        viewModel.getPlayList.removeObservers(requireActivity())
        viewModel.getFavorite.removeObservers(requireActivity())
        super.onPause()
    }

    private fun buildRecycleView(music: ArrayList<Music>?) {
        if (music != null && music.size != 0) {
            val adapter = MusicAdapter(requireContext(), music)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            adapter.setOnItemClickListener(object : MusicAdapter.OnItemClickListener {
                override fun playSong(id: String) {
                    val bundle = bundleOf("id" to id, "fav" to "fav")
                    navController.navigate(
                            R.id.action_favoriteFragment_to_playerFragment,
                            bundle
                    )
                }

                override fun musicMenu(music: Music, view: View, position: Int) {
                    val menuItem = MusicItemMenu(requireActivity(), playlist, favlist, viewModel, music, adapter, position)
                    menuItem.show(requireActivity().supportFragmentManager, "Music Options")
                }
            })
        }
    }
}