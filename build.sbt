import Dependencies._
import com.localytics.sbt.dynamodb.DynamoDBLocalKeys.dynamoDBLocalDownloadDir
import sbt.Keys._
import sbt.file
import uk.co.telegraph.cloud.AuthProfile

// give the user a nice default project!
lazy val buildNumber = sys.env.get("BUILD_NUMBER").map( bn => s"b$bn")

lazy val CommonSettings = Seq(
  name              := "usage-api",
  organization      := "uk.co.telegraph",
  version           := "1.1.0-" + buildNumber.getOrElse("dev"),
  scalaVersion      := "2.11.8",
  isSnapshot        := buildNumber.isEmpty,
  scalacOptions     += "-target:jvm-1.8",
  publishMavenStyle := false
)


lazy val root = (project in file(".")).
  configs ( IntegrationTest         ).
  settings( Defaults.itSettings: _* ).
  settings( CommonSettings     : _* ).
  settings(
    mainClass          in assembly := Some("uk.co.telegraph.usage.AppBoot"),
    target             in assembly := file("target"),
    assemblyJarName    in assembly := s"${name.value}-${version.value}.jar",
    test               in assembly := {},
    concurrentRestrictions         := Seq(
      Tags.limit(Tags.Test, 1)
    ),
    dynamoDBLocalDownloadDir       := file("target/dynamodb-local"),
    dynamoDBLocalPort              := 8000,
    dynamoDBLocalInMemory          := true
  ).
  settings(
    (stackCustomParams in DeployDev    ) += ("BuildVersion" -> version.value),
    (stackCustomParams in DeployPreProd) += ("BuildVersion" -> version.value),
    (stackCustomParams in DeployProd   ) += ("BuildVersion" -> version.value),
    (stackAuth in DeployPreProd        ) := AuthProfile(Some("PreProd")),
    (stackAuth in DeployProd           ) := AuthProfile(Some("Prod"))
  )

(testFrameworks in IntegrationTest) += new TestFramework("com.waioeka.sbt.runner.CucumberFramework")
//(test in IntegrationTest)           := (test in IntegrationTest).dependsOn(startDynamoDBLocal).value

libraryDependencies ++=
  ProjectDependencies ++
  UnitTestDependencies ++
  IntTestDependencies

dependencyOverrides ++= ProjectOverrides
publishTo := {
  if( isSnapshot.value ){
    Some("mvn-artifacts" at "s3://mvn-artifacts/snapshot")
  }else{
    Some("mvn-artifacts" at "s3://mvn-artifacts/release")
  }
}
