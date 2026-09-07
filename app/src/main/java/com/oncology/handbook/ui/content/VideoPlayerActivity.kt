package com.oncology.handbook.ui.content

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.oncology.handbook.databinding.ActivityVideoPlayerBinding
import java.io.File

class VideoPlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_VIDEO_PATH = "extra_video_path"
    }

    private lateinit var binding: ActivityVideoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoPath = intent.getStringExtra(EXTRA_VIDEO_PATH)
        if (videoPath.isNullOrEmpty() || !File(videoPath).exists()) {
            Toast.makeText(this, "视频不存在", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnClose.setOnClickListener { finish() }

        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)

        // 使用 setVideoPath 而不是 setVideoURI(Uri.fromFile())，避免 FileUriExposedException
        binding.videoView.setVideoPath(videoPath)

        binding.videoView.setOnPreparedListener { mp ->
            mp.setOnVideoSizeChangedListener { _, _, _ ->
                // 视频尺寸就绪后开始播放
            }
            binding.videoView.start()
        }

        binding.videoView.setOnErrorListener { _, what, extra ->
            Toast.makeText(this, "视频播放失败 (what=$what, extra=$extra)", Toast.LENGTH_LONG).show()
            finish()
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::binding.isInitialized) {
            binding.videoView.stopPlayback()
        }
    }
}
