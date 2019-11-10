val a = Class.forName("moonbox.rest.MoonboxRest")
  .getMethod("main", classOf[Array[String]])


val s = Array("1", "2", "3", "`4`")

a.invoke(null, s)


