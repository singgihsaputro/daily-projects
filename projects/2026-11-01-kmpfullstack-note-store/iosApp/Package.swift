// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "NoteApp",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(name: "NoteApp", targets: ["NoteApp"])
    ],
    targets: [
        .target(name: "NoteApp"),
        .testTarget(
            name: "NoteAppTests",
            dependencies: ["NoteApp"]
        )
    ]
)
