import Foundation

/// Result of splitting a bill. All amounts are in cents so the arithmetic is exact.
public struct TipBreakdown: Equatable, Sendable {
    public let billCents: Int
    public let tipCents: Int
    public let totalCents: Int
    /// What each diner pays, in order. Differs by at most one cent between diners.
    public let shareCents: [Int]

    public var perPersonCents: Int { shareCents.max() ?? 0 }
}

public enum TipCalculator {
    /// - Parameters:
    ///   - billCents: pre-tip bill, negative values are treated as zero.
    ///   - percent: tip percentage, clamped to 0...100.
    ///   - people: number of diners, at least 1.
    ///   - roundUp: round the total up to the next whole currency unit.
    public static func calculate(billCents: Int, percent: Int, people: Int, roundUp: Bool = false) -> TipBreakdown {
        let bill = max(billCents, 0)
        let pct = min(max(percent, 0), 100)
        let diners = max(people, 1)

        // Half-up rounding of bill * pct / 100.
        var tip = (bill * pct + 50) / 100
        if roundUp {
            let remainder = (bill + tip) % 100
            if remainder != 0 { tip += 100 - remainder }
        }
        let total = bill + tip

        let base = total / diners
        let extra = total % diners
        let shares = (0..<diners).map { $0 < extra ? base + 1 : base }
        return TipBreakdown(billCents: bill, tipCents: tip, totalCents: total, shareCents: shares)
    }

    /// Parses user input such as "42.50" or "42,5" into cents. Returns nil when invalid.
    public static func parseCents(_ text: String) -> Int? {
        let cleaned = text.trimmingCharacters(in: .whitespaces).replacingOccurrences(of: ",", with: ".")
        guard !cleaned.isEmpty,
              cleaned.allSatisfy({ $0.isNumber || $0 == "." }),
              cleaned.filter({ $0 == "." }).count <= 1 else { return nil }
        let parts = cleaned.split(separator: ".", omittingEmptySubsequences: false)
        guard let whole = Int(parts[0].isEmpty ? "0" : String(parts[0])) else { return nil }
        var cents = 0
        if parts.count == 2 {
            let frac = String(parts[1].prefix(2)).padding(toLength: 2, withPad: "0", startingAt: 0)
            cents = Int(frac) ?? 0
        }
        return whole * 100 + cents
    }

    public static func format(cents: Int, currency: String) -> String {
        let style = FloatingPointFormatStyle<Double>.Currency(code: currency)
        return (Double(cents) / 100).formatted(style)
    }
}
