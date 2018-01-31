import com.typesafe.sbt.digest.Import.DigestKeys._
import com.typesafe.sbt.digest.SbtDigest.DigestStage
import com.typesafe.sbt.web.Import.WebKeys._
import com.typesafe.sbt.web.pipeline.Pipeline
import net.ground5hark.sbt.css.Import._

import com.typesafe.sbt.uglify.Import.{uglify => jsCompress}

import scala.util.Properties

name := Properties.envOrElse("project.artifactId", "customer-portal")
version := Properties.envOrElse("project.version", "local")

scalaVersion := "2.11.8"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)
  .enablePlugins(SbtWeb)

sources in (Compile,doc) := Seq.empty

unmanagedBase := baseDirectory.value / "lib-runtime"
unmanagedBase in Test := baseDirectory.value / "lib-test"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

sassOptions in Assets ++= Seq("-I", "app/assets/toolkit/stylesheets")

val imagesDigest = TaskKey[Pipeline.Stage]("imagesDigest")
imagesDigest := digestAssets(imagesDigest, "*.png").value

val textAssetsDigest = TaskKey[Pipeline.Stage]("textAssetsDigest")
textAssetsDigest := digestAssets(textAssetsDigest, "*.css" || "*.js").value

def digestAssets(task: TaskKey[Pipeline.Stage], filter: FileFilter): Def.Initialize[Task[Pipeline.Stage]] = Def.task { mappings =>
  DigestStage.run(mappings, Seq("md5"), filter, HiddenFileFilter, webTarget.value / task.key.label, indexPath.value, indexWriter.value)
}

excludeFilter in cssCompress := "*.min.css"
excludeFilter in jsCompress := "*.min.js"

pipelineStages := Seq(imagesDigest, simpleUrlUpdate, cssCompress, jsCompress, textAssetsDigest,  gzip)
