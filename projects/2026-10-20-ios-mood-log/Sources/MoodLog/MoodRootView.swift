import SwiftUI
import MoodCore

public struct MoodRootView: View {
    @State private var store = MoodStore()
    @State private var selected: Mood = .okay
    @State private var note = ""

    public init() {}

    public var body: some View {
        NavigationStack {
            List {
                if let message = store.errorMessage {
                    Text(message).foregroundStyle(.red)
                }
                Section { summaryRow }
                Section("How are you today?") { picker }
                Section("History") {
                    ForEach(store.entries) { entry in
                        row(entry)
                    }
                }
            }
            .navigationTitle("Mood Log")
            .onAppear(perform: loadToday)
        }
    }

    private var summaryRow: some View {
        let summary = store.summary
        return HStack {
            stat(String(format: "%.1f", summary.average), "average")
            Divider()
            stat("\(summary.streak)", "day streak")
            Divider()
            stat(summary.best?.mood.emoji ?? "–", "best day")
        }
    }

    private func stat(_ value: String, _ caption: String) -> some View {
        VStack {
            Text(value).font(.title2.bold())
            Text(caption).font(.caption).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
    }

    private var picker: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                ForEach(Mood.allCases, id: \.self) { mood in
                    Button { selected = mood } label: {
                        VStack {
                            Text(mood.emoji).font(.largeTitle)
                            Text(mood.label).font(.caption2)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 6)
                        .background(selected == mood ? Color.accentColor.opacity(0.2) : .clear,
                                    in: RoundedRectangle(cornerRadius: 10))
                    }
                    .buttonStyle(.plain)
                }
            }
            TextField("A note about today (optional)", text: $note)
            Button(store.todayEntry == nil ? "Save today" : "Update today") {
                store.log(selected, note: note)
            }
            .buttonStyle(.borderedProminent)
        }
    }

    private func row(_ entry: MoodEntry) -> some View {
        HStack(spacing: 12) {
            Text(entry.mood.emoji).font(.title)
            VStack(alignment: .leading) {
                Text(entry.day == store.today ? "Today" : entry.day).font(.headline)
                if !entry.note.isEmpty {
                    Text(entry.note).font(.subheadline).foregroundStyle(.secondary)
                }
            }
        }
    }

    private func loadToday() {
        if let entry = store.todayEntry {
            selected = entry.mood
            note = entry.note
        }
    }
}
