package io.github.manamiproject.modb.anilist

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL

internal class AnilistDefaultTokenRetrieverConfigTest {

    @Test
    fun `hostname must be correct`() {
        // when
        val result = AnilistDefaultTokenRetrieverConfig.hostname()

        // then
        assertThat(result).isEqualTo("anilist.co")
    }

    @Test
    fun `build anime link URL correctly`() {
        // given
        val id = "4563"

        // when
        val result = AnilistDefaultTokenRetrieverConfig.buildAnimeLinkUrl(id)

        // then
        assertThat(result).isEqualTo(URL("https://anilist.co/anime/4563"))
    }

    @Test
    fun `build data download URL correctly`() {
        // given
        val type = "music"

        // when
        val result = AnilistDefaultTokenRetrieverConfig.buildDataDownloadUrl(type)

        // then
        assertThat(result).isEqualTo(URL("https://anilist.co"))
    }

    @Test
    fun `file suffix must be html`() {
        // when
        val result = AnilistDefaultTokenRetrieverConfig.fileSuffix()

        // then
        assertThat(result).isEqualTo("json")
    }
}