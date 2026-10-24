import Foundation

/// Talks to the real `server/` module over HTTP. The iOS Simulator shares the host's
/// network stack, so `localhost` reaches the same server Android reaches via `10.0.2.2`.
final class HttpHabitRepository: HabitRepository {
    private let baseUrl: URL
    private let decoder = JSONDecoder()
    private let encoder = JSONEncoder()

    init(baseUrl: URL = URL(string: "http://localhost:8080")!) {
        self.baseUrl = baseUrl
    }

    func list() async throws -> [Habit] {
        let (data, _) = try await URLSession.shared.data(from: baseUrl.appendingPathComponent("habits"))
        return try decoder.decode([Habit].self, from: data)
    }

    func add(name: String, goalPerWeek: Int) async throws -> Habit {
        let request = try jsonRequest(path: "habits", body: CreateHabitRequest(name: name, goalPerWeek: goalPerWeek))
        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse else {
            throw HabitRepositoryError.server("No response from server")
        }
        guard http.statusCode == 201 else {
            let apiError = try? decoder.decode(ApiError.self, from: data)
            throw HabitRepositoryError.rejected(apiError?.message ?? "The server rejected this habit")
        }
        return try decoder.decode(Habit.self, from: data)
    }

    func toggleCheckIn(id: Int, date: String) async throws -> Habit? {
        let request = try jsonRequest(path: "habits/\(id)/checkins", body: CheckInRequest(date: date))
        let (data, response) = try await URLSession.shared.data(for: request)
        guard (response as? HTTPURLResponse)?.statusCode == 200 else { return nil }
        return try decoder.decode(Habit.self, from: data)
    }

    func remove(id: Int) async throws -> Bool {
        var request = URLRequest(url: baseUrl.appendingPathComponent("habits/\(id)"))
        request.httpMethod = "DELETE"
        let (_, response) = try await URLSession.shared.data(for: request)
        return (response as? HTTPURLResponse)?.statusCode == 204
    }

    private func jsonRequest<Body: Encodable>(path: String, body: Body) throws -> URLRequest {
        var request = URLRequest(url: baseUrl.appendingPathComponent(path))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try encoder.encode(body)
        return request
    }
}
