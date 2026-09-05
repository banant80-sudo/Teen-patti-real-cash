package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural low-latency sound effects engine for Teen Patti gameplay actions.
 * Synthesizes 16-bit PCM audio in-memory for zero external asset dependencies,
 * sub-10ms trigger latency, and zero network requirements.
 */
object GameSoundManager {

    private const val SAMPLE_RATE = 22050

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private var dealTrack: AudioTrack? = null
    private var betTrack: AudioTrack? = null
    private var winTrack: AudioTrack? = null
    private var foldTrack: AudioTrack? = null
    private var showdownTrack: AudioTrack? = null

    private var isInitialized = false
    private val audioScope = CoroutineScope(Dispatchers.Default)

    init {
        initTracks()
    }

    @Synchronized
    fun initTracks() {
        if (isInitialized) return
        try {
            dealTrack = createTrack(generateCardDealPcm())
            betTrack = createTrack(generateChipBetPcm())
            winTrack = createTrack(generateWinFanfarePcm())
            foldTrack = createTrack(generateFoldPcm())
            showdownTrack = createTrack(generateShowdownPcm())
            isInitialized = true
        } catch (e: Exception) {
            // Fallback gracefully if audio hardware is restricted in environment
            e.printStackTrace()
        }
    }

    fun toggleSound(): Boolean {
        val newState = !_isSoundEnabled.value
        _isSoundEnabled.value = newState
        return newState
    }

    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
    }

    fun playDealCard() {
        if (!_isSoundEnabled.value) return
        audioScope.launch {
            safePlayTrack(dealTrack)
        }
    }

    fun playBetChips() {
        if (!_isSoundEnabled.value) return
        audioScope.launch {
            safePlayTrack(betTrack)
        }
    }

    fun playWinFanfare() {
        if (!_isSoundEnabled.value) return
        audioScope.launch {
            safePlayTrack(winTrack)
        }
    }

    fun playFold() {
        if (!_isSoundEnabled.value) return
        audioScope.launch {
            safePlayTrack(foldTrack)
        }
    }

    fun playShowdown() {
        if (!_isSoundEnabled.value) return
        audioScope.launch {
            safePlayTrack(showdownTrack)
        }
    }

    @Synchronized
    private fun safePlayTrack(track: AudioTrack?) {
        try {
            track?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.reloadStaticData()
                it.play()
            }
        } catch (e: Exception) {
            // Ignore playback interruptions safely
        }
    }

    private fun createTrack(pcmBytes: ByteArray): AudioTrack {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        val track = AudioTrack(
            attributes,
            format,
            pcmBytes.size,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        track.write(pcmBytes, 0, pcmBytes.size)
        return track
    }

    /**
     * Synthesizes a snappy card deal / slide across casino felt.
     * Duration: ~90ms. Filtered burst of paper friction with snappy attack.
     */
    private fun generateCardDealPcm(): ByteArray {
        val durationMs = 90
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val shorts = ShortArray(numSamples)
        val random = Random(1234)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val progress = i.toFloat() / numSamples
            val decay = exp(-progress * 10.0) // sharp fadeout

            // Card friction noise + low body thump
            val noise = (random.nextFloat() * 2f - 1f)
            val thump = sin(2.0 * Math.PI * 480.0 * t).toFloat()
            val snap = sin(2.0 * Math.PI * 1800.0 * t).toFloat() * (1f - progress)

            val mixed = (noise * 0.55f + thump * 0.25f + snap * 0.20f) * decay
            shorts[i] = (mixed * 26000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortsToByteArray(shorts)
    }

    /**
     * Synthesizes authentic ceramic casino chips clinking together.
     * Duration: ~140ms. Two rapid resonant strikes with high metal/ceramic decay.
     */
    private fun generateChipBetPcm(): ByteArray {
        val durationMs = 140
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val shorts = ShortArray(numSamples)

        val strike1Offset = 0
        val strike2Offset = (SAMPLE_RATE * 0.024f).toInt() // second chip click 24ms later

        for (i in 0 until numSamples) {
            var sample = 0.0

            // Strike 1
            if (i >= strike1Offset) {
                val t1 = (i - strike1Offset).toFloat() / SAMPLE_RATE
                val env1 = exp(-t1 * 55.0)
                val tone1 = sin(2.0 * Math.PI * 2650.0 * t1) * 0.6 + sin(2.0 * Math.PI * 3720.0 * t1) * 0.4
                sample += tone1 * env1
            }

            // Strike 2 (ricochet chip)
            if (i >= strike2Offset) {
                val t2 = (i - strike2Offset).toFloat() / SAMPLE_RATE
                val env2 = exp(-t2 * 60.0)
                val tone2 = sin(2.0 * Math.PI * 2950.0 * t2) * 0.5 + sin(2.0 * Math.PI * 4100.0 * t2) * 0.5
                sample += tone2 * env2 * 0.75
            }

            shorts[i] = (sample * 24000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortsToByteArray(shorts)
    }

    /**
     * Synthesizes a celebratory casino winning fanfare: C5 -> E5 -> G5 -> C6 chime chord.
     * Duration: ~850ms with rich harmonic decay.
     */
    private fun generateWinFanfarePcm(): ByteArray {
        val durationMs = 850
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val shorts = ShortArray(numSamples)

        // Frequencies in Hz: C5, E5, G5, C6
        val notes = listOf(523.25, 659.25, 783.99, 1046.50)
        val noteDurSamples = (SAMPLE_RATE * 0.14).toInt()

        for (i in 0 until numSamples) {
            var sample = 0.0

            for (n in notes.indices) {
                val noteStart = n * noteDurSamples
                if (i >= noteStart) {
                    val t = (i - noteStart).toFloat() / SAMPLE_RATE
                    val noteDuration = (numSamples - noteStart).toFloat() / SAMPLE_RATE
                    val env = exp(-t * (if (n == notes.lastIndex) 3.5 else 6.0))

                    val freq = notes[n]
                    // Fundamental + subtle octave overtone for bell clarity
                    val tone = sin(2.0 * Math.PI * freq * t) * 0.75 +
                               sin(2.0 * Math.PI * freq * 2.0 * t) * 0.25
                    sample += tone * env * 0.55
                }
            }

            shorts[i] = (sample * 25000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortsToByteArray(shorts)
    }

    /**
     * Synthesizes a gentle fold / cards conceded woosh.
     * Duration: ~110ms. Soft pitch downward glide.
     */
    private fun generateFoldPcm(): ByteArray {
        val durationMs = 110
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val shorts = ShortArray(numSamples)
        val random = Random(5678)

        for (i in 0 until numSamples) {
            val progress = i.toFloat() / numSamples
            val t = i.toFloat() / SAMPLE_RATE
            val env = sin(Math.PI * progress).toFloat() // smooth envelope
            val freq = 550.0 - progress * 320.0 // downward sweep
            val tone = sin(2.0 * Math.PI * freq * t).toFloat()
            val noise = (random.nextFloat() * 2f - 1f) * 0.35f

            val mixed = (tone * 0.65f + noise) * env
            shorts[i] = (mixed * 20000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortsToByteArray(shorts)
    }

    /**
     * Synthesizes a tense showdown bell alert.
     * Duration: ~280ms. Dual harmonious chime with ring.
     */
    private fun generateShowdownPcm(): ByteArray {
        val durationMs = 280
        val numSamples = (SAMPLE_RATE * durationMs) / 1000
        val shorts = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toFloat() / SAMPLE_RATE
            val env = exp(-t * 8.0)
            val bell1 = sin(2.0 * Math.PI * 880.0 * t) * 0.6
            val bell2 = sin(2.0 * Math.PI * 1320.0 * t) * 0.4
            val sample = (bell1 + bell2) * env
            shorts[i] = (sample * 23000).toInt().coerceIn(-32767, 32767).toShort()
        }
        return shortsToByteArray(shorts)
    }

    private fun shortsToByteArray(shorts: ShortArray): ByteArray {
        val bytes = ByteArray(shorts.size * 2)
        for (i in shorts.indices) {
            val s = shorts[i].toInt()
            bytes[i * 2] = (s and 0x00FF).toByte()
            bytes[i * 2 + 1] = ((s shr 8) and 0x00FF).toByte()
        }
        return bytes
    }
}
