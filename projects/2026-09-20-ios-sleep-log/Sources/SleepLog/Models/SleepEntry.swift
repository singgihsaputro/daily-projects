import Foundation

enum SleepQuality: String, Codable, CaseIterable, Identifiable {
    case poor, fair, good, great

    var id: String { rawValue }

    var label: String {
        switch self {
        case .poor: return "Poor"
        case .fair: return "Fair"
        case .good: return "Good"
        case .great: return "Great"
        }
    }

    var symbol: String {
        switch self {
        case .poor: return "cloud.rain"
        case .fair: return "cloud"
        case .good: return "cloud.sun"
        case .great: return "sun.max"
        }
    }
}

struct SleepEntry: Identifiable, Codable, Equatable {
    let id: String
    let date: String
    var hours: Double
    var quality: SleepQuality
}
