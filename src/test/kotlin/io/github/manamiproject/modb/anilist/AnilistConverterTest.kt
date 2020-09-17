package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.models.Anime.Status.*
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.*
import io.github.manamiproject.modb.test.loadTestResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URL

internal class AnilistConverterTest {

    @Nested
    inner class TitleTests {

        @Test
        fun `title containing special chars`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/title/special_chars.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.title).isEqualTo("Tobidasu PriPara: Mi~nna de Mezase! Idol☆Grand Prix")
        }
    }

    @Nested
    inner class TypeTests {

        @Test
        fun `type is tv`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/type/tv.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.type).isEqualTo(TV)
        }

        @Test
        fun `type is tv_short and is mapped to tv`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/type/tv_short.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.type).isEqualTo(TV)
        }

        @Test
        fun `type is special`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/type/special.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.type).isEqualTo(Special)
        }

        @Test
        fun `type is ova`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/type/ova.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.type).isEqualTo(OVA)
        }

        @Test
        fun `type is ona`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/type/ona.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.type).isEqualTo(ONA)
        }

        @Test
        fun `type is movie`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/type/movie.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.type).isEqualTo(Movie)
        }

        @Test
        fun `type is music is mapped to special`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/type/music.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.type).isEqualTo(Special)
        }

        @Test
        fun `type is null and is mapped to tv`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/type/null.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.type).isEqualTo(TV)
        }

        @Test
        fun `throws an exception, because the type is unknown`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/type/unknown.json")

            val converter = AnilistConverter()

            // when
            val result = assertThrows<IllegalStateException> {
                converter.convert(testFileContent)
            }

            // then
            assertThat(result).hasMessage("Unknown type [UNKNOWN]")
        }
    }

    @Nested
    inner class EpisodesTests {

        @Test
        fun `37 episodes`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/episodes/39.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.episodes).isEqualTo(39)
        }

        @Test
        fun `neither episodes nor nextairingepisode are set`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/episodes/null.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.episodes).isEqualTo(0)
        }

        @Test
        fun `ongoing series for which the value has to be taken from nextairingepisode`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/episodes/ongoing.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.episodes).isEqualTo(957)
        }
    }

    @Nested
    inner class PictureAndThumbnailTests {

        @Test
        fun `picture is available, but anilist never provides a thumbnail so the thumbnail is the same as the picture`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/picture_and_thumbnail/picture_available.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.picture).isEqualTo(URL("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/2167-EgfXVBt6MztP.png"))
            assertThat(result.thumbnail).isEqualTo(URL("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/2167-EgfXVBt6MztP.png"))
        }

        @Test
        fun `picture is unavailable`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/picture_and_thumbnail/picture_unavailable.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.picture).isEqualTo(URL("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"))
            assertThat(result.thumbnail).isEqualTo(URL("https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"))
        }
    }

    @Nested
    inner class SynonymsTests {

        @Test
        fun `synonyms taken from titles and synonyms, ignoring null`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/synonyms/synonyms_from_titles_and_synonyms.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.synonyms).containsExactly(
                "Prism Paradise",
                "Tobidasu PriPara: Minna de Mezase! Idol\u2606Grand Prix",
                "Tobidasu PuriPara: Mi~nna de Mezase! Idol\u2606Grand Prix",
                "とびだすプリパラ　み～んなでめざせ！アイドル☆グランプリ"
            )
        }
    }

    @Nested
    inner class SourcesTests {

        @Test
        fun `extract correct id and build anime link correctly`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/sources/15689.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.sources.first()).isEqualTo(URL("https://anilist.co/anime/15689"))
        }
    }

    @Nested
    inner class RelationsTests {

        @Test
        fun `no adaption, no relations`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/related_anime/no_adaption_no_relations.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.relatedAnime).isEmpty()
        }

        @Test
        fun `no adaption, multiple relations`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/related_anime/no_adaption_multiple_relations.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.relatedAnime).containsExactly(
                URL("https://anilist.co/anime/107298"),
                URL("https://anilist.co/anime/97857")
            )
        }

        @Test
        fun `one adaption, one relation`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/related_anime/has_one_adaption_and_one_relation.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.relatedAnime).containsExactly(
                URL("https://anilist.co/anime/2337")
            )
        }

        @Test
        fun `has adaption, multiple relations`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/related_anime/has_adaption_and_multiple_relations.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.relatedAnime).containsExactly(
                URL("https://anilist.co/anime/100148"),
                URL("https://anilist.co/anime/1081"),
                URL("https://anilist.co/anime/1301"),
                URL("https://anilist.co/anime/1302"),
                URL("https://anilist.co/anime/1491"),
                URL("https://anilist.co/anime/1645"),
                URL("https://anilist.co/anime/1676"),
                URL("https://anilist.co/anime/17269"),
                URL("https://anilist.co/anime/2202"),
                URL("https://anilist.co/anime/2203"),
                URL("https://anilist.co/anime/2470")
            )
        }

        @Test
        fun `has adaption, no relations`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/related_anime/has_adaption_but_no_relation.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.relatedAnime).isEmpty()
        }
    }

    @Nested
    inner class StatusTests {

        @Test
        fun `'FINISHED' is mapped to 'FINISHED_AIRING'`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/status/finished.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.status).isEqualTo(FINISHED)
        }

        @Test
        fun `'RELEASING' is mapped to 'CURRENTLY_AIRING'`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/status/releasing.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.status).isEqualTo(CURRENTLY)
        }

        @Test
        fun `'NOT_YET_RELEASED' is mapped to 'NOT_YET_AIRED'`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/status/not_yet_released.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.status).isEqualTo(UPCOMING)
        }

        @Test
        fun `'CANCELLED' is mapped to 'UNKNOWN'`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/status/cancelled.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.status).isEqualTo(UNKNOWN)
        }

        @Test
        fun `null is mapped to 'UNKNOWN'`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/status/null.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.status).isEqualTo(UNKNOWN)
        }

        @Test
        fun `throws an exception if the status is not mapped`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/status/not_mapped.json")

            val converter = AnilistConverter()

            // when
            val result = assertThrows<IllegalStateException> {
                converter.convert(testFileContent)
            }

            // then
            assertThat(result).hasMessage("Unknown status [anything else]")
        }
    }

    @Nested
    inner class TagsTests {

        @Test
        fun `put names of genres and tags as distinct list into the anime's tags`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/tags/tags.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.tags).containsExactly(
                "action",
                "adventure",
                "amnesia",
                "anti-hero",
                "comedy",
                "crime",
                "cyberpunk",
                "drama",
                "drugs",
                "ensemble cast",
                "episodic",
                "guns",
                "male protagonist",
                "martial arts",
                "noir",
                "philosophy",
                "primarily adult cast",
                "sci-fi",
                "seinen",
                "space",
                "tragedy",
                "yakuza"
            )
        }
    }

    @Nested
    inner class DurationTests {

        @Test
        fun `duration is not set and therefore 0`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/duration/null.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.duration).isEqualTo(Duration(0, SECONDS))
        }

        @Test
        fun `anilist only uses minutes for duration - 0 implies a duration of less than a minute`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/duration/0.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.duration).isEqualTo(Duration(0, SECONDS))
        }

        @Test
        fun `duration of 24 minutes`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/duration/24.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.duration).isEqualTo(Duration(24, MINUTES))
        }

        @Test
        fun `duration of 2 hours`() {
            // given
            val testFileContent = loadTestResource("file_converter_tests/duration/120.json")

            val converter = AnilistConverter()

            // when
            val result = converter.convert(testFileContent)

            // then
            assertThat(result.duration).isEqualTo(Duration(2, HOURS))
        }
    }

    @Nested
    inner class AnimeSeasonTests {

        @Nested
        inner class SeasonTests {

            @Test
            fun `season is 'undefined'`() {
                // given
                val testFileContent = loadTestResource("file_converter_tests/anime_season/season_is_null_and_start_date_is_null.json")

                val converter = AnilistConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason.season).isEqualTo(UNDEFINED)
            }

            @Test
            fun `season is 'spring'`() {
                // given
                val testFileContent = loadTestResource("file_converter_tests/anime_season/spring.json")

                val converter = AnilistConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason.season).isEqualTo(SPRING)
            }

            @Test
            fun `season is 'summer'`() {
                // given
                val testFileContent = loadTestResource("file_converter_tests/anime_season/summer.json")

                val converter = AnilistConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason.season).isEqualTo(SUMMER)
            }

            @Test
            fun `season is 'fall'`() {
                // given
                val testFileContent = loadTestResource("file_converter_tests/anime_season/fall.json")

                val converter = AnilistConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason.season).isEqualTo(FALL)
            }

            @Test
            fun `season is 'wimter'`() {
                // given
                val testFileContent = loadTestResource("file_converter_tests/anime_season/winter.json")

                val converter = AnilistConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason.season).isEqualTo(WINTER)
            }
        }

        @Nested
        inner class YearOfPremiereTests {

            @Test
            fun `year is not set and default is 0`() {
                // given
                val testFileContent = loadTestResource("file_converter_tests/anime_season/season_is_null_and_start_date_is_null.json")

                val converter = AnilistConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason.year).isEqualTo(0)
            }

            @Test
            fun `year is 2020`() {
                // given
                val testFileContent = loadTestResource("file_converter_tests/anime_season/season_is_null_and_start_date_is_2020.json")

                val converter = AnilistConverter()

                // when
                val result = converter.convert(testFileContent)

                // then
                assertThat(result.animeSeason.year).isEqualTo(2020)
            }
        }
    }
}