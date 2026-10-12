import SwiftUI
import ChecklistCore

public struct ChecklistRootView: View {
    @State private var store = ChecklistStore()
    @State private var selectedID: String?
    @State private var newItem = ""

    public init() {}

    public var body: some View {
        NavigationStack {
            Group {
                if let message = store.errorMessage {
                    ContentUnavailableView("Nothing to show", systemImage: "exclamationmark.triangle",
                                           description: Text(message))
                } else if let list = selected {
                    detail(list)
                }
            }
            .navigationTitle("Checklist")
            .toolbar {
                ToolbarItem(placement: .principal) { picker }
                ToolbarItem(placement: .primaryAction) {
                    if let list = selected {
                        Button("Reset") { store.update(list.id) { $0.resetAll() } }
                            .disabled(list.doneCount == 0)
                    }
                }
            }
        }
    }

    private var selected: Checklist? {
        store.checklists.first { $0.id == selectedID } ?? store.checklists.first
    }

    private var picker: some View {
        Picker("List", selection: Binding(
            get: { selected?.id ?? "" },
            set: { selectedID = $0 }
        )) {
            ForEach(store.checklists) { Text($0.title).tag($0.id) }
        }
        .pickerStyle(.menu)
    }

    private func detail(_ list: Checklist) -> some View {
        List {
            Section {
                ProgressView(value: list.progress) {
                    Text(list.isComplete ? "All done" : "\(list.doneCount) of \(list.items.count) done")
                        .font(.headline)
                }
                .tint(list.isComplete ? .green : .accentColor)
            }
            Section(list.title) {
                ForEach(list.items) { item in
                    Button {
                        store.update(list.id) { $0.toggle(item.id) }
                    } label: {
                        Label {
                            Text(item.title)
                                .strikethrough(item.isDone)
                                .foregroundStyle(item.isDone ? .secondary : .primary)
                        } icon: {
                            Image(systemName: item.isDone ? "checkmark.circle.fill" : "circle")
                                .foregroundStyle(item.isDone ? .green : .secondary)
                        }
                    }
                    .buttonStyle(.plain)
                }
                .onDelete { offsets in store.update(list.id) { $0.remove(at: offsets) } }
            }
            Section {
                HStack {
                    TextField("Add an item", text: $newItem)
                        .onSubmit { add(to: list) }
                    Button("Add") { add(to: list) }
                        .disabled(newItem.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }

    private func add(to list: Checklist) {
        var accepted = false
        store.update(list.id) { accepted = $0.add(newItem) }
        if accepted { newItem = "" }
    }
}
