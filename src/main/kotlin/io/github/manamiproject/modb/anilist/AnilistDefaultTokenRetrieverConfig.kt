package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URL

/**
 * Configuration for retrieving the CSRF token
 * @since 1.0.0
 */
public object AnilistDefaultTokenRetrieverConfig : MetaDataProviderConfig by AnilistConfig {

    override fun buildDataDownloadUrl(id: String): URL = URL("https://${hostname()}")
}