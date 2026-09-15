import SwiftUI

struct PackingListView: View {
    @State private var model = PackingListModel()

    var body: some View {
        NavigationStack {
            Group {
                if model.isLoading {
                    ProgressView()
                } else if let error = model.loadError {
                    ContentUnavailableView(error, systemImage: "exclamationmark.triangle")
                } else {
                    List {
                        Section {
                            VStack(alignment: .leading, spacing: 6) {
                                ProgressView(value: model.progress)
                                    .tint(.accentColor)
                                Text("\(model.packedCount) of \(model.items.count) packed")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                            .padding(.vertical, 4)
                        }

                        ForEach(model.categories) { category in
                            Section {
                                ForEach(model.items(in: category)) { item in
                                    PackingItemRowView(item: item) {
                                        model.toggle(item)
                                    }
                                }
                            } header: {
                                Label(category.rawValue, systemImage: category.symbol)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Pack for Lisbon")
            .task {
                await model.load()
            }
        }
    }
}

#Preview {
    PackingListView()
}
