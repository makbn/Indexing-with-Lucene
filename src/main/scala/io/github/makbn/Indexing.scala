package io.github.makbn

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field}
import org.apache.lucene.index._
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.Version
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer

object Indexing {

  private val APP_NAME = "Indexing with Lucene"
  private val MASTER = "local[*]"
  private val DATA_FILE_DIR = "src/main/resources/medical.all"
  private val analyzer = new StandardAnalyzer(Version.LUCENE_40)
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

  def createDoc(arg: (Int, String)): Document = {
    val doc = new Document()
    doc.add(new Field("text", arg._2, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS))
    doc.add(new Field("id", String.valueOf(arg._1), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS))
    return doc
  }

  def pars(dataFile: RDD[String]): Array[(Int, String)] = {
    val regex = ".I\\s\\d{1,}"
    val prescriptions = new ArrayBuffer[(Int, String)]()
    val arr = dataFile.collect()
    var i = 0
    var count = 1
    while (i < arr.length) {
      var prescription = ""
      if (arr(i).matches(regex)) {
        var j = i.+(2)
        while (j < arr.length && !arr(j).matches(regex)) {
          prescription = prescription.+(arr(j).trim + " ")
          j += 1
        }
        prescriptions += ((count, prescription))
        i = j.+(-2)
        count += 1
      }
      i += 1
    }
    return prescriptions.toArray
  }

  def indexing() = {
    val sparkConf = new SparkConf()
      .setAppName(APP_NAME)
      .setMaster(MASTER)
    val sc = new SparkContext(sparkConf)
    implicit val sparkContext = sc

    val indexWriter = new IndexWriter(ramDirectory, config)
    val dataFile = sc.textFile(DATA_FILE_DIR)
    val prs = pars(dataFile)

    for (i <- 0 to prs.length - 1)
      indexWriter.addDocument(createDoc(prs(i)))
    indexWriter.commit()
    indexWriter.close()
    sc.stop()
  }

  def getFreq(): HashMap[String, HashMap[Int, Long]] = {
    try {
      val result = new HashMap[String, HashMap[Int, Long]]()
      val idxReader = DirectoryReader.open(ramDirectory)
      for (i <- 0 to idxReader.numDocs - 1) {
        val terms: Terms = idxReader.getTermVector(i, "text")
        if (terms != null) {
          val termsEnum = terms.iterator(null)
          var bytesRef = termsEnum.next
          while (bytesRef != null) {
            val term = bytesRef.utf8ToString.toLowerCase
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
            bytesRef = termsEnum.next
          }
        }
      }
      return result
    }
  }
}
