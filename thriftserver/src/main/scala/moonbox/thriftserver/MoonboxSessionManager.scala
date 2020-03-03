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

package moonbox.thriftserver

import java.io.{PrintWriter, StringWriter}
import java.sql.{Connection, DriverManager}
import java.util
import java.util.Properties

import moonbox.common.MbLogging
import moonbox.thriftserver.ReflectionUtils._
import org.apache.hadoop.hive.conf.HiveConf
import org.apache.hive.service.cli.session.{HiveSession, SessionManager}
import org.apache.hive.service.cli.thrift.TProtocolVersion
import org.apache.hive.service.cli.{HiveSQLException, SessionHandle}

import scala.collection.mutable

class MoonboxSessionManager(hiveConf: HiveConf, serverConf: mutable.Map[String, String]) extends SessionManager(null) with ReflectedCompositeService with MbLogging {
  lazy val moonboxSqlOperationManager = new MoonboxOperationManager()

  override def init(hiveConf: HiveConf) {
    setSuperField(this, "hiveConf", hiveConf)
    setSuperField(this, "operationManager", moonboxSqlOperationManager)
    addService(moonboxSqlOperationManager)
    initCompositeService(hiveConf)
  }

  private def initMoonboxConnection(sessionHandle: SessionHandle, sessionConf: java.util.Map[String, String], username: String, password: String) = {
    val masterHost = serverConf.getOrElse(MOONBOX_SERVER_HOST_KEY, "localhost")
    val masterPort = serverConf.get(MOONBOX_SERVER_PORT_KEY).map(_.toInt).getOrElse(10010)
    val url = s"jdbc:moonbox://$masterHost:$masterPort"
    logInfo(s"Initializing moonbox jdbc connection $url ...")
    var conn: Connection = null
    val properties = new Properties()
    properties.setProperty("user", username)
    properties.setProperty("password", password)
    Option(SessionManager.getMaxRows).foreach(maxRows => properties.setProperty("maxrows", maxRows.toString))
    Option(SessionManager.getFetchSize).foreach(fetchSize => properties.setProperty("fetchsize", fetchSize.toString))
    Option(SessionManager.getQueryTimeout).foreach(queryTimeout => properties.setProperty("read_timeout", queryTimeout.toString))
    Option(SessionManager.getIsLocal).foreach(islocal => properties.setProperty("islocal", islocal.toString))
    try {
      Class.forName("moonbox.jdbc.MbDriver")
      conn = DriverManager.getConnection(url, properties)
    } catch {
      case ex: Exception =>
        log.error("get moonbox jdbc connection failed", ex)
        if (conn != null) {
          try {
            conn.close()
          } catch {
            case _: Exception => //nothing
          }
        }
        throw ex
    }
    moonboxSqlOperationManager.sessionHandleToMbJdbcConnection.put(sessionHandle, conn)
  }

  override def openSession(protocol: TProtocolVersion,
                           username: String,
                           password: String,
                           ipAddress: String,
                           sessionConf: java.util.Map[String, String],
                           withImpersonation: Boolean,
                           delegationToken: String) = {
    logInfo(s"Received openSession request from $ipAddress, user $username.")
    val orgUsername = SessionManager.getOrg + "@" + username
    val session = new MoonboxSession(protocol, orgUsername, password, hiveConf, ipAddress)
    session.setSessionManager(this)
    session.setOperationManager(moonboxSqlOperationManager)
    session.open(sessionConf)
    val handleToSession = getSuperField[util.Map[SessionHandle, HiveSession]](this, "handleToSession")
    handleToSession.put(session.getSessionHandle, session)
    val sessionHandle = session.getSessionHandle

    // initialize moonbox client
    try {
      initMoonboxConnection(sessionHandle, sessionConf, orgUsername, password)
    } catch {
      case e: Exception => throw new HiveSQLException(e.getMessage)
    }
    sessionHandle
  }

  override def closeSession(sessionHandle: SessionHandle) {
    try
      Option(moonboxSqlOperationManager.sessionHandleToMbJdbcConnection.remove(sessionHandle)).foreach(_.close())
    catch {
      case e: Exception =>
        val stringWriter = new StringWriter()
        e.printStackTrace(new PrintWriter(stringWriter))
        logError("Client close error: " + stringWriter.toString)
    }
  }
}
