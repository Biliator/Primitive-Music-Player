package com.example.hudebni_prehravac.fragments

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hudebni_prehravac.MainActivity
import com.example.hudebni_prehravac.R
import com.example.hudebni_prehravac.adapters.ArtistFilterAdapter
import com.example.hudebni_prehravac.adapters.MusicAdapter
import com.example.hudebni_prehravac.models.Artists
import com.example.hudebni_prehravac.models.Music
import com.example.hudebni_prehravac.room.FavoriteMusic
import com.example.hudebni_prehravac.room.MainViewModel
import com.example.hudebni_prehravac.room.PlayList
import com.example.hudebni_prehravac.utils.MusicItemMenu
import kotlinx.android.synthetic.main.fragment_music.*

@Suppress("DEPRECATION")
class MusicFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutNoMusic: ConstraintLayout
    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel
    private lateinit var musicList: ArrayList<Music>
    private lateinit var playlist: List<PlayList>
    private lateinit var favlist: List<FavoriteMusic>
    private var artists: ArrayList<Artists>? = null
    private var adapter: MusicAdapter? = null
    private var aToZ = true

    override fun onCreate(savedInstanceState: Bundle?) {
        (activity as MainActivity).update = false
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomBarVisibility(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar_music)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayShowTitleEnabled(true)
        (activity as AppCompatActivity).supportActionBar!!.title = "My music"
        setHasOptionsMenu(true)

        recyclerView = view.findViewById(R.id.recycler_view_music)
        layoutNoMusic = view.findViewById(R.id.layout_no_music)
        navController = Navigation.findNavController(view)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        viewModel.getFavorite.observe(requireActivity(), {
            favlist = it
            viewModel.getPlayList.observe(requireActivity(), { pl ->
                playlist = pl
                musicList = (activity as MainActivity).musicList
                if (musicList.isNotEmpty())
                    for (music in musicList) {
                        music.fav = false
                        for (fav in favlist) if (fav.musicId == music.id) music.fav = true
                    }
                if (!(activity as MainActivity).update) {
                    createArtistsList()
                    displayMusic(musicList)
                }
            })
        })
    }

    private fun createArtistsList() {
        artists = ArrayList()
        if (musicList.isNotEmpty()) {
            val artName = ArrayList<String>()
            for (i in musicList) {
                if (i.artist in artName) continue
                artName.add(i.artist)
                artists!!.add(Artists(i.artist))
            }
        }
    }
    
    override fun onPause() {
        viewModel.getPlayList.removeObservers(requireActivity())
        viewModel.getFavorite.removeObservers(requireActivity())
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        (activity as AppCompatActivity).menuInflater.inflate(R.menu.music_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> searchMusic(item)
            R.id.menu_item_filters -> openFilterDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openFilterDialog() {
        val builder = AlertDialog.Builder(context)
        val mView: View = LayoutInflater.from(context).inflate(R.layout.dialog_music_filter, null)
        builder.setView(mView)
        val dialog = builder.create()
        dialog.show()

        val recyclerView = mView.findViewById<RecyclerView>(R.id.recycler_view_filter)
        val checkAZ = mView.findViewById<RadioButton>(R.id.checkb_az)
        val checkZA = mView.findViewById<RadioButton>(R.id.checkb_za)
        val imgClose = mView.findViewById<ImageView>(R.id.img_close_dialog)
        val btnAccept = mView.findViewById<Button>(R.id.btn_dialog_filter_accept)

        if (aToZ) checkAZ.isChecked = true
        else checkZA.isChecked = true
        val artistCopy = ArrayList<Boolean>()
        for (artist in artists!!)
            artistCopy.add(artist.selected)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = ArtistFilterAdapter(requireContext(), artists!!)
        recyclerView.adapter = adapter
        imgClose.setOnClickListener {
            var x = 0
            for (i in artists!!) i.selected = artistCopy[x++]
            dialog.dismiss()
        }
        btnAccept.setOnClickListener {
            val selectedArtists = ArrayList<String>()
            val newMusicList = ArrayList<Music>()
            aToZ = true
            for (artist in artists!!)
                if (artist.selected)
                    selectedArtists.add(artist.name)
            for (i in musicList)
                if (i.artist in selectedArtists)
                    newMusicList.add(i)
            newMusicList.sortBy { it.title.toLowerCase() }
            if (checkZA.isChecked) {
                aToZ = false
                newMusicList.reverse()
            }
            displayMusic(newMusicList)
            dialog.dismiss()
        }
    }



    private fun searchMusic(menuItem: MenuItem) {
        val searchView = menuItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val newMusicList = ArrayList<Music>()
                for (item in musicList)
                    if (item.title.toLowerCase().contains(newText!!.toLowerCase())
                            || item.album.toLowerCase().contains(newText.toLowerCase())
                            || item.artist.toLowerCase().contains(newText.toLowerCase()))
                        newMusicList.add(item)
                displayMusic(newMusicList)
                return true
            }
        })
    }

    private fun displayMusic(musicList: ArrayList<Music>) {
        if (musicList.isNotEmpty()) {
            recyclerView.visibility = View.VISIBLE
            layoutNoMusic.visibility = View.INVISIBLE
            adapter = MusicAdapter(requireContext(), musicList)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            adapter!!.setOnItemClickListener(object : MusicAdapter.OnItemClickListener {
                override fun playSong(id: String) {
                    val bundle = bundleOf("id" to id)
                    navController.navigate(
                            R.id.action_nav_music_to_playerFragment,
                            bundle
                    )
                }

                @RequiresApi(Build.VERSION_CODES.M)
                override fun musicMenu(music: Music, view: View, position: Int) {
                    val menuItem = MusicItemMenu(requireActivity(), playlist, favlist, viewModel, music, adapter!!, position)
                    menuItem.show(requireActivity().supportFragmentManager, "Music Options")
                }
            })
        } else {
            recyclerView.visibility = View.INVISIBLE
            layout_no_music.visibility = View.VISIBLE
        }
    }
}