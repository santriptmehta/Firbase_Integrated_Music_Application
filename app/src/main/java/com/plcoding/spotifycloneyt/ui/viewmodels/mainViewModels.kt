package com.plcoding.spotifycloneyt.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import com.plcoding.spotifycloneyt.exoPlayer.MusicServiceConnection

class mainViewModels @ViewModelInject constructor(
    private val musicServiceConnection: MusicServiceConnection
) {

}