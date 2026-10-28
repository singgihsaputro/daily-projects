// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "PlantWatering",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(name: "PlantWatering", targets: ["PlantWatering"])
    ],
    targets: [
        .target(
            name: "PlantCore",
            resources: [
                .copy("Resources/plants.json")
            ]
        ),
        .target(
            name: "PlantWatering",
            dependencies: ["PlantCore"]
        ),
        .testTarget(
            name: "PlantCoreTests",
            dependencies: ["PlantCore"]
        )
    ]
)
