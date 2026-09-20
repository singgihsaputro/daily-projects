import SwiftUI

extension SleepQuality {
    var tint: Color {
        switch self {
        case .poor: return .red
        case .fair: return .orange
        case .good: return .blue
        case .great: return .green
        }
    }
}

struct SleepEntryRowView: View {
    let entry: SleepEntry
    let maxHours: Double

    private var barFraction: Double {
        maxHours == 0 ? 0 : entry.hours / maxHours
    }

    var body: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text(SleepLogModel.displayDate(for: entry.date))
                    .font(.subheadline)
                Label(entry.quality.label, systemImage: entry.quality.symbol)
                    .font(.caption)
                    .foregroundStyle(entry.quality.tint)
            }
            .frame(width: 120, alignment: .leading)

            GeometryReader { proxy in
                RoundedRectangle(cornerRadius: 4)
                    .fill(entry.quality.tint.opacity(0.8))
                    .frame(width: proxy.size.width * barFraction)
                    .frame(maxHeight: .infinity)
            }
            .frame(height: 18)

            Text(String(format: "%.1fh", entry.hours))
                .font(.subheadline.monospacedDigit())
                .frame(width: 44, alignment: .trailing)
        }
        .padding(.vertical, 4)
    }
}
