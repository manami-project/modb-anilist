package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.parseHtml
import kotlinx.coroutines.withContext

private const val CSRF_TOKEN_PREFIX = "window.al_token"

/**
 * Retrieves a valid token.
 * @since 1.0.0
 * @param config Configuration for retrieving the token.
 * @param httpClient To download the site from which the token will be extracted
 */
public class AnilistDefaultTokenRetriever(
    private val config: MetaDataProviderConfig = AnilistDefaultTokenRetrieverConfig,
    private val httpClient: HttpClient = DefaultHttpClient(isTestContext=config.isTestContext()),
): AnilistTokenRetriever {

    override suspend fun retrieveToken(): AnilistToken = withContext(LIMITED_NETWORK) {
        log.info { "Fetching token for anilist." }

        val response = httpClient.get(
            url = config.buildDataDownloadLink().toURL(),
        )

        val cookie = extractCookie(response)
        val csrfToken = extractCsrfToken(response)

        return@withContext AnilistToken(
            cookie = cookie,
            csrfToken = csrfToken
        )
    }

    private fun extractCookie(response: HttpResponse): String {
        return response.headers["set-cookie"]
            ?.map { it.split(';') }
            ?.map { it.first() }
            ?.sorted()
            ?.joinToString("; ") ?: throw IllegalStateException("Unable to extract cookie.")
    }

    private suspend fun extractCsrfToken(response: HttpResponse): String = withContext(LIMITED_CPU) {
        require(response.body.isNotBlank()) { "Response body must not be empty" }

        return@withContext parseHtml(response.body) { document ->
            val scriptElement = document.select("script")
                .find { it.data().startsWith(CSRF_TOKEN_PREFIX) } ?: throw IllegalStateException("Unable to extract CSRF token.")

            scriptElement.data()
                .replace("$CSRF_TOKEN_PREFIX = \"", EMPTY)
                .replace("\";", EMPTY)
                .trim()
        }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}