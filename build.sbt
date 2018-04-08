name := "IRProject-Indexing"

version := "0.1"

scalaVersion := "2.11.8"




javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

resolvers += "Apache Repos" at "https://repository.apache.org/content/repositories/releases"
resolvers += "OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"



libraryDependencies ++= Seq(

  "org.apache.lucene" % "lucene-core" % "4.0.0",
  "org.apache.lucene" % "lucene-analyzers-common" % "4.0.0",
  "org.apache.lucene" % "lucene-queryparser" % "4.0.0"

)
//