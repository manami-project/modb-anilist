package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.httpclient.APPLICATION_JSON
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.core.loadResource

/**
 * Downloads anime data from anilist.co
 * @since 1.0.0
 * @param config Configuration for downloading data.
 * @param httpClient To actually download the anime data.
 */
class AnilistDownloader(
    private val config: MetaDataProviderConfig,
    private val httpClient: HttpClient = DefaultHttpClient()
) : Downloader {

    private val requestBody = loadResource("anime_download_request.graphql")

    override fun download(id: AnimeId, onDeadEntry: (AnimeId) -> Unit): String {
        val requestBody =  RequestBody(
            mediaType = APPLICATION_JSON,
            body = requestBody
        )

        val requestUrl = config.buildAnimeLinkUrl(id)

        val response = httpClient.executeRetryable(config.hostname()) {
            val requestHeaders = AnilistHeaderCreator.createAnilistHeaders(
                requestBody = requestBody,
                referer = requestUrl
            )

            httpClient.post(
                url = config.buildDataDownloadUrl(id),
                headers = requestHeaders,
                requestBody = requestBody
            )
        }

        check(response.body.isNotBlank()) { "Response body was blank for [anilistId=$id] with response code [${response.code}]" }

        return when(response.code) {
            200 -> response.body
            404 -> {
                onDeadEntry.invoke(id)
                EMPTY
            }
            else -> throw IllegalStateException("Unable to determine the correct case for [anilistId=$id], [responseCode=${response.code}]")
        }
    }
}