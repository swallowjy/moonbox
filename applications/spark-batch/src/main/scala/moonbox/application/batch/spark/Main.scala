/*-
 * <<
 * Moonbox
 * ==
 * Copyright (C) 2016 - 2019 EDP
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package moonbox.application.batch.spark

import moonbox.common.{MbConf, MbLogging}
import moonbox.core._
import moonbox.core.command._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


object Main extends MbLogging {

  def main(args: Array[String]) {
    val conf = new MbConf()
    val keyValues = for (i <- 0 until(args.length, 2)) yield (args(i), args(i + 1))
    var org: String = null
    var username: String = null
    var sqls: Seq[String] = null
    keyValues.foreach {
      case (k, v) if k.equals("username") =>
        username = v
      case (k, v) if k.equals("org") =>
        org = v
      case (k, v) if k.equals("sqls") =>
        sqls = splitSql(v, ';')
      case (k, v) =>
        conf.set(k, v)
    }
    new Main(conf, org, username, sqls).runMain()
  }

  private def splitSql(sql: String, splitter: Char): Seq[String] = {
    val splitIndex = new ArrayBuffer[Int]()
    var doubleQuoteCount = 0
    var singleQuoteCount = 0
    var parenthesisCount = 0
    for ((char, idx) <- sql.toCharArray.zipWithIndex) {
      if (char == splitter) {
        if (parenthesisCount == 0 && doubleQuoteCount % 2 == 0 && singleQuoteCount % 2 == 0) splitIndex += idx
      }
      if (char == ''') singleQuoteCount += 1
      if (char == '"') doubleQuoteCount += 1
      if (char == '(') {
        if (singleQuoteCount % 2 == 0 && doubleQuoteCount % 2 == 0)
          parenthesisCount += 1
      }
      if (char == ')') {
        if (singleQuoteCount % 2 == 0 && doubleQuoteCount % 2 == 0)
          parenthesisCount -= 1
      }
    }
    splits(sql, splitIndex.toArray, 0).map(_.stripPrefix(splitter.toString).trim).filter(_.length > 0)
  }

  private def splits(sql: String, idxs: scala.Array[Int], offset: Int): Seq[String] = {
    if (idxs.nonEmpty) {
      val head = idxs.head
      val (h, t) = sql.splitAt(head - offset)
      h +: splits(t, idxs.tail, head)
    } else sql :: Nil
  }

}

class Main(conf: MbConf, org: String, username: String, sqls: Seq[String]) {

  private val mbSession: MoonboxSession = new MoonboxSession(conf, org, username)

  def runMain(): Unit = {
    sqls.foreach { sql =>
      mbSession.parsedCommand(sql) match {
        case runnable: MbRunnableCommand =>
          runnable.run(mbSession)

        case CreateTempView(name, query, isCache, replaceIfExists) =>
          val df = mbSession.engine.createDataFrame(query, prepared = false)
          if (isCache) {
            df.cache()
          }
          if (replaceIfExists) {
            df.createOrReplaceTempView(name)
          } else {
            df.createTempView(name)
          }

        case Statement(s) =>
          mbSession.sql(s, 0)

        case _ =>
          throw new Exception("Unsupport command in batch mode")
      }
    }
  }

}
