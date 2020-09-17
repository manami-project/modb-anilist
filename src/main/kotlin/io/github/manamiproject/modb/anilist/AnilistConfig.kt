package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import java.net.URL

/**
 * Configuration for downloading and converting anime data from anilist.co
 * @since 1.0.0
 */
object AnilistConfig : MetaDataProviderConfig {

    override fun buildDataDownloadUrl(id: String): URL = URL("https://${hostname()}/graphql")

    override fun hostname(): Hostname = "anilist.co"

    override fun fileSuffix(): FileSuffix = "json"
}