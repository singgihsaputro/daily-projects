import SwiftUI

public struct BookmarkApp: App {
    public init() {}

    public var body: some Scene {
        WindowGroup {
            BookmarkListView()
        }
    }
}
