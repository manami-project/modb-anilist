package io.github.manamiproject.modb.anilist

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.APPLICATION_JSON
import io.github.manamiproject.modb.core.httpclient.retry.RetryBehavior
import io.github.manamiproject.modb.core.httpclient.retry.RetryableRegistry
import io.github.manamiproject.modb.test.MockServerTestCase
import io.github.manamiproject.modb.test.WireMockServerCreator
import io.github.manamiproject.modb.test.loadTestResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

internal class AnilistDefaultTokenRetrieverTest : MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @AfterEach
    override fun afterEach() {
        serverInstance.stop()
        RetryableRegistry.clear()
    }

    @Test
    fun `throw exception if the csrf token cannot be retrieved`() {
        // given
        val testAnilistConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/$id")
            override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
        }

        RetryableRegistry.register(testAnilistConfig.hostname(), RetryBehavior(retryOnResponsePredicate = { false }))

        val anilistTokenRetriever = AnilistDefaultTokenRetriever(testAnilistConfig)

        serverInstance.stubFor(
            get(urlPathEqualTo("/")).willReturn(
                aResponse()
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withHeader(
                        "set-cookie",
                        "__cfduid=db93afbdcce117dd877b809ce8b6dde941579726597; expires=Fri, 21-Feb-20 20:56:37 GMT; path=/; domain=.anilist.co; HttpOnly; SameSite=Lax; Secure",
                        "laravel_session=NOz33Vu7KGVZK4TZqSES3lmv14JmKbe9IrHN4LnL; expires=Thu, 23-Jan-2020 08:56:37 GMT; Max-Age=43200; path=/; httponly")
                    .withStatus(200)
            )
        )

        // when
        val result = assertThrows<IllegalStateException> {
            anilistTokenRetriever.retrieveToken()
        }

        // then
        assertThat(result).hasMessage("Unable to extract CSRF token.")
    }

    @Test
    fun `throw exception if the cookie cannot be retrieved`() {
        // given
        val testAnilistConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/$id")
            override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
        }

        RetryableRegistry.register(testAnilistConfig.hostname(), RetryBehavior(retryOnResponsePredicate = { false }))

        val anilistTokenRetriever = AnilistDefaultTokenRetriever(testAnilistConfig)

        val responseBody = loadTestResource("token_retriever_tests/page_containing_token.html")

        serverInstance.stubFor(
            get(urlPathEqualTo("/")).willReturn(
                aResponse()
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withStatus(200)
                    .withBody(responseBody)
            )
        )

        // when
        val result = assertThrows<IllegalStateException> {
            anilistTokenRetriever.retrieveToken()
        }

        // then
        assertThat(result).hasMessage("Unable to extract cookie.")
    }

    @Test
    fun `correctly retrieve token`() {
        // given
        val testAnilistConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildDataDownloadLink(id: String): URI = URI("http://${hostname()}:$port/$id")
            override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
        }

        RetryableRegistry.register(testAnilistConfig.hostname(), RetryBehavior(retryOnResponsePredicate = { false }))

        val anilistTokenRetriever = AnilistDefaultTokenRetriever(testAnilistConfig)

        val responseBody = loadTestResource("token_retriever_tests/page_containing_token.html")

        serverInstance.stubFor(
            get(urlPathEqualTo("/")).willReturn(
                aResponse()
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withHeader(
                        "set-cookie",
                        "__cfduid=db93afbdcce117dd877b809ce8b6dde941579726597; expires=Fri, 21-Feb-20 20:56:37 GMT; path=/; domain=.anilist.co; HttpOnly; SameSite=Lax; Secure",
                        "laravel_session=NOz33Vu7KGVZK4TZqSES3lmv14JmKbe9IrHN4LnL; expires=Thu, 23-Jan-2020 08:56:37 GMT; Max-Age=43200; path=/; httponly")
                    .withStatus(200)
                    .withBody(responseBody)
            )
        )

        // when
        val result = anilistTokenRetriever.retrieveToken()

        // then
        assertThat(result.cookie).isEqualTo("__cfduid=db93afbdcce117dd877b809ce8b6dde941579726597; laravel_session=NOz33Vu7KGVZK4TZqSES3lmv14JmKbe9IrHN4LnL")
        assertThat(result.csrfToken).isEqualTo("IAasRzCsdYp2b5QWQEWtMzSvDzf8UboK0GiH907Y")
    }
}