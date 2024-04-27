package com.example.hudebni_prehravac.fragments

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hudebni_prehravac.MainActivity
import com.example.hudebni_prehravac.R
import com.example.hudebni_prehravac.adapters.PlaylistAdapter
import com.example.hudebni_prehravac.models.Music
import com.example.hudebni_prehravac.room.MainViewModel
import com.example.hudebni_prehravac.room.PlayList
import kotlinx.android.synthetic.main.dialog_create_playlist.view.*
import kotlinx.android.synthetic.main.dialog_create_playlist.view.btn_cancel
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.dialog_rename.view.*
import kotlinx.android.synthetic.main.fragment_playlist.*

class PlaylistFragment : Fragment() {
    private lateinit var playlist: List<PlayList>
    private lateinit var viewModel: MainViewModel
    private lateinit var navController: NavController
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomBarVisibility(true)

        recyclerView = view.findViewById(R.id.recycler_view_playlist_music)
        navController = Navigation.findNavController(view)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.getPlayList.observe(requireActivity(), Observer {
            playlist = it
            buildRecyclerView()
        })

        img_add_white.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            val mView: View = LayoutInflater.from(context).inflate(R.layout.dialog_create_playlist, null)
            builder.setView(mView)
            val dialog = builder.create()
            dialog.show()

            mView.btn_add.setOnClickListener {
                val newName = mView.edt_txt_new_playlist.text.toString().trimEnd().trimStart()
                if (newName.isNotEmpty()) {
                    val musics = ArrayList<Music>()
                    val playList = PlayList(0, newName, musics)
                    viewModel.addPlaylist(playList)
                    Toast.makeText(requireContext(), "DONE", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else
                    Toast.makeText(requireContext(), "Enter playlist name!", Toast.LENGTH_SHORT).show()
            }

            mView.btn_cancel.setOnClickListener { dialog.dismiss() }
        }
    }
    override fun onPause() {
        viewModel.getPlayList.removeObservers(requireActivity())
        super.onPause()
    }

    private fun buildRecyclerView() {
        if (playlist.isNotEmpty()) {
            recyclerView.visibility = View.VISIBLE
            val adapter = PlaylistAdapter(requireContext(), playlist)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            adapter.setOnItemClickListener(object : PlaylistAdapter.OnItemClickListener {
                override fun goToDetails(position: Int) {
                    val bundle = bundleOf("position" to position)
                    navController.navigate(
                            R.id.action_nav_playlist_to_playlistDetailsFragment,
                            bundle
                    )
                }

                override fun playlistMenu(position: Int, view: View) {
                    val menu = PopupMenu(requireContext(), view)
                    menu.menuInflater.inflate(R.menu.playlist_item_menu, menu.menu)
                    menu.show()
                    menu.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.menu_delete_playlist -> delete(position, adapter)
                            R.id.menu_rename_playlist -> rename(position, adapter)
                        }
                        true
                    }
                }
            })
        } else recyclerView.visibility = View.GONE
    }

    private fun delete(position: Int, adapter: PlaylistAdapter) {
        val builder = AlertDialog.Builder(context)
        val mView: View = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null)
        builder.setView(mView)
        val dialog = builder.create()
        dialog.show()
        mView.txt_delete_title.text = "Are you sure you want to delete this playlist"
        mView.btn_delete.setOnClickListener {
            val playList = playlist[position]
            viewModel.removePlaylist(playList)
            adapter.notifyItemRemoved(position)
            Toast.makeText(requireContext(), "DONE", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        mView.btn_cancel.setOnClickListener { dialog.dismiss() }
    }

    private fun rename(position: Int, adapter: PlaylistAdapter) {
        val builder = AlertDialog.Builder(context)
        val mView: View = LayoutInflater.from(context).inflate(R.layout.dialog_rename, null)
        builder.setView(mView)
        val dialog = builder.create()
        dialog.show()

        mView.btn_rename.setOnClickListener {
            val newName = mView.edit_txt_new_title.text.toString().trimEnd().trimStart()
            if (newName.isNotEmpty()) {
                val playList = playlist[position]
                playList.playListName = newName
                viewModel.updatePlaylist(playList)
                adapter.notifyItemChanged(position)
                Toast.makeText(requireContext(), "DONE", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else Toast.makeText(requireContext(), "Enter playlist name!", Toast.LENGTH_SHORT).show()
        }
        mView.btn_cancel.setOnClickListener { dialog.dismiss() }
    }
}