package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_NETWORK
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.retry.RetryBehavior
import io.github.manamiproject.modb.core.httpclient.retry.RetryableRegistry
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.random
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

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
    private val httpClient: HttpClient = DefaultHttpClient(isTestContext = config.isTestContext()),
    private val anilistTokenRepository: AnilistTokenRepository = AnilistDefaultTokenRepository,
): AnilistTokenRetriever {

    init {
        runBlocking {
            registerRetryBehavior()
        }
    }

    @Deprecated("Use coroutine instead",
        ReplaceWith("runBlocking { retrieveTokenSuspendable() }", "kotlinx.coroutines.runBlocking")
    )
    override fun retrieveToken(): AnilistToken = runBlocking {
        retrieveTokenSuspendable()
    }

    override suspend fun retrieveTokenSuspendable(): AnilistToken = withContext(LIMITED_NETWORK) {
        val response = httpClient.getSuspedable(
            url = config.buildDataDownloadLink().toURL(),
            retryWith = config.hostname()
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
        val document = Jsoup.parse(response.body)

        val scriptElement = document.select("script")
            .find { it.data().startsWith(CSRF_TOKEN_PREFIX) } ?: throw IllegalStateException("Unable to extract CSRF token.")

        return@withContext scriptElement.data()
            .replace("$CSRF_TOKEN_PREFIX = \"", EMPTY)
            .replace("\";", EMPTY)
            .trim()
    }

    private suspend fun registerRetryBehavior() {
        val retryBehaviorConfig = RetryBehavior(
            waitDuration = { random(4000, 8000).toDuration(MILLISECONDS) },
            isTestContext = config.isTestContext(),
        ).apply {
            addCase {
                it.code in setOf(500, 502, 520)
            }
            addCase(
                retryIf = { httpResponse -> httpResponse.code == 403 },
                executeBeforeRetry = {
                    log.warn { "Anilist responds with 403. Refreshing token." }
                    anilistTokenRepository.token = runBlocking { retrieveTokenSuspendable() }
                    log.info { "Token has been renewed" }
                }
            )
        }

        RetryableRegistry.register(config.hostname(), retryBehaviorConfig)
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}