package example

import io.micronaut.core.annotation.Introspected

@Introspected
class BookStats(
    val max_pages: Int,
    val min_pages: Int,
    val avg_pages: Double,
    val sum_pages: Int,
)

@Introspected
class NamedBookStats(
    val authorName: String,
    val maxPages: Int,
    val minPages: Int,
    val avgPages: Double,
)
