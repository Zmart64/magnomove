import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.magnomove.R
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize TensorFlow Lite
        try {
            tflite = Interpreter(loadModelFile(this))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileInputStream = FileInputStream(context.assets.openFd("model.tflite").fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = context.assets.openFd("model.tflite").startOffset
        val declaredLength = context.assets.openFd("model.tflite").declaredLength
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
}