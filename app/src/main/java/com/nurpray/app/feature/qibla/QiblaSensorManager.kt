package com.nurpray.app.feature.qibla

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Sensor fusion manager handling Rotation Vector and Accelerometer + Magnetometer
 * with Low-Pass filter and GeomagneticField True-North declination correction.
 */
class QiblaSensorManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _azimuthFlow = MutableStateFlow(0f)
    val azimuthFlow: StateFlow<Float> = _azimuthFlow.asStateFlow()

    private val _accuracyFlow = MutableStateFlow(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    val accuracyFlow: StateFlow<Int> = _accuracyFlow.asStateFlow()

    private var rotationVectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var hasAccelerometer = false
    private var hasMagnetometer = false

    private var smoothedAzimuth = 0f
    private var declination = 0f

    // Low-pass filter constant (0.15 gives very smooth response without perceptible lag)
    private val alpha = 0.15f

    init {
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor == null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }
    }

    /**
     * Updates magnetic declination to convert Magnetic North to True Geographic North.
     */
    fun updateLocation(latitude: Double, longitude: Double, altitudeMeters: Double = 0.0) {
        val geoField = GeomagneticField(
            latitude.toFloat(),
            longitude.toFloat(),
            altitudeMeters.toFloat(),
            System.currentTimeMillis()
        )
        declination = geoField.declination
    }

    fun startListening() {
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(
                this,
                rotationVectorSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            accelerometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
            magnetometerSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        var rawAzimuth = 0f

        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthRad = orientationAngles[0]
                rawAzimuth = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            }
            Sensor.TYPE_ACCELEROMETER -> {
                applyLowPass(event.values, lastAccelerometer)
                hasAccelerometer = true
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                applyLowPass(event.values, lastMagnetometer)
                hasMagnetometer = true
            }
        }

        if (rotationVectorSensor == null && hasAccelerometer && hasMagnetometer) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val azimuthRad = orientationAngles[0]
                rawAzimuth = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            }
        }

        // Add magnetic declination to align with True North
        rawAzimuth += declination

        // Normalize to 0..360
        if (rawAzimuth < 0f) rawAzimuth += 360f
        rawAzimuth %= 360f

        // Smooth output to eliminate jitter while avoiding circular boundary wrap-around glitch
        smoothedAzimuth = filterCircularAngle(smoothedAzimuth, rawAzimuth, alpha)
        _azimuthFlow.value = smoothedAzimuth
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _accuracyFlow.value = accuracy
    }

    private fun applyLowPass(input: FloatArray, output: FloatArray) {
        for (i in input.indices) {
            output[i] = output[i] + alpha * (input[i] - output[i])
        }
    }

    /**
     * Circular exponential filter for degrees (0..360) avoiding 359° <-> 0° jumps.
     */
    private fun filterCircularAngle(current: Float, target: Float, factor: Float): Float {
        var diff = target - current
        while (diff < -180f) diff += 360f
        while (diff > 180f) diff -= 360f
        var result = current + factor * diff
        while (result < 0f) result += 360f
        while (result >= 360f) result -= 360f
        return result
    }
}
