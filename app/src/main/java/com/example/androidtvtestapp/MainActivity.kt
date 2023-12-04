package com.example.androidtvtestapp

//import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
//import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
//import com.google.android.exoplayer2.source.MediaSource
//import com.google.android.exoplayer2.source.ProgressiveMediaSource
//import com.google.android.exoplayer2.source.ads.AdsMediaSource
//import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
//import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
//import com.google.android.exoplayer2.trackselection.TrackSelection
//import com.google.android.exoplayer2.ui.PlayerView
//import com.google.android.exoplayer2.upstream.DataSource
//import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
//import com.google.android.exoplayer2.util.Util
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.example.androidtvlibrary.main.TVTestLibrary
import com.example.androidtvlibrary.main.adapter.DataSource
import com.example.androidtvlibrary.main.adapter.ImaAdsLoaderTest
import com.example.androidtvlibrary.main.adapter.Media.ProgressiveMediaSource
import com.example.androidtvlibrary.main.adapter.PlayerView
import com.example.androidtvlibrary.main.adapter.TrackSelection
import com.example.androidtvlibrary.main.adapter.Util
import com.example.androidtvlibrary.main.adapter.ads.AdsMediaSource
import com.example.androidtvlibrary.main.adapter.factory.DefaultDataSourceFactory
import com.example.androidtvlibrary.main.adapter.wow.AdaptiveTrackSelection
import com.example.androidtvlibrary.main.adapter.wow.DefaultTrackSelector
import com.example.androidtvlibrary.main.adapter.wow.MediaSource
import com.example.androidtvlibrary.main.adapter.wow.SimpleWowPlayer
import com.example.androidtvlibrary.main.adapter.wow.TrackSelector
import com.google.ads.interactivemedia.v3.api.AdErrorEvent
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import java.util.Arrays


/**
 * Loads [MainFragment].
 */
class MainActivity : FragmentActivity() {

    private val SAMPLE_VIDEO_URL = "https://storage.googleapis.com/gvabox/media/samples/stock.mp4"
    private val SAMPLE_VAST_TAG_URL =
        "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_ad_samples&sz=640x480&cust_params=sample_ct%3Dredirectlinear&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator="

    private var adsPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commitNow()
        }

//        val adsLoader = ImaAdsLoader(this, Uri.parse(SAMPLE_VAST_TAG_URL))
//        adsLoader.adsLoader


//        val playerView = findViewById<PlayerView>(R.id.video_view)
//        val videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory()
//        val trackSelector = DefaultTrackSelector(this, videoTrackSelectionFactory)
//        val player = SimpleWowPlayer.Builder(this).setTrackSelector(trackSelector).build()
//        Log.e("aaaaaaa", player.toString())
//        val adsLoader = ImaAdsLoaderTest(this, Uri.parse(SAMPLE_VAST_TAG_URL))
//        playerView.player = player
//        Log.e("aaaaaaa", (player == null).toString())
//
//        adsLoader.setPlayer(player)
//        val sourceFactory: DataSource.Factory = DefaultDataSourceFactory(
//            this,
//            Util.getUserAgent(this, "appname")
//        )
//        var contentSource: MediaSource?
//
//        val contentSourceFactory = ProgressiveMediaSource.Factory(sourceFactory)
//        contentSource = contentSourceFactory.createMediaSource(Uri.parse(SAMPLE_VIDEO_URL))
//        contentSource =
//            AdsMediaSource(contentSource, sourceFactory, adsLoader, playerView)
//
//        player.prepare(contentSource)
//        player.playWhenReady = true
//
//        adsLoader.adsLoader.addAdsLoadedListener { adsManagerLoadedEvent: AdsManagerLoadedEvent ->
//            // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
//            // events for ad playback and errors.
//            var adsManager = adsManagerLoadedEvent.adsManager
//
//            // Attach event and error event listeners.
//            adsManager.addAdErrorListener(
//                AdErrorEvent.AdErrorListener { adErrorEvent: AdErrorEvent ->
//                    /** An event raised when there is an error loading or playing ads.   */
//                    /** An event raised when there is an error loading or playing ads.   */
//                    Log.e(
//                        "LOGTAG",
//                        "Ad Error: " + adErrorEvent.error.message
//                    )
//                    val universalAdIds: String =
//                        Arrays.toString(
//                            adsManager.getCurrentAd().getUniversalAdIds()
//                        )
//                    Log.i(
//                        "LOGTAG",
//                        "Discarding the current ad break with universal "
//                                + "ad Ids: "
//                                + universalAdIds
//                    )
//                    adsManager.discardAdBreak()
//                }
//            )
//            adsManager.addAdEventListener(
//                AdEvent.AdEventListener { adEvent: AdEvent ->
//                    /** Responds to AdEvents.   */
//                    if (adEvent.type != AdEvent.AdEventType.AD_PROGRESS) {
//                        Log.i("LOGTAG", "Event: " + adEvent.type)
//                    }
//                    if (adEvent.type == AdEvent.AdEventType.LOADED) {
//                        // AdEventType.LOADED is fired when ads are ready to play.
//
//                        // This sample app uses the sample tag
//                        // single_preroll_skippable_ad_tag_url that requires calling
//                        // AdsManager.start() to start ad playback.
//                        // If you use a different ad tag URL that returns a VMAP or
//                        // an ad rules playlist, the adsManager.init() function will
//                        // trigger ad playback automatically and the IMA SDK will
//                        // ignore the adsManager.start().
//                        // It is safe to always call adsManager.start() in the
//                        // LOADED event.
//                        adsManager.start()
//                    } else if (adEvent.type == AdEvent.AdEventType.ALL_ADS_COMPLETED) {
//
//                        // Calling adsManager.destroy() triggers the function
//                        // VideoAdPlayer.release().
//                        adsManager.destroy()
//                        adsManager = null
//                    } else if (adEvent.type == AdEvent.AdEventType.TAPPED) {
//                        player.playWhenReady = !adsPaused
//                        adsPaused = !adsPaused
//                    } else if (adEvent.type == AdEvent.AdEventType.CLICKED) {
//                        player.playWhenReady = !adsPaused
//                        adsPaused = !adsPaused
//                    } else {
//                    }
//                }
//            )
//            val adsRenderingSettings =
//                ImaSdkFactory.getInstance()
//                    .createAdsRenderingSettings()
//            adsManager.init(adsRenderingSettings)
//        }
    }

}