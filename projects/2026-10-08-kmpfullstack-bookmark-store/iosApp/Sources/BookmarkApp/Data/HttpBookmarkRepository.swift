import Foundation

/// Talks to the real `server/` module over HTTP. The iOS Simulator shares the host's
/// network stack, so `localhost` reaches the same server Android reaches via `10.0.2.2`.
final class HttpBookmarkRepository: BookmarkRepository {
    private let baseUrl: URL
    private let decoder = JSONDecoder()
    private let encoder = JSONEncoder()

    init(baseUrl: URL = URL(string: "http://localhost:8080")!) {
        self.baseUrl = baseUrl
    }

    func list() async throws -> [Bookmark] {
        let (data, _) = try await URLSession.shared.data(from: baseUrl.appendingPathComponent("bookmarks"))
        return try decoder.decode([Bookmark].self, from: data)
    }

    func add(title: String, url: String, tag: String) async throws -> Bookmark {
        var request = URLRequest(url: baseUrl.appendingPathComponent("bookmarks"))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try encoder.encode(CreateBookmarkRequest(title: title, url: url, tag: tag))

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse else {
            throw BookmarkRepositoryError.server("No response from server")
        }
        guard http.statusCode == 201 else {
            let apiError = try? decoder.decode(ApiError.self, from: data)
            throw BookmarkRepositoryError.rejected(apiError?.message ?? "The server rejected this bookmark")
        }
        return try decoder.decode(Bookmark.self, from: data)
    }

    func toggleStar(id: Int) async throws -> Bookmark? {
        var request = URLRequest(url: baseUrl.appendingPathComponent("bookmarks/\(id)/star"))
        request.httpMethod = "POST"
        let (data, response) = try await URLSession.shared.data(for: request)
        guard (response as? HTTPURLResponse)?.statusCode == 200 else { return nil }
        return try decoder.decode(Bookmark.self, from: data)
    }

    func remove(id: Int) async throws -> Bool {
        var request = URLRequest(url: baseUrl.appendingPathComponent("bookmarks/\(id)"))
        request.httpMethod = "DELETE"
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 204
    }
}
