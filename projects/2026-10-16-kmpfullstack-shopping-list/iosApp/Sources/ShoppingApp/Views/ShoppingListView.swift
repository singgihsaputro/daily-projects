import SwiftUI

public struct ShoppingListView: View {
    @State private var model = ShoppingModel()

    public init() {}

    public var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 10) {
                Text("Pointed at server/ on http://localhost:8080")
                    .font(.caption)
                    .foregroundStyle(.secondary)

                HStack {
                    TextField("Item", text: $model.nameInput).textFieldStyle(.roundedBorder)
                    Stepper("Qty \(model.quantityInput)", value: $model.quantityInput, in: 1...maxQuantity)
                        .fixedSize()
                }
                HStack {
                    TextField("Category (optional)", text: $model.categoryInput).textFieldStyle(.roundedBorder)
                    Button("Add") { Task { await model.add() } }
                        .buttonStyle(.borderedProminent)
                }

                if let message = model.errorMessage {
                    Text(message).font(.caption).foregroundStyle(.red)
                }

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack {
                        categoryChip(label: "All", category: "")
                        ForEach(model.categories, id: \.self) { categoryChip(label: $0, category: $0) }
                    }
                }

                if model.isLoading && model.items.isEmpty {
                    ProgressView("Loading...").frame(maxWidth: .infinity)
                } else {
                    List(model.items) { item in
                        ItemRow(
                            item: item,
                            onCheck: { Task { await model.toggleChecked(id: item.id) } },
                            onRemove: { Task { await model.remove(id: item.id) } }
                        )
                    }
                    .listStyle(.plain)
                }

                if model.hasCheckedItems {
                    Button("Clear ticked items") { Task { await model.clearChecked() } }
                        .buttonStyle(.bordered)
                }
            }
            .padding()
            .navigationTitle("Shopping list")
            .task { await model.refresh() }
            .refreshable { await model.refresh() }
        }
    }

    private func categoryChip(label: String, category: String) -> some View {
        Button(label) { Task { await model.select(category: category) } }
            .buttonStyle(.bordered)
            .tint(model.selectedCategory == category ? .green : .gray)
    }
}

private struct ItemRow: View {
    let item: ShoppingItem
    let onCheck: () -> Void
    let onRemove: () -> Void

    var body: some View {
        HStack {
            Button(action: onCheck) {
                Image(systemName: item.checked ? "checkmark.circle.fill" : "circle")
                    .foregroundStyle(.green)
            }
            .buttonStyle(.plain)
            VStack(alignment: .leading, spacing: 2) {
                Text("\(item.name) × \(item.quantity)")
                    .font(.headline)
                    .strikethrough(item.checked)
                    .foregroundStyle(item.checked ? .secondary : .primary)
                Text(item.category.isEmpty ? "uncategorised" : item.category)
                    .font(.caption)
                    .foregroundStyle(.tertiary)
            }
            Spacer()
            Button("Delete", role: .destructive, action: onRemove)
                .buttonStyle(.bordered)
        }
        .padding(.vertical, 4)
    }
}
