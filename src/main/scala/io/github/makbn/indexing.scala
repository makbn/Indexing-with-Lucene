package io.github.makbn

import org.apache.lucene.analysis.core.StopAnalyzer
import org.apache.lucene.document.{Document, Field}
import org.apache.lucene.index._
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.Version
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable._


object indexing {

  private val AppName = "Indexing with Lucene"
  private val Master = "local[*]"
  private val DataFileDir = "src/main/resources/medical.all"
  private val analyzer = new StopAnalyzer(Version.LUCENE_40)
 // private val analyzer = new StandardAnalyzer(Version.LUCENE_40)
  private val config = new IndexWriterConfig(Version.LUCENE_40, analyzer)
  private val ramDirectory = new RAMDirectory

  def main(args: Array[String]): Unit = {
    indexing
    getFreq.foreach {
      case (k, v) => print(k)
        v.foreach {
          case (k2, v2) => print("," + k2 + "(" + v2 + ")")
        }
        println
    }
  }

  /**
    * create document for each record
    * @param arg
    * @return
    */
  def createDoc(arg: (Int, String)): Document = {
    val doc = new Document()
    val id = new Field("id", String.valueOf(arg._1), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS)
    val text = new Field("text", arg._2, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS)
    doc.add(text)
    doc.add(id)
    doc
  }

  /**
    * convert lines of input file to prescriptions
    * @param dataFile
    * @return
    */
  def pars(dataFile: RDD[String]): Array[(Int, String)] = {
    val regex = ".I\\s\\d{1,}"
    val prescriptions = new ArrayBuffer[(Int, String)]()
    val arr = dataFile.collect()

    var count : Int = 1
    var i : Int = 0
    while (i < arr.length) {
      var prescription = new StringBuilder
      if (arr(i).matches(regex)) {
        var j = i.+(2)
        while (j < arr.length && !arr(j).matches(regex)) {
          prescription.appendAll(arr(j).trim + " ")
          j += 1
        }
        prescriptions += ((count, prescription.result))
        i = j.+(-2)
        count += 1
      }
      i += 1
    }
    prescriptions.toArray
  }

  def indexing() = {
    val sparkConf = new SparkConf()
      .setAppName(AppName)
      .setMaster(Master)
    val sc = new SparkContext(sparkConf)
    implicit val sparkContext = sc

    val indexWriter = new IndexWriter(ramDirectory, config)
    val dataFile = sc.textFile(DataFileDir)
    val prs = pars(dataFile)


    for (i <- 0 to prs.length - 1)
      indexWriter.addDocument(createDoc(prs(i)))
    indexWriter.commit()
    indexWriter.close()
    sc.stop()
  }

  /**
    * generate output result
    * @return
    */
  def getFreq(): HashMap[String, HashMap[Int, Long]] = {
    try {
      val result = new HashMap[String, HashMap[Int, Long]]()
      val idxReader = DirectoryReader.open(ramDirectory)
      for (i <- 0 to idxReader.numDocs - 1) {
        val terms: Option[Terms] = Option(idxReader.getTermVector(i, "text"))
        if (terms.isDefined) {
          val termsEnum = terms.get.iterator(TermsEnum.EMPTY)
          var bytesRef = Option(termsEnum.next)
          while (bytesRef.isDefined) {
            val term = bytesRef.get.utf8ToString.toLowerCase
            if (result.contains(term)) {
              var record = result(term)
              if (record.contains(i))
                record += (i -> record(i).+(termsEnum.totalTermFreq))
              else
                record += (i -> termsEnum.totalTermFreq)
            } else
              result += (term -> new HashMap[Int, Long]() {
                {
                  put(i, termsEnum.totalTermFreq())
                }
              })
            bytesRef = Option(termsEnum.next)
          }
        }
      }
      result
    }
  }
}
