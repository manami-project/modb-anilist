package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.coroutines.CoroutineManager.runCoroutine
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.test.testResource
import java.nio.file.Path
import java.nio.file.Paths

internal fun main() {
    val downloader = AnilistDownloader(AnilistConfig)
    
    runCoroutine {
        downloader.download("104464").writeToFile(resourceFile("file_converter_tests/anime_season/fall.json"))
        downloader.download("126434").writeToFile(resourceFile("file_converter_tests/anime_season/season_is_null_and_start_date_is_2021.json"))
        downloader.download("100050").writeToFile(resourceFile("file_converter_tests/anime_season/season_is_null_and_start_date_is_null.json"))
        downloader.download("101922").writeToFile(resourceFile("file_converter_tests/anime_season/spring.json"))
        downloader.download("106286").writeToFile(resourceFile("file_converter_tests/anime_season/summer.json"))
        downloader.download("101759").writeToFile(resourceFile("file_converter_tests/anime_season/winter.json"))
    
        downloader.download("114196").writeToFile(resourceFile("file_converter_tests/duration/0.json"))
        downloader.download("1").writeToFile(resourceFile("file_converter_tests/duration/24.json"))
        downloader.download("100290").writeToFile(resourceFile("file_converter_tests/duration/120.json"))
        downloader.download("10004").writeToFile(resourceFile("file_converter_tests/duration/null.json"))
        downloader.download("102655").writeToFile(resourceFile("file_converter_tests/duration/min_duration.json"))
    
        downloader.download("1251").writeToFile(resourceFile("file_converter_tests/episodes/39.json"))
        downloader.download("114441").writeToFile(resourceFile("file_converter_tests/episodes/neither_episodes_nor_nextairingepisode_is_set.json"))
        downloader.download("235").writeToFile(resourceFile("file_converter_tests/episodes/ongoing.json"))
    
        downloader.download("2167").writeToFile(resourceFile("file_converter_tests/picture_and_thumbnail/picture_available.json"))
        downloader.download("157371").writeToFile(resourceFile("file_converter_tests/picture_and_thumbnail/picture_unavailable.json"))
    
        downloader.download("1000").writeToFile(resourceFile("file_converter_tests/related_anime/has_adaption_and_multiple_relations.json"))
        downloader.download("100").writeToFile(resourceFile("file_converter_tests/related_anime/has_adaption_but_no_relation.json"))
        downloader.download("10005").writeToFile(resourceFile("file_converter_tests/related_anime/has_one_adaption_and_one_relation.json"))
        downloader.download("100133").writeToFile(resourceFile("file_converter_tests/related_anime/no_adaption_multiple_relations.json"))
        downloader.download("10003").writeToFile(resourceFile("file_converter_tests/related_anime/no_adaption_no_relations.json"))
    
        downloader.download("15689").writeToFile(resourceFile("file_converter_tests/sources/15689.json"))
    
        downloader.download("101704").writeToFile(resourceFile("file_converter_tests/status/cancelled.json"))
        downloader.download("1").writeToFile(resourceFile("file_converter_tests/status/finished.json"))
        downloader.download("100081").writeToFile(resourceFile("file_converter_tests/status/not_yet_released.json"))
        downloader.download("135643").writeToFile(resourceFile("file_converter_tests/status/null.json"))
        downloader.download("118123").writeToFile(resourceFile("file_converter_tests/status/releasing.json"))
    
        downloader.download("21453").writeToFile(resourceFile("file_converter_tests/synonyms/synonyms_from_titles_and_synonyms.json"))
    
        downloader.download("1").writeToFile(resourceFile("file_converter_tests/tags/tags.json"))
    
        downloader.download("21453").writeToFile(resourceFile("file_converter_tests/title/special_chars.json"))
    
        downloader.download("20954").writeToFile(resourceFile("file_converter_tests/type/movie.json"))
        downloader.download("97731").writeToFile(resourceFile("file_converter_tests/type/music.json"))
        downloader.download("109731").writeToFile(resourceFile("file_converter_tests/type/null.json"))
        downloader.download("3167").writeToFile(resourceFile("file_converter_tests/type/ona.json"))
        downloader.download("2685").writeToFile(resourceFile("file_converter_tests/type/ova.json"))
        downloader.download("106169").writeToFile(resourceFile("file_converter_tests/type/special.json"))
        downloader.download("5114").writeToFile(resourceFile("file_converter_tests/type/tv.json"))
        downloader.download("98291").writeToFile(resourceFile("file_converter_tests/type/tv_short.json"))
    }
}

private fun resourceFile(file: String): Path {
    return Paths.get(
        testResource(file).toAbsolutePath()
            .toString()
            .replace("/build/resources/test/", "/src/test/resources/")
    )
}