import Foundation

public protocol PlantRepository: Sendable {
    func loadPlants() throws -> [Plant]
}

public enum PlantRepositoryError: Error, Equatable {
    case missingResource
    case invalidData
}

/// Reads plants from the bundled `mock/plants.json` fixture.
/// A real client (server or iCloud) replaces this one type.
public struct MockPlantRepository: PlantRepository {
    private struct Payload: Decodable {
        struct Item: Decodable {
            let id: String
            let name: String
            let room: String
            let everyDays: Int
            let lastWatered: String
        }
        let plants: [Item]
    }

    private let data: Data?

    public init() {
        data = Bundle.module.url(forResource: "plants", withExtension: "json")
            .flatMap { try? Data(contentsOf: $0) }
    }

    public init(data: Data) {
        self.data = data
    }

    public func loadPlants() throws -> [Plant] {
        guard let data else { throw PlantRepositoryError.missingResource }
        guard let payload = try? JSONDecoder().decode(Payload.self, from: data) else {
            throw PlantRepositoryError.invalidData
        }
        return try payload.plants.map { item in
            guard item.everyDays > 0, DayMath.date(from: item.lastWatered) != nil else {
                throw PlantRepositoryError.invalidData
            }
            return Plant(id: item.id, name: item.name, room: item.room,
                         everyDays: item.everyDays, lastWatered: item.lastWatered)
        }
    }
}
