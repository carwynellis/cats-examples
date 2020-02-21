name := "cats-examples"

version := "1.0"

scalaVersion := "2.13.1"

val catsVersion = "2.0.0"

libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core"   % catsVersion,
    "org.typelevel" %% "cats-free"   % catsVersion
)

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

scalacOptions ++= Seq(
  "-target:jvm-1.8",        // Target Java 8
  "-explaintypes",          // Explain type errors with more detail
  "-deprecation",           // Emit deprecation warnings
  "-feature",               // Emit warnings where feature needs explicit import
  "-unchecked",             // Emit warnings related to type erasure
  "-Ywarn-unused:imports",  // Warn on unused imports
  "-Xfatal-warnings"        // Make warnings fatal
)

// Filter options that don't play well with the scala console.
// See https://tpolecat.github.io/2017/04/25/scalac-flags.html
scalacOptions in (Compile, console) ~= (_.filterNot(Set(
  "-Ywarn-unused:imports",
  "-Xfatal-warnings"
)))
