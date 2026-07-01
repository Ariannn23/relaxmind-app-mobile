package com.relaxmind.app.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SoundPlayerManager {
    private val activePlayers = mutableMapOf<String, ExoPlayer>()
    private val loopingPreferences = mutableMapOf<String, Boolean>()
    private val _playingSoundIds = MutableStateFlow<Set<String>>(emptySet())
    val playingSoundIds: StateFlow<Set<String>> = _playingSoundIds.asStateFlow()

    private val _loopingSoundIds = MutableStateFlow<Set<String>>(emptySet())
    val loopingSoundIds: StateFlow<Set<String>> = _loopingSoundIds.asStateFlow()

    fun play(context: Context, soundId: String, resId: Int) {
        if (activePlayers.containsKey(soundId)) return
        try {
            val isLooping = getLooping(soundId)
            val player = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri("android.resource://${context.packageName}/$resId"))
                repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                prepare()
                play()
            }
            activePlayers[soundId] = player
            _playingSoundIds.value = activePlayers.keys.toSet()
            syncLoopingState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop(soundId: String) {
        activePlayers[soundId]?.release()
        activePlayers.remove(soundId)
        _playingSoundIds.value = activePlayers.keys.toSet()
        syncLoopingState()
    }

    fun setVolume(soundId: String, volume: Float) {
        activePlayers[soundId]?.volume = volume
    }

    fun getVolume(soundId: String): Float {
        return activePlayers[soundId]?.volume ?: 1.0f
    }

    fun setLooping(soundId: String, isLooping: Boolean) {
        loopingPreferences[soundId] = isLooping
        activePlayers[soundId]?.repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        syncLoopingState()
    }

    fun getLooping(soundId: String): Boolean {
        return loopingPreferences[soundId] ?: true
    }

    fun stopAll() {
        activePlayers.values.forEach { it.release() }
        activePlayers.clear()
        _playingSoundIds.value = emptySet()
        syncLoopingState()
    }

    fun isPlaying(soundId: String): Boolean = activePlayers.containsKey(soundId)

    private fun syncLoopingState() {
        _loopingSoundIds.value = activePlayers
            .filterKeys { soundId -> getLooping(soundId) }
            .keys
            .toSet()
    }
}
