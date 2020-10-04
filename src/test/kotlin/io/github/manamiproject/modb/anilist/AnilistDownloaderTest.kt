package io.github.manamiproject.modb.anilist

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.toAnimeId
import io.github.manamiproject.modb.core.httpclient.APPLICATION_JSON
import io.github.manamiproject.modb.core.httpclient.retry.RetryableRegistry
import io.github.manamiproject.modb.test.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URL

internal class AnilistDownloaderTest : MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @BeforeEach
    fun beforeEach() {
        // populate RetryableRegisty
        val testConfig = object: MetaDataProviderConfig by AnilistDefaultTokenRetrieverConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildDataDownloadUrl(id: String): URL = URL("http:localhost:$port")
        }
        AnilistDefaultTokenRetriever(testConfig)
    }

    @AfterEach
    override fun afterEach() {
        serverInstance.stop()
        RetryableRegistry.clear()
    }

    @Test
    fun `retrieves a token upon creation and stores it in the token repository in case no token has been added yet`() {
        // given
        val testToken = AnilistToken("fresh-cookies", "fresh-csrf")
        var hasBeenInvoked = false

        val testAnilistTokenRetriever = object: AnilistTokenRetriever {
            override fun retrieveToken(): AnilistToken {
                hasBeenInvoked = true
                return testToken
            }
        }

        val testAnilistTokenRepository = object: AnilistTokenRepository {
            private var currentToken: AnilistToken = AnilistToken(EMPTY, EMPTY)

            override var token: AnilistToken
                get() = currentToken
                set(value) { currentToken = value }

        }

        // when
        AnilistDownloader(
                config = MetaDataProviderTestConfig,
                anilistTokenRetriever = testAnilistTokenRetriever,
                anilistTokenRepository = testAnilistTokenRepository
        )

        // then
        assertThat(hasBeenInvoked).isTrue()
        assertThat(testAnilistTokenRepository.token).isEqualTo(testToken)
    }

    @Test
    fun `successfully load an entry`() {
        // given
        val id = 1535

        val testAnilistConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLinkUrl(id: AnimeId): URL = AnilistConfig.buildAnimeLinkUrl(id)
            override fun buildDataDownloadUrl(id: String): URL = URL("http://${hostname()}:$port/graphql")
            override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
        }

        @Suppress("UNUSED_PARAMETER")
        val testAnilistTokenRepository = object: AnilistTokenRepository {
            override var token: AnilistToken
                get() = AnilistToken("value", "value")
                set(value) { shouldNotBeInvoked() }
        }

        val responseBody = "{ \"anilistId\": $id }"

        serverInstance.stubFor(
            post(urlPathEqualTo("/graphql")).willReturn(
                aResponse()
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withStatus(200)
                    .withBody(responseBody)
            )
        )

        val downloader = AnilistDownloader(
                config = testAnilistConfig,
                anilistTokenRetriever = TestAnilistTokenRetriever,
                anilistTokenRepository = testAnilistTokenRepository,
        )

        // when
        val result = downloader.download(id.toAnimeId()) { shouldNotBeInvoked() }

        // then
        assertThat(result).isEqualTo(responseBody)
    }

    @Test
    fun `throws an exception if the response body is empty`() {
        // given
        val id = 1535

        val testAnilistConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLinkUrl(id: AnimeId): URL = AnilistConfig.buildAnimeLinkUrl(id)
            override fun buildDataDownloadUrl(id: String): URL = URL("http://${hostname()}:$port/graphql")
            override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
        }

        @Suppress("UNUSED_PARAMETER")
        val testAnilistTokenRepository = object: AnilistTokenRepository {
            override var token: AnilistToken
                get() = AnilistToken("value", "value")
                set(value) { shouldNotBeInvoked() }
        }

        serverInstance.stubFor(
            post(urlPathEqualTo("/graphql")).willReturn(
                aResponse()
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withStatus(200)
                    .withBody(EMPTY)
            )
        )

        val downloader = AnilistDownloader(
                config = testAnilistConfig,
                anilistTokenRetriever = TestAnilistTokenRetriever,
                anilistTokenRepository = testAnilistTokenRepository,
        )

        // when
        val result = org.junit.jupiter.api.assertThrows<IllegalStateException> {
            downloader.download(id.toAnimeId()) { shouldNotBeInvoked() }
        }

        // then
        assertThat(result).hasMessage("Response body was blank for [anilistId=1535] with response code [200]")
    }

    @Test
    fun `responding 404 indicating dead entry - add to dead entry list`() {
        tempDirectory {
            // given
            val id = 1535

            val testAnilistConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
                override fun hostname(): Hostname = "localhost"
                override fun buildAnimeLinkUrl(id: AnimeId): URL = AnilistConfig.buildAnimeLinkUrl(id)
                override fun buildDataDownloadUrl(id: String): URL = URL("http://${hostname()}:$port/graphql")
                override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
            }

            @Suppress("UNUSED_PARAMETER")
            val testAnilistTokenRepository = object: AnilistTokenRepository {
                override var token: AnilistToken
                    get() = AnilistToken("value", "value")
                    set(value) { shouldNotBeInvoked() }
            }

            serverInstance.stubFor(
                post(urlPathEqualTo("/graphql"))
                    .willReturn(
                        aResponse()
                            .withHeader("Content-Type", "text/html")
                            .withStatus(404)
                            .withBody("<html><head/><body></body></html>")
                    )
            )

            val downloader = AnilistDownloader(
                    config = testAnilistConfig,
                    anilistTokenRetriever = TestAnilistTokenRetriever,
                    anilistTokenRepository = testAnilistTokenRepository,
            )

            var deadEntriesId = EMPTY

            // when
            val result = downloader.download(id.toAnimeId()) { deadEntriesId = it }

            // then
            assertThat(result).isBlank()
            assertThat(deadEntriesId).isEqualTo(id.toAnimeId())
        }
    }

    @Test
    fun `verify request body and additional header`() {
        // given
        val id = 1535

        val testAnilistConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLinkUrl(id: AnimeId): URL = AnilistConfig.buildAnimeLinkUrl(id)
            override fun buildDataDownloadUrl(id: String): URL = URL("http://${hostname()}:$port/graphql")
            override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
        }

        AnilistDefaultTokenRepository.token = AnilistToken("valid-cookie", "valid-csrf-token")

        val responseBody = "{ \"anilistId\": $id }"

        serverInstance.stubFor(
                post(urlPathEqualTo("/graphql")).willReturn(
                        aResponse()
                                .withHeader("Content-Type", APPLICATION_JSON)
                                .withStatus(200)
                                .withBody(responseBody)
                )
        )

        val requestBody = loadTestResource("downloader_tests/anime_download_request.graphql")

        val downloader = AnilistDownloader(testAnilistConfig)

        // when
        downloader.download(id.toAnimeId()) { shouldNotBeInvoked() }

        // then
        serverInstance.verify(
                postRequestedFor(urlEqualTo("/graphql"))
                        .withHeader("authority", equalTo("https://anilist.co"))
                        .withHeader("method", equalTo("POST"))
                        .withHeader("path", equalTo("/graphql"))
                        .withHeader("scheme", equalTo("https"))
                        .withHeader("accept", equalTo("*/*"))
                        .withHeader("accept-language", equalTo("en-US;q=0.9,en;q=0.8"))
                        .withHeader("content-length", equalTo(requestBody.length.toString()))
                        .withHeader("content-type", equalTo("application/json"))
                        .withHeader("referer", equalTo("https://anilist.co/anime/$id"))
                        .withHeader("schema", equalTo("default"))
                        .withHeader("cookie", equalTo("valid-cookie"))
                        .withHeader("x-csrf-token", equalTo("valid-csrf-token"))
                        .withRequestBody(EqualToJsonPattern(requestBody, true, true))
        )
    }

    @Test
    fun `refresh token in case anilist responds with a 403 and try to download the same entry again`() {
        // given
        val id = 1535

        val testAnilistConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLinkUrl(id: AnimeId): URL = AnilistConfig.buildAnimeLinkUrl(id)
            override fun buildDataDownloadUrl(id: String): URL = URL("http://${hostname()}:$port/graphql")
            override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
        }

        val downloader = AnilistDownloader(
            config = testAnilistConfig
        )

        AnilistDefaultTokenRepository.token = AnilistToken("my-cookie", "my-csrf-token")

        val refreshedTokenResponseBody = loadTestResource("downloader_tests/page_containing_token.html")

        serverInstance.stubFor(
            get(urlPathEqualTo("/")).willReturn(
                aResponse()
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withHeader(
                        "set-cookie",
                        "__cfduid=db93afbdcce117dd877b809ce8b6dde941579726597; expires=Fri, 21-Feb-20 20:56:37 GMT; path=/; domain=.anilist.co; HttpOnly; SameSite=Lax; Secure",
                        "laravel_session=NOz33Vu7KGVZK4TZqSES3lmv14JmKbe9IrHN4LnL; expires=Thu, 23-Jan-2020 08:56:37 GMT; Max-Age=43200; path=/; httponly")
                    .withStatus(200)
                    .withBody(refreshedTokenResponseBody)
            )
        )

        serverInstance.stubFor(
            post(urlPathEqualTo("/graphql"))
                .inScenario("refresh token and retry")
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("token denied")
                .withHeader("cookie", matching("my-cookie"))
                .withHeader("x-csrf-token", matching("my-csrf-token"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withStatus(403)
                        .withBody("Forbidden")
                )
        )

        val responseBody = "{ \"anilistId\": $id }"

        serverInstance.stubFor(
            post(urlPathEqualTo("/graphql"))
                .inScenario("refresh token and retry")
                .whenScenarioStateIs("token denied")
                .withHeader("cookie", matching("__cfduid=db93afbdcce117dd877b809ce8b6dde941579726597; laravel_session=NOz33Vu7KGVZK4TZqSES3lmv14JmKbe9IrHN4LnL"))
                .withHeader("x-csrf-token", matching("refreshed-csrf-token"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withStatus(200)
                        .withBody(responseBody)
                )
        )

        // when
        val result = downloader.download(id.toAnimeId()) { shouldNotBeInvoked() }

        // then
        assertThat(result).isEqualTo(responseBody)
    }

    @Test
    fun `throws an exception if the response code is unknown`() {
        // given
        val id = 1535

        val testAnilistConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLinkUrl(id: AnimeId): URL = AnilistConfig.buildAnimeLinkUrl(id)
            override fun buildDataDownloadUrl(id: String): URL = URL("http://${hostname()}:$port/graphql")
            override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
        }

        @Suppress("UNUSED_PARAMETER")
        val testAnilistTokenRepository = object: AnilistTokenRepository {
            override var token: AnilistToken
                get() = AnilistToken("value", "value")
                set(value) { shouldNotBeInvoked() }
        }

        val responseBody = "<html><head><title>ERROR</title></head></html>"

        serverInstance.stubFor(
            post(urlPathEqualTo("/graphql")).willReturn(
                aResponse()
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withStatus(400)
                    .withBody(responseBody)
            )
        )

        val downloader = AnilistDownloader(
                config = testAnilistConfig,
                anilistTokenRetriever = TestAnilistTokenRetriever,
                anilistTokenRepository = testAnilistTokenRepository,
        )

        // when
        val result = org.junit.jupiter.api.assertThrows<IllegalStateException> {
            downloader.download(id.toAnimeId()) { shouldNotBeInvoked() }
        }

        // then
        assertThat(result).hasMessage("Unable to determine the correct case for [anilistId=$id], [responseCode=400]")
    }

    @ParameterizedTest
    @ValueSource(ints = [500, 502, 520])
    fun `pause and retry on response code`(responseCode: Int) {
        // given
        val id = 1535

        val testAnilistConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "localhost"
            override fun buildAnimeLinkUrl(id: AnimeId): URL = AnilistConfig.buildAnimeLinkUrl(id)
            override fun buildDataDownloadUrl(id: String): URL = URL("http://${hostname()}:$port/graphql")
            override fun fileSuffix(): FileSuffix = AnilistConfig.fileSuffix()
        }

        @Suppress("UNUSED_PARAMETER")
        val testAnilistTokenRepository = object: AnilistTokenRepository {
            override var token: AnilistToken
                get() = AnilistToken("value", "value")
                set(value) { shouldNotBeInvoked() }
        }

        serverInstance.stubFor(
                post(urlPathEqualTo("/graphql"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs(STARTED)
                        .willSetStateTo("successful retrieval")
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "text/plain")
                                        .withStatus(responseCode)
                                        .withBody("Internal Server error")
                        )
        )

        val responseBody = "{ \"anilistId\": $id }"

        serverInstance.stubFor(
                post(urlPathEqualTo("/graphql"))
                        .inScenario("pause and retry")
                        .whenScenarioStateIs("successful retrieval")
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", APPLICATION_JSON)
                                        .withStatus(200)
                                        .withBody(responseBody)
                        )
        )

        val downloader = AnilistDownloader(
                config = testAnilistConfig,
                anilistTokenRetriever = TestAnilistTokenRetriever,
                anilistTokenRepository = testAnilistTokenRepository,
        )

        // when
        val result = downloader.download(id.toAnimeId()) { shouldNotBeInvoked() }

        // then
        assertThat(result).isEqualTo(responseBody)
    }
}