import Foundation

/// Talks to the real `server/` module over HTTP. The iOS Simulator shares the host's
/// network stack, so `localhost` reaches the same server Android reaches via `10.0.2.2`.
final class HttpShoppingRepository: ShoppingRepository {
    private let baseUrl: URL
    private let decoder = JSONDecoder()
    private let encoder = JSONEncoder()

    init(baseUrl: URL = URL(string: "http://localhost:8080")!) {
        self.baseUrl = baseUrl
    }

    func list() async throws -> [ShoppingItem] {
        let (data, _) = try await URLSession.shared.data(from: baseUrl.appendingPathComponent("items"))
        return try decoder.decode([ShoppingItem].self, from: data)
    }

    func add(name: String, quantity: Int, category: String) async throws -> ShoppingItem {
        var request = URLRequest(url: baseUrl.appendingPathComponent("items"))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try encoder.encode(CreateItemRequest(name: name, quantity: quantity, category: category))

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse else {
            throw ShoppingRepositoryError.server("No response from server")
        }
        guard http.statusCode == 201 else {
            let apiError = try? decoder.decode(ApiError.self, from: data)
            throw ShoppingRepositoryError.rejected(apiError?.message ?? "The server rejected this item")
        }
        return try decoder.decode(ShoppingItem.self, from: data)
    }

    func toggleChecked(id: Int) async throws -> ShoppingItem? {
        var request = URLRequest(url: baseUrl.appendingPathComponent("items/\(id)/check"))
        request.httpMethod = "POST"
        let (data, response) = try await URLSession.shared.data(for: request)
        guard (response as? HTTPURLResponse)?.statusCode == 200 else { return nil }
        return try decoder.decode(ShoppingItem.self, from: data)
    }

    func remove(id: Int) async throws -> Bool {
        var request = URLRequest(url: baseUrl.appendingPathComponent("items/\(id)"))
        request.httpMethod = "DELETE"
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 204
    }

    func clearChecked() async throws -> Int {
        var request = URLRequest(url: baseUrl.appendingPathComponent("items/checked"))
        request.httpMethod = "DELETE"
        let (data, _) = try await URLSession.shared.data(for: request)
        return try decoder.decode(ClearedResponse.self, from: data).removed
    }
}
