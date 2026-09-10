import SwiftUI

struct HabitRowView: View {
    let habit: Habit
    let onToggle: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Text(habit.emoji)
                .font(.system(size: 28))

            VStack(alignment: .leading, spacing: 4) {
                Text(habit.name)
                    .font(.headline)

                HStack(spacing: 4) {
                    Text("🔥 \(habit.streak) day streak")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                HStack(spacing: 3) {
                    ForEach(Array(habit.last7Days.enumerated()), id: \.offset) { _, done in
                        Circle()
                            .fill(done ? Color.accentColor : Color.secondary.opacity(0.25))
                            .frame(width: 6, height: 6)
                    }
                }
            }

            Spacer()

            Button(action: onToggle) {
                Image(systemName: habit.completedToday ? "checkmark.circle.fill" : "circle")
                    .font(.system(size: 26))
                    .foregroundStyle(habit.completedToday ? Color.accentColor : Color.secondary)
            }
            .buttonStyle(.plain)
        }
        .padding(.vertical, 6)
    }
}
