package src.controllers

import play.api.mvc._
import src.models.ingredient.Ingredient

import scala.Array._
import scala.collection.JavaConversions._
import scala.collection.mutable._
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

  def fullSearch(query: String) = Action {
    val start = System.nanoTime()

    val ingredients = Ingredient.getAll().toList
    val names: List[String] = ingredients map { _.getName() }
    val words: List[String] = names map { _.split("( |/)").toList } flatten

//    val words = "cat, dog, foo, table, foosball, banana, doggy, catacombs, april".split(", ").toList

    val trie = new Trie(words)
    val sorted_names = Levenshtein.getMatches(query, trie, 100).map { case (name, score) => name + " " + score }

    val end = System.nanoTime()

    Ok("Query time : " + ((end - start) / 1000 / 1000) + "\n\n" + (sorted_names mkString "\n"))
  }
}

object Levenshtein {
  def getMatches(query: String, dict: Trie, maxResults: Int): List[(String, Double)] = {
    val results = new PriorityQueue[(String, Double)]()(Ordering.by({ case (result, value) => value}))

    val initialRow = List(range(0, query.length + 1).map({_.toDouble}))
    getMatches(query, dict, maxResults, results, Nil, initialRow)

    val resultList : List[(String, Double)] = results.dequeueAll
    resultList.reverse
  }

  // Matches all words in the dictionary (stored in a trie) against the
  // query by computing the Damereau-Levenshtein distance. This is computed
  // efficiently by having each node in the trie correspond to a row in the
  // dynamic table.
  def getMatches(query: String,
                 dict: Trie,
                 maxResults: Int,
                 results: PriorityQueue[(String, Double)],
                 currentChars: List[Char],
                 dynamicTable: List[Array[Double]]) : Unit = dynamicTable match {
    case previousRow :: remainingRows => {
      // The Levenshtein distance for the string built so far traversing the trie
      // is always the last value of the lastest row.
      val distance = previousRow(query.length)

      // Early termination optimization.
      if (results.length >= maxResults && distance > results.head._2) {
        return
      }

      if (dict.terminal) {
        if (results.length >= maxResults)
          results.dequeue()
        results += ((currentChars.reverse.mkString, distance))
      }

      // Traverse trie.
      dict.nodes foreach { case (char, node) =>
        val nextRow : Array[Double] = Array.ofDim(query.length + 1)
        nextRow(0) = currentChars.length

        remainingRows match {
          case Nil => {
            for (i <- 1 to query.length) {
              nextRow(i) = if (query(i - 1) == char) previousRow(i - 1)
                // Favor first-letter matches.
              else if (i == 1) min3(nextRow(i - 1), previousRow(i), previousRow(i - 1)) + 2
              else min3(nextRow(i - 1), previousRow(i), previousRow(i - 1)) + 1
            }
          }
          case transposeRow :: _ => {
            // If we've already built a row (other than the default row), we can
            // look for adjacent character transposition.
            for (i <- 1 to query.length) {
              nextRow(i) = if (query(i - 1) == char) previousRow(i - 1)
              else min3(nextRow(i - 1), previousRow(i), previousRow(i - 1)) + 1
              // Tranpose recursive case
              if (i > 1 && query(i - 1) == currentChars.head && query(i - 2) == char)
                nextRow(i) = min(nextRow(i), transposeRow(i-2)) + 1
            }
          }
        }

        getMatches(query, node, maxResults, results, char :: currentChars, nextRow :: dynamicTable)
      }
    }
    case Nil => Unit
  }

  def min3(f1: Double, f2: Double, f3: Double) = min(min(f1, f2), f3)
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
