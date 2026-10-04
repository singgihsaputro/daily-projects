import Testing
@testable import TipCore

@Suite struct TipCalculationTests {
    @Test func basicTip() {
        let r = TipCalculator.calculate(billCents: 10_000, percent: 18, people: 1)
        #expect(r.tipCents == 1_800)
        #expect(r.totalCents == 11_800)
    }

    @Test func splitSharesSumToTotal() {
        let r = TipCalculator.calculate(billCents: 10_000, percent: 20, people: 7)
        #expect(r.shareCents.reduce(0, +) == r.totalCents)
        #expect(r.shareCents.max()! - r.shareCents.min()! <= 1)
    }

    @Test func roundUpGivesWholeUnits() {
        let r = TipCalculator.calculate(billCents: 4_250, percent: 15, people: 1, roundUp: true)
        #expect(r.totalCents % 100 == 0)
        #expect(r.totalCents == 4_900)
    }

    @Test func clampsBadInput() {
        let r = TipCalculator.calculate(billCents: -5, percent: 500, people: 0)
        #expect(r.totalCents == 0)
        #expect(r.shareCents.count == 1)
    }

    @Test func parsing() {
        #expect(TipCalculator.parseCents("42.50") == 4_250)
        #expect(TipCalculator.parseCents("42,5") == 4_250)
        #expect(TipCalculator.parseCents("7") == 700)
        #expect(TipCalculator.parseCents(".5") == 50)
        #expect(TipCalculator.parseCents("abc") == nil)
        #expect(TipCalculator.parseCents("1.2.3") == nil)
        #expect(TipCalculator.parseCents("") == nil)
    }

    @Test func mockRepositoryDecodes() throws {
        let guides = try MockTippingRepository().guides()
        #expect(guides.count == 6)
        #expect(guides.first { $0.country == "Japan" }?.defaultPercent == 0)
    }
}
