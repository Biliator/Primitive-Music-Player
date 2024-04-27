package com.example.hudebni_prehravac.fragments

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.hudebni_prehravac.MainActivity
import com.example.hudebni_prehravac.R
import com.example.hudebni_prehravac.models.Music
import com.example.hudebni_prehravac.room.MainViewModel
import com.example.hudebni_prehravac.room.PlayList
import com.example.hudebni_prehravac.utils.TimeLabel.createTimeLabel
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {
    private lateinit var viewModel: MainViewModel
    private var id = ""
    private var position = -1
    private var mediaPlayer: MediaPlayer? = null
    private var album: String? = null
    private var playListPosition: String? = null
    private var fav: String? = null
    private lateinit var musicList: ArrayList<Music>
    private var shuffledMusic: ArrayList<Music>? = null
    private lateinit var playlist: List<PlayList>
    private lateinit var job: Job
    private var repeat = false
    private var shuffle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = requireArguments().getString("id")!!
        album = requireArguments().getString("album")
        playListPosition = requireArguments().getString("playlist")
        fav = requireArguments().getString("fav")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setBottomBarVisibility(false)

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        viewModel.getPlayList.observe(requireActivity(), { playlist = it })
        musicList =
            when {
                album != null -> albumMusic()
                playListPosition != null -> playListMusic()
                fav != null -> favListMusic()
                else -> (activity as MainActivity).musicList
            }
        for (i in musicList.indices) if (musicList[i].id == id) position = i
        initPlayer()
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.img_onclick_animation)
        img_play.setOnClickListener {
            img_play.startAnimation(animation)
            play()
        }
        img_next.setOnClickListener {
            job.cancel()
            img_next.startAnimation(animation)
            img_music_photo.startAnimation(animation)
            if (position < musicList.size - 1) position++
            else position = 0
            initPlayer('n')
        }
        img_previous.setOnClickListener {
            job.cancel()
            img_previous.startAnimation(animation)
            img_music_photo.startAnimation(animation)
            if (position <= 0) position = musicList.size - 1
            else position--
            initPlayer('p')
        }
        img_shuffle.setOnClickListener {
            img_shuffle.startAnimation(animation)
            shuffle = !shuffle
            shuffledMusic = musicList
            shuffledMusic!!.shuffle()
        }
        img_repeat.setOnClickListener {
            img_repeat.startAnimation(animation)
            repeat = !repeat
        }
    }

    override fun onPause() {
        job.cancel()
        pause()
        super.onPause()
    }

    private fun initPlayer(skip: Char = 'x') {
        if (skip != 'x') {
            if (getMusic().id in (activity as MainActivity).queue) {
               if (skip == 'n') {
                   if (position < musicList.size - 1) position++
                   else position = 0
                   initPlayer('n')
               } else {
                   if (position <= 0) position = musicList.size - 1
                   else position--
                   initPlayer('p')
               }
               return
            }
        }
        job = Job()
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
        }

        mediaPlayer = MediaPlayer.create(requireContext(), Uri.parse(getMusic().path))
        mediaPlayer!!.start()

        setUI()

        mediaPlayer!!.setOnCompletionListener {
            if (!repeat) {
                if (position < musicList.size - 1) position++
                else position = 0
            }
            initPlayer()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer!!.seekTo(progress)
                    seekBar!!.progress = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        musicProgress()
    }

    private fun musicProgress() = GlobalScope.launch {
        job = launch {
            while (mediaPlayer != null) {
                try {
                    if (mediaPlayer!!.isPlaying) {
                        val msg = Message()
                        msg.what = mediaPlayer!!.currentPosition
                        handler.sendMessage(msg)
                        delay(1000)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (txt_current_time != null) {
                val currentPosition: Int = msg.what
                seekBar.progress = currentPosition
                val time = createTimeLabel(currentPosition)
                txt_current_time.text = time
            }
        }
    }

    private fun albumMusic(): ArrayList<Music> {
        val albumMusicList = ArrayList<Music>()
        for (music in (activity as MainActivity).musicList)
            if (music.album == album)
                albumMusicList.add(music)
        return albumMusicList
    }

    private fun playListMusic(): ArrayList<Music> {
        val playListMusicList = ArrayList<Music>()
        for (music in playlist[playListPosition!!.toInt()].musics)
            playListMusicList.add(music)
        return playListMusicList
    }

    private fun favListMusic(): ArrayList<Music> {
        val favListMusic = ArrayList<Music>()
        for (music in (activity as MainActivity).musicList)
            if (music.fav)
                favListMusic.add(music)
        return favListMusic
    }

    private fun setUI() {
        if (getMusic().art != null) {
            val bitmap = BitmapFactory.decodeByteArray(getMusic().art, 0, getMusic().art!!.size)
            img_music_photo.setImageBitmap(bitmap)
        } else img_music_photo.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_music))
        createTimeLabel(mediaPlayer!!.duration)
        seekBar.max = mediaPlayer!!.duration
        txt_total_time.text = createTimeLabel(mediaPlayer!!.duration)
        txt_song_title.text = getMusic().title
    }

    private fun play() {
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
            img_play.setImageResource(R.drawable.ic_pause)
        } else pause()
    }

    private fun pause() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            img_play.setImageResource(R.drawable.ic_play)
        }
    }

    private fun getMusic(): Music {
        return if (!shuffle) musicList[position]
        else shuffledMusic!![position]
    }
}