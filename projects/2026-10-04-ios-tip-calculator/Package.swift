// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "TipCalculator",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(name: "TipCalculator", targets: ["TipCalculator"])
    ],
    targets: [
        .target(
            name: "TipCore",
            resources: [
                .copy("Resources/tipping.json")
            ]
        ),
        .target(
            name: "TipCalculator",
            dependencies: ["TipCore"]
        ),
        .testTarget(
            name: "TipCoreTests",
            dependencies: ["TipCore"]
        )
    ]
)
