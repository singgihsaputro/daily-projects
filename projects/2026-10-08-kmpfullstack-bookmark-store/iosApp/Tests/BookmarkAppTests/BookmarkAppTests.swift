import Testing
@testable import BookmarkApp

struct ValidationTests {
    @Test func acceptsHttpAndHttps() {
        #expect(isValidHttpUrl("https://kotlinlang.org/docs/multiplatform.html"))
        #expect(isValidHttpUrl("  http://example.com  "))
    }

    @Test func rejectsMissingSchemeOrHost() {
        #expect(!isValidHttpUrl("kotlinlang.org"))
        #expect(!isValidHttpUrl("ftp://example.com"))
        #expect(!isValidHttpUrl("https://no spaces.com"))
    }

    @Test func normalizesTags() {
        #expect(normalizeTag("  Kotlin ") == "kotlin")
        #expect(normalizeTag("Build   Tools") == "build-tools")
        #expect(normalizeTag("   ") == "")
    }
}

private final class FakeBookmarkRepository: BookmarkRepository {
    private(set) var items: [Bookmark]

    init(seed: [Bookmark] = []) {
        items = seed
    }

    func list() async throws -> [Bookmark] { items }

    func add(title: String, url: String, tag: String) async throws -> Bookmark {
        let bookmark = Bookmark(id: items.count + 1, title: title, url: url, tag: tag, createdAtEpochSeconds: Int64(items.count))
        items.append(bookmark)
        return bookmark
    }

    func toggleStar(id: Int) async throws -> Bookmark? {
        guard let index = items.firstIndex(where: { $0.id == id }) else { return nil }
        items[index].starred.toggle()
        return items[index]
    }

    func remove(id: Int) async throws -> Bool {
        let before = items.count
        items.removeAll { $0.id == id }
        return items.count < before
    }
}

@MainActor
struct BookmarkModelTests {
    @Test func addRejectsBlankTitleAndBadUrl() async {
        let model = BookmarkModel(repository: FakeBookmarkRepository())
        model.titleInput = "  "
        model.urlInput = "https://example.com"
        await model.add()
        #expect(model.errorMessage != nil)

        model.titleInput = "Docs"
        model.urlInput = "nope"
        await model.add()
        #expect(model.bookmarks.isEmpty)
    }

    @Test func addStoresNormalizedBookmarkAndClearsInputs() async {
        let model = BookmarkModel(repository: FakeBookmarkRepository())
        model.titleInput = " Ktor docs "
        model.urlInput = "https://ktor.io"
        model.tagInput = "Server Side"
        await model.add()
        #expect(model.titleInput.isEmpty)
        #expect(model.bookmarks.first?.title == "Ktor docs")
        #expect(model.bookmarks.first?.tag == "server-side")
    }

    @Test func starredFirstThenNewestAndTagFilter() async {
        let seed = [
            Bookmark(id: 1, title: "old", url: "https://a.io", tag: "kotlin", createdAtEpochSeconds: 1),
            Bookmark(id: 2, title: "new", url: "https://b.io", tag: "kotlin", createdAtEpochSeconds: 9),
            Bookmark(id: 3, title: "starred", url: "https://c.io", tag: "swift", starred: true, createdAtEpochSeconds: 0),
        ]
        let model = BookmarkModel(repository: FakeBookmarkRepository(seed: seed))
        await model.refresh()
        #expect(model.bookmarks.map(\.id) == [3, 2, 1])
        await model.select(tag: "Kotlin")
        #expect(model.bookmarks.map(\.id) == [2, 1])
    }

    @Test func toggleStarAndRemove() async {
        let seed = [Bookmark(id: 1, title: "a", url: "https://a.io", createdAtEpochSeconds: 1)]
        let model = BookmarkModel(repository: FakeBookmarkRepository(seed: seed))
        await model.refresh()
        await model.toggleStar(id: 1)
        #expect(model.bookmarks.first?.starred == true)
        await model.remove(id: 1)
        #expect(model.bookmarks.isEmpty)
    }
}
