// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "SleepLog",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(name: "SleepLog", targets: ["SleepLog"])
    ],
    targets: [
        .target(
            name: "SleepLog",
            resources: [
                .copy("Resources/sleep.json")
            ]
        ),
        .testTarget(
            name: "SleepLogTests",
            dependencies: ["SleepLog"]
        )
    ]
)
