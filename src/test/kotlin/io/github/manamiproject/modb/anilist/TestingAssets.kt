package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URI

internal object MetaDataProviderTestConfig : MetaDataProviderConfig {
    override fun isTestContext(): Boolean = true
    override fun hostname(): Hostname = shouldNotBeInvoked()
    override fun buildAnimeLink(id: AnimeId): URI = shouldNotBeInvoked()
    override fun buildDataDownloadLink(id: String): URI = shouldNotBeInvoked()
    override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
}

internal object TestAnilistTokenRetriever : AnilistTokenRetriever {
    @Deprecated("Use coroutine instead",
        ReplaceWith("shouldNotBeInvoked()", "io.github.manamiproject.modb.test.shouldNotBeInvoked")
    )
    override fun retrieveToken(): AnilistToken = shouldNotBeInvoked()
    override suspend fun retrieveTokenSuspendable(): AnilistToken = shouldNotBeInvoked()
}