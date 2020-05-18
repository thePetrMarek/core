package com.promethist.core.runtime

import com.promethist.core.Input
import com.promethist.core.dialogue.*
import com.promethist.core.type.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DialogueScriptTest {

    data class Movie(val name: String, val director: String, val year: Int)

    val movies = listOf(
            Movie("Blade Runner", "Ridley Scott", 1982),
            Movie("Blade Runner 2049", "Denis Villeneuve", 2017),
            Movie("The Indian Runner", "Sean Penn", 1991),
            Movie("Runner", "Bill Gallagher", 2019)
    )

    @Test
    fun `test similarity2`() {
        println("2001: A Space Odyssey" similarityTo "Space Odyssey")
    }

    @Test
    fun `test datetime`() {
        val now = DateTime.now()
        val yesterday = now - 1
        println(yesterday)
        println(now + 1)


    }

    @Test
    fun `test similarity`() {
        val input = Input()
        input.tokens.add(Input.Word("blade"))
        //input.tokens.add(Input.Word("runner"))

        println(
                movies.filter { it.name similarityTo input >= 0.5 }
                        .maxBy { it.name similarityTo input }
                        .let { movie ->
                            if (movie != null) {
                                val favoriteMovie = movie.name
                                println("So you like $favoriteMovie. Did you know that the movie was shot by director ${movie.director}?")
                            } else {
                                println("Unfortunately I don't know such movie.")
                            }
                        }
        )

        println(movies.maxBy { it.name similarityTo input }) // movie with highest similarity of name
        println(movies.sortedByDescending { it.name similarityTo input }.take(2)) // take first two movies with highest similarity

        println(movies.map { it.name similarityTo input })
        println(movies.find { it.name similarityTo input >= 0.5 })
    }

    @Test
    fun `test other`() {
        object : BasicDialogue() {
            override val dialogueName = "test"

            //val movieSeen by userAttribute<Dynamic>()

            init {
                val v = Dynamic()
                v("a", 1)
                v("b", 2)
                println(enumerate(v))
                println(plural("science fiction"))

                println(indent("hola", ", "))
                println(plural("this+ mouse+ is+", 2))
                println(3 of "other point")
                println(movies.list { name })
                println(enumerate(movies.random(2).list { name }, subj = "My favourite movie+ is+", before = true))
                println(enumerate(1 of "point+ for math", 2 of "another? point+ for history", 3 of "more? other point"))
                println(enumerate("point+ for math" to 3))
                println(enumerate("a", "b", "c", conj = "or", subj = "movie"))
                println(enumerate(movies.list { name }, {
                    when (it) {
                        0 -> "movie is none"
                        1 -> "movie is"
                        else -> "movies are"
                    }
                }, before = true))
                println("i like ${movies.list { name } of "cool movie"}")

                val t = DateTime.now()
                val animals = Dynamic()

                animals.list<DateTime>("cat") { value.add(t) }
                animals.list<DateTime>("dog") { value.add(t) }
                animals.list<DateTime>("dog") { value.add(t) }

                println(animals.list { (value as List<DateTime>).run { key + " " + (size of "time") }})
                println(animals.list { (value as List<DateTime>).run { key + " " + describe(last()) + (if (size > 1) " last time" else "") }})
            }
        }
    }
}