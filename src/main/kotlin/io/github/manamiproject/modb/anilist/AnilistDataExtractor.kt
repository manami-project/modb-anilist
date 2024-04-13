package io.github.manamiproject.modb.anilist

import com.nfeld.jsonpathkt.JsonPath
import com.nfeld.jsonpathkt.extension.read
import io.github.manamiproject.modb.core.converter.DataExtractor
import io.github.manamiproject.modb.core.converter.OutputKey
import io.github.manamiproject.modb.core.converter.Selector
import io.github.manamiproject.modb.core.extensions.EMPTY

/**
 * Allows to selectively extract data using JsonPath.
 * @since 5.4.0
 */
public class AnilistDataExtractor: DataExtractor {

    override suspend fun extract(rawContent: String, selection: Map<OutputKey, Selector>): Map<OutputKey, Any> {
        return selection.map{
            it.key to (JsonPath.parse(rawContent)?.read<Any>(it.value) ?: EMPTY)
        }.toMap()
    }
}