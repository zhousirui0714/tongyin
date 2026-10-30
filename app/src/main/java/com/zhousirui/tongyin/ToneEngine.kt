package com.zhousirui.tongyin

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

/**
 * 0.1 版的发声引擎：把一段波形算好，交给 AudioTrack 播出去。
 *
 * 这只是临时方案，目的只有一个 —— 验证这台平板能不能出声、延迟有多大。
 * 正式版会换成 Oboe + 音序器（见技术方案第四章），那时这个类会被整体替换。
 */
class ToneEngine {

    /**
     * 播放一个音。
     * 每次按都是在独立线程里新建一条音轨，所以可以多个音同时响（简易复音）。
     */
    fun play(frequency: Double, durationMs: Int = 900) {
        Thread {
            runCatching { renderAndPlay(frequency, durationMs) }
        }.apply {
            name = "tongyin-voice"
            priority = Thread.MAX_PRIORITY
        }.start()
    }

    private fun renderAndPlay(frequency: Double, durationMs: Int) {
        val total = SAMPLE_RATE * durationMs / 1000
        if (total <= 0) return

        val samples = ShortArray(total)

        // 包络：快速起音、尾部渐弱，避免开头和结尾的「咔哒」声
        val attack = 0.008 * SAMPLE_RATE
        val release = 0.180 * SAMPLE_RATE

        var phase = 0.0
        val increment = 2.0 * PI * frequency / SAMPLE_RATE

        for (i in 0 until total) {
            val envelope = when {
                i < attack -> i / attack
                i > total - release -> ((total - i) / release).coerceIn(0.0, 1.0)
                else -> 1.0
            }
            // 基波 + 两个泛音，听起来比纯正弦更像乐器
            val wave = 0.74 * sin(phase) + 0.18 * sin(2.0 * phase) + 0.08 * sin(3.0 * phase)
            samples[i] = (wave * envelope * 0.26 * Short.MAX_VALUE).toInt().toShort()
            phase += increment
        }

        val minBuffer = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(maxOf(minBuffer, 4096) * 2)
            .build()

        try {
            track.play()
            track.write(samples, 0, samples.size)
            // 等缓冲区里剩下的声音放完，否则尾音会被硬切断
            Thread.sleep(150)
        } finally {
            runCatching { track.stop() }
            runCatching { track.flush() }
            track.release()
        }
    }

    companion object {
        const val SAMPLE_RATE = 48000
    }
}
