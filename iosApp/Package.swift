// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "NurPray",
    defaultLocalization: "it",
    platforms: [
        .iOS(.v16),
        .macOS(.v13)
    ],
    products: [
        .library(
            name: "NurPrayCore",
            targets: ["NurPrayCore"]
        )
    ],
    targets: [
        .target(
            name: "NurPrayCore",
            path: "NurPray",
            exclude: ["Info.plist"]
        ),
        .testTarget(
            name: "NurPrayTests",
            dependencies: ["NurPrayCore"],
            path: "Tests"
        )
    ]
)
