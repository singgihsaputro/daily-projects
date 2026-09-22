import Foundation

/// Talks to the real `server/` module over HTTP. The iOS Simulator shares the host's
/// network stack, so `localhost` reaches the same server Android reaches via `10.0.2.2`.
final class HttpLinkRepository: NSObject, LinkRepository {
    private let baseUrl: URL
    private lazy var noRedirectSession = URLSession(configuration: .default, delegate: self, delegateQueue: nil)
    private let decoder = JSONDecoder()
    private let encoder = JSONEncoder()

    init(baseUrl: URL = URL(string: "http://localhost:8080")!) {
        self.baseUrl = baseUrl
    }

    func list() async throws -> [ShortLink] {
        let (data, _) = try await URLSession.shared.data(from: baseUrl.appendingPathComponent("links"))
        return try decoder.decode([ShortLink].self, from: data)
    }

    func create(longUrl: String) async throws -> ShortLink {
        var request = URLRequest(url: baseUrl.appendingPathComponent("links"))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try encoder.encode(CreateLinkRequest(longUrl: longUrl))

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse else {
            throw LinkRepositoryError.server("No response from server")
        }
        guard http.statusCode == 201 else {
            let apiError = try? decoder.decode(ApiError.self, from: data)
            throw LinkRepositoryError.invalidUrl(apiError?.message ?? "Enter a full http:// or https:// URL")
        }
        return try decoder.decode(ShortLink.self, from: data)
    }

    func visit(code: String) async throws -> ShortLink? {
        // The endpoint answers with a redirect to longUrl; this client only needs the
        // click to be counted, not to actually follow it — hence the no-redirect session.
        let (_, response) = try await noRedirectSession.data(from: baseUrl.appendingPathComponent("r/\(code)"))
        guard let http = response as? HTTPURLResponse, http.statusCode == 302 else {
            throw LinkRepositoryError.notFound("No link with code '\(code)'")
        }
        return try await list().first { $0.code == code }
    }
}

extension HttpLinkRepository: URLSessionTaskDelegate {
    func urlSession(
        _ session: URLSession,
        task: URLSessionTask,
        willPerformHTTPRedirection response: HTTPURLResponse,
        newRequest request: URLRequest,
        completionHandler: @escaping (URLRequest?) -> Void
    ) {
        completionHandler(nil)
    }
}
