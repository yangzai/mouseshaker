name := "mouseshaker"

version := "0.1"

scalaVersion := "2.13.3"

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "2.4.0"
)

scalacOptions ++= Seq(
  "-feature",
  "-language:higherKinds"
)

assemblyJarName in assembly := s"${name.value}.jar"
test in assembly := {}
