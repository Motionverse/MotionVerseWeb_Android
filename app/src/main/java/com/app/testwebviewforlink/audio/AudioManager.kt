package com.app.testwebviewforlink.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

object AudioManager {
    private val TAG = "AudioManager"
    private var pcmFile: File? = null
    private var wavFile: File? = null
    private var sampleRateInHz = 0
    private var channelConfig = AudioFormat.CHANNEL_IN_MONO
    private var audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private var bufferSize =
        AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
    private var audioRecord: AudioRecord? = null

    private var isRecording = false

    fun isRecording(): Boolean {
        if (audioRecord != null) {
            return isRecording
        }
        return false
    }

    /**
     * 初始化录音
     */
    fun initAudioRecord(context: Context) {
        sampleRateInHz = 16000
        channelConfig = AudioFormat.CHANNEL_IN_MONO
        audioFormat = AudioFormat.ENCODING_PCM_16BIT
        bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        Log.e(TAG, "initAudioRecord: bufferSize:$bufferSize")
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "请授予权限", Toast.LENGTH_SHORT).show()
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            bufferSize
        )
        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "initAudioRecord: audioRecord 初始化失败")
            return
        }
        initPCMFile(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun stopRecord(context: Context) {
        if (audioRecord == null) {
            Toast.makeText(context, "audioRecord is not init", Toast.LENGTH_SHORT).show()
            return
        }
        isRecording = false
        audioRecord?.stop()
        convertPcmToWav(context)
        Log.e(TAG, "stopRecord: 录制完成")
    }

    fun startRecord(context: Context) {
        if (audioRecord == null) {
            Toast.makeText(context, "audioRecord is not init", Toast.LENGTH_SHORT).show()
            return
        }
        if (pcmFile == null) {
            Toast.makeText(context, "pcmFile is null", Toast.LENGTH_SHORT).show()
            return
        }
        if (pcmFile!!.exists()) {
            Log.e(TAG, "startRecord: file is exist,delete " + pcmFile!!.delete())
        }
        isRecording = true
        val buffer = ByteArray(bufferSize)
        audioRecord?.startRecording()

        thread {
            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(pcmFile)
                while (isRecording) {
                    val readStatus = audioRecord?.read(buffer, 0, bufferSize)
                    Log.e(TAG, "startRecord readStatus: $readStatus")
                    fileOutputStream.write(buffer)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, "startRecord err:${e.message} ")
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun release(context: Context) {
        if (audioRecord == null) {
            Toast.makeText(context, "audioRecord is not init", Toast.LENGTH_SHORT).show()
            return
        }
        audioRecord?.release()
    }

    private fun initPCMFile(context: Context) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        if (dir?.exists() != true) {
            Log.e(TAG, "initPCMFile: 目录不存在，创建: ${dir?.mkdirs()}")
        }
        pcmFile = File(dir, "raw.pcm")
        Log.e(TAG, "initPCMFile: path:" + pcmFile?.absolutePath)
    }

    private fun convertPcmToWav(context: Context) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        wavFile = File(dir, "convert.wav")
        if (wavFile?.exists() == true) {
            Log.e(TAG, "convertPcmToWav: wavFile is exist. delete:" + wavFile?.delete())
        }
        var fileInputStream: FileInputStream? = null
        var fileOutputStream: FileOutputStream? = null
        try {
            fileInputStream = FileInputStream(pcmFile)
            fileOutputStream = FileOutputStream(wavFile)
            val audioByteLen = fileInputStream.channel.size()
            val wavByteLen = audioByteLen + 36
            addWavHeader(
                fileOutputStream, audioByteLen, wavByteLen, sampleRateInHz,
                channelConfig, audioFormat
            )
            val buffer = ByteArray(bufferSize)
            while (fileInputStream.read(buffer) != -1) {
                fileOutputStream.write(buffer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                fileInputStream?.close()
                fileOutputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun addWavHeader(
        fileOutputStream: FileOutputStream,
        audioByteLen: Long,
        wavByteLen: Long,
        sampleRateInHz: Int,
        channelConfig: Int,
        audioFormat: Int
    ) {
        val header = ByteArray(44)

        // RIFF/WAVE header chunk
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (wavByteLen and 0xff).toByte()
        header[5] = (wavByteLen shr 8 and 0xff).toByte()
        header[6] = (wavByteLen shr 16 and 0xff).toByte()
        header[7] = (wavByteLen shr 24 and 0xff).toByte()

        //WAVE
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // 'fmt ' chunk 4 个字节
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        // 4 bytes: size of 'fmt ' chunk（格式信息数据的大小 header[20] ~ header[35]）
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        // format = 1 编码方式
        header[20] = 1
        header[21] = 0
        // 声道数目
        val channelSize = if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2
        header[22] = channelSize.toByte()
        header[23] = 0
        // 采样频率
        header[24] = (sampleRateInHz and 0xff).toByte()
        header[25] = (sampleRateInHz shr 8 and 0xff).toByte()
        header[26] = (sampleRateInHz shr 16 and 0xff).toByte()
        header[27] = (sampleRateInHz shr 24 and 0xff).toByte()
        // 每秒传输速率
        val byteRate = audioFormat.toLong() * sampleRateInHz * channelSize
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        // block align 数据库对齐单位，每个采样需要的字节数
        header[32] = (2 * 16 / 8).toByte()
        header[33] = 0
        // bits per sample 每个采样需要的 bit 数
        header[34] = 16
        header[35] = 0

        //data chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        // pcm字节数
        header[40] = (audioByteLen and 0xff).toByte()
        header[41] = (audioByteLen shr 8 and 0xff).toByte()
        header[42] = (audioByteLen shr 16 and 0xff).toByte()
        header[43] = (audioByteLen shr 24 and 0xff).toByte()
        try {
            fileOutputStream.write(header, 0, 44)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getWavFile(context: Context): File? {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        wavFile = File(dir, "convert.wav")
        return wavFile
    }
}