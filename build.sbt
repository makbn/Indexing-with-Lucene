name := "IRProject-Indexing"

version := "0.1"

scalaVersion := "2.11.8"

val sparkV = "2.2.0"


javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

resolvers += "Apache Repos" at "https://repository.apache.org/content/repositories/releases"
resolvers += "OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"



libraryDependencies ++= Seq(

 // "org.apache.spark" % "spark-core_2.11" % "2.1.0",
  "org.apache.spark" %% "spark-core" % sparkV ,
  "org.apache.spark" %% "spark-sql" % sparkV  ,
  //"org.zouzias" %% "spark-lucenerdd" % "0.3.1",
  "org.apache.lucene" % "lucene-core" % "4.0.0",
  "org.apache.lucene" % "lucene-analyzers-common" % "4.0.0",
  "org.apache.lucene" % "lucene-queryparser" % "4.0.0"

)
//