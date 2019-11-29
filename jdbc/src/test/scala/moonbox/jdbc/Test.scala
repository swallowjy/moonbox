/*-
 * <<
 * Moonbox
 * ==
 * Copyright (C) 2016 - 2018 EDP
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

package moonbox.jdbc

import java.sql._
import java.util.Properties

import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.compat.Platform

class Test extends FunSuite with BeforeAndAfterAll {

  var conn: Connection = _
  var stmt: Statement = _
  var res: ResultSet = _
  var meta: ResultSetMetaData = _
  val url = "jdbc:moonbox://node6:10010/default"
  val sql = "select cast(null as string) a, \"\" as b, 10.2345 as c, 1 as d"
  val prop = new Properties()
  prop.setProperty(MoonboxJDBCUtils.FETCH_SIZE, 200.toString)
  prop.setProperty(MoonboxJDBCUtils.USER_KEY, "adx@adx")
  prop.setProperty(MoonboxJDBCUtils.PASSWORD_KEY, "adx")

  test("test") {
    val t1 = System.currentTimeMillis()
    conn = DriverManager.getConnection(url, prop)
    val t2 = System.currentTimeMillis()
    stmt = conn.createStatement()
    val t3 = System.currentTimeMillis()
    stmt.setFetchSize(10)
    res = stmt.executeQuery(sql)
    val t4 = Platform.currentTime
    println(s"Get connection spend (${t2 - t1} ms)")
    println(s"Get statement spend (${t3 - t2} ms)")
    println(s"Execute query('$sql') spend (${t4 - t3} ms)")
    printResultSet(res)
    println("----------------------------------------")
    stmt.setFetchSize(5)
    val res2 = stmt.executeQuery(sql + " limit 5")
    printResultSet(res2)
    println(JDBCType.INTEGER.getName)
    println(JDBCType.DECIMAL.getName)
    println(JDBCType.FLOAT.getName)

  }

  def printResultSet(res: ResultSet): Unit = {
    meta = res.getMetaData
    val fieldCount = meta.getColumnCount
    println(s"Column count: $fieldCount")
    while (res.next()) {
      var seq = Seq.empty[Object]
      var index = 1
      while (index <= fieldCount) {
        println(s"Column $index label: ${meta.getColumnLabel(index)}")
        println(s"Column $index type: ${meta.getColumnType(index)}")
        println(s"Column $index typeName: ${meta.getColumnTypeName(index)}")

        seq :+= res.getObject(index)
        index += 1
      }
      println(seq.mkString(", "))
    }
  }

  override protected def beforeAll() = {
    Class.forName("moonbox.jdbc.MbDriver")
  }

  override protected def afterAll() = {
    res.close()
    stmt.close()
    conn.close()
  }
}
