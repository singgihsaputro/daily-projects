import SwiftUI

struct AddSleepEntrySheet: View {
    let onSave: (Double, SleepQuality) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var hours: Double = 7.5
    @State private var quality: SleepQuality = .good

    var body: some View {
        NavigationStack {
            Form {
                Section("Hours slept") {
                    Stepper(value: $hours, in: 0...12, step: 0.5) {
                        Text(String(format: "%.1f hours", hours))
                    }
                }

                Section("Quality") {
                    Picker("Quality", selection: $quality) {
                        ForEach(SleepQuality.allCases) { option in
                            Text(option.label).tag(option)
                        }
                    }
                    .pickerStyle(.segmented)
                }
            }
            .navigationTitle("Log tonight")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        onSave(hours, quality)
                        dismiss()
                    }
                }
            }
        }
    }
}
