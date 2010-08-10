import sbt._

class CupolaRelaisProject( info: ProjectInfo ) extends DefaultProject( info ) {
   val dep1 = "de.sciss" % "netutil" % "0.38" from "http://github.com/downloads/Sciss/Cupola/netutil-0.38.jar"
}
