import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._
import spray.revolver.RevolverPlugin._
import org.scalastyle.sbt._
import com.markatta.sbttaglist.TagListPlugin
import com.typesafe.sbt.SbtScalariform
import sbt.Package.ManifestAttributes

object GraphBuild extends Build {

  val akkaVersion = "2.2.3"
  val sprayVersion = "1.2.0"
  
  lazy val sharedSettings = Project.defaultSettings ++
    ScalastylePlugin.Settings ++ 
    Seq(assemblySettings: _*) ++ 
    Revolver.settings ++ 
    TagListPlugin.tagListSettings ++ 
    SbtScalariform.scalariformSettings ++
    Seq(
      name := "graph",
      description := "Creates a social graph from twitter api.",
      organizationName := "babel",
      organization := "net.babel",
      version := "0.1.0",
      scalaVersion := "2.10.3",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-remote" % akkaVersion,
        "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
        "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
        "com.typesafe.akka" % "akka-slf4j_2.10" % akkaVersion,
        "com.thinkaurelius.titan" % "titan-persistit" % "0.4.2",
        "io.spray" % "spray-can" % sprayVersion,
        "io.spray" % "spray-routing" % sprayVersion,
        "io.spray" % "spray-caching" % sprayVersion,
        "io.spray" % "spray-testkit" % sprayVersion,
        "io.spray" % "spray-client" % sprayVersion,
        "io.spray" %%  "spray-json" % "1.2.5"
      ),
      resolvers ++= Seq(
        Opts.resolver.sonatypeSnapshots,
        Opts.resolver.sonatypeReleases,
        "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
        "spray" at "http://repo.spray.io/"
      ),
      parallelExecution in Test := false,
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions", "-language:reflectiveCalls", "-language:postfixOps", "-Yresolve-term-conflict:package"),
      publishMavenStyle := true,
      pomIncludeRepository := { x => false },
      publishArtifact in Test := false,
      javaOptions += "-Xmx5G"
    )

  lazy val jerksonRand = RootProject(uri("https://github.com/randhindi/jerkson.git"))

  lazy val graph = Project(id="graph", base=file("."), settings=sharedSettings).dependsOn(jerksonRand).settings(
    mainClass in (Compile, run) := Some("net.babel.graph.SampleClient")
  )
}