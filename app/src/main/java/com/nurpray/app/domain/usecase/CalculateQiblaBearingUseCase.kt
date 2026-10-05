package com.nurpray.app.domain.usecase

import com.nurpray.app.domain.model.QiblaBearing
import kotlin.math.*

class CalculateQiblaBearingUseCase {

    companion object {
        // Precise geographic coordinates of the Kaaba in Mecca
        const val KAABA_LATITUDE = 21.422477
        const val KAABA_LONGITUDE = 39.826182
        private const val EARTH_RADIUS_KM = 6371.0
        private const val ALIGNMENT_TOLERANCE_DEGREES = 3.0f
    }

    /**
     * Calculates the orthodromic forward azimuth (bearing) from user's coordinates to the Kaaba.
     * Formula:
     * θ = atan2(sin(Δλ) * cos(φ2), cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(Δλ))
     */
    fun calculateQiblaAzimuth(latitude: Double, longitude: Double): Float {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLambda = Math.toRadians(KAABA_LONGITUDE - longitude)

        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val initialBearingRad = atan2(y, x)

        var bearingDeg = Math.toDegrees(initialBearingRad).toFloat()
        if (bearingDeg < 0f) {
            bearingDeg += 360f
        }
        return bearingDeg
    }

    /**
     * Calculates great circle distance in kilometers.
     */
    fun calculateDistanceToKaabaKm(latitude: Double, longitude: Double): Double {
        val dLat = Math.toRadians(KAABA_LATITUDE - latitude)
        val dLon = Math.toRadians(KAABA_LONGITUDE - longitude)
        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(KAABA_LATITUDE)

        val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Combines geographic calculation with current sensor device azimuth.
     */
    operator fun invoke(
        userLatitude: Double,
        userLongitude: Double,
        deviceHeading: Float
    ): QiblaBearing {
        val qiblaAzimuth = calculateQiblaAzimuth(userLatitude, userLongitude)
        val distance = calculateDistanceToKaabaKm(userLatitude, userLongitude)

        // Relative angle between where device points and Mecca
        var relative = qiblaAzimuth - deviceHeading
        while (relative > 180f) relative -= 360f
        while (relative < -180f) relative += 360f

        val isAligned = abs(relative) <= ALIGNMENT_TOLERANCE_DEGREES

        return QiblaBearing(
            qiblaDirectionDegrees = qiblaAzimuth,
            deviceHeadingDegrees = deviceHeading,
            relativeAngleDegrees = relative,
            distanceToKaabaKm = distance,
            isAligned = isAligned
        )
    }
}
