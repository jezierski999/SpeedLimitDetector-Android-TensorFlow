package com.example.speedlimitdetector

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.Image
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.InputType
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleOwner
import com.example.speedlimitdetector.ml.Ssd
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var context: Context
    private var cameraHelper: CameraHelper? = null
    private val PERMISSIONS_REQUEST_CODE = 225
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private lateinit var model: Ssd
    private lateinit var locationHelper: LocationHelper
    private lateinit var camera: Camera
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private lateinit var byteBuffer: ByteBuffer
    private lateinit var inputFeature: TensorBuffer

    private lateinit var imageView: ImageView
    private lateinit var iv1: ImageView
    private lateinit var iv2: ImageView
    private lateinit var iv3: ImageView
    private lateinit var iv4: ImageView
    private lateinit var iv5: ImageView
    private lateinit var iv6: ImageView
    private lateinit var iv7: ImageView
    private lateinit var iv8: ImageView
    private lateinit var tvSpeed: TextView
    private lateinit var tvYld: ImageView
    private lateinit var tvFps: TextView
    private lateinit var buttonSettings: Button
    private lateinit var layoutLeft: LinearLayout
    private lateinit var layoutRight: LinearLayout
    private lateinit var tvX: TextView
    private lateinit var tvY: TextView
    private lateinit var buttonConfidence: Button
    private lateinit var buttonSeconds: Button
    private lateinit var buttonMinDetectionCountThreshold: Button
    private lateinit var tvKMH: TextView
    private lateinit var tvCamera: TextView

    private var cropX = 0
    private var cropY = 0
    private val cropSize = 224
    private var Settings: Boolean = false
    private var seconds: Int = 10
    private var confidence: Float = 0.70f
    private var minDetectionCountThreshold: Int = 2
    private var helpList = ArrayList<Bitmap>()
    private var helpClassList = ArrayList<String>()
    private var colors = listOf<Int>(Color.RED, Color.BLUE, Color.WHITE, Color.RED, Color.GREEN, Color.CYAN, Color.GRAY, Color.BLACK, Color.DKGRAY, Color.MAGENTA)
    private val speedList: List<String> = listOf("20", "30", "40", "50", "60", "70", "80", "90", "100", "110", "120", "130")
    private var speed: Double = 0.0
    private lateinit var scores: FloatArray
    private lateinit var locations: FloatArray
    private lateinit var classes: FloatArray
    private var maxIndex by Delegates.notNull<Int>()
    private var x = 0
    private var x1 by Delegates.notNull<Int>()
    private var y1 by Delegates.notNull<Int>()
    private var w1 by Delegates.notNull<Int>()
    private var h1 by Delegates.notNull<Int>()
    private var maksimum by Delegates.notNull<Int>()
    private var indeksMaksimum by Delegates.notNull<Int>()
    private var rt by Delegates.notNull<String>()
    private var yld by Delegates.notNull<Int>()
    private var ne by Delegates.notNull<Int>()
    private var maxScore by Delegates.notNull<Float>()
    private var fl by Delegates.notNull<Float>()
    private lateinit var bitmap: Bitmap
    private lateinit var mutable: Bitmap
    private lateinit var helpMutable: Bitmap
    private var elapsedTimeMillis by Delegates.notNull<Long>()
    private var startTimeMillis = 0L
    private var frameCount = 0
    private var fps = 0.0
    private val helpArray = IntArray(12)
    private lateinit var result: Text
    private lateinit var helpImage: InputImage

    @SuppressLint("ClickableViewAccessibility") override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        imageView = findViewById<ImageView>(R.id.imageView)
        iv1 = findViewById<ImageView>(R.id.iv1)
        iv2 = findViewById<ImageView>(R.id.iv2)
        iv3 = findViewById<ImageView>(R.id.iv3)
        iv4 = findViewById<ImageView>(R.id.iv4)
        iv5 = findViewById<ImageView>(R.id.iv5)
        iv6 = findViewById<ImageView>(R.id.iv6)
        iv7 = findViewById<ImageView>(R.id.iv7)
        iv8 = findViewById<ImageView>(R.id.iv8)
        tvSpeed = findViewById<TextView>(R.id.tvSpeed)
        tvYld = findViewById<ImageView>(R.id.tvYld)
        tvFps = findViewById<TextView>(R.id.tvFps)
        buttonSettings = findViewById<Button>(R.id.button_Settings)
        layoutLeft = findViewById<LinearLayout>(R.id.layoutLeft)
        layoutRight = findViewById<LinearLayout>(R.id.layoutRight)
        tvX = findViewById<TextView>(R.id.tv_x)
        tvY = findViewById<TextView>(R.id.tv_y)
        buttonConfidence = findViewById<Button>(R.id.button_confidence)
        buttonSeconds = findViewById<Button>(R.id.button_seconds)
        buttonMinDetectionCountThreshold = findViewById<Button>(R.id.button_minDetectionCountThreshold)
        tvKMH = findViewById<TextView>(R.id.tvKMH)
        tvCamera = findViewById<TextView>(R.id.tv_camera)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initializeCameraAndLocation()
        } else {
            requestCameraAndLocationPermissions()
        }


        //--------------------------------------------------------------------------------------Database
        val db = DatabaseHelper(this)

        if(db.getValueById(1) == 0.0f) {
            confidence = 0.70f
            db.updateValue(1, 0.70f)
            cropX = 150
            db.updateValue(2, 150f)
            cropY = 90
            db.updateValue(3, 90f)
            seconds = 10
            db.updateValue(4, 10f)
            minDetectionCountThreshold = 2
            db.updateValue(5, 2f)
        }
        else {
            confidence = db.getValueById(1)
            cropX = db.getValueById(2).toInt()
            cropY = db.getValueById(3).toInt()
            seconds = db.getValueById(4).toInt()
            minDetectionCountThreshold = db.getValueById(5).toInt()
        }


        buttonSeconds.text = "Duration:\n${seconds.toString()} s"
        buttonConfidence.text = "Confidence:\n${(confidence * 100).toInt().toString()} %"
        buttonMinDetectionCountThreshold.text = "Detection\nCount\nThreshold - $minDetectionCountThreshold"
        tvX.text = "x:$cropX"
        tvY.text = "y:$cropY"

        //------------------------------------------------------------------------------------------

        tvFps.keepScreenOn = true
        model = Ssd.newInstance(this)
        context = applicationContext


        var lastTouchX = 0f
        var lastTouchY = 0f

        imageView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    cropX -= (dx / 4).toInt()
                    cropY -= (dy / 4).toInt()
                    cropX = cropX.coerceIn(0, 250)
                    cropY = cropY.coerceIn(0, 400)
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
            db.updateValue(2, cropX.toFloat())
            db.updateValue(3, cropY.toFloat())
            tvX.text = "x:$cropX"
            tvY.text = "y:$cropY"
            true
        }

        buttonSeconds.setOnClickListener {
            animateButtonScale(buttonSeconds)
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Set sign display duration\n(between 1 and 30 seconds).")

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER
            builder.setView(input)

            builder.setPositiveButton("OK") { _, _ ->
                val value = input.text.toString()
                try {
                    val number = value.toInt()
                    if (number in 1..30) {
                        buttonSeconds.text = "Duration:\n$number s"
                        seconds = number
                        db.updateValue(4, number.toFloat())
                    } else {
                        Toast.makeText(this, "Please enter a value between 1 and 30.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid input. Please enter a number.", Toast.LENGTH_SHORT).show()
                }
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(input.windowToken, 0)
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            builder.create().show()
        }

        buttonConfidence.setOnClickListener {
            animateButtonScale(buttonConfidence)

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Set the minimum recognition confidence (1% - 100%)")

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            builder.setView(input)

            builder.setPositiveButton("OK") { _, _ ->
                val value = input.text.toString()
                try {
                    val number = value.toFloat()
                    if (number in 1f..100f) {
                        buttonConfidence.text = "Confidence:\n$value %"
                        confidence = number / 100f
                        db.updateValue(1, confidence)
                    } else {
                        Toast.makeText(this, "Please enter a value between 1 and 100.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid input. Please enter a number.", Toast.LENGTH_SHORT).show()
                }
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(input.windowToken, 0)
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            builder.create().show()
        }

        buttonMinDetectionCountThreshold.setOnClickListener {
            animateButtonScale(buttonMinDetectionCountThreshold)

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Enter the minimum number of detected signs before processing:")

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            builder.setView(input)

            builder.setPositiveButton("OK") { _, _ ->
                val value = input.text.toString()
                try {
                    val number = value.toInt()
                    if (number in 1..5) {
                        buttonMinDetectionCountThreshold.text = "Detection\nCount\nThreshold - $value"
                        minDetectionCountThreshold = number.toInt()
                        db.updateValue(5, minDetectionCountThreshold.toFloat())
                    } else {
                        Toast.makeText(this, "Please enter a value between 1 and 5.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Invalid input. Please enter a number.", Toast.LENGTH_SHORT).show()
                }
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(input.windowToken, 0)
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            builder.create().show()
        }

        buttonSettings.setOnClickListener {
            if (buttonSettings.text == "Settings") {
                animateButtonScale(buttonSettings)
                imageView.visibility = View.VISIBLE
                layoutLeft.visibility = View.VISIBLE
                layoutRight.visibility = View.VISIBLE
                tvCamera.visibility = View.VISIBLE
                Settings = true
                Handler(Looper.getMainLooper()).postDelayed({
                    buttonSettings.text = "Close Settings"
                }, 50)
            } else {
                animateButtonScale(buttonSettings)
                layoutRight.visibility = View.INVISIBLE
                layoutLeft.visibility = View.INVISIBLE
                imageView.visibility = View.INVISIBLE
                tvCamera.visibility = View.INVISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    buttonSettings.text = "Settings"
                }, 50)
                Settings = false
            }
        }
    }


    inner class CameraHelper(private val context: Context) {

        private lateinit var cameraProvider: ProcessCameraProvider
        val labels = FileUtil.loadLabels(context, "labelmap.txt")
        val beep = MediaPlayer.create(context, R.raw.beep)

        fun startCamera() {

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener(kotlinx.coroutines.Runnable {
                cameraProvider = cameraProviderFuture.get()
                val imageAnalyzer = ImageAnalysis.Builder().setTargetResolution(Size(720, 1200)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor(), ImageAnalyzer { image -> // TODO: add image processing logic
                        image.close()
                    })
                }

                val availableCameraInfos = cameraProvider.availableCameraInfos
                val cameraSelector = if (availableCameraInfos.size > 2 && availableCameraInfos[2] != null) {
                    availableCameraInfos[2].cameraSelector
                } else {
                    availableCameraInfos[0].cameraSelector
                }

                cameraProvider.unbindAll()

                try {
                    Handler(Looper.getMainLooper()).postDelayed({

                        if (::cameraProvider.isInitialized) {
                            camera = cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, imageAnalyzer)
                        } }, 2000)

                    


                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))
        }


        /**
         * ImageAnalyzer analyzes camera frames using a TensorFlow Lite model and Google's ML Kit Text Recognition.
         *
         * - Crops the input image based on user-defined coordinates.
         * - Feeds the cropped image into a trained TFLite object detection model.
         * - Detects traffic signs and evaluates recognition confidence.
         * - If confidence is high enough, draws a bounding box and stores the cropped region for further analysis.
         * - Uses OCR (Optical Character Recognition) to analyze text inside the detected sign area (e.g., speed limit values).
         * - Updates UI with recognized signs or triggers warning sounds if necessary.
         *
         * This analyzer runs on a background thread and posts results to the main thread for UI updates.
         *
         * @property listener Callback used to receive the raw camera image if needed (not directly used here).
         */

        private inner class ImageAnalyzer(private val listener: (image: Image) -> Unit) : ImageAnalysis.Analyzer {

            val handler = Handler()
            private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            @SuppressLint("SetTextI18n") override fun analyze(imageProxy: ImageProxy) {
                bitmap = BitmapUtils.getBitmap(imageProxy)!!
                imageProxy.close()

                if (bitmap != null) {

                    // Crop the input bitmap to the specified region, convert it to a ByteBuffer,
                    // run inference through the TensorFlow Lite model, and select the detection with the highest confidence score.

                    val maxX = bitmap.width - cropSize
                    val maxY = bitmap.height - cropSize

                    val safeX = cropX.coerceIn(0, maxX)
                    val safeY = cropY.coerceIn(0, maxY)

                    bitmap = Bitmap.createBitmap(bitmap, safeX, safeY, cropSize, cropSize)


                    byteBuffer = bitmap_to_bytebuffer(bitmap)
                    inputFeature = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)
                    inputFeature.loadBuffer(byteBuffer)
                    val outputs = model.process(inputFeature)

                    scores = outputs.outputFeature0AsTensorBuffer.floatArray
                    locations = outputs.outputFeature1AsTensorBuffer.floatArray
                    classes = outputs.outputFeature3AsTensorBuffer.floatArray

                    mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)



                    maxIndex = -1
                    maxScore = Float.MIN_VALUE
                    for (index in scores.indices) {
                        fl = scores[index]
                        if (fl > maxScore) {
                            maxIndex = index
                            maxScore = fl
                        }
                    }

                    if (maxIndex != -1) {
                        if (scores[maxIndex] > confidence) {
                            x = maxIndex * 4
                            fl = scores[maxIndex]

                            if (Settings) {

                                // If settings mode is enabled, draw a bounding box and label around the detected object on the bitmap.
                                canvas = Canvas(mutable)
                                paint = Paint()
                                paint.textSize = 10f
                                paint.strokeWidth = 3f

                                paint.color = colors[maxIndex]
                                paint.style = Paint.Style.STROKE
                                canvas.drawRoundRect(locations[x + 1] * 224, locations[x] * 224, locations[x + 3] * 224, locations[x + 2] * 224, 5f, 5f, paint)
                                paint.style = Paint.Style.FILL
                                canvas.drawText(labels[classes[maxIndex].toInt()] + " " + (fl * 100).toInt().toString() + "%", locations[x + 1] * 224, locations[x] * 224 - 3, paint)
                            }

                            // Extract the detected object's region from the image, resize and crop it, then store it for later recognition or display.
                            x1 = (locations[x + 1] * 224).toInt()
                            y1 = (locations[x] * 224).toInt()
                            w1 = ((locations[x + 3] * 224) - (locations[x + 1] * 224).toInt()).toInt()
                            h1 = ((locations[x + 2] * 224) - (locations[x] * 224)).toInt()

                            if (x1 < 0) x1 = 0
                            if (y1 < 0) y1 = 0
                            if (x1 + w1 > bitmap.width) w1 = bitmap.width - x1
                            if (y1 + h1 > bitmap.height) h1 = bitmap.height - y1

                            if (w1 > 0 && h1 > 0) {
                                helpMutable = Bitmap.createBitmap(bitmap, x1, y1, w1, h1)
                                helpMutable = Bitmap.createScaledBitmap(helpMutable, 120, 120, true)
                                helpMutable = Bitmap.createBitmap(helpMutable, 25, 25, 80, 70)

                                helpList.add(helpMutable)

                                helpClassList.add(labels[classes[maxIndex].toInt()])
                                if (Settings) {
                                    shiftImageHistory()
                                    iv1.post { iv1.setImageBitmap(helpMutable) }
                                }
                            }

                        } else {
                            // If the next recognized object is missing

                            if (helpList.size >= minDetectionCountThreshold) {
                                yld = 0
                                ne = 0

                                for (i in helpClassList) {
                                    if (i == "u") yld++
                                    else ne++
                                }

                                //Displaying the yield sign
                                if (yld > ne && helpList.size > 2) {
                                    tvYld.post { //
                                        tvSpeed.visibility = View.INVISIBLE
                                        tvYld.visibility = View.VISIBLE
                                        beep.start()
                                        handler.postDelayed({
                                            tvYld.visibility = View.INVISIBLE
                                        }, (seconds.toLong() * 1000))
                                    }

                                    helpList.clear()
                                    helpClassList.clear()
                                }

                                // Displaying the speed limit sign
                                else {
                                    Arrays.fill(helpArray, 0)

                                    //Performing text recognition on each object sequentially and saving results to helpArray.
                                    for (i in helpList) {
                                        rt = ""
                                        helpImage = InputImage.fromBitmap(i, 0)

                                        try {
                                            val textTask = recognizer.process(helpImage)
                                            result = Tasks.await(textTask, 500L, TimeUnit.MILLISECONDS)
                                            rt = result.text
                                        } catch (e: Exception) {
                                            Log.e("DEBUG", "Task exc: ${e.message}")
                                        } finally {
                                        }
                                        if (rt.contains("100")) helpArray[8]++
                                        else if (rt.contains("110")) helpArray[9]++
                                        else if (rt.contains("120")) helpArray[10]++
                                        else if (rt.contains("130")) helpArray[11]++
                                        else if (rt.contains("20") || rt.contains("Z0") || rt.contains("z0") || rt.contains("0Z") || rt.contains("0z") || rt.contains("2o") || rt.contains("2O") || rt.contains(
                                                "2o")) helpArray[0]++
                                        else if (rt.contains("30")) helpArray[1]++
                                        else if (rt.contains("40")) helpArray[2]++
                                        else if (rt.contains("50") || rt.contains("S0") || rt.contains("s0") || rt.contains("0S") || rt.contains("0s") || rt.contains("5o") || rt.contains("5O") || rt.contains(
                                                "5o")) helpArray[3]++
                                        else if (rt.contains("60") || rt.contains("6O") || rt.contains("09") || rt.contains("O9") || rt.contains("o9") || rt.contains("6o")) helpArray[4]++
                                        else if (rt.contains("70")) helpArray[5]++
                                        else if (rt.contains("80") || rt.contains("08") || rt.contains("8o") || rt.contains("8O") || rt.contains("o8") || rt.contains("O8")) helpArray[6]++
                                        else if (rt.contains("90") || rt.contains("9O") || rt.contains("06") || rt.contains("O6") || rt.contains("o6") || rt.contains("9o")) helpArray[7]++
                                    }

                                    maksimum = helpArray[0]
                                    indeksMaksimum = 0

                                    //Finding the maximum value.
                                    for (i in 1 until helpArray.size) {
                                        if (helpArray[i] > maksimum) {
                                            maksimum = helpArray[i]
                                            indeksMaksimum = i
                                        }
                                    }

                                    //Displaying the sign.
                                    if (helpArray[indeksMaksimum] != 0) {
                                        speed = locationHelper.takespeed()

                                        if (speedList[indeksMaksimum].toDouble() < speed) {
                                            tvSpeed.post { beep.start() }
                                        }
                                        tvSpeed.post {
                                            tvSpeed.text = speedList[indeksMaksimum]
                                            tvYld.visibility = View.INVISIBLE
                                            tvSpeed.visibility = View.VISIBLE

                                            handler.postDelayed({
                                                tvSpeed.visibility = View.INVISIBLE
                                            }, (seconds.toLong() * 1000))
                                        }
                                    }
                                    helpList.clear()
                                    helpClassList.clear()
                                }

                            } else {
                                helpList.clear()
                                helpClassList.clear()
                            }
                        }
                    }

                    //Updating objects in settings.
                    if (Settings) {
                        mutable = Bitmap.createBitmap(mutable, 0, 0, 224, 160)
                        mutable = getRoundedCornerBitmap(mutable, 5f)
                        imageView.post { imageView.setImageBitmap(mutable) }
                    }
                }

                //Calculating frames per second.
                if (frameCount == 0) {
                    startTimeMillis = SystemClock.uptimeMillis()
                } else if (frameCount % 30 == 0) {
                    elapsedTimeMillis = SystemClock.uptimeMillis() - startTimeMillis
                    fps = (frameCount * 1000.0) / elapsedTimeMillis
                    tvFps.post { tvFps.text = "${fps.toInt()}" }
                    startTimeMillis = 0
                    frameCount = -1
                }
                frameCount++
            }
        }
    }

    fun shiftImageHistory() {
        iv8.post { iv7.drawable?.let { iv8.setImageBitmap(it.toBitmap()) } }
        iv7.post { iv6.drawable?.let { iv7.setImageBitmap(it.toBitmap()) } }
        iv6.post { iv5.drawable?.let { iv6.setImageBitmap(it.toBitmap()) } }
        iv5.post { iv4.drawable?.let { iv5.setImageBitmap(it.toBitmap()) } }
        iv4.post { iv3.drawable?.let { iv4.setImageBitmap(it.toBitmap()) } }
        iv3.post { iv2.drawable?.let { iv3.setImageBitmap(it.toBitmap()) } }
        iv2.post { iv1.drawable?.let { iv2.setImageBitmap(it.toBitmap()) } }
    }


    fun getRoundedCornerBitmap(bitmap: Bitmap, cornerRadius: Float): Bitmap {
        val outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)

        val color = -0xbdbdbe
        val paint = Paint()
        val rect = RectF(0F, 0F, bitmap.width.toFloat(), bitmap.height.toFloat())
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0F, 0F, paint)

        return outputBitmap
    }


    fun bitmap_to_bytebuffer(bitmap: Bitmap): ByteBuffer {

        var pixel: Int = 0
        var red: Int = 0
        var green: Int = 0
        var blue: Int = 0
        val bufferSize = 1 * 224 * 224 * 3
        val byteBuffer = ByteBuffer.allocateDirect(bufferSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                pixel = bitmap.getPixel(x, y)

                red = Color.red(pixel)
                green = Color.green(pixel)
                blue = Color.blue(pixel)

                byteBuffer.put(red.toByte())
                byteBuffer.put(green.toByte())
                byteBuffer.put(blue.toByte())
            }
        }
        byteBuffer.position(0)
        return byteBuffer
    }

    private fun animateButtonScale(button: Button) {

        val scaleX = ObjectAnimator.ofFloat(button, View.SCALE_X, 1f, 0.0f).apply {
            duration = 100
            repeatCount = 1
            repeatMode = ValueAnimator.REVERSE
        }

        val scaleY = ObjectAnimator.ofFloat(button, View.SCALE_Y, 1f, 0.0f).apply {
            duration = 100
            repeatCount = 1
            repeatMode = ValueAnimator.REVERSE
        }

        val alphaAnimator = ObjectAnimator.ofFloat(button, View.ALPHA, 1f, 0.3f, 1f).apply {
            duration = 100
        }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            scaleX,
            scaleY,
            alphaAnimator
        )

        animatorSet.start()
    }

    private fun initializeCameraAndLocation() {
            coroutineScope.launch(Dispatchers.IO) {
                cameraHelper = CameraHelper(this@MainActivity)
            }
            locationHelper = LocationHelper(this, this, tvKMH)
            locationHelper.startLocationUpdates()
    }

    private fun requestCameraAndLocationPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    initializeCameraAndLocation()
                } else {
                    Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    override fun onStop() {
        locationHelper.stopLocationUpdates()

        coroutineScope.cancel()
        super.onStop()

        Handler(Looper.getMainLooper()).postDelayed({
            finishAffinity()
            exitProcess(0)
        }, 300)
    }

    override fun onResume() {
        super.onResume()
        coroutineScope.launch(Dispatchers.IO) {
            cameraHelper?.startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}