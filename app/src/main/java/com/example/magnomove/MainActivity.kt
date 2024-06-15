package com.example.magnomove

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import android.widget.Button

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var tflite: Interpreter
    private lateinit var toggleButton: Button
    private var isMeasuring: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize TensorFlow Lite
        try {
            tflite = Interpreter(loadModelFile(this))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        toggleButton = findViewById(R.id.toggleButton)
        toggleButton.setOnClickListener {
            toggleMeasurement()
            toggleButton.text = if (isMeasuring) "Stop Measuring" else "Start Measuring"
        }
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileInputStream = FileInputStream(context.assets.openFd("bestmodel_test2.tflite").fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = context.assets.openFd("bestmodel_test2.tflite").startOffset
        val declaredLength = context.assets.openFd("bestmodel_test2.tflite").declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val magnetometerValues = event.values

            // Prepare input and output buffers
            val inputBuffer = ByteBuffer.allocateDirect(3 * 4).order(ByteOrder.nativeOrder())
            inputBuffer.putFloat(magnetometerValues[0])
            inputBuffer.putFloat(magnetometerValues[1])
            inputBuffer.putFloat(magnetometerValues[2])

            val outputBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())

            // Run inference
            tflite.run(inputBuffer, outputBuffer)

            // Get the result
            outputBuffer.rewind()
            val result = outputBuffer.getFloat()

            // Change screen color based on result
            window.decorView.setBackgroundColor(
                if (result > 0.5) Color.GREEN else Color.RED
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No implementation needed
    }

    private fun toggleMeasurement() {
        isMeasuring = !isMeasuring
    }
}