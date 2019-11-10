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

package moonbox.catalyst.adapter.jdbc.mongo

import java.sql.{Connection, DriverManager, ResultSet, Statement}
import java.util.Properties

import moonbox.catalyst.jdbc.CatalystResultSet
import org.bson.BsonTimestamp
import org.scalatest.{BeforeAndAfterAll, FunSuite}

class CatalystResultSetTest extends FunSuite with BeforeAndAfterAll {

  var connection: Connection = _
  var url: String = null
  var sql: String = null
  var stmt: Statement = _
  var res: ResultSet = _
  var props: Properties = new Properties()
  props.setProperty("authsource", "test")
  //  props.setProperty("database", "test")
  //  props.setProperty("table", "books")
  props.setProperty("user", "yan")
  props.setProperty("password", "123456")


  override protected def beforeAll() = {
    Class.forName("moonbox.catalyst.jdbc.Driver")
  }

  override protected def afterAll() = super.afterAll()

  test("get(i: Int): string, int") {
    url = "jdbc:mongo://localhost:27017/test?collection=books"
    sql = "select name, price, author, pages from books limit 10"
    connection = DriverManager.getConnection(url, props)
    stmt = connection.createStatement()
    res = stmt.executeQuery(sql)
    while (res.next()) {
      println(s"name: ${res.getString(1)}, price: ${res.getInt(2)}, author: ${res.getString(3)}, pages: ${res.getInt(4)} ")
    }
  }

  test("get(columnName: String): string, int through field name") {
    url = "jdbc:mongo://localhost:27017/test?collection=books"
    connection = DriverManager.getConnection(url, props)
    stmt = connection.createStatement()
    sql = "select name, price, author, pages from books limit 20"
    res = stmt.executeQuery(sql)
    while (res.next()) {
      println("name: " + res.getString("name") + s", price: ${res.getInt("price")}, author: ${res.getString("author")}, pages: ${res.getInt("pages")}")
    }
  }

  test("get(i: Int): string, double, array") {
    url = "jdbc:mongo://localhost:27017/test?collection=author_withArray"
    sql = "select authorname, age, books.price from author_withArray limit 10"
    connection = DriverManager.getConnection(url, props)
    stmt = connection.createStatement()
    res = stmt.executeQuery(sql)
    while (res.next()) {
      println(s"authorname: ${res.getString(1)}, age: ${res.getDouble(2)}, books.price: ${res.getArray(3).getArray().asInstanceOf[Array[Any]].mkString(", ")}")
    }
  }

  test("get(i: Int): Timestamp") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=timestamp", props)
    stmt = connection.createStatement()
    sql = "select mytimestamp from timestamp limit 20"
    res = stmt.executeQuery(sql)
    while (res.next()) {
      println("Mongo Timestamp: " + res.getObject(1))
    }
  }

  test("get(i: Int): BsonObjectId, Date") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=date", props)
    stmt = connection.createStatement()
    sql = "select _id, mydate from date limit 20"
    res = stmt.executeQuery(sql)
    while (res.next()) {
      println(s"_id: ${res.getObject(1)}, date: " + s"${res.getTimestamp(2)}")
    }
  }

  test("get(i: Int): BigDecimal") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/zhicheng?collection=bigdecimal1", props)
    stmt = connection.createStatement()
    sql = "select mybigdecimal1 from bigdecimal1 limit 20"
    res = stmt.executeQuery(sql)
    while (res.next()) {
      println("mybigdecimal1: " + res.getBigDecimal(1))
    }
  }

  test("get(i: Int): String, BsonDocument") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=book_nested_normal", props)
    stmt = connection.createStatement()
    sql = "select bookname, bookinfo from book_nested_normal"
    res = stmt.executeQuery(sql)
    while (res.next()) {
      println(s"bookname: ${res.getString(1)}, bookinfo: ${res.getObject(2)}")
    }
  }

  test("get(i: Int): String, nested String") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=book_nested_normal", props)
    stmt = connection.createStatement()
    sql = "select bookname, bookinfo.info1 from book_nested_normal"
    res = stmt.executeQuery(sql)
    while (res.next()) {
      println(s"bookname: ${res.getString(1)}, bookinfo: ${res.getString(2)}")
    }
  }

  test("nested field in filter") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=book_nested_normal", props)
    stmt = connection.createStatement()
    sql = "select bookname, bookinfo.info1 from book_nested_normal where bookinfo.info1 = 'info_1'"
    res = stmt.executeQuery(sql)
    while (res.next()) {
      println(s"bookname: ${res.getString(1)}, bookinfo.info1: ${res.getString(2)}")
    }
  }

  test("filter") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=books", props)
    stmt = connection.createStatement()
    sql = "select name, author, price, pages from books where price < 20 and pages > 100 or price = 22"
    res = stmt.executeQuery(sql)
    while (res.next()) {
      println(s"name: ${res.getString(1)}, author: ${res.getString(2)}, price: ${res.getInt(3)}, pages: ${res.getInt(4)}")
    }
  }

  test("aggregate") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=books", props)
    stmt = connection.createStatement()
    sql = "select author as a, max(price) from books where price > 10 and 100 < pages or pages < 50 group by author"
    res = stmt.executeQuery(sql)
    while (res.next()) {
      println(s"author: ${res.getString(1)}, max(price): ${res.getInt(2)}")
    }
  }

  test("SQLs: sql1-sql21") {
    val sqls = Seq(
      SQLs.sql1,
      SQLs.sql2,
      SQLs.sql3,
      SQLs.sql4,
      SQLs.sql5,
      SQLs.sql6,
      SQLs.sql7,
      SQLs.sql8,
      SQLs.sql9,
      SQLs.sql10,
      SQLs.sql11,
      SQLs.sql12,
      SQLs.sql13,
      SQLs.sql14,
      SQLs.sql15,
      SQLs.sql16,
      SQLs.sql17,
      SQLs.sql18,
      SQLs.sql19,
      //      SQLs.sql20,
      SQLs.sql21
    )
    var count = 1
    var exceptionSqls = Seq[String]()
    sqls.foreach { sql =>
      try {
        connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=books", props)
        stmt = connection.createStatement()
        res = stmt.executeQuery(sql)
        println(s"-------------------sql$count-------------------")
        while (res.next()) {
          println(res.asInstanceOf[CatalystResultSet].currentRow)
        }
      } catch {
        case e: Exception =>
          println(s"exception wihle execute sql${count}")
          e.printStackTrace()
          exceptionSqls :+= s"sql$count"
      } finally {
        count += 1
      }
    }
    println("----------------------------------")
    if (exceptionSqls.nonEmpty)
      println(s"exception sqls: ${exceptionSqls.mkString(", ")}")
    assert(exceptionSqls.isEmpty)
  }

  test("sql23") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=books", props)
    stmt = connection.createStatement()
    sql = SQLs.sql23
    res = stmt.executeQuery(sql)
    println("-------------------sql23-------------------")
    while (res.next()) {
      println(s"first price: ${res.getInt(1)}")
    }
  }

  test("array_map in mongo") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=author_withArray", props)
    stmt = connection.createStatement()
    sql = "select authorname, age, books.price, array_map(books.price, value => value * 2) from author_withArray"
    res = stmt.executeQuery(sql)
    println("-------------------test array_map in mongo-------------------")
    while (res.next()) {
      println(s"authorname: ${res.getString(1)}, age: ${res.getDouble(2)}, books.price: ${res.getArray(3).getArray.asInstanceOf[Array[Any]].map(_.asInstanceOf[Double]).mkString(", ")}, array_map: ${res.getArray(4).getArray.asInstanceOf[Array[Any]].map(_.toString).mkString(", ")}")
    }
  }

  test("array_filter in mongo") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=author_withArray", props)
    stmt = connection.createStatement()
    sql = "select authorname, age, books.price, array_filter(books.price, value => value > 2) from author_withArray"
    res = stmt.executeQuery(sql)
    println("-------------------test array_filter in mongo-------------------")
    while (res.next()) {
      println(s"authorname: ${res.getString(1)}, age: ${res.getDouble(2)}, books.price: ${res.getArray(3).getArray.asInstanceOf[Array[Any]].map(_.asInstanceOf[Double]).mkString(", ")}, array_map: ${res.getArray(4).getArray.asInstanceOf[Array[Any]].map(_.toString).mkString(", ")}")
    }
  }

  //  test("array_exists in mongo"){
  //    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=author_withArray")
  //    stmt = connection.createStatement()
  //    sql = "select authorname, age, books.price, array_filter(books.price, value => value > 2) from author_withArray where array_exists(books.price, value => value = 2)"
  //    res = stmt.executeQuery(sql)
  //    println("-------------------test array_exists in mongo-------------------")
  //    while (res.next()) {
  //      println(s"authorname: ${res.getString(1)}, age: ${res.getDouble(2)}, books.price: ${res.getArray(3).getArray.asInstanceOf[Array[Any]].map(_.asInstanceOf[Double]).mkString(", ")}, array_map: ${res.getArray(4).getArray.asInstanceOf[Array[Any]].map(_.toString).mkString(", ")}")
  //    }
  //  }

  test("other driver by reflection") {
    url = "jdbc:other://localhost:27017/test?table=books"
    sql = "select name, price, author, pages from books limit 10"
    props.setProperty("executor", "moonbox.catalyst.adapter.mongo.MongoCatalystQueryExecutor")
    connection = DriverManager.getConnection(url, props)
    stmt = connection.createStatement()
    res = stmt.executeQuery(sql)
    while (res.next()) {
      println(s"name: ${res.getString(1)}, price: ${res.getInt(2)}, author: ${res.getString(3)}, pages: ${res.getInt(4)} ")
    }
  }

  test("data type test in mongo") {
    val props = new Properties()
    props.setProperty("user", "yan")
    props.setProperty("password", "123456")
    props.setProperty("authsource", "test")
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/zhicheng?collection=data_type_test", props)
    stmt = connection.createStatement()
    sql = "select * from data_type_test"
    res = stmt.executeQuery(sql)
    println("-------------------data type test in mongo-------------------")
    while (res.next()) {
      println(s"objectId: ${res.getObject(1)}, BsonTimestamp: ${res.getObject(2)}, java.sql.Timestamp: ${res.getTimestamp(3).getTime}, java BigDecimal: ${res.getBigDecimal(4)}")
    }
  }

  test("test for adding mongo operators: string related") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=books", props)
    stmt = connection.createStatement()
    sql = "select substr(name, 0, 3), lower(name), upper(name), concat(name, '_', author) from books"
    res = stmt.executeQuery(sql)
    println("-------------------test for adding mongo operators: string related-------------------")
    while (res.next()) {
      println(res.getObject(1))
      println(res.getObject(2))
      println(res.getObject(3))
      println(res.getObject(4))
    }
  }

  test("test for adding mongo operators: time related") {
    val props = new Properties()
    props.setProperty("user", "yan")
    props.setProperty("password", "123456")
    props.setProperty("authsource", "test")
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/zhicheng?collection=data_type_test", props)
    stmt = connection.createStatement()
    sql = "select year(datetime), month(datetime), dayOfMonth(datetime), Hour(datetime), minute(datetime), second(datetime), dayOfYear(datetime), weekOfYear(datetime), datetime from data_type_test"
    res = stmt.executeQuery(sql)
    println("-------------------test for adding mongo operators: time related-------------------")
    while (res.next()) {
      println(res.getObject(1))
      println(res.getObject(2))
      println(res.getObject(3))
      println(res.getObject(4))
      println(res.getObject(5))
      println(res.getObject(6))
      println(res.getObject(7))
      println(res.getObject(8))
      println(res.getObject(9))
    }
  }

  test("test for adding mongo operators: case when") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=books", props)
    stmt = connection.createStatement()
    sql = "select (case when price+2 < 20 and 15 < price then 0 when price >50 then 2 else 1 end) as aaa from books"
    res = stmt.executeQuery(sql)
    println("-------------------test for adding mongo operators: case when-------------------")
    while (res.next()) {
      println(res.getObject(1))
    }
  }

  test("field appears in Alias and Order by at the same time") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=books&user=yan&password=123456", props)
    stmt = connection.createStatement()
    sql = "select name, price as aaa from books order by price"
    res = stmt.executeQuery(sql)
    println("-------------------field appears in Alias and Order by at the same time-------------------")
    while (res.next()) {
      println(res.getObject(1))
      println(res.getObject(2))
    }
  }

  test("limit 0") {
    connection = DriverManager.getConnection("jdbc:mongo://localhost:27017/test?collection=books&user=yan&password=123456", props)
    stmt = connection.createStatement()
    sql = "select * from books where 1=0 limit 0"
    res = stmt.executeQuery(sql)
    println("-------------------local_relation-------------------")
    while (res.next()) {
      println(res.getObject(1))
    }
  }

}
