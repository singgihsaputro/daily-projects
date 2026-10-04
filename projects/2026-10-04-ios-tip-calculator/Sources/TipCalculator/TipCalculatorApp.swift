import SwiftUI

/// Entry point. Set this as the `@main` of an iOS App target that depends on the
/// `TipCalculator` library (see README).
public struct TipCalculatorApp: App {
    public init() {}

    public var body: some Scene {
        WindowGroup { TipCalculatorView() }
    }
}
