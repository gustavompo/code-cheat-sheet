/**
 Queries Zendesk Search API to find and extract some specific fields
**/
import java.time.LocalDateTime

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

import org.scalatest.{ FreeSpec, MustMatchers }
import org.scalatestplus.play.OneAppPerSuite

import play.api.libs.json._
import play.api.{ Application, Logger }
import play.api.libs.ws.{ WSAuthScheme, WSClient }
import play.api.test.WsTestClient

import test.{ AppSpec, fakeApp }

class TicketFixer extends FreeSpec with OneAppPerSuite with AppSpec with MustMatchers {

  val ZENDESK_USER = "xx@xx.com/token"
  val ZENDESK_PASSWORD = "xx"
  val CSV_FILE_PATH = "/xx/dev/output.csv"
  val MINIMUM_VALID_JOBID = 90000000
  val MAXIMUM_VALID_JOBID = 180000000

  implicit val idvReads = new Reads[IdV] {
    override def reads(json: JsValue): JsResult[IdV] =
      Try {
        JsSuccess(IdV((json \ "id").as[Long], Some((json \ "value").get.toString)))
      }.getOrElse(JsError(s"error parsing ${json.toString}"))
  }
  implicit val ticketReads: Reads[Ticket] = Json.reads[Ticket]
  implicit val resultsReads: Reads[ZendeskResults] = Json.reads[ZendeskResults]

  override def myApp: Application = fakeApp

  "List zendesk tickets" - {
    "Extract jobID from description field" in {
      WsTestClient.withClient { client =>
        val initialUrl =
          s"""https://xx.zendesk.com/api/v2/search.json?query=fieldvalue:"assalto__roubo"&sort_by=created_at&sort_order=desc"""
        processPaginatedTickets(client, initialUrl)
      }
    }
  }

  def processPaginatedTickets(client: WSClient, url: String): Any = {
    listTickets(client, url) match {
      case Some(tickets) =>
        Logger.info(s"Total of ${tickets.results.size} tickets")
        val jobIdsAndTickets = extractJobIdFromDescription(tickets.results)
        Logger.info(s"Tickets with jobid in description ${jobIdsAndTickets.size}")
        saveToCsv(jobIdsAndTickets)
        if (tickets.next_page.isDefined)
          processPaginatedTickets(client, tickets.next_page.get)
    }
  }

  def extractJobIdFromDescription(tickets: List[Ticket]): List[(Ticket, Long)] = {
    val jobIdRegEx = """[^\d]((\d{8,9}))[^\d]""".r
    tickets
        .map { r => (r, jobIdRegEx.findAllMatchIn(r.description).map(x => x.group(1).toLong)) }
        .map(k => (k._1, k._2.filter(l => l >= MINIMUM_VALID_JOBID && l <= MAXIMUM_VALID_JOBID)))
        .filter { case (_, jobids) => jobids.nonEmpty }
        .map { case (ticket, jobids) => (ticket, jobids.toList.head) }
  }

  def saveToCsv(jobIdsAndTicket: List[(Ticket, Long)]): Unit = {
    jobIdsAndTicket.foreach { case (ticket, jobId) =>
      if(extractExistingJobId(ticket).replaceAll("\"", "").toLong != jobId)
        scala.tools.nsc.io.File(CSV_FILE_PATH+"2").appendAll(formatToCsv(ticket, jobId))
      scala.tools.nsc.io.File(CSV_FILE_PATH).appendAll(formatToCsv(ticket, jobId))
    }
  }

  def formatToCsv(ticket: Ticket, extractedJobId: Long): String = {
    s"${ticket.id},$extractedJobId,${extractExistingJobId(ticket)}\n"
  }

  def extractExistingJobId(ticket: Ticket): String =
    ticket.custom_fields.find(f => f.id == 78396628).flatMap(_.value).getOrElse("")

  def listTickets(client: WSClient, url: String): Option[ZendeskResults] = {
    Logger.info(s"Querying $url")
    try {
      val request = client
          .url(url)
          .withAuth(ZENDESK_USER, ZENDESK_PASSWORD, WSAuthScheme.BASIC)
          .get

      val res = Await.result(request, Duration.Inf)
      Some(res.json.as[ZendeskResults])
    } catch {
      case e: Throwable =>
        Logger.error(s"error on $url", e)
        None
    }
  }
}

case class IdV(id: Long, value: Option[String])

case class Ticket(
    id: Long,
    created_at: LocalDateTime,
    description: String,
    custom_fields: List[IdV]
)

case class ZendeskResults(results: List[Ticket], next_page: Option[String])
