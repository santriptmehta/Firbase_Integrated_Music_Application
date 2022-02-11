package com.plcoding.spotifycloneyt.exoPlayer

import android.content.ComponentName
import android.content.Context
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.plcoding.spotifycloneyt.other.Constance.NETWORK_ERROR
import com.plcoding.spotifycloneyt.other.Event
import com.plcoding.spotifycloneyt.other.Resource

class MusicServiceConnection(
    context: Context
) {
    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected : LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError : LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackstate = MutableLiveData<PlaybackStateCompat?>()
    val playbackstate : LiveData<PlaybackStateCompat?> = _playbackstate

    private val _currPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val currPlayingSong : LiveData<MediaMetadataCompat?> = _currPlayingSong

    lateinit var mediaController: MediaControllerCompat

    private val mediaBrowserConnectCallback = MediaBrowserConnectCallback(context)


    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java
        ),
        mediaBrowserConnectCallback,
        null
    ).apply { connect() }


    val transportControls : MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    fun subscribe(parentID: String,callback: MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.subscribe(parentID, callback)
    }
    fun unsubscribe(parentID: String,callback: MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.unsubscribe(parentID, callback)
    }


    private inner class MediaBrowserConnectCallback(
        private val context: Context
    ): MediaBrowserCompat.ConnectionCallback(){
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(Event(Resource.error(
                "The connection was suspended",false
            )))
        }

        override fun onConnectionFailed() {
            _isConnected.postValue(Event(Resource.error(
                "Couldn't connect to media browser", false
            )))
        }
    }



    private inner class MediaControllerCallback : MediaControllerCompat.Callback(){

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackstate.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when(event){
                NETWORK_ERROR -> _networkError.postValue(
                    Event(
                        Resource.error(
                            "Couldn't Connect to server, Check internet",
                            null
                        )
                    )
                )
            }
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectCallback.onConnectionSuspended()
        }

    }
}