import SwiftUI

struct HabitListView: View {
    @State private var model = HabitListModel()

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
                            ForEach(model.habits) { habit in
                                HabitRowView(habit: habit) {
                                    model.toggle(habit)
                                }
                            }
                        } footer: {
                            Text("\(model.completedCount) of \(model.habits.count) done today")
                        }
                    }
                }
            }
            .navigationTitle("Habits")
            .task {
                await model.load()
            }
        }
    }
}

#Preview {
    HabitListView()
}
