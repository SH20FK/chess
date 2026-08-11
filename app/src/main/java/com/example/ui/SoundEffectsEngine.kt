package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

object SoundEffectsEngine {

    private val sampleRate = 22050

    fun playMoveSound() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val numSamples = sampleRate / 10 // 100ms
                val buffer = ShortArray(numSamples)
                val freq = 440.0
                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    val envelope = (1.0 - (i.toDouble() / numSamples))
                    val wave = sin(2.0 * Math.PI * freq * time)
                    buffer[i] = (wave * 12000 * envelope).toInt().toShort()
                }
                playBuffer(buffer)
            } catch (_: Exception) {}
        }
    }

    fun playCaptureSound() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val numSamples = sampleRate / 6 // 160ms
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 600.0 - (progress * 400.0)
                    val wave = sin(2.0 * Math.PI * freq * (i.toDouble() / sampleRate))
                    val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.3
                    val envelope = 1.0 - progress
                    buffer[i] = ((wave + noise) * 16000 * envelope).toInt().toShort()
                }
                playBuffer(buffer)
            } catch (_: Exception) {}
        }
    }

    fun playNukeExplosionSound() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val numSamples = sampleRate * 1 // 1 second explosion
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val noise = (Random.nextDouble() * 2.0 - 1.0)
                    val rumbleFreq = 80.0 - (progress * 50.0)
                    val rumble = sin(2.0 * Math.PI * rumbleFreq * (i.toDouble() / sampleRate))
                    val envelope = Math.pow(1.0 - progress, 1.5)
                    val valSample = (noise * 0.7 + rumble * 0.3) * envelope
                    buffer[i] = (valSample * 28000).toInt().coerceIn(-32767, 32767).toShort()
                }
                playBuffer(buffer)
            } catch (_: Exception) {}
        }
    }

    fun playAirstrikeSound() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val numSamples = (sampleRate * 0.8).toInt()
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 120.0 + (sin(progress * Math.PI) * 500.0)
                    val wave = sin(2.0 * Math.PI * freq * (i.toDouble() / sampleRate))
                    val envelope = sin(progress * Math.PI)
                    buffer[i] = (wave * 18000 * envelope).toInt().toShort()
                }
                playBuffer(buffer)
            } catch (_: Exception) {}
        }
    }

    private fun playBuffer(buffer: ShortArray) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
    }
}
