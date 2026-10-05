import Foundation
import CoreLocation
import Combine

public struct QiblaBearingInfo: Equatable {
    public let qiblaDirectionDegrees: Double
    public let deviceHeadingDegrees: Double
    public let relativeAngleDegrees: Double
    public let distanceToKaabaKm: Double
    public let isAligned: Bool

    public static let zero = QiblaBearingInfo(
        qiblaDirectionDegrees: 118.0,
        deviceHeadingDegrees: 0.0,
        relativeAngleDegrees: 118.0,
        distanceToKaabaKm: 3800.0,
        isAligned: false
    )
}

public class LocationManager: NSObject, ObservableObject, CLLocationManagerDelegate {

    private let manager = CLLocationManager()
    private let kaabaLat = 21.4225
    private let kaabaLon = 39.8262

    @Published public var cityName: String = "Pistoia"
    @Published public var countryName: String = "Italia"
    @Published public var latitude: Double = 43.9333
    @Published public var longitude: Double = 10.9167
    @Published public var elevationMeters: Double = 65.0
    @Published public var heading: Double = 0.0
    @Published public var qiblaBearing: QiblaBearingInfo = .zero
    @Published public var authorizationStatus: CLAuthorizationStatus = .notDetermined

    public override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.headingFilter = 1.0 // 1 degree updates for ultra-fluid 120Hz compass
        updateQiblaBearing()
    }

    public func requestLocation() {
        manager.requestWhenInUseAuthorization()
        manager.startUpdatingLocation()
        manager.startUpdatingHeading()
    }

    public func setManualLocation(city: String, country: String, lat: Double, lon: Double, elevation: Double = 0.0) {
        self.cityName = city
        self.countryName = country
        self.latitude = lat
        self.longitude = lon
        self.elevationMeters = elevation
        updateQiblaBearing()
    }

    public func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        authorizationStatus = manager.authorizationStatus
        if authorizationStatus == .authorizedWhenInUse || authorizationStatus == .authorizedAlways {
            manager.startUpdatingLocation()
            manager.startUpdatingHeading()
        }
    }

    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.last else { return }
        self.latitude = loc.coordinate.latitude
        self.longitude = loc.coordinate.longitude
        self.elevationMeters = loc.altitude

        let geocoder = CLGeocoder()
        geocoder.reverseGeocodeLocation(loc) { [weak self] placemarks, _ in
            if let place = placemarks?.first {
                DispatchQueue.main.async {
                    self?.cityName = place.locality ?? place.subAdministrativeArea ?? "Città rilevata"
                    self?.countryName = place.country ?? ""
                }
            }
        }
        updateQiblaBearing()
    }

    public func locationManager(_ manager: CLLocationManager, didUpdateHeading newHeading: CLHeading) {
        guard newHeading.headingAccuracy >= 0 else { return }
        self.heading = newHeading.trueHeading > 0 ? newHeading.trueHeading : newHeading.magneticHeading
        updateQiblaBearing()
    }

    private func updateQiblaBearing() {
        let lat1Rad = latitude * .pi / 180.0
        let lon1Rad = longitude * .pi / 180.0
        let lat2Rad = kaabaLat * .pi / 180.0
        let lon2Rad = kaabaLon * .pi / 180.0

        let deltaLon = lon2Rad - lon1Rad
        let y = sin(deltaLon) * cos(lat2Rad)
        let x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(deltaLon)

        var qiblaDeg = atan2(y, x) * 180.0 / .pi
        if qiblaDeg < 0 { qiblaDeg += 360.0 }

        // Great circle distance in km (Haversine formula)
        let dLat = lat2Rad - lat1Rad
        let a = sin(dLat / 2) * sin(dLat / 2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2) * sin(deltaLon / 2)
        let c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        let distanceKm = 6371.0 * c

        // Relative Angle
        var relative = qiblaDeg - heading
        while relative > 180 { relative -= 360 }
        while relative < -180 { relative += 360 }

        let isAligned = abs(relative) < 3.0

        DispatchQueue.main.async {
            self.qiblaBearing = QiblaBearingInfo(
                qiblaDirectionDegrees: qiblaDeg,
                deviceHeadingDegrees: self.heading,
                relativeAngleDegrees: relative,
                distanceToKaabaKm: distanceKm,
                isAligned: isAligned
            )
        }
    }
}
