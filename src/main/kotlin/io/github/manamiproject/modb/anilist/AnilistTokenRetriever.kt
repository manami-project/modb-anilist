package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.extensions.EMPTY

/**
 * Retrieves the [AnilistToken] which is needed to query the GraphQL endpoints.
 * @since 1.0.0
 */
public interface AnilistTokenRetriever {

    /**
     * @since 1.0.0
     * @return Valid [AnilistToken] which is needed to download data
     */
    @Deprecated("Use coroutine instead", ReplaceWith(EMPTY))
    public fun retrieveToken(): AnilistToken

    /**
     * @since 5.0.0
     * @return Valid [AnilistToken] which is needed to download data
     */
    public suspend fun retrieveTokenSuspendable(): AnilistToken
}