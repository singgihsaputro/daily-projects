import Testing
import Foundation
@testable import PlantCore

@Suite struct PlantTests {
    let plan = WateringPlan(today: "2026-10-28")

    func plant(every: Int, last: String) -> Plant {
        Plant(id: "p", name: "P", room: "R", everyDays: every, lastWatered: last)
    }

    @Test func overdueDueAndUpcoming() {
        #expect(plan.status(of: plant(every: 5, last: "2026-10-22")) == .overdue(days: 1))
        #expect(plan.status(of: plant(every: 7, last: "2026-10-21")) == .dueToday)
        #expect(plan.status(of: plant(every: 3, last: "2026-10-27")) == .upcoming(days: 2))
    }

    @Test func monthBoundaryMath() {
        #expect(DayMath.days(from: "2026-10-30", to: "2026-11-02") == 3)
        #expect(DayMath.adding(days: 3, to: "2026-12-30") == "2027-01-02")
    }

    @Test func rejectsInvalidDates() {
        #expect(DayMath.date(from: "2026-02-30") == nil)
        #expect(DayMath.date(from: "nope") == nil)
    }

    @Test func sortsMostUrgentFirst() throws {
        let plants = try MockPlantRepository().loadPlants()
        let sorted = plan.sorted(plants)
        #expect(sorted.map(\.id) == ["aloe", "pothos", "monstera", "snake", "basil", "fern"])
        #expect(plan.dueCount(in: plants) == 4)
    }

    @Test func labels() {
        #expect(WaterStatus.overdue(days: 1).label == "1 day overdue")
        #expect(WaterStatus.upcoming(days: 1).label == "Tomorrow")
        #expect(WaterStatus.dueToday.needsWater)
    }

    @Test func invalidFixtureThrows() {
        let bad = Data(#"{"plants":[{"id":"a","name":"A","room":"R","everyDays":0,"lastWatered":"2026-10-01"}]}"#.utf8)
        #expect(throws: PlantRepositoryError.invalidData) {
            try MockPlantRepository(data: bad).loadPlants()
        }
    }
}
