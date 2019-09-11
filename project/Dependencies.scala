import sbt._
import sbt.Keys.libraryDependencies
import Dependencies._
import sbtcrossproject.CrossProject
import sbtcrossproject.CrossPlugin.autoImport._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {

  // scala version
  val scalaOrganization  = "org.scala-lang" // "org.typelevel"
  val scalaVersion       = "2.12.9" // "2.12.4-bin-typelevel-4"
  val crossScalaVersions = Seq("2.11.12", "2.12.9", "2.13.0")

  // build tools version
  val scalaFmtVersion = "1.5.1"

  // libraries versions
  val specs2Version   = "4.6.0"

  // resolvers
  val resolvers = Seq(
    Resolver sonatypeRepo "public",
    Resolver typesafeRepo "releases"
  )

  val scalaReflect       = libraryDependencies += "org.scala-lang"  %   "scala-reflect" % sbt.Keys.scalaVersion.value
  val shapeless          = libraryDependencies += "com.chuusai"     %%% "shapeless"     % "2.3.3"
}

trait Dependencies {

  val scalaOrganizationUsed = scalaOrganization
  val scalaVersionUsed = scalaVersion
  val crossScalaVersionsUsed = crossScalaVersions

  val scalaFmtVersionUsed = scalaFmtVersion

  // resolvers
  val commonResolvers = resolvers

  val mainDeps = Seq(shapeless)

  implicit class ProjectRoot(project: Project) {

    def root: Project = project in file(".")
  }

  implicit class ProjectFrom(project: CrossProject) {

    private val commonDir = "modules"

    def from(dir: String): CrossProject = project in file(s"$commonDir/$dir")
  }

  implicit class DependsOnProject(project: CrossProject) {

    private val testConfigurations = Set("test")
    private def findCompileAndTestConfigs(p: CrossProject) =
      (p.projects(JVMPlatform).configurations.map(_.name).toSet intersect testConfigurations) + "compile"

    private val thisProjectsConfigs = findCompileAndTestConfigs(project)
    private def generateDepsForProject(p: CrossProject) =
      p % (thisProjectsConfigs intersect findCompileAndTestConfigs(p) map (c => s"$c->$c") mkString ";")

    def compileAndTestDependsOn(projects: CrossProject*): CrossProject =
      project dependsOn (projects.map(generateDepsForProject): _*)
  }
}
