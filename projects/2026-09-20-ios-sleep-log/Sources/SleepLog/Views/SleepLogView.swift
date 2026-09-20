import SwiftUI

struct SleepLogView: View {
    @State private var model = SleepLogModel()
    @State private var isPresentingAddSheet = false

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
                                Text(String(format: "%.1fh average", model.averageHours))
                                    .font(.title2.bold())
                                Text("Last \(model.entries.count) nights")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                            .padding(.vertical, 4)
                        }

                        Section("Nightly log") {
                            ForEach(model.entries) { entry in
                                SleepEntryRowView(entry: entry, maxHours: model.maxHours)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Sleep Log")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        isPresentingAddSheet = true
                    } label: {
                        Label("Log tonight", systemImage: "plus")
                    }
                }
            }
            .sheet(isPresented: $isPresentingAddSheet) {
                AddSleepEntrySheet { hours, quality in
                    model.addEntry(hours: hours, quality: quality)
                }
            }
            .task {
                await model.load()
            }
        }
    }
}

#Preview {
    SleepLogView()
}
