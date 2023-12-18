package com.example.androidtvtestapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackGlue.PlayerCallback
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.leanback.widget.PlaybackControlsRow
import com.example.androidtvlibrary.main.TVTestLibrary
import com.example.androidtvlibrary.main.adapter.PlayerView

class PlaybackVideoFragment : VideoSupportFragment() {

    private lateinit var mTransportControlGlue: PlaybackTransportControlGlue<MediaPlayerAdapter>
    private var savedPosition = 0
    private var tVTestLibrary:TVTestLibrary = TVTestLibrary()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (_, title, description, _, _, videoUrl, ads) =
            activity?.intent?.getSerializableExtra(DetailsActivity.MOVIE) as Movie

        val glueHost = VideoSupportFragmentGlueHost(this@PlaybackVideoFragment)
        val playerAdapter = MediaPlayerAdapter(activity)
        playerAdapter.setRepeatAction(PlaybackControlsRow.RepeatAction.INDEX_NONE)

        mTransportControlGlue = PlaybackTransportControlGlue(activity, playerAdapter)
        mTransportControlGlue.host = glueHost
        mTransportControlGlue.title = title
        mTransportControlGlue.subtitle = description
        mTransportControlGlue.playWhenPrepared()

        playerAdapter.setDataSource(Uri.parse(videoUrl))

        val videoPlayer = requireActivity().findViewById<PlayerView>(R.id.videoView)
        val fragment = this

        mTransportControlGlue.addPlayerCallback(object : PlayerCallback() {
            override fun onPreparedStateChanged(glue: PlaybackGlue?) {}
            override fun onPlayStateChanged(glue: PlaybackGlue?) {
                if ( fragment.isVisible && !glue?.isPlaying!! && !playerAdapter.isPlaying) {
                    tVTestLibrary.adAdsLoader(
                        videoPlayer,
                        context,
                        {
                            fragment.view?.visibility = INVISIBLE
                            savedPosition = playerAdapter.mediaPlayer.currentPosition
                            playerAdapter.mediaPlayer.pause()

                        },
                        {
                            playerAdapter.mediaPlayer.seekTo(savedPosition)
                            fragment.view?.visibility = VISIBLE
//                            playerAdapter.play()
//                            mTransportControlGlue.playWhenPrepared()
                        }
                    )
                }
            }

            override fun onPlayCompleted(glue: PlaybackGlue?) {}
        })

    }

    override fun onPause() {
        super.onPause()
        mTransportControlGlue.pause()
        tVTestLibrary.closeAdsManager()
    }
}