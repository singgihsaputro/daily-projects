import Testing
@testable import ShortLinkApp

struct UrlValidationTests {
    @Test func acceptsHttpAndHttps() {
        #expect(isValidHttpUrl("https://kotlinlang.org/docs/multiplatform.html"))
        #expect(isValidHttpUrl("  http://example.com/path?query=1  "))
    }

    @Test func rejectsMissingSchemeOrHost() {
        #expect(!isValidHttpUrl("kotlinlang.org"))
        #expect(!isValidHttpUrl("ftp://example.com"))
        #expect(!isValidHttpUrl("https://no spaces.com"))
        #expect(!isValidHttpUrl(""))
    }
}

private final class FakeLinkRepository: LinkRepository {
    private(set) var links: [ShortLink]

    init(seed: [ShortLink] = []) {
        links = seed
    }

    func list() async throws -> [ShortLink] { links }

    func create(longUrl: String) async throws -> ShortLink {
        let link = ShortLink(code: "code\(links.count)", longUrl: longUrl, createdAtEpochSeconds: Int64(links.count))
        links.append(link)
        return link
    }

    func visit(code: String) async throws -> ShortLink? {
        guard let index = links.firstIndex(where: { $0.code == code }) else { return nil }
        links[index] = ShortLink(
            code: links[index].code,
            longUrl: links[index].longUrl,
            createdAtEpochSeconds: links[index].createdAtEpochSeconds,
            clicks: links[index].clicks + 1
        )
        return links[index]
    }
}

@MainActor
struct ShortLinkModelTests {
    @Test func addLinkRejectsAMalformedUrl() async {
        let model = ShortLinkModel(repository: FakeLinkRepository())
        model.newUrlInput = "not-a-url"
        await model.addLink()
        #expect(model.errorMessage != nil)
        #expect(model.links.isEmpty)
    }

    @Test func addLinkStoresAValidUrlAndClearsTheInput() async {
        let model = ShortLinkModel(repository: FakeLinkRepository())
        model.newUrlInput = "https://example.com/page"
        await model.addLink()
        #expect(model.newUrlInput.isEmpty)
        #expect(model.links.first?.longUrl == "https://example.com/page")
    }

    @Test func visitIncrementsClicks() async {
        let seed = [ShortLink(code: "abc123", longUrl: "https://example.com", createdAtEpochSeconds: 1, clicks: 2)]
        let model = ShortLinkModel(repository: FakeLinkRepository(seed: seed))
        await model.refresh()
        await model.visit(code: "abc123")
        #expect(model.links.first?.clicks == 3)
    }
}
