// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "Checklist",
    platforms: [
        .iOS(.v17),
        .macOS(.v14)
    ],
    products: [
        .library(name: "Checklist", targets: ["Checklist"])
    ],
    targets: [
        .target(
            name: "ChecklistCore",
            resources: [
                .copy("Resources/checklists.json")
            ]
        ),
        .target(
            name: "Checklist",
            dependencies: ["ChecklistCore"]
        ),
        .testTarget(
            name: "ChecklistCoreTests",
            dependencies: ["ChecklistCore"]
        )
    ]
)
