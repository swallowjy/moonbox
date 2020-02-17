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

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.compat.Platform

class Test extends FunSuite with BeforeAndAfterAll {

  var conn: Connection = _
  var stmt: Statement = _
  var res: ResultSet = _
  var meta: ResultSetMetaData = _
  val url = "jdbc:moonbox://node6:10010/default"
  val sql = "select t.user_id  \n,t.p2p_aum  \n,t.p2p_aum_3m  \n,case  when  p2p_3m>=0 and p2p_3m<0.25 then '1-(0,25%)'  \n   when p2p_3m>=0.25 and p2p_3m<0.5  then '2-[25%,50%)'  \n    else 'other'  \n    end as zt_3m_type  \nfrom  \n(  \nselect t1.user_id  \n,nvl(t2.max_p2p_aum,0) as max_p2p_aum  \n,nvl(t3.p2p_curr_amount,0) as p2p_aum  \n,nvl(t4.p2p_aum_1m,0) as p2p_aum_1m  \n,nvl(t5.p2p_aum_3m,0) as p2p_aum_3m  \n,case when nvl(t3.p2p_curr_amount,0) >0 and nvl(t5.p2p_aum_3m ,0)>0 then cast(t3.p2p_curr_amount/t5.p2p_aum_3m as decimal(22,4))  \n  else 0 end p2p_3m  \nfrom  \n(  \n\tselect user_id  \n\tfrom dw_user_info  \n\twhere dt='2019-12-18'  \n\tand regist_date<='2019-12-18'  \n\tand user_id !='80542005'  \n) t1  \nleft join  \n(  \n\tselect user_id,  \n\tmax(p2p_aum) as max_p2p_aum  \n\tfrom  \n\t(  \n\t\tselect user_id,  \n\t\tdt,  \n\t\tsum(p2p_curr_amount) as p2p_aum  \n\t\tfrom dm_user_cur_p2p  \n\t\tgroup by user_id,dt  \n\t) a  \n\tgroup by user_id  \n) t2 on t1.user_id=t2.user_id  -- 历史最高在投  \nleft join  \n(  \n\tselect user_id,  \n\tp2p_curr_amount  \n\tfrom dm_user_cur_p2p  \n\twhere dt='2019-12-18'  \n) t3 on t1.user_id=t3.user_id  -- 当前在投  \nleft join  \n(  \n\tselect user_id,  \n\tsum(curr_amount) as p2p_aum_1m  \n\tfrom dw_cur_p2p  \n\twhere dt=add_months('2019-12-18',-1)  \n\tgroup by user_id  \n)t4 on t1.user_id=t4.user_id    -- 1m前aum  \nleft join  \n(  \n\tselect user_id,  \n\tsum(curr_amount) as p2p_aum_3m  \n\tfrom dw_cur_p2p  \n\twhere dt=add_months('2019-12-18',-3)  \n\tgroup by user_id  \n)t5 on t1.user_id=t5.user_id    -- 3m前aum  \nleft join  \n(  \n\tselect user_id,  \n\tp2p_first_order_trade_date  \n\tfrom dm_user_trade_p2p  \n\twhere dt='2019-12-18'  \n\tand p2p_first_order_trade_date<=dt  \n) t6 on t1.user_id=t6.user_id  -- 首借时长  \nleft join  \n(  \n\tselect user_id,  \n\tmax(invalid_date) as invalid_date,  \n\tmax(case when finance_manager_type in (3,4,5,6) then invalid_date else '1900-01-01' end) as invalid_date_sb  \n\tfrom  \n\t(  \n\t\tselect user_id,  \n\t\tfinance_manager_type,  \n\t\tcase when finance_manager_type in (3,4,5,6) and product_cycle_type=0 then add_months(valid_date,product_cycle)  \n\t\t\twhen finance_manager_type in (3,4,5,6) and product_cycle_type=1 then date_add(valid_date,product_cycle)  \n\t\t\telse invalid_date end as invalid_date  \n\t\tfrom dw_order_p2p  \n\t\twhere dt='2019-12-18'  \n\t) a  \n\tgroup by user_id  \n) t7 on t1.user_id=t7.user_id  \n)t" +
    ";" + "select \"';'\""
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

  def splitSql(sql: String, splitter: Char): Seq[String] = {
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

  def splits(sql: String, idxs: scala.Array[Int], offset: Int): Seq[String] = {
    if (idxs.nonEmpty) {
      val head = idxs.head
      val (h, t) = sql.splitAt(head - offset)
      h +: splits(t, idxs.tail, head)
    } else sql :: Nil
  }

  test("splitsql") {
    val sql = "select \";''\"; select 'gfsgreg' (;';';) ('')"
    val sql1 = "concat_ws('', split(option_list, '\\[^A-Z;]*)) as answer"
    val sql2 = "select * from a"
    val sqls = splitSql(sql2, ';')
    sqls.foreach(println(_))
  }
}
