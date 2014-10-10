package src.controllers

import play.api.mvc._
import src.models.Ingredient

import scala.collection.mutable._
import scala.collection.JavaConversions._
import scala.math._

/**
 * Created by rudi on 10/7/14.
 *
 * Implementation ideas :
 * - Split names into words
 * - Weighted Damerau-Levenshtein on words (supports transpositions) but only the simple version (?)
 * ----- Weight table by character distance on keyboard
 * ----- Weight differences by character position
 * - Get results with at least one of the words.
 * - Weight result = sum (word-levenshtein-weight * another-coefficient)
 * - Return top k
 *
 * Optimizations :
 * - Precomputed word splitting and word -> name hash map
 * - Use trie http://stevehanov.ca/blog/index.php?id=114
 * ----- Or automata http://en.wikipedia.org/wiki/Levenshtein_automaton
 * - Substring search first (rolling hash for each word)
 * - Early termination
 * ----- Character histogram early prune
 * ----- Early prune if exact match exists, switch algorithm
 * ----- DL-distance exceeds current min value
 * ----- Word length
 * - Cache searches (priority queue for most frequent searches)
 * - Sort words and start with those most likely to have small distance (e.g. first letter matches)
 *
 */
object IngredientSearch extends Controller {
  def partialSearch(query: String) = Action {
    Ok("Not implemented yet.")
  }

  def fullSearch(name: String) = Action {
    val start = System.nanoTime()

    val ingredients = Ingredient.getAll().toList
    val names: List[String] = ingredients map { _.getName() }

    val trie = new Trie(names)

    val ranked_names = names map { n => (Levenshtein.distance(name, n), n) }
    val sorted_names = ranked_names sortBy { _._1 } map { case (score, n) => n + " " + score.toString }

    val end = System.nanoTime()

    Ok("Query time : " + ((end - start) / 1000 / 1000) + "\n\n" + (sorted_names mkString "\n"))
  }
}

object Levenshtein {
  def getMatches(query: String, dict: Trie, previous: List[Array[Float]] = Nil): Unit = {

  }

  def minimum(i1: Int, i2: Int, i3: Int) = min(min(i1, i2), i3)

  def distance(s1: String, s2: String) = {
    val dist = Array.tabulate(s2.length + 1, s1.length + 1) { (j, i) => if (j == 0) i else if (i == 0) j else 0 }

    for (j <- 1 to s2.length; i <- 1 to s1.length)
      dist(j)(i) = if (s2(j - 1) == s1(i - 1)) dist(j - 1)(i - 1)
      else minimum(dist(j - 1)(i) + 1, dist(j)(i - 1) + 1, dist(j - 1)(i - 1) + 1)

    dist(s2.length)(s1.length)
  }
}

class Trie {
  var terminal = false
  val nodes = new HashMap[Char, Trie]()

  def this(words: List[String]) = {
    this()
    words foreach insert
  }

  def insert(word: String): Unit = {
    insert(word.toList)
  }

  def insert(word: List[Char]): Unit = {
    // Scala confuses my brain, it makes me mix functional
    // and imperative style in odd ways...
    word match {
      case char :: rest => {
        if (!nodes.contains(char)) {
          nodes += ((char, new Trie))
        }
        nodes(char).insert(rest)
      }
      case Nil => terminal = true
    }
  }
}
