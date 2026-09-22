import SwiftUI

public struct ShortLinkListView: View {
    @State private var model = ShortLinkModel()

    public init() {}

    public var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 12) {
                Text("Pointed at server/ on http://localhost:8080")
                    .font(.caption)
                    .foregroundStyle(.secondary)

                HStack {
                    TextField("Paste a URL to shorten", text: $model.newUrlInput)
                        .textFieldStyle(.roundedBorder)
                        .autocorrectionDisabled()
                    Button("Shorten") {
                        Task { await model.addLink() }
                    }
                }

                if let message = model.errorMessage {
                    Text(message)
                        .font(.caption)
                        .foregroundStyle(.red)
                }

                if model.isLoading && model.links.isEmpty {
                    ProgressView("Loading...")
                        .frame(maxWidth: .infinity)
                } else {
                    List(model.links) { link in
                        ShortLinkRow(link: link) {
                            Task { await model.visit(code: link.code) }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .padding()
            .navigationTitle("Short Links")
            .task { await model.refresh() }
            .refreshable { await model.refresh() }
        }
    }
}

private struct ShortLinkRow: View {
    let link: ShortLink
    let onVisit: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("/r/\(link.code)").font(.headline)
            Text(link.longUrl).font(.subheadline).lineLimit(1).foregroundStyle(.secondary)
            HStack {
                Text("\(link.clicks) clicks").font(.caption).foregroundStyle(.tertiary)
                Spacer()
                Button("Visit", action: onVisit)
                    .buttonStyle(.bordered)
                    .tint(.teal)
            }
        }
        .padding(.vertical, 4)
    }
}
