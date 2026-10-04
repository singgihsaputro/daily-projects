import Foundation

/// Tipping customs for one country, decoded from `mock/tipping.json`.
public struct TippingGuide: Codable, Identifiable, Equatable, Sendable {
    public let country: String
    public let flag: String
    public let currency: String
    public let note: String
    public let suggested: [Int]
    public let defaultPercent: Int

    public var id: String { country }
}

/// Source of tipping guides. A networked implementation can replace the mock
/// one without touching the model or views.
public protocol TippingRepository: Sendable {
    func guides() throws -> [TippingGuide]
}

public struct MockTippingRepository: TippingRepository {
    public init() {}

    public func guides() throws -> [TippingGuide] {
        guard let url = Bundle.module.url(forResource: "tipping", withExtension: "json") else {
            throw CocoaError(.fileNoSuchFile)
        }
        return try JSONDecoder().decode([TippingGuide].self, from: Data(contentsOf: url))
    }
}
