package edu.blazepose.fallencheck.notice

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import edu.blazepose.fallencheck.util.shortToast

/**
 * 警报
 *
 * 使用 MediaPlayer 播放 Ringtone 模拟
 */
@RequiresApi(Build.VERSION_CODES.M)
class AlertViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "Alert ViewModel"
    }

    private val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) // 铃声
    private val audioManager: AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private val maxVolume: Int // 最大音量

    init {
        audioManager = application.applicationContext
            .getSystemService(Context.AUDIO_SERVICE) as AudioManager
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
        setVolume() // 音量设置
        playerReset() // 播放器初始化
    }

    /**
     * 设置音量
     */
    private fun setVolume(volume: Int = maxVolume) {
        if (volume in 0..maxVolume) {
            try {
                audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, 0) // 设置铃声为最大值
            } catch (se: SecurityException) {
                val msg = "铃声音量设置异常: ${se.localizedMessage}"
                Log.e(TAG, msg, se)
                shortToast(getApplication(), msg)
            }
        }
    }

    /**
     * 重设播放器
     */
    private fun playerReset() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(
            getApplication(),
            defaultUri // 铃声
        ).apply {
            setOnCompletionListener { play() }
            setOnErrorListener { _, what, extra ->
                val w = if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
                    "Unknown Error"
                } else {
                    "Server Died"
                }
                val e = when (extra) {
                    MediaPlayer.MEDIA_ERROR_IO -> "IO Error"
                    MediaPlayer.MEDIA_ERROR_MALFORMED -> "Malformed Error"
                    MediaPlayer.MEDIA_ERROR_UNSUPPORTED -> "Unsupported Error"
                    MediaPlayer.MEDIA_ERROR_TIMED_OUT -> "Time Out"
                    else -> "Other"
                }
                Log.e(TAG, "Media Error: what->{$w}, extra->{$e}")
                shortToast(getApplication(), "播放器异常")
                playerReset()
                true
            }
            isLooping = true
        }
        setVolume()
    }

    /**
     * 播放
     */
    fun play() {
        mediaPlayer?.let { if (!it.isPlaying) it.start() }
    }

    /**
     * 停止警报
     */
    fun stop() {
        mediaPlayer?.stop()
        playerReset()
    }

    /**
     * Activity Destory 时, 释放资源
     */
    fun destory() {
        stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}