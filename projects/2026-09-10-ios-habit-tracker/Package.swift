// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "HabitTracker",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(name: "HabitTracker", targets: ["HabitTracker"])
    ],
    targets: [
        .target(
            name: "HabitTracker",
            resources: [
                .copy("Resources/habits.json")
            ]
        ),
        .testTarget(
            name: "HabitTrackerTests",
            dependencies: ["HabitTracker"]
        )
    ]
)
