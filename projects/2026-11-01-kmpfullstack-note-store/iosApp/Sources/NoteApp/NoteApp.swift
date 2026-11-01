import SwiftUI

public struct NoteApp: App {
    public init() {}

    public var body: some Scene {
        WindowGroup {
            NoteListView()
        }
    }
}
