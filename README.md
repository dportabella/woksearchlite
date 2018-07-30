# WokSearchLite

A Scala client for [Web of Science WokSearchLite SOAP webservice](http://ipscience-help.thomsonreuters.com/wosWebServicesLite/WebServicesLiteOverviewGroup/Introduction.html).
  

## How to use it
Add to sbt build.sbt:

```
resolvers += "dportabella-3rd-party-mvn-repo-snapshots" at "https://github.com/dportabella/3rd-party-mvn-repo/raw/master/snapshots/"
libraryDependencies += "com.github.dportabella" %% "woksearchlite" % "0.2.0-SNAPSHOT"
```

## Example code

See full example at `src/test/scala/Example.scala`.

```
import com.github.dportabella.wokSearchLite._
val doi = "\"10.1002/(SICI)1096-9136(199704)14:4<284::AID-DIA348>3.0.CO;2-0\""

val client = new WokSearchLiteClient()
client.authenticate()

val queryParameters = QueryParameters(databaseId = "WOS", userQuery = s"DO='$doi'", queryLanguage = "en")
val retrieveParameters = RetrieveParameters(firstRecord = 1, count = 5, Seq(Some(SortField(name = "RS", sort = "D"))))

val searchFutureResponse = client.service.search(queryParameters, retrieveParameters)

val searchResponse = Await.result(searchFutureResponse, 5.seconds)

println(WokSearchLiteHelper.toXML(searchResponse))

val records: List[LiteRecord] = WokSearchLiteHelper.records(searchResponse)

records.foreach { r =>
  val h = new LiteRecordHelper(r)
  h.pairs.foreach(println)
  println("doctypes: " + h.values("Doctype"))
  println("sourceTitle: " + h.value("SourceTitle"))
  println("publishedBiblioYear: " + h.value("Published.BiblioYear"))
}

client.closeSession()
```

Output:
```
authenticateResponse: AuthenticateResponse(Some(C6zJrdvVTownOBvZ...))
authenticationCookie: C6zJrdvVTownOBvZ...

<?xml version="1.0"?>
<searchResponse xmlns:ns1="http://woksearch.v3.wokmws.thomsonreuters.com" xmlns:tns0="http://woksearchlite.v3.wokmws.thomsonreuters.com" xmlns:tns="http://auth.cxf.wokmws.thomsonreuters.com" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <return>
    <queryId>1</queryId>
    <recordsFound>1</recordsFound>
    <recordsSearched>68014680</recordsSearched>
    <records>
      <uid>WOS:A1997WT30700004</uid>
      <title>
        <label>Title</label>
        <value>NO-dependent smooth muscle vasodilatation is reduced in NIDDM patients with peripheral sensory neuropathy</value>
      </title>
      <doctype>
        <label>Doctype</label>
        <value>Article</value>
      </doctype>
      <source>
        <label>Issue</label>
        <value>4</value>
      </source>
      <source>
        <label>Pages</label>
        <value>284-290</value>
      </source>
      <source>
        <label>Published.BiblioDate</label>
        <value>APR</value>
      </source>
      <source>
        <label>Published.BiblioYear</label>
        <value>1997</value>
      </source>
      <source>
        <label>SourceTitle</label>
        <value>DIABETIC MEDICINE</value>
      </source>
      <source>
        <label>Volume</label>
        <value>14</value>
      </source>
      <authors>
        <label>Authors</label>
        <value>Pitei, DL</value>
        <value>Watkins, PJ</value>
        <value>Edmonds, ME</value>
      </authors>
      <keywords>
        <label>Keywords</label>
        <value>diabetic peripheral sensory neuropathy</value>
        <value>endothelium-dependent and endothelium-independent vasodilatation iontophoresis</value>
        <value>vascular smooth muscle</value>
      </keywords>
      <other>
        <label>Identifier.Ids</label>
        <value>WT307</value>
      </other>
      <other>
        <label>Identifier.Issn</label>
        <value>0742-3071</value>
      </other>
      <other>
        <label>Identifier.Xref_Doi</label>
        <value>10.1002/(SICI)1096-9136(199704)14:4&lt;284::AID-DIA348&gt;3.0.CO;2-0</value>
      </other>
      <other>
        <label>ResearcherID.Disclaimer</label>
        <value>ResearcherID data provided by Clarivate Analytics</value>
      </other>
    </records>
  </return>
</searchResponse>


(Published.BiblioYear,List(1997))
(Published.BiblioDate,List(APR))
(Title,List(NO-dependent smooth muscle vasodilatation is reduced in NIDDM patients with peripheral sensory neuropathy))
(Volume,List(14))
(SourceTitle,List(DIABETIC MEDICINE))
(Pages,List(284-290))
(Identifier.Issn,List(0742-3071))
(Doctype,List(Article))
(ResearcherID.Disclaimer,List(ResearcherID data provided by Clarivate Analytics))
(Keywords,List(diabetic peripheral sensory neuropathy, endothelium-dependent and endothelium-independent vasodilatation iontophoresis, vascular smooth muscle))
(Issue,List(4))
(Authors,List(Pitei, DL, Watkins, PJ, Edmonds, ME))
(Identifier.Xref_Doi,List(10.1002/(SICI)1096-9136(199704)14:4<284::AID-DIA348>3.0.CO;2-0))
(Identifier.Ids,List(WT307))


doctypes: List(Article)
sourceTitle: Some(DIABETIC MEDICINE)
publishedBiblioYear: Some(1997)
```

The scalaxb plugin generates the sources to call the WoS API, based on the official `src/main/wsdl/*.wsdl` WoS API files. 
Files are generated on `target/scala-2.*/src_managed/main/sbt-scalaxb/`.
These are the simplified case classes:

```
case class RetrieveById(databaseId: String,
  uid: Seq[String],
  queryLanguage: String,
  retrieveParameters: RetrieveParameters)

case class RetrieveByIdResponse(returnValue: Option[SearchResults])

case class Search(queryParameters: QueryParameters, retrieveParameters: RetrieveParameters)

case class QueryParameters(databaseId: String,
  userQuery: String,
  editions: Seq[Option[EditionDesc]],
  symbolicTimeSpan: Option[String],
  timeSpan: Option[TimeSpan],
  queryLanguage: String)

case class EditionDesc(collection: String, edition: String)
case class TimeSpan(begin: String, end: String)

case class Retrieve(queryId: String, retrieveParameters: RetrieveParameters)
case class RetrieveParameters(firstRecord: Int, count: Int, sortField: Seq[Option[SortField]])
case class SortField(name: String, sort: String)
case class RetrieveResponse(returnValue: Option[SearchResults])
      

case class SearchResponse(returnValue: Option[SearchResults])

case class SearchResults(queryId: Option[String],
  recordsFound: Int,
  recordsSearched: Long,
  parent: Option[LiteRecord],
  records: Seq[Option[LiteRecord]])
      
case class LiteRecord(uid: Option[String],
  title: Seq[Option[LabelValuesPair]],
  doctype: Seq[Option[LabelValuesPair]],
  source: Seq[Option[LabelValuesPair]],
  authors: Seq[Option[LabelValuesPair]],
  keywords: Seq[Option[LabelValuesPair]],
  other: Seq[Option[LabelValuesPair]])

case class LabelValuesPair(label: Option[String], value: Seq[Option[String]])
```

Note that `LabelValuesPair` values are cumbersome to use. That's why we add the the `LiteRecordHelper`. See the example above.


# Compile
Build and publish with `sbt "+ publishLocal"`.

This fails the first time, because managed files `./target/scala-2.1*/src_managed/main/sbt-scalaxb/` are not generated before compiling `./src/main/scala` files.
The problem is solved by running the command again.
Let me know if you know how to solve this.
