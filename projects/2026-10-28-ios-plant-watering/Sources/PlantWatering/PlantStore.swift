import Foundation
import Observation
import PlantCore

@MainActor
@Observable
final class PlantStore {
    private(set) var plants: [Plant] = []
    private(set) var loadError: String?
    let plan: WateringPlan

    init(repository: PlantRepository = MockPlantRepository(), today: String = "2026-10-28") {
        plan = WateringPlan(today: today)
        do {
            plants = plan.sorted(try repository.loadPlants())
        } catch {
            loadError = "Could not load plants."
        }
    }

    var dueCount: Int { plan.dueCount(in: plants) }

    func status(of plant: Plant) -> WaterStatus { plan.status(of: plant) }

    func water(_ plant: Plant) {
        guard let index = plants.firstIndex(where: { $0.id == plant.id }) else { return }
        plants[index].lastWatered = plan.today
        plants = plan.sorted(plants)
    }
}
