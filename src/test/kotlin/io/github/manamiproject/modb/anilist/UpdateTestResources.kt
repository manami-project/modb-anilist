package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.extensions.writeToFileSuspendable
import io.github.manamiproject.modb.test.testResource
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import java.nio.file.Paths

fun main() {
    val downloader = AnilistDownloader(AnilistConfig)
    
    runBlocking { 
        downloader.downloadSuspendable("104464").writeToFileSuspendable(resourceFile("file_converter_tests/anime_season/fall.json"))
        downloader.downloadSuspendable("126434").writeToFileSuspendable(resourceFile("file_converter_tests/anime_season/season_is_null_and_start_date_is_2021.json"))
        downloader.downloadSuspendable("100050").writeToFileSuspendable(resourceFile("file_converter_tests/anime_season/season_is_null_and_start_date_is_null.json"))
        downloader.downloadSuspendable("101922").writeToFileSuspendable(resourceFile("file_converter_tests/anime_season/spring.json"))
        downloader.downloadSuspendable("106286").writeToFileSuspendable(resourceFile("file_converter_tests/anime_season/summer.json"))
        downloader.downloadSuspendable("101759").writeToFileSuspendable(resourceFile("file_converter_tests/anime_season/winter.json"))
    
        downloader.downloadSuspendable("114196").writeToFileSuspendable(resourceFile("file_converter_tests/duration/0.json"))
        downloader.downloadSuspendable("1").writeToFileSuspendable(resourceFile("file_converter_tests/duration/24.json"))
        downloader.downloadSuspendable("100290").writeToFileSuspendable(resourceFile("file_converter_tests/duration/120.json"))
        downloader.downloadSuspendable("10004").writeToFileSuspendable(resourceFile("file_converter_tests/duration/null.json"))
        downloader.downloadSuspendable("102655").writeToFileSuspendable(resourceFile("file_converter_tests/duration/min_duration.json"))
    
        downloader.downloadSuspendable("1251").writeToFileSuspendable(resourceFile("file_converter_tests/episodes/39.json"))
        downloader.downloadSuspendable("114441").writeToFileSuspendable(resourceFile("file_converter_tests/episodes/neither_episodes_nor_nextairingepisode_is_set.json"))
        downloader.downloadSuspendable("235").writeToFileSuspendable(resourceFile("file_converter_tests/episodes/ongoing.json"))
    
        downloader.downloadSuspendable("2167").writeToFileSuspendable(resourceFile("file_converter_tests/picture_and_thumbnail/picture_available.json"))
        downloader.downloadSuspendable("133124").writeToFileSuspendable(resourceFile("file_converter_tests/picture_and_thumbnail/picture_unavailable.json"))
    
        downloader.downloadSuspendable("1000").writeToFileSuspendable(resourceFile("file_converter_tests/related_anime/has_adaption_and_multiple_relations.json"))
        downloader.downloadSuspendable("100").writeToFileSuspendable(resourceFile("file_converter_tests/related_anime/has_adaption_but_no_relation.json"))
        downloader.downloadSuspendable("10005").writeToFileSuspendable(resourceFile("file_converter_tests/related_anime/has_one_adaption_and_one_relation.json"))
        downloader.downloadSuspendable("100133").writeToFileSuspendable(resourceFile("file_converter_tests/related_anime/no_adaption_multiple_relations.json"))
        downloader.downloadSuspendable("10003").writeToFileSuspendable(resourceFile("file_converter_tests/related_anime/no_adaption_no_relations.json"))
    
        downloader.downloadSuspendable("15689").writeToFileSuspendable(resourceFile("file_converter_tests/sources/15689.json"))
    
        downloader.downloadSuspendable("101704").writeToFileSuspendable(resourceFile("file_converter_tests/status/cancelled.json"))
        downloader.downloadSuspendable("1").writeToFileSuspendable(resourceFile("file_converter_tests/status/finished.json"))
        downloader.downloadSuspendable("100081").writeToFileSuspendable(resourceFile("file_converter_tests/status/not_yet_released.json"))
        downloader.downloadSuspendable("135643").writeToFileSuspendable(resourceFile("file_converter_tests/status/null.json"))
        downloader.downloadSuspendable("118123").writeToFileSuspendable(resourceFile("file_converter_tests/status/releasing.json"))
    
        downloader.downloadSuspendable("21453").writeToFileSuspendable(resourceFile("file_converter_tests/synonyms/synonyms_from_titles_and_synonyms.json"))
    
        downloader.downloadSuspendable("1").writeToFileSuspendable(resourceFile("file_converter_tests/tags/tags.json"))
    
        downloader.downloadSuspendable("21453").writeToFileSuspendable(resourceFile("file_converter_tests/title/special_chars.json"))
    
        downloader.downloadSuspendable("20954").writeToFileSuspendable(resourceFile("file_converter_tests/type/movie.json"))
        downloader.downloadSuspendable("97731").writeToFileSuspendable(resourceFile("file_converter_tests/type/music.json"))
        downloader.downloadSuspendable("109731").writeToFileSuspendable(resourceFile("file_converter_tests/type/null.json"))
        downloader.downloadSuspendable("3167").writeToFileSuspendable(resourceFile("file_converter_tests/type/ona.json"))
        downloader.downloadSuspendable("2685").writeToFileSuspendable(resourceFile("file_converter_tests/type/ova.json"))
        downloader.downloadSuspendable("106169").writeToFileSuspendable(resourceFile("file_converter_tests/type/special.json"))
        downloader.downloadSuspendable("5114").writeToFileSuspendable(resourceFile("file_converter_tests/type/tv.json"))
        downloader.downloadSuspendable("98291").writeToFileSuspendable(resourceFile("file_converter_tests/type/tv_short.json"))
    }
}

private fun resourceFile(file: String): Path {
    return Paths.get(
        testResource(file).toAbsolutePath()
            .toString()
            .replace("/build/resources/test/", "/src/test/resources/")
    )
}