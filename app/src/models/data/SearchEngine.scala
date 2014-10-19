package src.models.data

import play.api.mvc._
import src.controllers.util.TimerAction

import scala.Array._
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable._
import scala.math._

/**
 * More ideas :
 * - Weight during DP by character distance on keyboard to guess typos.
 * - Optimize by first doing a direct or substring search (via rolling hash) before
 * traversing the trie and doing DP.
 *
 */

class SearchEngine[T] extends Controller {
  val wordToNames = new mutable.HashMap[String, mutable.Set[String]]()
  val nameToWords = new mutable.HashMap[String, mutable.HashMap[String, Int]]()
  var trie: Trie = _
  var namesToObjs : java.util.Map[String, T] = _

  def this(_namesToObjs: java.util.Map[String, T]) = {
    this()

    namesToObjs = _namesToObjs
    namesToObjs foreach { case (name, _) =>
      // Not case-sensitive.
      val words = name.toUpperCase.split("( |/)").toList

      val wordPositionMap = new mutable.HashMap[String, Int]
      words.zipWithIndex foreach { case (word, index) =>
          wordPositionMap.put(word, index)
      }
      nameToWords.put(name, wordPositionMap)

      words foreach { word =>
        if (!wordToNames.contains(word)) {
          wordToNames.put(word, new mutable.HashSet[String])
        }
        wordToNames(word).add(name)
      }
    }

    trie = new Trie(wordToNames.keys)
  }

  // Levenshtein gives the edit distance between two strings, but the longer the string, the
  // less this distance should be considered an "error". This function changes the distance
  // into a score. The lower the score the better the match.
  def normalizeDistance(queryLength: Int)(matchResult: (String, Double)) = matchResult match {
    case (result, distance) => (result, sqrt(distance / queryLength) + distance * 0.2)
  }

  def getScoredNames(fullWords: List[String], partialWord: String) : mutable.Map[String, Double] = {
    val nameToScore = mutable.HashMap[String, Double]().withDefaultValue(0)

    val completedWords = trie.getAllWithPrefix(partialWord)
    completedWords foreach { completedWord =>
      val score = if (completedWord == partialWord) 1.0 else 0.5
      wordToNames.getOrElse(completedWord, mutable.Set.empty[String]) foreach { name =>
        nameToScore.put(name, max(nameToScore(name), score))
      }
    }

    fullWords foreach { word =>
      wordToNames.getOrElse(word, mutable.Set.empty[String]) foreach { name =>
        nameToScore(name) += 1
      }
    }

    nameToScore
  }

  def partialSearch(query: String): List[T] = {
    // Not case-sensitive.
    val queryWords = query.toUpperCase.split(" ").toList
    val fullWords = queryWords.dropRight(1)
    val partialWord = queryWords.last

    val nameToScore = getScoredNames(fullWords, partialWord)
    val results = mutable.PriorityQueue[(String, Double)]()(Ordering.by(_._2))

    nameToScore foreach { case (name, score) =>
      results += ((name, score))
    }

    val maxResults = 3
    results.take(maxResults).toList.map(result => namesToObjs.get(result))
  }

  def fullSearch(query: String): List[T] = {
    val queryWords = query.split(" ").toList
    val matches = queryWords.map(queryWord => Levenshtein.getMatches(queryWord, trie, 100)
      .map(normalizeDistance(queryWord.length)))
      .flatten
    val scores = mutable.HashMap[String, Double]()

    matches foreach { case (result, score) =>
      wordToNames(result) foreach { name =>
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

    slicedResults.map(result => namesToObjs.get(result))
  }
}

object Levenshtein {

  def getMatches(query: String, dict: Trie, maxResults: Int): List[(String, Double)] = {
    val results = mutable.PriorityQueue[(String, Double)]()(Ordering.by({ case (result, value) => value }))

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
                 results: mutable.PriorityQueue[(String, Double)],
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

      // Make a list of the children of the current node.
      // If one of the children correspond to the character in the query whose index
      // is the same as the current depth of the search, we move that children
      // to the front of the list so that it can be processed first.
      //
      // This is an optimization that should allow the algorithm to terminate earlier,
      // since my processing that children first, we get matches with lower distances
      // earlier.
      val pulledNodes = if (currentChars.length >= query.length) dict.nodes.toList
      else {
        val char = query(currentChars.length)
        dict.nodes.get(char) match {
          case Some(node) => (char, node) :: dict.nodes.toList.filter{ case (c, _) => c != char }
          case None => dict.nodes.toList
        }
      }

      // Traverse trie.
      pulledNodes foreach { case (char, node) =>
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

// Note that the trie is not case-sensitive.
class Trie {
  var terminal = false
  val nodes = new mutable.HashMap[Char, Trie]()

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

  def getPrefixNode(prefix: List[Char]): Option[Trie] = prefix match {
    case char :: rest =>  nodes.get(char).flatMap(trie => trie.getPrefixNode(rest))
    case Nil => Some(this)
  }

  def getPrefixNode(prefix: String): Option[Trie] = {
    getPrefixNode(prefix.toList)
  }

  // prefix is the current chars obtained from traversing the trie
  // and is in reverse order of the corresponding string
  def listAll(prefix: List[Char], buffer: mutable.Buffer[String]): Unit = {
    if (terminal) {
      buffer += prefix.reverse.mkString
    }
    nodes foreach { case (char, trie) =>
      trie.listAll(char :: prefix, buffer)
    }
  }

  def getAllWithPrefix(prefix: String): List[String] = {
    val buffer = ArrayBuffer[String]()
    val upper = prefix.toUpperCase
    getPrefixNode(upper).foreach { node =>
      node.listAll(upper.toList.reverse, buffer)
    }
    buffer.toList
  }
}
