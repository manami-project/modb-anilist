package io.github.manamiproject.modb.anilist

import io.github.manamiproject.modb.core.extensions.EMPTY
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test

internal class AnilistDataExtractorTest {

    @ParameterizedTest
    @ValueSource(strings = ["", "    "])
    fun `contains empty values for the OutputKey if raw content is blank`(input: String) {
        runBlocking {
            // when
            val result = AnilistDataExtractor().extract(input, mapOf("description" to "$.data.Media.description"))

            // then
            assertThat(result).containsEntry("description", EMPTY)
        }
    }

    @Test
    fun `contains empty string for the OutputKey if selector cannot be found`() {
        runBlocking {
            // given
            val raw = """
                {
                    "testKey": "testValue"
                }
            """.trimIndent()

            // when
            val result = AnilistDataExtractor().extract(raw, mapOf("description" to "$.data.Media.description"))

            // then
            assertThat(result).containsEntry("description", EMPTY)
        }
    }

    @Test
    fun `returns empty map if selector is empty`() {
        runBlocking {
            // given
            val raw = """
                {
                    "testKey": "testValue"
                }
            """.trimIndent()

            // when
            val result = AnilistDataExtractor().extract(raw, emptyMap())

            // then
            assertThat(result).isEmpty()
        }
    }

    @Test
    fun `correctly extracts strings`() {
        runBlocking {
            // given
            val raw = """
                {
                    "string": "testValue",
                    "number": 5,
                    "array_strings": [
                        "one",
                        "two"
                    ],
                    "array_numbers": [
                        2,
                        3,
                        5
                    ],
                    "object": {
                        "inner_string": "otherValue",
                        "inner_number": 7
                    }
                }
            """.trimIndent()

            // when
            val result = AnilistDataExtractor().extract(raw, mapOf("result" to "$.string"))

            // then
            assertThat(result).containsEntry("result", "testValue")
        }
    }

    @Test
    fun `correctly extracts numbers`() {
        runBlocking {
            // given
            val raw = """
                {
                    "string": "testValue",
                    "number": 5,
                    "array_strings": [
                        "one",
                        "two"
                    ],
                    "array_numbers": [
                        2,
                        3,
                        5
                    ],
                    "object": {
                        "inner_string": "otherValue",
                        "inner_number": 7
                    }
                }
            """.trimIndent()

            // when
            val result = AnilistDataExtractor().extract(raw, mapOf("result" to "$.number"))

            // then
            assertThat(result).containsEntry("result", 5)
        }
    }

    @Test
    fun `correctly extracts array of strings`() {
        runBlocking {
            // given
            val raw = """
                {
                    "string": "testValue",
                    "number": 5,
                    "array_strings": [
                        "one",
                        "two"
                    ],
                    "array_numbers": [
                        2,
                        3,
                        5
                    ],
                    "object": {
                        "inner_string": "otherValue",
                        "inner_number": 7
                    }
                }
            """.trimIndent()

            // when
            val result = AnilistDataExtractor().extract(raw, mapOf("result" to "$.array_strings"))

            // then
            assertThat(result).containsEntry("result", listOf("one", "two"))
        }
    }

    @Test
    fun `correctly extracts array of numbers`() {
        runBlocking {
            // given
            val raw = """
                {
                    "string": "testValue",
                    "number": 5,
                    "array_strings": [
                        "one",
                        "two"
                    ],
                    "array_numbers": [
                        2,
                        3,
                        5
                    ],
                    "object": {
                        "inner_string": "otherValue",
                        "inner_number": 7
                    }
                }
            """.trimIndent()

            // when
            val result = AnilistDataExtractor().extract(raw, mapOf("result" to "$.array_numbers"))

            // then
            assertThat(result).containsEntry("result", listOf(2, 3, 5))
        }
    }

    @Test
    fun `correctly extracts objects`() {
        runBlocking {
            // given
            val raw = """
                {
                    "string": "testValue",
                    "number": 5,
                    "array_strings": [
                        "one",
                        "two"
                    ],
                    "array_numbers": [
                        2,
                        3,
                        5
                    ],
                    "object": {
                        "inner_string": "otherValue",
                        "inner_number": 7
                    }
                }
            """.trimIndent()

            // when
            val result = AnilistDataExtractor().extract(raw, mapOf("result" to "$.object"))

            // then
            assertThat(result).containsEntry("result", mapOf("inner_string" to "otherValue", "inner_number" to 7))
        }
    }

    @Test
    fun `correctly extracts nested string`() {
        runBlocking {
            // given
            val raw = """
                {
                    "string": "testValue",
                    "number": 5,
                    "array_strings": [
                        "one",
                        "two"
                    ],
                    "array_numbers": [
                        2,
                        3,
                        5
                    ],
                    "object": {
                        "inner_string": "otherValue",
                        "inner_number": 7
                    }
                }
            """.trimIndent()

            // when
            val result = AnilistDataExtractor().extract(raw, mapOf("result" to "$.object.inner_string"))

            // then
            assertThat(result).containsEntry("result", "otherValue")
        }
    }

    @Test
    fun `correctly extracts specific array element`() {
        runBlocking {
            // given
            val raw = """
                {
                    "string": "testValue",
                    "number": 5,
                    "array_strings": [
                        "one",
                        "two"
                    ],
                    "array_numbers": [
                        2,
                        3,
                        5
                    ],
                    "object": {
                        "inner_string": "otherValue",
                        "inner_number": 7
                    }
                }
            """.trimIndent()

            // when
            val result = AnilistDataExtractor().extract(raw, mapOf("result" to "$.array_numbers[1]"))

            // then
            assertThat(result).containsEntry("result", 3)
        }
    }
}