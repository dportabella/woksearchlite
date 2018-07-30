import com.github.dportabella.wokSearchLite._

import scala.concurrent._
import scala.concurrent.duration._

object Example {
  def main(args: Array[String]) {
    // val exampleDoi = "10.1126/science.1227970"
    // val exampleDoi = "10.1001/ARCHDERM.139.2.165"
    // val exampleDoi = "10.1002/(SICI)1096-9136(199704)14:4<284::AID-DIA348>3.0.CO;2-0"
    val exampleDoi = "\"10.1002/(SICI)1096-9136(199704)14:4<284::AID-DIA348>3.0.CO;2-0\""

    val doi = if (args.nonEmpty) args.head else exampleDoi

    try search(doi)
    finally { System.exit(0) } // workaround, otherwise application hangs up. why?
  }

  def search(doi: String) {
    println(s"search: $doi")

    val client = new WokSearchLiteClient()
    client.authenticate()

    val queryParameters = QueryParameters(databaseId = "WOS", userQuery = s"DO='$doi'", queryLanguage = "en")
    val retrieveParameters = RetrieveParameters(firstRecord = 1, count = 5, Seq(Some(SortField(name = "RS", sort = "D"))))

    val searchFutureResponse = client.service.search(queryParameters, retrieveParameters)

    val searchResponse = Await.result(searchFutureResponse, 5.seconds)

    println(searchResponse)

    println(WokSearchLiteHelper.toXML(searchResponse))

    val records: List[LiteRecord] = WokSearchLiteHelper.records(searchResponse)

    println(s"numRecords: ${records.length}")
    records.foreach { r =>
      println(s"record: $r")
      val h = new LiteRecordHelper(r)
      h.pairs.foreach(println)

      println("doctypes: " + h.values("Doctype"))
      println("sourceTitle: " + h.value("SourceTitle"))
      println("publishedBiblioYear: " + h.value("Published.BiblioYear"))
    }

    client.closeSession()
  }
}
