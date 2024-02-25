package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_CPU
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.models.*
import io.github.manamiproject.modb.core.models.Anime.Status
import io.github.manamiproject.modb.core.models.Anime.Status.*
import io.github.manamiproject.modb.core.models.Anime.Type
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.MINUTES
import kotlinx.coroutines.withContext
import java.net.URI

/**
 * Converts raw data to an [Anime].
 * @since 1.0.0
 * @param config Configuration for converting data.
 */
public class AnilistConverter(
    private val config: MetaDataProviderConfig = AnilistConfig,
) : AnimeConverter {

    override suspend fun convert(rawContent: String): Anime = withContext(LIMITED_CPU) {
        val document = Json.parseJson<AnilistDocument>(rawContent)!!

        val picture = extractPicture(document)

        return@withContext Anime(
            _title = extractTitle(document),
            episodes = extractEpisodes(document),
            type = extractType(document),
            picture = picture,
            thumbnail = picture,
            status = extractStatus(document),
            duration = extractDuration(document),
            animeSeason = extractAnimeSeason(document)
        ).apply {
            addSources(extractSourcesEntry(document))
            addSynonyms(extractSynonyms(document))
            addRelatedAnime(extractRelatedAnime(document))
            addTags(extractTags(document))
        }
    }

    private fun extractTitle(document: AnilistDocument) = document.data.Media.title["userPreferred"] ?: throw IllegalStateException("Blank title for [${document.data.Media.id}]")

    private fun extractEpisodes(document: AnilistDocument): Int {
        return document.data.Media.episodes ?: document.data.Media.nextAiringEpisode?.episode ?: 0
    }

    private fun extractType(document: AnilistDocument): Type {
        return when(document.data.Media.format) {
            "TV" -> TV
            "TV_SHORT" -> TV
            null -> Type.UNKNOWN
            "MOVIE" -> MOVIE
            "ONA" -> ONA
            "OVA" -> OVA
            "SPECIAL" -> SPECIAL
            "MUSIC" -> SPECIAL
            else -> throw IllegalStateException("Unknown type [${document.data.Media.format}]")
        }
    }

    private fun extractPicture(document: AnilistDocument): URI {
        val link = document.data.Media.coverImage["large"] ?: "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/default.jpg"

        return URI(link)
    }

    private fun extractSynonyms(document: AnilistDocument): MutableList<Title> {
        return document.data.Media.title.filterKeys { it != "userPreferred" }
            .map { it.value }
            .union(document.data.Media.synonyms)
            .filterNotNull()
            .toMutableList()
    }

    private fun extractSourcesEntry(document: AnilistDocument) = mutableListOf(
        config.buildAnimeLink(document.data.Media.id.toString())
    )

    private fun extractRelatedAnime(document: AnilistDocument): MutableList<URI> {
        return document.data.Media.relations.edges.map { it.node }
            .filter { it.type == "ANIME" }
            .map { config.buildAnimeLink(it.id.toString()) }
            .toMutableList()
    }

    private fun extractStatus(document: AnilistDocument): Status {
        return when(document.data.Media.status) {
            "FINISHED" -> FINISHED
            "RELEASING" -> ONGOING
            "NOT_YET_RELEASED" -> UPCOMING
            "CANCELLED" -> Status.UNKNOWN
            null -> Status.UNKNOWN
            else -> throw IllegalStateException("Unknown status [${document.data.Media.status}]")
        }
    }

    private fun extractDuration(document: AnilistDocument): Duration {
        val durationInMinutes = document.data.Media.duration ?: 0

        return Duration(durationInMinutes, MINUTES)
    }

    private fun extractAnimeSeason(document: AnilistDocument): AnimeSeason {
        val season = AnimeSeason.Season.of(document.data.Media.season ?: EMPTY)
        val year = document.data.Media.seasonYear ?: document.data.Media.startDate?.year ?: 0

        return AnimeSeason(
            season = season,
            year = year
        )
    }

    private fun extractTags(document: AnilistDocument): MutableList<Tag> {
        val genres = document.data.Media.genres
        val tags = document.data.Media.tags.map { it.name }

        return genres.plus(tags).distinct().toMutableList()
    }
}

private data class AnilistDocument(
    val data: AnilistData
)

private data class AnilistData(
    val Media: AnilistDataMedia
)

private data class AnilistDataMedia(
    val id: Int,
    val title: Map<String, String>,
    val coverImage: Map<String, String>,
    val startDate: AnilistDataMediaStartDate?,
    val season: String?,
    val seasonYear: Int?,
    val episodes: Int?,
    val duration: Int?,
    val format: String?,
    val synonyms: List<String?>,
    val relations: AnilistRelations,
    val nextAiringEpisode: AnilistNextAiringEpisode?,
    val status: String?,
    val genres: List<String>,
    val tags: List<AnilistTagEntry>
)

private data class AnilistDataMediaStartDate(
    val year: Int?
)

private data class AnilistRelations(
    val edges: List<AnilistRelationsEdge>
)

private data class AnilistRelationsEdge(
    val node: AnilistRelationsNode
)

private data class AnilistRelationsNode(
    val id: Int,
    val type: String
)

private data class AnilistNextAiringEpisode(
    val episode: Int
)

private data class AnilistTagEntry(
    val name: String
)