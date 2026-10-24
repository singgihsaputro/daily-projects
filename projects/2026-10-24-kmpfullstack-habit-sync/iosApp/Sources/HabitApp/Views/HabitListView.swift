import SwiftUI

public struct HabitListView: View {
    @State private var model = HabitModel()

    public init() {}

    public var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 10) {
                Text("Pointed at server/ on http://localhost:8080")
                    .font(.caption)
                    .foregroundStyle(.secondary)

                HStack {
                    TextField("New habit", text: $model.nameInput).textFieldStyle(.roundedBorder)
                    Stepper("\(model.goalInput) d/wk", value: $model.goalInput, in: 1...maxGoalPerWeek)
                        .fixedSize()
                    Button("Add") { Task { await model.add() } }
                        .buttonStyle(.borderedProminent)
                }

                if let message = model.errorMessage {
                    Text(message).font(.caption).foregroundStyle(.red)
                }

                if model.isLoading && model.habits.isEmpty {
                    ProgressView("Loading...").frame(maxWidth: .infinity)
                } else {
                    List(model.habits) { view in
                        HabitRow(
                            view: view,
                            onToggle: { Task { await model.toggleToday(id: view.habit.id) } },
                            onRemove: { Task { await model.remove(id: view.habit.id) } }
                        )
                    }
                    .listStyle(.plain)
                }
            }
            .padding()
            .navigationTitle("Habit sync")
            .task { await model.refresh() }
            .refreshable { await model.refresh() }
        }
    }
}

private struct HabitRow: View {
    let view: HabitView
    let onToggle: () -> Void
    let onRemove: () -> Void

    var body: some View {
        HStack {
            Button(action: onToggle) {
                Image(systemName: view.doneToday ? "checkmark.circle.fill" : "circle")
                    .foregroundStyle(.orange)
            }
            .buttonStyle(.plain)
            VStack(alignment: .leading, spacing: 2) {
                Text(view.habit.name).font(.headline)
                Text("\(view.streak)-day streak · \(view.doneThisWeek)/\(view.habit.goalPerWeek) this week" + (view.goalMet ? " ✓" : ""))
                    .font(.caption)
                    .foregroundStyle(view.goalMet ? Color.teal : Color.secondary)
            }
            Spacer()
            Button("Delete", role: .destructive, action: onRemove)
                .buttonStyle(.bordered)
        }
        .padding(.vertical, 4)
    }
}
