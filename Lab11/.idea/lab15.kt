package com.example.audiorecordapp

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
    private val audioSampleRate = 44100
    private val audioChannel = AudioFormat.CHANNEL_IN_MONO
    private val audioEncoding = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(audioSampleRate, audioChannel, audioEncoding)

    private var audioData = ByteArray(bufferSize)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
        }

        setContent {
            AudioRecordApp()
        }
    }

    @Composable
    fun AudioRecordApp() {
        var isRecording by remember { mutableStateOf(false) }
        var isPlaying by remember { mutableStateOf(false) }
        var recordJob by remember { mutableStateOf<Job?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (!isRecording) {
                        isRecording = true
                        recordJob = startRecording()
                    } else {
                        isRecording = false
                        recordJob?.cancel()
                    }
                },
                modifier = Modifier
                    .padding(8.dp)
                    .width(200.dp)
            ) {
                Text(if (isRecording) "Stop Recording" else "Start Recording")
            }

            Button(
                onClick = {
                    if (!isPlaying) {
                        isPlaying = true
                        playAudio()
                        isPlaying = false
                    }
                },
                enabled = !isRecording,
                modifier = Modifier
                    .padding(8.dp)
                    .width(200.dp)
            ) {
                Text("Play Audio")
            }
        }
    }

    private fun startRecording() = CoroutineScope(Dispatchers.IO).launch {
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            audioSampleRate,
            audioChannel,
            audioEncoding,
            bufferSize
        )

        if (recorder.state == AudioRecord.STATE_INITIALIZED) {
            recorder.startRecording()
            while (isActive) {
                recorder.read(audioData, 0, bufferSize)
            }
            recorder.stop()
            recorder.release()
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Failed to initialize recorder", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playAudio() {
        CoroutineScope(Dispatchers.IO).launch {
            val audioTrack = AudioTrack.Builder()
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(audioSampleRate)
                        .setEncoding(audioEncoding)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()

            if (audioTrack.state == AudioTrack.STATE_INITIALIZED) {
                audioTrack.play()
                audioTrack.write(audioData, 0, audioData.size)
                audioTrack.stop()
                audioTrack.release()
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Failed to initialize audio track", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
