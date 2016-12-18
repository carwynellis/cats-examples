name := "cats-examples"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-deprecation")

val catsVersion = "0.8.1"

libraryDependencies ++= Seq(
    "org.typelevel" %% "cats" % catsVersion,
    "org.typelevel" %% "cats-free" % catsVersion
)

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")
