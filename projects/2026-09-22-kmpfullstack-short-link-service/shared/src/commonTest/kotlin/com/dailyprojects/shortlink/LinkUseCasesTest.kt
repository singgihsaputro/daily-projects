package com.dailyprojects.shortlink

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private class FakeLinkRepository(seed: List<ShortLink> = emptyList()) : LinkRepository {
    private val links = seed.toMutableList()

    override suspend fun list(): List<ShortLink> = links.toList()

    override suspend fun create(longUrl: String): ShortLink {
        val link = ShortLink(
            code = ShortCodeGenerator.generate(links.map { it.code }.toSet()),
            longUrl = longUrl,
            createdAtEpochSeconds = links.size.toLong(),
        )
        links += link
        return link
    }

    override suspend fun visit(code: String): ShortLink? {
        val index = links.indexOfFirst { it.code == code }
        if (index == -1) return null
        val updated = links[index].copy(clicks = links[index].clicks + 1)
        links[index] = updated
        return updated
    }
}

class LinkUseCasesTest {

    @Test
    fun createRejectsAMalformedUrl() = runTest {
        val useCase = CreateShortLinkUseCase(FakeLinkRepository())
        val result = useCase("not-a-url")
        assertIs<LinkResult.Failure>(result)
    }

    @Test
    fun createStoresAValidUrl() = runTest {
        val repository = FakeLinkRepository()
        val result = CreateShortLinkUseCase(repository)("https://example.com/page")
        assertIs<LinkResult.Success<ShortLink>>(result)
        assertEquals("https://example.com/page", result.value.longUrl)
        assertEquals(0, result.value.clicks)
    }

    @Test
    fun listOrdersNewestFirst() = runTest {
        val repository = FakeLinkRepository(
            seed = listOf(
                ShortLink("aaa111", "https://old.example.com", createdAtEpochSeconds = 1),
                ShortLink("bbb222", "https://new.example.com", createdAtEpochSeconds = 99),
            ),
        )
        val links = ListLinksUseCase(repository)()
        assertEquals("bbb222", links.first().code)
    }

    @Test
    fun visitIncrementsClicksOnAKnownCode() = runTest {
        val repository = FakeLinkRepository(seed = listOf(ShortLink("aaa111", "https://example.com", 1, clicks = 2)))
        val result = VisitLinkUseCase(repository)("aaa111")
        assertIs<LinkResult.Success<ShortLink>>(result)
        assertEquals(3, result.value.clicks)
    }

    @Test
    fun visitFailsOnAnUnknownCode() = runTest {
        val result = VisitLinkUseCase(FakeLinkRepository())("missing")
        assertIs<LinkResult.Failure>(result)
        assertTrue(result.message.contains("missing"))
    }
}
