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

package moonbox.core.datasys.mongo

import com.mongodb.spark.MongoSpark
import com.mongodb.{ConnectionString, MongoClient, MongoClientURI}
import moonbox.catalyst.core.parser.SqlParser
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.bson.Document
import org.scalatest.FunSuite

class MongoDataSysTest extends FunSuite {

	test("buildScan") {
		//-----------get logicalPlan by spark-----------
		val spark = SparkSession.builder()
			.master("local[*]")
			.appName("mongo-spark")
			.config("spark.mongodb.input.uri", "mongodb://yan:123456@localhost:27017/test.books?authSource=test")
			.config("spark.mongodb.output.uri", "mongodb://yan:123456@localhost:27017/test.books?authSource=test")
			.getOrCreate()
		val df = MongoSpark.load(spark)
		df.createOrReplaceTempView("books")
		val df1 = spark.sql("select * from books limit 10")
		val planBySpark = df1.queryExecution.optimizedPlan
		//----------buildScan(logicalPlan)-----------
		val map = Map(
			"spark.mongodb.input.uri" -> "mongodb://yan:123456@localhost:27017/test.books?authSource=test",
			"spark.mongodb.output.uri" -> "mongodb://yan:123456@localhost:27017/test.books?authSource=test"
		)
		new MongoDataSystem(map).buildScan(planBySpark, spark).show()
	}

	test("build query") {
		val map = Map(
			"spark.mongodb.input.uri" -> "mongodb://yan:123456@localhost:27017/test?authSource=test",
			"spark.mongodb.input.database" -> "test",
			"spark.mongodb.input.collection" -> "book_nested"
		)
		val dataSys = new MongoDataSystem(map)
		val executor = dataSys.readExecutor
		val schema = executor.getTableSchema
		val tableName = dataSys.readExecutor.client.collectionName
		/* get logicalPlan by sqlParser */
		val parser = new SqlParser()
		parser.registerTable(tableName, schema, "mongo")
		//executor.adaptorFunctionRegister(parser.getRegister)
		val sql = "select * from book_nested limit 20"
		val logicalPlan = parser.parse(sql)
		/* buildQuery */
		val dataTable = dataSys.buildQuery(logicalPlan, null)
		dataTable.iterator.foreach(r => println(r))
	}

	test("insert test") {
		val map = Map(
			"spark.mongodb.input.uri" -> "mongodb://yan:123456@localhost:27017/test?authSource=test",
			"spark.mongodb.output.uri" -> "mongodb://yan:123456@localhost:27017/zhicheng?authSource=test",
			"spark.mongodb.input.database" -> "test",
			"spark.mongodb.output.database" -> "zhicheng",
			"spark.mongodb.input.collection" -> "books",
			"spark.mongodb.output.collection" -> "tt"
		)
		val dataSys = new MongoDataSystem(map)
		val executor = dataSys.readExecutor
		val schema = executor.getTableSchema
		val tableName = dataSys.readExecutor.client.collectionName
		/* get logicalPlan by sqlParser */
		val parser = new SqlParser()
		parser.registerTable(tableName, schema, "mongo")
		// executor.adaptorFunctionRegister(parser.getRegister)
		val sql = "select * from books limit 3"
		val plan = parser.parse(sql)
		/* buildQuery */
		val dataTable = dataSys.buildQuery(plan, null)
		/* insert */
		dataSys.insert(dataTable, SaveMode.Ignore)
	}

	test("truncate test") {
		val map = Map(
			"spark.mongodb.output.uri" -> "mongodb://yan:123456@localhost:27017/zhicheng?authSource=test",
			"spark.mongodb.output.database" -> "zhicheng",
			"spark.mongodb.output.collection" -> "tt"
		)
		new MongoDataSystem(map).truncate()
	}

	test("tableNames") {
		val map = Map(
			"spark.mongodb.input.uri" -> "mongodb://yan:123456@localhost:27017/test?authSource=test",
			"spark.mongodb.input.database" -> "test",
			"spark.mongodb.input.collection" -> "books"
		)
		val names = new MongoDataSystem(map).tableNames()
		names.foreach(println)
	}

	test("tableProperties") {
		val map = Map(
			"type" -> "mongo",
			"spark.mongodb.input.uri" -> "mongodb://yan:123456@localhost:27017/test?authSource=test",
			"spark.mongodb.output.uri" -> "mongodb://yan:123456@localhost:27017/test?authSource=test",
			"spark.mongodb.input.database" -> "test",
			"spark.mongodb.output.database" -> "test"
		)
		val names = new MongoDataSystem(map).tableProperties("books")
		names.foreach(println)
	}

	test("mongo spark with authentication") {
		val spark = SparkSession.builder().master("local[*]").appName("mongo-spark-auth")
			.config("spark.mongodb.input.uri", "mongodb://yan:123456@localhost:27017/test?authSource=test")
			.config("spark.mongodb.input.database", "zhicheng") // this config overrides the database in the uri
			.config("spark.mongodb.input.collection", "bigdecimal")
			.getOrCreate()
		val df = MongoSpark.load(spark)
		df.show()
	}

	test("mongo uri test") {
		val inputUri = "mongodb://yan:123456@localhost:27017/zhicheng"
		val connectionString = new ConnectionString(inputUri)
		Seq(connectionString.getDatabase,
			connectionString.getCollection,
			connectionString.getUsername,
			connectionString.getPassword,
			connectionString.getCredential,
			connectionString.getConnectionString
		).foreach(println)
		val cli = new MongoClient(new MongoClientURI(inputUri))
		val collection = cli.getDatabase("test").getCollection("books")
		println(collection.getNamespace)
	}

	test("create collection with schema") {
		import com.mongodb.client.model.{CreateCollectionOptions, Filters, ValidationOptions}
		import org.bson.BsonType

		val map = Map(
			"spark.mongodb.input.uri" -> "mongodb://yan:123456@localhost:27017/zhicheng?authSource=test",
			"spark.mongodb.output.uri" -> "mongodb://yan:123456@localhost:27017/zhicheng?authSource=test",
			"spark.mongodb.input.database" -> "zhicheng",
			"spark.mongodb.output.database" -> "zhicheng"
		)
		val datasys = new MongoDataSystem(map)
		val executor = datasys.writeExecutor
		val client = executor.client.client

		val username = Filters.`type`("username", BsonType.STRING)
		val email = Filters.regex("email", "@*.*$")
		val password = Filters.`type`("password", BsonType.STRING)
		val validator = Filters.and(username, email, password)
		val validationOptions = new ValidationOptions().validator(validator)
		val database = client.getDatabase(datasys.writeDatabase)
		database.createCollection("accounts", new CreateCollectionOptions().validationOptions(validationOptions))
	}

	test("insert document into collection with schema") {
		val map = Map(
			"spark.mongodb.input.uri" -> "mongodb://yan:123456@localhost:27017/zhicheng?authSource=test",
			"spark.mongodb.output.uri" -> "mongodb://yan:123456@localhost:27017/zhicheng?authSource=test",
			"spark.mongodb.input.database" -> "zhicheng",
			"spark.mongodb.output.database" -> "zhicheng"
		)
		val datasys = new MongoDataSystem(map)
		val executor = datasys.writeExecutor
		val client = executor.client.client
		val database = client.getDatabase(datasys.writeDatabase)
		val doc = new Document()
		doc.put("username", "Alice")
		doc.put("email", "123@abc.com")
		doc.put("password", "123456")
		database.getCollection("accounts").insertOne(doc)
		println()
		executor.close()
	}

	test("query with spark") {
		val map = Map(
			"spark.mongodb.input.uri" -> "mongodb://yan:123456@localhost:27017/test?authSource=test",
			"spark.mongodb.input.database" -> "test",
			"spark.mongodb.input.collection" -> "book_nested"
		)
		val sparkBuilder = SparkSession.builder().appName("nested collection").master("local[*]")
		map.foreach(kv => sparkBuilder.config(kv._1, kv._2))
		val spark = sparkBuilder.getOrCreate()
		MongoSpark.load(spark).show()
	}

	test("mongo client test") {
		val map = Map(
			"spark.mongodb.input.uri" -> "mongodb://yan:123456@localhost:27017/test?authSource=test",
			"spark.mongodb.output.uri" -> "mongodb://yan:123456@localhost:27017/test?authSource=test",
			"spark.mongodb.input.database" -> "test",
			"spark.mongodb.output.database" -> "test"
		)
		val mongoSys = new MongoDataSystem(map)
		assertThrows(mongoSys.test())

	}

}
