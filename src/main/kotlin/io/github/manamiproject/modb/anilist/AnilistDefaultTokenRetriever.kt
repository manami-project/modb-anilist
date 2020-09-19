package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.retry.RetryBehavior
import io.github.manamiproject.modb.core.httpclient.retry.RetryableRegistry
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.random
import org.jsoup.Jsoup

private const val CSRF_TOKEN_PREFIX = "window.al_token"

/**
 * Registers [RetryBehavior] in the [RetryableRegistry] upon creation.
 * Retrieves a valid token.
 * @since 1.0.0
 * @param config Configuration for retrieving the token.
 * @param httpClient To download the site from which the token will be extracted
 */
public class AnilistDefaultTokenRetriever(
    private val config: MetaDataProviderConfig = AnilistDefaultTokenRetrieverConfig,
    private val httpClient: HttpClient = DefaultHttpClient()
): AnilistTokenRetriever {

    init {
        registerRetryBehavior()
    }

    override fun retrieveToken(): AnilistToken {
        val response = httpClient.get(
            url = config.buildDataDownloadUrl(),
            retryWith = config.hostname()
        )

        val cookie = extractCookie(response)
        val csrfToken = extractCsrfToken(response)

        return AnilistToken(
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

    private fun extractCsrfToken(response: HttpResponse): String {
        val document = Jsoup.parse(response.body)

        val scriptElement = document.select("script")
            .find { it.data().startsWith(CSRF_TOKEN_PREFIX) } ?: throw IllegalStateException("Unable to extract CSRF token.")

        return scriptElement.data()
            .replace("$CSRF_TOKEN_PREFIX = \"", EMPTY)
            .replace("\";", EMPTY)
            .trim()
    }

    private fun registerRetryBehavior() {
        val retryBehaviorConfig = RetryBehavior(
            waitDuration = { random(4000, 8000) },
            retryOnResponsePredicate = { httpResponse -> httpResponse.code in listOf(403, 500, 502, 520) }
        ).apply {
            addExecuteBeforeRetryPredicate(403) {
                log.warn("Anilist responds with 403. Refreshing token.")
                AnilistTokenRepository.token = retrieveToken()
                log.info("Token has been renewed")
            }
        }

        RetryableRegistry.register(config.hostname(), retryBehaviorConfig)
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}