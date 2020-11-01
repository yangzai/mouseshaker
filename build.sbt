import sbt.file

name := "mouseshaker"

version := "0.1"

scalaVersion := "2.13.3"

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "2.4.4"
)

scalacOptions ++= Seq(
  "-feature",
  "-language:higherKinds"
)

assemblyJarName in assembly := s"${name.value}-uber.jar"
test in assembly := {}

enablePlugins(SbtProguard)
javaOptions in (Proguard, proguard) := Seq("-Xmx2G")
proguardVersion in Proguard := "6.2.2"
artifactPath in Proguard := (proguardDirectory in Proguard).value / s"${name.value}.jar"

proguardOptions in Proguard ++= Seq("-dontnote", "-dontwarn", "-ignorewarnings",// "-dontobfuscate",
  "-optimizations !method/inlining/short,!method/inlining/unique", //inlining buggy for scala
  ProguardOptions keepMain "Main",
  //https://www.guardsquare.com/en/products/proguard/manual/examples
  "-keep class * implements org.xml.sax.EntityResolver",
  """-keepclassmembers class * {
    |    ** MODULE$;
    |}
    |-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinPool {
    |    long eventCount;
    |    int  workerCounts;
    |    int  runControl;
    |    scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode syncStack;
    |    scala.concurrent.forkjoin.ForkJoinPool$WaitQueueNode spareStack;
    |}
    |-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinWorkerThread {
    |    int base;
    |    int sp;
    |    int runState;
    |}
    |-keepclassmembernames class scala.concurrent.forkjoin.ForkJoinTask {
    |    int status;
    |}
    |-keepclassmembernames class scala.concurrent.forkjoin.LinkedTransferQueue {
    |    scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference head;
    |    scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference tail;
    |    scala.concurrent.forkjoin.LinkedTransferQueue$PaddedAtomicReference cleanMe;
    |}""".stripMargin
)

//integrate proguard with assembly dependency
proguardInputs in Proguard := Seq((assemblyOutputPath in assembly).value)
proguardLibraries in Proguard ~= { _.filter(_.toPath startsWith file(scala.util.Properties.jdkHome).toPath) }
proguardInputFilter in Proguard := { _ => None }
proguardMerge in Proguard := false
(proguard in Proguard) := (proguard in Proguard).dependsOn(assembly).value
