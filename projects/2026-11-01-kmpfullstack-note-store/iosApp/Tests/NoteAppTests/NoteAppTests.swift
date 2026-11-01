import XCTest
@testable import NoteApp

private final class FakeRepository: NoteRepository {
    var notes: [Note]

    init(notes: [Note] = []) {
        self.notes = notes
    }

    func list() async throws -> [Note] { notes }

    func add(title: String, body: String) async throws -> Note {
        let note = Note(id: (notes.map(\.id).max() ?? 0) + 1, title: title, body: body)
        notes.append(note)
        return note
    }

    func togglePin(id: Int) async throws -> Note? {
        guard let index = notes.firstIndex(where: { $0.id == id }) else { return nil }
        notes[index].pinned.toggle()
        return notes[index]
    }

    func remove(id: Int) async throws -> Bool {
        let before = notes.count
        notes.removeAll { $0.id == id }
        return notes.count < before
    }
}

final class NoteAppTests: XCTestCase {
    private let sample = [
        Note(id: 1, title: "banana bread", body: "ripe bananas, flour"),
        Note(id: 2, title: "Apple pie", body: "granny smith", pinned: true),
        Note(id: 3, title: "Zebra facts", body: "stripes. Also eats APPLE sometimes"),
    ]

    func testPinnedFirstThenAlphabetical() {
        XCTAssertEqual(arrange(sample).map(\.note.title), ["Apple pie", "banana bread", "Zebra facts"])
    }

    func testSearchIsCaseInsensitiveOverTitleAndBody() {
        XCTAssertEqual(arrange(sample, query: "apple").map(\.note.id), [2, 3])
        XCTAssertTrue(arrange(sample, query: "kiwi").isEmpty)
    }

    func testValidation() {
        XCTAssertNotNil(validateNote(title: "   ", body: ""))
        XCTAssertNotNil(validateNote(title: String(repeating: "x", count: maxTitleLength + 1), body: ""))
        XCTAssertNotNil(validateNote(title: "ok", body: String(repeating: "y", count: maxBodyLength + 1)))
        XCTAssertNil(validateNote(title: "ok", body: "fine"))
    }

    func testPreviewCollapsesWhitespaceAndTruncates() {
        XCTAssertEqual(previewOf("  a\n b\t c "), "a b c")
        let long = previewOf(String(repeating: "word ", count: 40))
        XCTAssertEqual(long.count, previewLength)
        XCTAssertTrue(long.hasSuffix("…"))
    }

    func testModelAddPinRemove() async {
        let repo = FakeRepository(notes: sample)
        let model = NoteModel(repository: repo)
        model.titleInput = "  Tea "
        model.bodyInput = " oolong "
        await model.add()
        XCTAssertEqual(repo.notes.last, Note(id: 4, title: "Tea", body: "oolong"))
        XCTAssertEqual(model.titleInput, "")

        await model.togglePin(id: 1)
        XCTAssertEqual(model.notes.first?.note.id, 1)

        await model.remove(id: 1)
        XCTAssertFalse(model.notes.contains { $0.note.id == 1 })
        await model.remove(id: 1)
        XCTAssertEqual(model.errorMessage, "No note with id 1")
    }

    func testModelRejectsBlankTitle() async {
        let model = NoteModel(repository: FakeRepository())
        await model.add()
        XCTAssertEqual(model.errorMessage, "Give the note a title")
    }
}
