package example

import io.micronaut.data.runtime.criteria.get
import io.micronaut.data.runtime.criteria.joinOne
import io.micronaut.data.runtime.criteria.query
import io.micronaut.data.runtime.criteria.where
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest(transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookRepositorySpec : AbstractTest(false) {

    @Inject
    lateinit var blockingBookRepository: BlockingBookRepository

    @Inject
    lateinit var bookRepository: BookRepository

    @Inject
    lateinit var blockingAuthorRepository: BlockingAuthorRepository

    @AfterAll
    fun cleanupData() {
        blockingBookRepository.deleteAll()
        blockingAuthorRepository.deleteAll()
    }

    @Test
    fun testDto() {
        runBlocking {
            val author = Author("Some")
            blockingAuthorRepository.save(author)
            blockingBookRepository.save(Book("The Shining", 400, author))
            val bookDTO = bookRepository.customFindOne("The Shining")!!
            assertEquals("The Shining", bookDTO.title)
            val bookDTO2 = bookRepository.findOne("The Shining")!!
            assertEquals("The Shining", bookDTO2.title)
        }
    }

    @Test
    fun testDtoSpecs() {
        runBlocking {
            val author = Author("Some")
            blockingAuthorRepository.save(author)
            blockingBookRepository.save(Book("The Shining", 400, author))
            val bookDTO = bookRepository.findOneBookDTO(where {
                root[Book::title] eq "The Shining"
            })!!
            assertEquals("The Shining", bookDTO.title)
        }
    }

    @Test
    fun testFindWithGroup() {
        runBlocking {
            val author = Author("Some")
            blockingAuthorRepository.save(author)
            blockingBookRepository.save(Book("The Shining", 400, author))
            blockingBookRepository.save(Book("Leviathan Wakes", 200, author))
            val stats = bookRepository.findWithQueryBuilder(query<Book, NamedBookStats> {
                val authorJoin = root.joinOne(Book::author)
                multiselect(
                    authorJoin[Author::name].alias(NamedBookStats::authorName),
                    max(Book::pages).alias(NamedBookStats::maxPages),
                    min(Book::pages).alias(NamedBookStats::minPages),
                    avg(Book::pages).alias(NamedBookStats::avgPages)
                )
                group(authorJoin[Author::name])
            })

            assertEquals(400, stats.maxPages)
            assertEquals(200, stats.minPages)
            assertEquals(300.0, stats.avgPages)
        }
    }

    @Test
    fun testFindSingleStat() {
        runBlocking {
            val author = Author("Some")
            blockingAuthorRepository.save(author)
            blockingBookRepository.save(Book("The Shining", 400, author))
            blockingBookRepository.save(Book("Leviathan Wakes", 200, author))
            val stat = bookRepository.findWithQueryBuilder(query<Book, Long> {
                val authorJoin = root.joinOne(Book::author)
                select(
                    sumAsLong(Book::pages)
                )
                group(authorJoin[Author::name])
            })
            assertEquals(600, stat)
        }
    }

    @Test
    fun testFindDto() {
        runBlocking {
            val author = Author("Some")
            blockingAuthorRepository.save(author)
            blockingBookRepository.save(Book("The Shining", 400, author))
            blockingBookRepository.save(Book("Leviathan Wakes", 200, author))
            val results = bookRepository.findStatsWithQuery(query<Book, BookStats> {
                multiselect(
                    max(Book::pages).alias(BookStats::max_pages),
                    min(Book::pages).alias(BookStats::min_pages),
                    avg(Book::pages).alias(BookStats::avg_pages),
                    sum(Book::pages).alias(BookStats::sum_pages),
                )
            })

            val stats = results.first()

            assertEquals(400, stats.max_pages)
            assertEquals(200, stats.min_pages)
            assertEquals(300.0, stats.avg_pages)
            assertEquals(600, stats.sum_pages)
        }
    }
}
