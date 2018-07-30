package com.github.dportabella.wokSearchLite

import scala.concurrent.Await
import scala.xml.Node
import scala.concurrent.Future
import scala.concurrent._
import scala.concurrent.duration._

object WokSearchLiteHelper {
	def toXML(searchResponse: SearchResponse): Node = {
		val responseXML = scalaxb.toXML[SearchResponse](searchResponse, None, Some("searchResponse"), com.github.dportabella.wokSearchLite.defaultScope)
		assert(responseXML.size == 1)
		responseXML.head
	}

	def fromXML(raw: String): SearchResponse =
		scalaxb.fromXML[SearchResponse](scala.xml.XML.loadString(raw))

	def records(searchResponse: SearchResponse): List[LiteRecord] =
		searchResponse.returnValue.toList.flatMap(_.records.flatten)

	def request[T](waitMillisBetweenRequests: Long, waitMillisOnExceededThrottle: Long)(f: => T): T = {
    Thread.sleep(waitMillisBetweenRequests)

    while (true) {
			try {
				return f
			} catch {
				case t: Throwable =>
					t.printStackTrace()
					if (t.toString.contains("Throttle server"))
            Thread.sleep(waitMillisOnExceededThrottle)
          else
            throw t
			}
		}

    throw new Error("unreachable code")
	}

  def requestAndWaitResult[T](requestTimeoutMillis: Long, waitMillisBetweenRequests: Long, waitMillisOnExceededThrottle: Long)(f: => Future[T]): T =
    request(waitMillisBetweenRequests, waitMillisOnExceededThrottle) {
      Await.result(f, requestTimeoutMillis.millis)
    }
}
