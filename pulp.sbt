import sbt._
import Settings._
import sbtcrossproject.CrossPlugin.autoImport.{ crossProject, CrossType }

lazy val root = project.root
  .setName("pulp")
  .setDescription("Pulp build")
  .configureRoot
  .noPublish
  .aggregate(pulpJVM, pulpJS)

lazy val pulp = crossProject(JVMPlatform, JSPlatform).crossType(CrossType.Pure).build.from("pulp")
  .setName("pulp")
  .setDescription("Scala library for guiceless dependency injection")
  .setInitialImport("cats.implicits._")
  .configureModule
  .configureTests()
  .publish
  .settings(Dependencies.scalaReflect)
  .settings(libraryDependencies ++= Seq(
    "org.specs2" %%% "specs2-core"       % Dependencies.specs2Version % "test",
    "org.specs2" %%% "specs2-scalacheck" % Dependencies.specs2Version % "test"
  ))

lazy val pulpJVM = pulp.jvm
lazy val pulpJS = pulp.js

lazy val readme = scalatex.ScalatexReadme(
    projectId = "readme",
    wd        = file(""),
    url       = "https://github.com/scalalandio/pulp/tree/master",
    source    = "Readme"
  )
  .configureModule
  .noPublish
  .enablePlugins(GhpagesPlugin)
  .settings(
    siteSourceDirectory := target.value / "scalatex",
    git.remoteRepo := "git@github.com:scalalandio/pulp.git",
    Jekyll / makeSite / includeFilter := new FileFilter { def accept(p: File) = true }
  )

addCommandAlias("fullTest", ";test;scalastyle")
addCommandAlias("fullCoverageTest", ";coverage;test;coverageReport;coverageAggregate;scalastyle")
addCommandAlias("relock", ";unlock;reload;update;lock")
