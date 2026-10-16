// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "ShoppingApp",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(name: "ShoppingApp", targets: ["ShoppingApp"])
    ],
    targets: [
        .target(name: "ShoppingApp"),
        .testTarget(
            name: "ShoppingAppTests",
            dependencies: ["ShoppingApp"]
        )
    ]
)
