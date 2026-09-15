import SwiftUI

struct PackingItemRowView: View {
    let item: PackingItem
    let onToggle: () -> Void

    var body: some View {
        Button(action: onToggle) {
            HStack(spacing: 12) {
                Image(systemName: item.packed ? "checkmark.circle.fill" : "circle")
                    .font(.system(size: 22))
                    .foregroundStyle(item.packed ? Color.accentColor : Color.secondary)

                Text(item.name)
                    .strikethrough(item.packed)
                    .foregroundStyle(item.packed ? .secondary : .primary)

                Spacer()
            }
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .padding(.vertical, 4)
    }
}
