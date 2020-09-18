package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.extensions.EMPTY

/**
 * Central place to store the current anilist token so that the same token is always being used.
 * @since 1.0.0
 */
public object AnilistTokenRepository {

    private var currentToken = AnilistToken(EMPTY, EMPTY)

    /**
     * Currently valid token or a token with empty fields after initialization.
     * The token is only accepted if both cookie and csrf field are not blank.
     * @since 1.0.0
     */
    public var token: AnilistToken
        get() = currentToken
        set(value) {
            require(value.cookie.isNotBlank()) { "Cookie must not blank" }
            require(value.csrfToken.isNotBlank()) { "CSRF token must not blank" }
            currentToken = value
        }
}