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

package moonbox.core.udf

import org.apache.spark.sql.catalyst.JavaTypeInference
import org.apache.spark.sql.types.DataType


object JavaSourceUDF {
	def apply(src: String, className: String, methodName: Option[String]): (AnyRef, DataType) = {
		val (argumentNum, returnType) = getFunctionReturnType(src, className, methodName)
		(generateFunction(src, className, methodName, argumentNum), returnType)
	}

	private def getFunctionReturnType(src: String, className: String, methodName: Option[String]): (Int, DataType) = {
		val clazz = SourceCompiler.compileJava(src, className)
		val method = UdfUtils.getMethod(clazz, methodName.getOrElse("call"))
		val dataType: (DataType, Boolean) = JavaTypeInference.inferDataType(method.getReturnType)
		(method.getParameterCount, dataType._1)
	}

	def generateFunction(src: String, className: String, methodName: Option[String], argumentNum: Int): AnyRef = {
		lazy val clazz = SourceCompiler.compileJava(src, className)
		lazy val instance = UdfUtils.newInstance(clazz)
		lazy val method = UdfUtils.getMethod(clazz, methodName.getOrElse("call"))
		argumentNum match {
			case 0 => new Function0[Any] with Serializable {
				override def apply(): Any = {
					method.invoke(instance)
				}
			}
			case 1 => new Function1[Object, Any] with Serializable {
				override def apply(v1: Object): Any = {
					method.invoke(instance, v1)
				}
			}
			case 2 => new Function2[Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object): Any = {
					method.invoke(instance, v1, v2)
				}
			}
			case 3 => new Function3[Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object): Any = {
					method.invoke(instance, v1, v2, v3)
				}
			}
			case 4 => new Function4[Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4)
				}
			}
			case 5 => new Function5[Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5)
				}
			}
			case 6 => new Function6[Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6)
				}
			}
			case 7 => new Function7[Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7)
				}
			}
			case 8 => new Function8[Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8)
				}
			}
			case 9 => new Function9[Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9)
				}
			}
			case 10 => new Function10[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10)
				}
			}
			case 11 => new Function11[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object, v11: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11)
				}
			}
			case 12 => new Function12[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object, v11: Object, v12: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12)
				}
			}
			case 13 => new Function13[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object, v11: Object, v12: Object, v13: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13)
				}
			}
			case 14 => new Function14[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object, v11: Object, v12: Object, v13: Object, v14: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14)
				}
			}
			case 15 => new Function15[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object, v11: Object, v12: Object, v13: Object, v14: Object, v15: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15)
				}
			}
			case 16 => new Function16[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object, v11: Object, v12: Object, v13: Object, v14: Object, v15: Object, v16: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16)
				}
			}
			case 17 => new Function17[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object, v11: Object, v12: Object, v13: Object, v14: Object, v15: Object, v16: Object, v17: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17)
				}
			}
			case 18 => new Function18[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object, v11: Object, v12: Object, v13: Object, v14: Object, v15: Object, v16: Object, v17: Object, v18: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18)
				}
			}
			case 19 => new Function19[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object, v11: Object, v12: Object, v13: Object, v14: Object, v15: Object, v16: Object, v17: Object, v18: Object, v19: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19)
				}
			}
			case 20 => new Function20[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object, v11: Object, v12: Object, v13: Object, v14: Object, v15: Object, v16: Object, v17: Object, v18: Object, v19: Object, v20: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20)
				}
			}
			case 21 => new Function21[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object, v11: Object, v12: Object, v13: Object, v14: Object, v15: Object, v16: Object, v17: Object, v18: Object, v19: Object, v20: Object, v21: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21)
				}
			}
			case 22 => new Function22[Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Any] with Serializable {
				override def apply(v1: Object, v2: Object, v3: Object, v4: Object, v5: Object, v6: Object, v7: Object, v8: Object, v9: Object, v10: Object, v11: Object, v12: Object, v13: Object, v14: Object, v15: Object, v16: Object, v17: Object, v18: Object, v19: Object, v20: Object, v21: Object, v22: Object): Any = {
					method.invoke(instance, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22)
				}
			}
			case n => throw new Exception(s"UDF with $n arguments is not supported ")
		}
	}
}
