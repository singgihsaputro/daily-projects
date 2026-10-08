// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "BookmarkApp",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(name: "BookmarkApp", targets: ["BookmarkApp"])
    ],
    targets: [
        .target(name: "BookmarkApp"),
        .testTarget(
            name: "BookmarkAppTests",
            dependencies: ["BookmarkApp"]
        )
    ]
)
