package src.controllers

import play.api.mvc._
import src.models.data.Ingredient
import src.controllers.util.TimerAction

import scala.Array._
import scala.collection.JavaConversions._
import scala.collection.mutable._
import scala.math._

/**
 * More ideas :
 * - Weight during DP by character distance on keyboard to guess typos.
 * - When traversing the trie, start with the matching character, which is likely
 *   to result in shorter distance matches found first and more early termination.
 * - Optimize by first doing a direct or substring search (via rolling hash) before
 *   traversing the trie and doing DP.
 *
 */

class IngredientSearch extends Controller {
  val wordToName = new HashMap[String, Set[String]]()
  var trie: Trie = _

  def this(names: java.util.List[String]) = {
    this()

    names foreach { name =>
      val words = name.split("( |/)").toList
      words foreach { word =>
        if (!wordToName.contains(word)) {
          wordToName.put(word, new HashSet[String])
        }
        wordToName(word).add(name)
      }
    }

    trie = new Trie(wordToName.keys)
  }

  // Levenshtein gives the edit distance between two strings, but the longer the string, the
  // less this distance should be considered an "error". This function changes the distance
  // into a score. The lower the score the better the match.
  def normalizeDistance(queryLength: Int)(matchResult: (String, Double)) = matchResult match {
    case (result, distance) => (result, sqrt(distance / queryLength) + distance * 0.2)
  }

  def fullSearch(query: String): List[String] = {
    val queryWords = query.split(" ").toList
    val matches = queryWords.map(queryWord => Levenshtein.getMatches(queryWord, trie, 100)
      .map(normalizeDistance(queryWord.length)))
      .flatten
    val scores = new HashMap[String, Double]()

    matches foreach { case (result, score) =>
      wordToName(result) foreach { name =>
        if (!scores.contains(name)) {
          scores.put(name, 0.0)
        }
        scores(name) += (1.0 - score)
      }
    }

    // Decreasing sort (for full name matches, the higher the score the better).
    val weightedResults = scores.toList.sortBy { case (name, score) => -score }

    // Optimization idea : sorting with a priority queue with a max number of elements.
    val slicedResults = weightedResults.slice(0, 50)

    // For debugging.
//    slicedResults.foreach { case (name, score) => println(f"$name $score%.3f") }

    slicedResults.map { _._1 }
  }
}

object IngredientSearch extends Controller {
  var instance: Option[IngredientSearch] = None

  def getInstance(): IngredientSearch = instance match {
    case Some(x) => x
    case None => {
      val ingredients = Ingredient.getAll.toList
      val names: List[String] = ingredients map { _.getName }
      instance = Some(new IngredientSearch(names))
      instance.get
    }
  }

  def partialSearch(query: String) = TimerAction {
    Ok("Not implemented yet.")
  }

  def fullSearch(query: String) = TimerAction {
    val sorted_names = getInstance().fullSearch(query)

    Ok(sorted_names mkString "\n")
  }
}

object Levenshtein {
  def getMatches(query: String, dict: Trie, maxResults: Int): List[(String, Double)] = {
    val results = new PriorityQueue[(String, Double)]()(Ordering.by({ case (result, value) => value }))

    val initialRow = List(range(0, query.length + 1).map({ _.toDouble }))
    getMatches(query.toUpperCase, dict, maxResults, results, Nil, initialRow)

    val resultList: List[(String, Double)] = results.dequeueAll
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
                 dynamicTable: List[Array[Double]]): Unit = dynamicTable match {
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
        val nextRow: Array[Double] = Array.ofDim(query.length + 1)
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
                nextRow(i) = min(nextRow(i), transposeRow(i - 2)) + 1
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

  def this(words: collection.Iterable[String]) = {
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
        val uppercase = char.toUpper
        if (!nodes.contains(uppercase)) {
          nodes.put(uppercase, new Trie)
        }
        nodes(uppercase).insert(rest)
      }
      case Nil => terminal = true
    }
  }
}
