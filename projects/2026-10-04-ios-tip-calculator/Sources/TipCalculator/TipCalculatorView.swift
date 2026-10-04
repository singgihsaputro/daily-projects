import SwiftUI
import TipCore

public struct TipCalculatorView: View {
    @State private var model = TipCalculatorModel()

    public init() {}

    public var body: some View {
        NavigationStack {
            Form {
                if let error = model.loadError {
                    Section { Text(error).foregroundStyle(.red) }
                }
                Section("Where are you?") {
                    Picker("Country", selection: $model.selectedCountry) {
                        ForEach(model.guides) { guide in
                            Text("\(guide.flag) \(guide.country)").tag(guide.country)
                        }
                    }
                    if let guide = model.guide {
                        Text(guide.note).font(.footnote).foregroundStyle(.secondary)
                    }
                }
                Section("Bill") {
                    TextField("Amount (\(model.currency))", text: $model.billText)
                        #if os(iOS)
                        .keyboardType(.decimalPad)
                        #endif
                    if !model.billText.isEmpty && model.billCents == nil {
                        Text("Enter a number like 42.50").font(.footnote).foregroundStyle(.red)
                    }
                }
                Section("Tip: \(model.percent)%") {
                    if let guide = model.guide {
                        Picker("Suggested", selection: $model.percent) {
                            ForEach(guide.suggested, id: \.self) { Text("\($0)%").tag($0) }
                        }
                        .pickerStyle(.segmented)
                    }
                    Slider(value: percentBinding, in: 0...30, step: 1)
                    Stepper("Split between \(model.people)", value: $model.people, in: 1...20)
                    Toggle("Round total up", isOn: $model.roundUp)
                }
                Section("Result") {
                    row("Tip", model.breakdown.tipCents)
                    row("Total", model.breakdown.totalCents)
                    row("Each person", model.breakdown.perPersonCents, emphasised: true)
                }
            }
            .navigationTitle("Tip Calculator")
        }
    }

    private var percentBinding: Binding<Double> {
        Binding(get: { Double(model.percent) }, set: { model.percent = Int($0) })
    }

    private func row(_ title: String, _ cents: Int, emphasised: Bool = false) -> some View {
        HStack {
            Text(title)
            Spacer()
            Text(model.money(cents))
                .font(emphasised ? .title2.bold() : .body)
                .monospacedDigit()
        }
    }
}
