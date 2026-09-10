import SwiftUI

public struct HabitTrackerApp: App {
    public init() {}

    public var body: some Scene {
        WindowGroup {
            HabitListView()
        }
    }
}
