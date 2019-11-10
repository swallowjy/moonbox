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

package moonbox.catalyst.adapter.elasticsearch5.plan

import moonbox.catalyst.core.CatalystContext
import moonbox.catalyst.core.plan.{RowDataSourceScanExec}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.expressions.Attribute
import org.apache.spark.sql.catalyst.plans.physical.Partitioning
import org.apache.spark.sql.catalyst.{InternalRow, TableIdentifier}
import org.apache.spark.sql.sources.BaseRelation

import scala.collection.mutable

class EsRowDataSourceScanExec(output: Seq[Attribute],
                              rdd: RDD[InternalRow],
                              relation: BaseRelation,
                              outputPartitioning: Partitioning,
                              metadata: Map[String, String],
                              metastoreTableIdentifier: Option[TableIdentifier])  extends RowDataSourceScanExec(output,
                                                                                            rdd,
                                                                                            relation,
                                                                                            outputPartitioning,
                                                                                            metadata,
                                                                                            metastoreTableIdentifier ){
    override def translate(context: CatalystContext): Seq[String] = {
        Seq.empty[String]
    }

}


object EsRowDataSourceScanExec{
    def apply(output: Seq[Attribute],
              rdd: RDD[InternalRow],
              relation: BaseRelation,
              outputPartitioning: Partitioning,
              metadata: Map[String, String],
              metastoreTableIdentifier: Option[TableIdentifier]): EsRowDataSourceScanExec = {
        new EsRowDataSourceScanExec(output, rdd, relation, outputPartitioning, metadata, metastoreTableIdentifier)
    }

}
