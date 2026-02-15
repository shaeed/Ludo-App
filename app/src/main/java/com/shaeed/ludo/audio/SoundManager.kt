package com.shaeed.ludo.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class GameSound {
    DICE_ROLL,
    TOKEN_MOVE,
    TOKEN_CAPTURE,
    TOKEN_HOME,
    GAME_WIN
}

class SoundManager {

    private var enabled = true

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun isEnabled(): Boolean = enabled

    suspend fun play(sound: GameSound) {
        if (!enabled) return
        withContext(Dispatchers.IO) {
            val samples = when (sound) {
                GameSound.DICE_ROLL -> generateDiceRoll()
                GameSound.TOKEN_MOVE -> generateTokenMove()
                GameSound.TOKEN_CAPTURE -> generateTokenCapture()
                GameSound.TOKEN_HOME -> generateTokenHome()
                GameSound.GAME_WIN -> generateGameWin()
            }
            playPcm(samples)
        }
    }

    private fun playPcm(samples: ShortArray) {
        val sampleRate = SAMPLE_RATE
        val bufferSize = samples.size * 2 // 2 bytes per short
        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(maxOf(bufferSize, minBuffer))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(samples, 0, samples.size)
        track.play()
        // Wait for playback to complete
        val durationMs = (samples.size * 1000L) / SAMPLE_RATE
        Thread.sleep(durationMs + 50)
        track.stop()
        track.release()
    }

    // Short click sound ~40ms
    private fun generateTokenMove(): ShortArray {
        val duration = 0.04f
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = 1f - (t / duration)
            val wave = sin(2.0 * PI * 800.0 * t).toFloat()
            samples[i] = (wave * envelope * AMPLITUDE * 0.5f).toInt().toShort()
        }
        return samples
    }

    // Rattling dice sound ~200ms
    private fun generateDiceRoll(): ShortArray {
        val duration = 0.2f
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = 1f - (t / duration)
            // Frequency sweep from 300 to 600 Hz
            val freq = 300.0 + 300.0 * (t / duration)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val wave = sin(phase).toFloat()
            // Add some noise-like harmonics
            val harmonic = sin(phase * 2.7).toFloat() * 0.3f
            samples[i] = ((wave + harmonic) * envelope * AMPLITUDE * 0.4f).toInt().toShort()
        }
        return samples
    }

    // Impact sound ~150ms
    private fun generateTokenCapture(): ShortArray {
        val duration = 0.15f
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = exp(-8.0 * t).toFloat()
            // Low thud with overtones
            val wave = sin(2.0 * PI * 200.0 * t).toFloat()
            val overtone = sin(2.0 * PI * 450.0 * t).toFloat() * 0.4f
            samples[i] = ((wave + overtone) * envelope * AMPLITUDE * 0.6f).toInt().toShort()
        }
        return samples
    }

    // Ascending chime ~300ms
    private fun generateTokenHome(): ShortArray {
        val duration = 0.3f
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = (1f - (t / duration)) * 0.8f
            // Rising tone from 400 to 800
            val freq = 400.0 + 400.0 * (t / duration)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            val wave = sin(phase).toFloat()
            val harmonic = sin(phase * 2.0).toFloat() * 0.3f
            samples[i] = ((wave + harmonic) * envelope * AMPLITUDE * 0.5f).toInt().toShort()
        }
        return samples
    }

    // Victory fanfare ~500ms — two rising tones
    private fun generateGameWin(): ShortArray {
        val duration = 0.5f
        val numSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(numSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val envelope = (1f - (t / duration))
            // Two-note fanfare
            val freq1 = if (t < 0.25f) 523.0 else 659.0 // C5 -> E5
            val freq2 = if (t < 0.25f) 659.0 else 784.0 // E5 -> G5
            phase1 += 2.0 * PI * freq1 / SAMPLE_RATE
            phase2 += 2.0 * PI * freq2 / SAMPLE_RATE
            val wave = sin(phase1).toFloat() * 0.5f + sin(phase2).toFloat() * 0.5f
            samples[i] = (wave * envelope * AMPLITUDE * 0.5f).toInt().toShort()
        }
        return samples
    }

    companion object {
        private const val SAMPLE_RATE = 22050
        private const val AMPLITUDE = 16000f
    }
}
