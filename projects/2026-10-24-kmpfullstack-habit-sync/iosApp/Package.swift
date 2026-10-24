// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "HabitApp",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(name: "HabitApp", targets: ["HabitApp"])
    ],
    targets: [
        .target(name: "HabitApp"),
        .testTarget(
            name: "HabitAppTests",
            dependencies: ["HabitApp"]
        )
    ]
)
