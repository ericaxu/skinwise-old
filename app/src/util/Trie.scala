package src.util

import scala.collection.mutable
import scala.collection.mutable._

/**
 * Note that the trie is not case-sensitive.
 */
class Trie {
  var terminal = false
  val nodes = new mutable.HashMap[Char, Trie]()

  def insertWords(word: List[String]): Unit = {
    word foreach insert
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
          nodes.put(char, new Trie)
        }
        nodes(char).insert(rest)
      }
      case Nil => terminal = true
    }
  }

  def getPrefixNode(prefix: List[Char]): Option[Trie] = prefix match {
    case char :: rest => nodes.get(char).flatMap(trie => trie.getPrefixNode(rest))
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
    getPrefixNode(prefix).foreach { node =>
      node.listAll(prefix.toList.reverse, buffer)
    }
    buffer.toList
  }
}
