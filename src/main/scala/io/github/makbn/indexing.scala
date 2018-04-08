package io.github.makbn

import org.apache.lucene.analysis.core.StopAnalyzer
import org.apache.lucene.document.{Document, Field}
import org.apache.lucene.index._
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.Version
import scala.collection.mutable._
import scala.io.Source


object indexing {
  
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
    * @return
    */
  def pars(): Array[(Int,String)] ={
    val regex = "\\.I\\s\\d{1,}.W"
    val file= Source.fromFile(DataFileDir).getLines().toArray
    val raw =new StringBuilder
    for(line <- file)
      raw.appendAll(line)
    val per=raw.result.split(regex)
    val prescriptions = new ArrayBuffer[(Int, String)]()

    for(id <- 0 to per.length-1){
      prescriptions += ((id, per(id)))
    }
    prescriptions.toArray
  }

  def indexing() = {
    val indexWriter = new IndexWriter(ramDirectory, config)
    val prs = pars()
    for (i <- 0 to prs.length - 1)
      indexWriter.addDocument(createDoc(prs(i)))
    indexWriter.commit()
    indexWriter.close()
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
