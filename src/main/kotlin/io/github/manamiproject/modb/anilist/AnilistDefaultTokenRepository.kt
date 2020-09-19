package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.extensions.EMPTY

/**
 * Central place to store the current anilist token so that the same token is always being used.
 * @since 1.0.0
 */
public object AnilistDefaultTokenRepository : AnilistTokenRepository {

    private var currentToken = AnilistToken(EMPTY, EMPTY)

    override var token: AnilistToken
        get() = currentToken
        set(value) {
            require(value.cookie.isNotBlank()) { "Cookie must not blank" }
            require(value.csrfToken.isNotBlank()) { "CSRF token must not blank" }
            currentToken = value
        }
}