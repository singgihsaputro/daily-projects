import SwiftUI

public struct BookmarkListView: View {
    @State private var model = BookmarkModel()

    public init() {}

    public var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 10) {
                Text("Pointed at server/ on http://localhost:8080")
                    .font(.caption)
                    .foregroundStyle(.secondary)

                TextField("Title", text: $model.titleInput).textFieldStyle(.roundedBorder)
                TextField("https://…", text: $model.urlInput)
                    .textFieldStyle(.roundedBorder)
                    .autocorrectionDisabled()
                HStack {
                    TextField("Tag (optional)", text: $model.tagInput).textFieldStyle(.roundedBorder)
                    Button("Save") { Task { await model.add() } }
                }

                if let message = model.errorMessage {
                    Text(message).font(.caption).foregroundStyle(.red)
                }

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack {
                        tagChip(label: "All", tag: "")
                        ForEach(model.tags, id: \.self) { tagChip(label: "#\($0)", tag: $0) }
                    }
                }

                if model.isLoading && model.bookmarks.isEmpty {
                    ProgressView("Loading...").frame(maxWidth: .infinity)
                } else {
                    List(model.bookmarks) { bookmark in
                        BookmarkRow(
                            bookmark: bookmark,
                            onStar: { Task { await model.toggleStar(id: bookmark.id) } },
                            onRemove: { Task { await model.remove(id: bookmark.id) } }
                        )
                    }
                    .listStyle(.plain)
                }
            }
            .padding()
            .navigationTitle("Bookmarks")
            .task { await model.refresh() }
            .refreshable { await model.refresh() }
        }
    }

    private func tagChip(label: String, tag: String) -> some View {
        Button(label) { Task { await model.select(tag: tag) } }
            .buttonStyle(.bordered)
            .tint(model.selectedTag == tag ? .indigo : .gray)
    }
}

private struct BookmarkRow: View {
    let bookmark: Bookmark
    let onStar: () -> Void
    let onRemove: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(bookmark.title).font(.headline)
            Text(bookmark.url).font(.subheadline).lineLimit(1).foregroundStyle(.secondary)
            HStack {
                Text(bookmark.tag.isEmpty ? "untagged" : "#\(bookmark.tag)")
                    .font(.caption)
                    .foregroundStyle(.tertiary)
                Spacer()
                Button(bookmark.starred ? "★ Starred" : "☆ Star", action: onStar)
                    .buttonStyle(.bordered)
                    .tint(.orange)
                Button("Delete", role: .destructive, action: onRemove)
                    .buttonStyle(.bordered)
            }
        }
        .padding(.vertical, 4)
    }
}
