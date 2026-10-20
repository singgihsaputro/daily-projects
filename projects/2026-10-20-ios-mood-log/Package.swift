// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "MoodLog",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(name: "MoodLog", targets: ["MoodLog"])
    ],
    targets: [
        .target(
            name: "MoodCore",
            resources: [
                .copy("Resources/entries.json")
            ]
        ),
        .target(
            name: "MoodLog",
            dependencies: ["MoodCore"]
        ),
        .testTarget(
            name: "MoodCoreTests",
            dependencies: ["MoodCore"]
        )
    ]
)
