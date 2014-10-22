package src.models.data

import src.models.MemCache
import src.util.ScalaUtils._
import src.util.Trie

import scala.Array._
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.math._

/**
 * More ideas :
 * - Weight during DP by character distance on keyboard to guess typos.
 * - Optimize by first doing a direct or substring search (via rolling hash) before
 * traversing the trie and doing DP.
 *
 */

class SearchEngine[T] {
  val wordToNames = new mutable.HashMap[String, mutable.Set[String]]()
    .withDefaultValue(mutable.Set.empty[String])
  val nameToWords = new mutable.HashMap[String, mutable.HashMap[String, Int]]()
    .withDefaultValue(mutable.HashMap.empty[String, Int])
  var trie: Trie = _
  var namesToObjs: java.util.Map[String, T] = _

  def init(_namesToObjs: java.util.Map[String, T]) = {
    namesToObjs = _namesToObjs
    trie = new Trie()

    namesToObjs foreach { case (name, _) =>
      update(name)
    }
  }

  def update(name: String) = {
    val words = MemCache.Matcher.splitIngredient(name).toList

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

    trie insertWords words
  }

  def getScoredNames(fullWords: List[String], partialWord: String): mutable.Map[String, Double] = {
    val nameToScore = mutable.HashMap[String, Double]().withDefaultValue(0)

    val completedWords = trie.getAllWithPrefix(partialWord)
    completedWords foreach { completedWord =>
      val score = if (completedWord == partialWord) 1.0 else 0.5
      wordToNames(completedWord) foreach { name =>
        nameToScore.put(name, max(nameToScore(name), score))
      }
    }

    fullWords foreach { word =>
      wordToNames(word) foreach { name =>
        nameToScore(name) += 1
      }
    }

    // Give a higher score to smaller results, which should match the query more "tightly"
    nameToScore.keys foreach { name =>
      nameToScore(name) += 0.1 / name.length
    }

    nameToScore
  }

  def partialSearch(query: String, limit: Int): java.util.List[T] = {
    val queryWords = MemCache.Matcher.splitIngredient(query).toList
    if (queryWords.length > 0) {
      val fullWords = queryWords.dropRight(1)
      val partialWord = queryWords.last

      val nameToScore = getScoredNames(fullWords, partialWord)
      val results = mutable.PriorityQueue[(String, Double)]()(Ordering.by(_._2))

      nameToScore foreach { case (name, score) =>
        results += ((name, score))
      }

      results.take(limit).toList.map(result => namesToObjs.get(result._1))
    } else {
      List.empty[T]
    }
  }

  def fullSearchMatches(queryWord: String) : List[(String, Double)] = {
    toInt(queryWord) match {
      case Some(int) =>
        val matches = wordToNames(queryWord).toList
        // Exact matches have distance of 0
        val distances = List.fill(matches.length)(0.0)
        matches.zip(distances)
      case None =>
        Levenshtein.findMatches(queryWord, trie, 100)
    }
  }

  // Calculate the penalty as a function of the query length and the edit distance. Lower is better.
  //
  // The score is a function of the query length since the lower the query, the more tolerance
  // to errors. However, the edit distance has higher weight.
  def calculatePenalty(queryLength: Int, distance: Double) = {
    if (distance == 0.0) {
      // No penalty if the distance is 0
      0.0
    } else {
      // Added weight for the edit distance. We add 1.0 to the distance because any non-zero
      // distance is bad.
      val edit = sqrt(distance + 1.0)
      val ratio = distance / queryLength
      val basePenalty = edit * ratio

      // From numerical inspection, any penalty above 0.5 starts to be really bad, so
      // increase the penalty further above that threshold.
      basePenalty + max(basePenalty - 0.5, 0)
    }
  }

  def fullSearch(query: String, limit: Int): java.util.List[T] = {
    val queryWords = MemCache.Matcher.splitIngredient(query).toList
    val matches = queryWords.map(fullSearchMatches).flatten
    val scores = mutable.HashMap[String, Double]()

    matches foreach { case (result, distance) =>
      wordToNames(result) foreach { name =>
        val minScore = 1.0
        val scoreForPenalty = max(0.0, minScore - calculatePenalty(query.length, distance))
        if (scoreForPenalty > 0.0) {
          if (!scores.contains(name)) {
            scores.put(name, 0.0)
          }
          scores(name) += scoreForPenalty

          // Give a higher score to smaller results, which should match the query more "tightly"
          scores(name) += 0.1 / name.length
        }
      }
    }

    // Decreasing sort (for full name matches, the higher the score the better).
    val weightedResults = scores.toList.sortBy { case (name, score) => -score }

    // Optimization idea : sorting with a priority queue with a max number of elements.
    val slicedResults = weightedResults.slice(0, limit)

    // For debugging.
    //    slicedResults.foreach { case (name, score) => sln(f"$name $score%.3f") }

    slicedResults.map(result => namesToObjs.get(result._1))
  }
}

object Levenshtein {

  def findMatches(query: String, dict: Trie, maxResults: Int): List[(String, Double)] = {
    val results = mutable.PriorityQueue[(String, Double)]()(Ordering.by({ case (result, value) => value }))

    val initialRow = List(range(0, query.length + 1).map({ _.toDouble }))
    findMatches(query, dict, maxResults, results, Nil, initialRow)

    val resultList: List[(String, Double)] = results.dequeueAll
    resultList.reverse
  }

  // Matches all words in the dictionary (stored in a trie) against the
  // query by computing the Damereau-Levenshtein distance. This is computed
  // efficiently by having each node in the trie correspond to a row in the
  // dynamic table.
  def findMatches(query: String,
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
          case Some(node) => (char, node) :: dict.nodes.toList.filter { case (c, _) => c != char }
          case None => dict.nodes.toList
        }
      }

      // Traverse trie.
      pulledNodes foreach { case (char, node) =>
        val nextRow: Array[Double] = Array.ofDim(query.length + 1)
        nextRow(0) = currentChars.length

        remainingRows match {
          case Nil =>
            for (i <- 1 to query.length) {
              nextRow(i) = if (query(i - 1) == char) previousRow(i - 1)
              // Favor first-letter matches.
              else if (i == 1) min3(nextRow(i - 1), previousRow(i), previousRow(i - 1)) + 2
              else min3(nextRow(i - 1), previousRow(i), previousRow(i - 1)) + 1
            }
          case transposeRow :: _ =>
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

        findMatches(query, node, maxResults, results, char :: currentChars, nextRow :: dynamicTable)
      }
    }
    case Nil => Unit
  }

  def min3(f1: Double, f2: Double, f3: Double) = min(min(f1, f2), f3)
}
