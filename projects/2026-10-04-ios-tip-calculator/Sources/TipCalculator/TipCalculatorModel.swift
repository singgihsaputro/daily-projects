import Foundation
import Observation
import TipCore

@Observable
@MainActor
final class TipCalculatorModel {
    var guides: [TippingGuide] = []
    var loadError: String?
    var selectedCountry = "" {
        didSet { applyDefaults() }
    }
    var billText = ""
    var percent = 18
    var people = 1
    var roundUp = false

    private let repository: TippingRepository

    init(repository: TippingRepository = MockTippingRepository()) {
        self.repository = repository
        load()
    }

    var guide: TippingGuide? { guides.first { $0.country == selectedCountry } }
    var currency: String { guide?.currency ?? "USD" }
    var billCents: Int? { TipCalculator.parseCents(billText) }

    var breakdown: TipBreakdown {
        TipCalculator.calculate(billCents: billCents ?? 0, percent: percent, people: people, roundUp: roundUp)
    }

    func money(_ cents: Int) -> String {
        TipCalculator.format(cents: cents, currency: currency)
    }

    private func load() {
        do {
            guides = try repository.guides()
            selectedCountry = guides.first?.country ?? ""
        } catch {
            loadError = "Could not load tipping guides: \(error.localizedDescription)"
        }
    }

    private func applyDefaults() {
        if let guide { percent = guide.defaultPercent }
    }
}
