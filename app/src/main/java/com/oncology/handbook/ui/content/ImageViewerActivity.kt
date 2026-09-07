package com.oncology.handbook.ui.content

import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.oncology.handbook.databinding.ActivityImageViewerBinding
import java.io.File

class ImageViewerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IMAGE_PATH = "extra_image_path"
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    private lateinit var binding: ActivityImageViewerBinding

    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private var mode = NONE
    private var scale = 1f
    private var minScale = 1f
    private val maxScale = 5f

    private val startPoint = PointF()
    private val midPoint = PointF()
    private var oldDist = 1f

    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        if (imagePath.isNullOrEmpty() || !File(imagePath).exists()) {
            Toast.makeText(this, "图片不存在", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnClose.setOnClickListener { finish() }

        // 图片加载完成后初始化 matrix 为适配屏幕居中
        binding.ivFullscreen.load(File(imagePath)) {
            listener(object : coil.request.ImageRequest.Listener {
                override fun onSuccess(
                    request: coil.request.ImageRequest,
                    result: coil.request.SuccessResult
                ) {
                    binding.ivFullscreen.post { initMatrix() }
                }
            })
        }

        setupZoom()
    }

    /** 初始化 matrix：图片等比缩放适配屏幕，居中显示 */
    private fun initMatrix() {
        val drawable: Drawable = binding.ivFullscreen.drawable ?: return
        val viewW = binding.ivFullscreen.width.toFloat()
        val viewH = binding.ivFullscreen.height.toFloat()
        val drawableW = drawable.intrinsicWidth.toFloat()
        val drawableH = drawable.intrinsicHeight.toFloat()

        if (viewW == 0f || viewH == 0f || drawableW == 0f || drawableH == 0f) return

        val scaleX = viewW / drawableW
        val scaleY = viewH / drawableH
        val fitScale = minOf(scaleX, scaleY)

        matrix.reset()
        matrix.postScale(fitScale, fitScale)
        val dx = (viewW - drawableW * fitScale) / 2f
        val dy = (viewH - drawableH * fitScale) / 2f
        matrix.postTranslate(dx, dy)

        scale = fitScale
        minScale = fitScale
        binding.ivFullscreen.imageMatrix = matrix
    }

    private fun setupZoom() {
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        gestureDetector = GestureDetector(this, GestureListener())

        binding.ivFullscreen.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)

            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    savedMatrix.set(matrix)
                    startPoint.set(event.x, event.y)
                    mode = DRAG
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    oldDist = spacing(event)
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix)
                        midPoint.set(midPointOf(event))
                        mode = ZOOM
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mode == DRAG) {
                        matrix.set(savedMatrix)
                        matrix.postTranslate(event.x - startPoint.x, event.y - startPoint.y)
                    } else if (mode == ZOOM && !scaleGestureDetector.isInProgress) {
                        val newDist = spacing(event)
                        if (newDist > 10f) {
                            matrix.set(savedMatrix)
                            val newScale = newDist / oldDist
                            val resultScale = scale * newScale
                            if (resultScale in minScale..maxScale) {
                                matrix.postScale(newScale, newScale, midPoint.x, midPoint.y)
                                scale = resultScale
                            }
                        }
                    }
                }
            }

            binding.ivFullscreen.imageMatrix = matrix
            true
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newScale = scale * scaleFactor
            if (newScale in minScale..maxScale) {
                matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                scale = newScale
                binding.ivFullscreen.imageMatrix = matrix
            }
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (scale > minScale) {
                initMatrix()
            } else {
                matrix.postScale(2f, 2f, e.x, e.y)
                scale *= 2f
                binding.ivFullscreen.imageMatrix = matrix
            }
            return true
        }
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPointOf(event: MotionEvent): PointF {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        return PointF(x / 2, y / 2)
    }
}
