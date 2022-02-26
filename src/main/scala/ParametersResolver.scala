class ParametersResolver(params : Array[String]) {


  def insertOrThrow(pair: (String,String)): (String, String) = {
    pair._1 match {
      case ParametersResolver.applicationConfig => pair
      case _ => ("","")//throw new Exception("Option " + pair + " not appliciable")
    }
  }

  def paramSplit(param: String) = {
    if(param.startsWith("-")) {
      val keyAndValue = param.split("=")
      (keyAndValue(0).substring(1), keyAndValue(1))
    } else {
      throw new Exception("Unrecognized token: " + param)
    }
  }

  val paramsMap : Map[String, String] = params.map(x => insertOrThrow(paramSplit(x))).toMap

}

//todo: to zostanie w projekcie, ale klas\e mo\znaby nawet do osobnego modul\u (w przyszlości)
object ParametersResolver {
  val applicationConfig : String = "appconfig"

}
