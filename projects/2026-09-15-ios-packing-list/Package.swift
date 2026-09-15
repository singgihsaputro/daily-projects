// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "PackingList",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(name: "PackingList", targets: ["PackingList"])
    ],
    targets: [
        .target(
            name: "PackingList",
            resources: [
                .copy("Resources/packing.json")
            ]
        ),
        .testTarget(
            name: "PackingListTests",
            dependencies: ["PackingList"]
        )
    ]
)
