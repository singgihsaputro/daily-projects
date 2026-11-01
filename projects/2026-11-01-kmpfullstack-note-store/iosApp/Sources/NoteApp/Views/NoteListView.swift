import SwiftUI

public struct NoteListView: View {
    @State private var model = NoteModel()

    public init() {}

    public var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 10) {
                Text("Pointed at server/ on http://localhost:8080")
                    .font(.caption)
                    .foregroundStyle(.secondary)

                TextField("Search notes", text: $model.query)
                    .textFieldStyle(.roundedBorder)
                    .onChange(of: model.query) { Task { await model.refresh() } }

                TextField("New note title", text: $model.titleInput).textFieldStyle(.roundedBorder)
                HStack {
                    TextField("Body", text: $model.bodyInput, axis: .vertical)
                        .textFieldStyle(.roundedBorder)
                    Button("Add") { Task { await model.add() } }
                        .buttonStyle(.borderedProminent)
                }

                if let message = model.errorMessage {
                    Text(message).font(.caption).foregroundStyle(.red)
                }

                if model.isLoading && model.notes.isEmpty {
                    ProgressView("Loading...").frame(maxWidth: .infinity)
                } else {
                    List(model.notes) { view in
                        NoteRow(
                            view: view,
                            onPin: { Task { await model.togglePin(id: view.note.id) } },
                            onRemove: { Task { await model.remove(id: view.note.id) } }
                        )
                    }
                    .listStyle(.plain)
                }
            }
            .padding()
            .navigationTitle("Note store")
            .task { await model.refresh() }
            .refreshable { await model.refresh() }
        }
    }
}

private struct NoteRow: View {
    let view: NoteView
    let onPin: () -> Void
    let onRemove: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text((view.note.pinned ? "📌 " : "") + view.note.title).font(.headline)
            Text(view.preview).font(.subheadline).foregroundStyle(.secondary)
            HStack {
                Button(view.note.pinned ? "Unpin" : "Pin", action: onPin).buttonStyle(.bordered)
                Button("Delete", role: .destructive, action: onRemove).buttonStyle(.bordered)
            }
        }
        .padding(.vertical, 4)
    }
}
