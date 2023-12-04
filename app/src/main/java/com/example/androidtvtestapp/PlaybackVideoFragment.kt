package com.example.androidtvtestapp

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.PlaybackControlsRow
import com.example.androidtvlibrary.main.TVTestLibrary
import com.example.androidtvlibrary.main.adapter.PlayerView

//import com.example.androidtvlibrary.main.adapter.ImaAdsLoaderTest


class PlaybackVideoFragment : VideoSupportFragment() {

    private lateinit var mTransportControlGlue: PlaybackTransportControlGlue<MediaPlayerAdapter>
    private var savedPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (_, title, description, _, _, videoUrl, ads) =
            activity?.intent?.getSerializableExtra(DetailsActivity.MOVIE) as Movie

        val glueHost = VideoSupportFragmentGlueHost(this@PlaybackVideoFragment)


//        val mediaController = MediaController(requireActivity())

        val videoPlayer = requireActivity().findViewById<PlayerView>(R.id.videoView)

//        mediaController.setAnchorView(videoPlayer)
//        videoPlayer?.setMediaController(mediaController)
        val videoPlayerContainer =
            requireActivity().findViewById<ViewGroup>(R.id.details_screen)
        val audioManager =
            requireActivity().getSystemService(FragmentActivity.AUDIO_SERVICE) as AudioManager

        val playerAdapter = MediaPlayerAdapter(activity)
        playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)

        mTransportControlGlue = PlaybackTransportControlGlue(activity, playerAdapter)
        mTransportControlGlue.host = glueHost
        mTransportControlGlue.title = title
        mTransportControlGlue.subtitle = description
        mTransportControlGlue.playWhenPrepared()

        playerAdapter.setDataSource(Uri.parse(videoUrl))

//        TVTestLibrary().adAdsLoader(videoPlayer, context, {
//            savedPosition = playerAdapter.mediaPlayer.currentPosition
//            playerAdapter.mediaPlayer.pause()
//        },
//            {
//                this.view?.visibility = VISIBLE
////                playerAdapter.mediaPlayer.start()
//                playerAdapter.play()
//                mTransportControlGlue.playWhenPrepared()
////                playerAdapter.mediaPlayer.seekTo(savedPosition)
//            })


        playerAdapter.mediaPlayer.setOnPreparedListener { mediaPlayer: MediaPlayer ->
            run {
//                this.view?.visibility = INVISIBLE
//                videoPlayer.visibility = VISIBLE
//                TVTestLibrary().adAdsLoader(
//                    videoPlayer, context,
//                    {
////                            this.view?.visibility = INVISIBLE
////                            videoPlayer.visibility = VISIBLE
////                            Log.e("aaaaaaaa", "pause")
////                            savedPosition = playerAdapter.mediaPlayer.currentPosition
////                            mediaPlayer.pause()
//
//                    },
//                    {
////                            Log.e("aaaaaaaa", "resume")
////                            mediaPlayer.seekTo(savedPosition)
////                            playerAdapter.mediaPlayer.setOnInfoListener(null)
//                            videoPlayer.visibility = INVISIBLE
//                            this.view?.visibility = VISIBLE
////                            playerAdapter.play()
////                            mTransportControlGlue.playWhenPrepared()
//                    }
////                        ,
////                        mTransportControlGlue
//                )
                //        adsLoader = sdkFactory.createAdsLoader(context, settings, adDisplayContainer);

//                lib.adAdsLoader(
//                    mediaPlayer,
//                    videoPlayer, videoPlayerContainer, requireActivity(),
//                    {
//                        Log.e("aaaaaaaa", "pause")
////                            savedPosition = playerAdapter.mediaPlayer.currentPosition
////                            mediaPlayer.pause()
//
//                    },
//                    {
//                        Log.e("aaaaaaaa", "resume")
////                            mediaPlayer.seekTo(savedPosition)
////                            playerAdapter.mediaPlayer.setOnInfoListener(null)
//                        videoPlayer.visibility = INVISIBLE
//                        this.view?.visibility = VISIBLE
////                            playerAdapter.play()
////                            mTransportControlGlue.playWhenPrepared()
//                    },
//                    mTransportControlGlue
//                )

            }
        }
//        var isAds = true
//
        playerAdapter.mediaPlayer.setOnInfoListener { mediaPlayer: MediaPlayer, i: Int, i1: Int ->
            run {
//                if (
////                    mediaPlayer.isPlaying &&
//                    isAds) {
//                    isAds = false
////                    this.view?.visibility = INVISIBLE
////                    videoPlayer.visibility = VISIBLE
//                    TVTestLibrary().adAdsLoader(
//                        videoPlayer, context,
//                        {
////                            this.view?.visibility = INVISIBLE
////                            videoPlayer.visibility = VISIBLE
////                            Log.e("aaaaaaaa", "pause")
////                            savedPosition = playerAdapter.mediaPlayer.currentPosition
////                            mediaPlayer.pause()
//
//                        },
//                        {
////                            Log.e("aaaaaaaa", "resume")
////                            mediaPlayer.seekTo(savedPosition)
////                            playerAdapter.mediaPlayer.setOnInfoListener(null)
////                            videoPlayer.visibility = INVISIBLE
////                            this.view?.visibility = VISIBLE
////                            playerAdapter.play()
////                            mTransportControlGlue.playWhenPrepared()
//                        }
////                        ,
////                        mTransportControlGlue
//                    )
//
//                }
//                this.view?.visibility = INVISIBLE
//                videoPlayer.visibility = VISIBLE
//                TVTestLibrary().adAdsLoader(
//                    videoPlayer, context,
//                    {
////                            this.view?.visibility = INVISIBLE
////                            videoPlayer.visibility = VISIBLE
////                            Log.e("aaaaaaaa", "pause")
////                            savedPosition = playerAdapter.mediaPlayer.currentPosition
////                            mediaPlayer.pause()
//
//                    },
//                    {
////                            Log.e("aaaaaaaa", "resume")
////                            mediaPlayer.seekTo(savedPosition)
////                            playerAdapter.mediaPlayer.setOnInfoListener(null)
//                        videoPlayer.visibility = INVISIBLE
//                        this.view?.visibility = VISIBLE
////                            playerAdapter.play()
////                            mTransportControlGlue.playWhenPrepared()
//                    }
//                )
                true
            }
        }

    }

    override fun onPause() {
        super.onPause()
        mTransportControlGlue.pause()
    }
}