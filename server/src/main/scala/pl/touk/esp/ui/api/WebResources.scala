package pl.touk.esp.ui.api

import akka.http.scaladsl.server.Directives

object WebResources extends Directives {

  val route =
    pathSingleSlash {
      get {
        getFromResource("web/index.html")
      }
    } ~ pathPrefix("static") {
      get {
        getFromResourceDirectory("web/static")
      }
    }

}
