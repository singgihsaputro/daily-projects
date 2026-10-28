import SwiftUI
import PlantCore

public struct PlantRootView: View {
    @State private var store = PlantStore()

    public init() {}

    public var body: some View {
        NavigationStack {
            List {
                if let error = store.loadError {
                    Text(error).foregroundStyle(.red)
                } else {
                    Section {
                        summary
                    }
                    Section("Plants") {
                        ForEach(store.plants) { plant in
                            row(for: plant)
                        }
                    }
                }
            }
            .navigationTitle("Plant Watering")
        }
    }

    private var summary: some View {
        HStack {
            Image(systemName: "drop.fill").foregroundStyle(.blue)
            Text(store.dueCount == 0
                 ? "All plants are watered"
                 : "\(store.dueCount) of \(store.plants.count) need water")
                .font(.headline)
        }
    }

    private func row(for plant: Plant) -> some View {
        let status = store.status(of: plant)
        return HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(plant.name).font(.body.weight(.semibold))
                Text("\(plant.room) · every \(plant.everyDays) days")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Text(status.label)
                .font(.subheadline)
                .foregroundStyle(color(for: status))
            if status.needsWater {
                Button("Water") { store.water(plant) }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.small)
            }
        }
    }

    private func color(for status: WaterStatus) -> Color {
        switch status {
        case .overdue: return .red
        case .dueToday: return .orange
        case .upcoming: return .secondary
        }
    }
}
