package com.example.androidtvlibrary.main;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.androidtvlibrary.main.adapter.AdsLoaderTest;
import com.example.androidtvlibrary.main.adapter.DataSource;
import com.example.androidtvlibrary.main.adapter.ImaAdsLoaderTest;
import com.example.androidtvlibrary.main.adapter.Media.ProgressiveMediaSource;
import com.example.androidtvlibrary.main.adapter.PlayerView;
import com.example.androidtvlibrary.main.adapter.TrackSelection;
import com.example.androidtvlibrary.main.adapter.Util;
import com.example.androidtvlibrary.main.adapter.VideoAdPlayerAdapter;
import com.example.androidtvlibrary.main.adapter.ads.AdsMediaSource;
import com.example.androidtvlibrary.main.adapter.factory.DefaultDataSourceFactory;
import com.example.androidtvlibrary.main.adapter.wow.AdaptiveTrackSelection;
import com.example.androidtvlibrary.main.adapter.wow.DefaultTrackSelector;
import com.example.androidtvlibrary.main.adapter.wow.MediaSource;
import com.example.androidtvlibrary.main.adapter.wow.SimpleWowPlayer;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsRenderingSettings;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;

import java.util.Arrays;

public class TVTestLibrary {

    /**
     * IMA sample tag for a single skippable inline video ad. See more IMA sample tags at
     * https://developers.google.com/interactive-media-ads/docs/sdks/html5/client-side/tags
     */
//    private static String SAMPLE_VAST_TAG_URL =
//            "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_ad_samples&sz=640x480&cust_params=sample_ct%3Dredirectlinear&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=";

//    private static String SAMPLE_VAST_TAG_URL ="https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/nonlinear_ad_samples&sz=480x70&cust_params=sample_ct%3Dnonlinear&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=";

    private static String SAMPLE_VAST_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_preroll_skippable&sz=640x480&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=";
    private String SAMPLE_VIDEO_URL = "https://storage.googleapis.com/gvabox/media/samples/stock.mp4";

    private VideoView videoPlayer;
    private MediaController mediaController;
    private View playButton;
    private VideoAdPlayerAdapter videoAdPlayerAdapter;
    // The saved content position, used to resumed content following an ad break.
    private int savedPosition = 0;
    // Factory class for creating SDK objects.
    private ImaSdkFactory sdkFactory;

    // The AdsLoader instance exposes the requestAds method.
    private AdsLoader adsLoader;

    // AdsManager exposes methods to control ad playback and listen to ad events.
    private AdsManager adsManager;

    private boolean adsPaused;

    static public Uri getAdsUrl() {
        return Uri.parse("https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose.mp4");
    }

    public TVTestLibrary getInstance() {
        return new TVTestLibrary();
    }

    public void adAdsLoader(
            PlayerView playerView,
//            PlayerView videoPlayer,
//            ViewGroup videoPlayerContainer,
            Context context,
            PauseCallback pauseCallback,
            ResumeCallback resumeCallback
    ) {
//        this.videoPlayer = videoPlayer;
//        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
//        this.videoAdPlayerAdapter = new VideoAdPlayerAdapter(videoPlayer, audioManager);
//        sdkFactory = ImaSdkFactory.getInstance();
//
//        AdDisplayContainer adDisplayContainer =
//                ImaSdkFactory.createAdDisplayContainer(videoPlayerContainer, videoAdPlayerAdapter);
//
//        // Create an AdsLoader.
//        ImaSdkSettings settings = sdkFactory.createImaSdkSettings();
////        adsLoader = sdkFactory.createAdsLoader(context, settings, adDisplayContainer);
////        ImaAdsLoaderTest adsLoader = new ImaAdsLoaderTest(context, Uri.parse(SAMPLE_VAST_TAG_URL));


        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(context, videoTrackSelectionFactory);
        SimpleWowPlayer player = new SimpleWowPlayer.Builder(context).setTrackSelector(trackSelector).build();
        Log.e("aaaaaaa", player.toString());
        AdsLoaderTest adsLoader = new ImaAdsLoaderTest(context, Uri.parse(SAMPLE_VAST_TAG_URL));
        playerView.setPlayer(player);
        Log.e("aaaaaaa", String.valueOf((player == null)));

        adsLoader.setPlayer(player);
        DataSource.Factory sourceFactory = new DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, "appname")
        );
        MediaSource contentSource;

        ProgressiveMediaSource.Factory contentSourceFactory = new ProgressiveMediaSource.Factory(sourceFactory);
        contentSource = contentSourceFactory.createMediaSource(Uri.parse(SAMPLE_VIDEO_URL));
        contentSource = new
                AdsMediaSource(contentSource, sourceFactory, adsLoader, playerView);

        player.prepare(contentSource);
        player.setPlayWhenReady(true);
//
////        videoPlayer.player = player;
//        adsLoader.setPlayer(player);
//        DataSource.Factory sourceFactory = new DefaultDataSourceFactory(
//            context,
//            Util.getUserAgent(context, "appname")
//        );
//        MediaSource contentSource;
//
//        ProgressiveMediaSource.Factory contentSourceFactory = new ProgressiveMediaSource.Factory(sourceFactory);
//        contentSource = contentSourceFactory.createMediaSource(Uri.parse(SAMPLE_VIDEO_URL));
//        contentSource =
//            new AdsMediaSource(contentSource, sourceFactory, adsLoader, videoPlayer);
//
//        player.prepare(contentSource);
//        player.playWhenReady = true;

//        // When the play button is clicked, request ads and hide the button.
//        playButton = findViewById(R.id.playButton)
//        playButton!!.setOnClickListener { view: View ->
//                videoPlayer!!.setVideoPath(SAMPLE_VIDEO_URL)
//            videoPlayer!!.start()
//            Log.e("AAAAAA", "setOnClickListener0")
//            requestAds(SAMPLE_VAST_TAG_URL)
//            Log.e("AAAAAA", "setOnClickListener1")
//            view.visibility = View.GONE
//        }
        // Add listeners for when ads are loaded and for errors.
        this.adsLoader = ((ImaAdsLoaderTest) adsLoader).getAdsLoader();
        ((ImaAdsLoaderTest) adsLoader).getAdsLoader().addAdErrorListener(adErrorEvent -> {
            /** An event raised when there is an error loading or playing ads.  */
            Log.i("AAAAAA$LOGTAG", "Ad Error: " + adErrorEvent.getError().getMessage());
            resumeContent();
        });
        ((ImaAdsLoaderTest) adsLoader).getAdsLoader().addAdsLoadedListener(adsManagerLoadedEvent ->
                {
                    // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
                    // events for ad playback and errors.
                    Log.e("AAAAAA", "Ads were successfully loaded");
                    adsManager = adsManagerLoadedEvent.getAdsManager();

                    // Attach event and error event listeners.
                    adsManager.addAdErrorListener(
                            adErrorEvent ->
                            {
                                /** An event raised when there is an error loading or playing ads.  */
                                /** An event raised when there is an error loading or playing ads.  */
                                Log.e("LOGTAG", "Ad Error: " + adErrorEvent.getError().getMessage());
                                String universalAdIds =
                                        Arrays.toString(adsManager.getCurrentAd().getUniversalAdIds());
                                Log.i(
                                        "LOGTAG",
                                        "Discarding the current ad break with universal "
                                                + "ad Ids: "
                                                + universalAdIds
                                );
                                adsManager.discardAdBreak();
                            }
                    );

                    adsManager.addAdEventListener(adEvent -> {

                                /** Responds to AdEvents.  */
                                if (adEvent.getType() != AdEvent.AdEventType.AD_PROGRESS) {
                                    Log.i("LOGTAG", "Event: " + adEvent.getType());
                                }

                                if (adEvent.getType() == AdEvent.AdEventType.LOADED) {
                                    // AdEventType.LOADED is fired when ads are ready to play.

                                    // This sample app uses the sample tag
                                    // single_preroll_skippable_ad_tag_url that requires calling
                                    // AdsManager.start() to start ad playback.
                                    // If you use a different ad tag URL that returns a VMAP or
                                    // an ad rules playlist, the adsManager.init() function will
                                    // trigger ad playback automatically and the IMA SDK will
                                    // ignore the adsManager.start().
                                    // It is safe to always call adsManager.start() in the
                                    // LOADED event.
                                    adsManager.start();
                                } else if (adEvent.getType() == AdEvent.AdEventType.CONTENT_PAUSE_REQUESTED) {
//                                    player.setPlayWhenReady(false);
                                    // AdEventType.CONTENT_PAUSE_REQUESTED is fired when you
                                    // should pause your content and start playing an ad.
//                                    pauseContentForAds();
                                    pauseCallback.onPauseCall();
                                } else if (adEvent.getType() == AdEvent.AdEventType.CONTENT_RESUME_REQUESTED) {
                                    player.setPlayWhenReady(false);
                                    // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad
                                    // you should play your content.
//                                    resumeContent();
//                                    resumeCallback.onResumeCall();
//                                    mediaPlayer.playWhenPrepared();
                                } else if (adEvent.getType() == AdEvent.AdEventType.ALL_ADS_COMPLETED) {
                                    player.setPlayWhenReady(false);
                                    // Calling adsManager.destroy() triggers the function
                                    // VideoAdPlayer.release().
                                    playerView.setVisibility(View.GONE);
                                    adsManager.destroy();
                                    adsManager = null;
                                    resumeCallback.onResumeCall();
//                                    mediaPlayer.playWhenPrepared();

                                } else if (adEvent.getType() == AdEvent.AdEventType.TAPPED) {
                                    player.setPlayWhenReady(!adsPaused);
                                    adsPaused = !adsPaused;
                                } else {
                                }
                            }

                    );
                    AdsRenderingSettings adsRenderingSettings = ImaSdkFactory.getInstance().createAdsRenderingSettings();
                    adsManager.init(adsRenderingSettings);
                }
        );
//        AdsRequest request = sdkFactory.createAdsRequest();
//        request.setAdTagUrl(SAMPLE_VAST_TAG_URL);
//        request.setContentProgressProvider(() -> {
//            if (videoPlayer.getDuration() <= 0) {
//                return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
//            } else {
//                return new VideoProgressUpdate(
//                        videoPlayer.getCurrentPosition(), videoPlayer.getDuration());
//            }
//        });
//        request.setAdWillAutoPlay(true);
//        ((ImaAdsLoaderTest) adsLoader).requestAds(request);
//        requestAds(SAMPLE_VAST_TAG_URL, videoPlayer);
    }

    private void requestAds(String adTagUrl, VideoView videoPlayer) {
        // Create the ads request.
        AdsRequest request = sdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setContentProgressProvider(() -> {
            if (videoPlayer.getDuration() <= 0) {
                return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
            } else {
                return new VideoProgressUpdate(
                        videoPlayer.getCurrentPosition(), videoPlayer.getDuration());
            }
        });
        request.setAdWillAutoPlay(true);
        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        adsLoader.requestAds(request);
    }

    private void pauseContentForAds() {
        Log.i("LOGTAG", "pauseContentForAds");
        savedPosition = videoPlayer.getCurrentPosition();
        videoPlayer.stopPlayback();
        // Hide the buttons and seek bar controlling the video view.
        videoPlayer.setMediaController(null);
    }

    private void resumeContent() {
        Log.i("LOGTAG", "resumeContent");

        // Show the buttons and seek bar controlling the video view.
        videoPlayer.setVideoPath(SAMPLE_VIDEO_URL);
        videoPlayer.setMediaController(mediaController);
        videoPlayer.setOnPreparedListener(mediaPlayer -> {
            if (savedPosition > 0) {
                mediaPlayer.seekTo(savedPosition);
            }
            mediaPlayer.start();
        });
        videoPlayer.setOnCompletionListener(
                mediaPlayer -> videoAdPlayerAdapter.notifyImaOnContentCompleted()
        );
    }
}



