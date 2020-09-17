package io.github.manamiproject.modb.anilist

/**
 * Retrieves the [AnilistToken] which is needed to query the GraphQL endpoints.
 * @since 1.0.0
 */
interface AnilistTokenRetriever {

    /**
     * @since 1.0.0
     * @return Valid [AnilistToken] which is needed to download data
     */
    fun retrieveToken(): AnilistToken
}