package com.xiaomi

import akka.actor._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext

/**
 * @author chenmengjie@xiaomi.com
 * @create 10/23/19
 **/
class Rest(system: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout = timeout

  implicit def executionContext = system.dispatcher

  def createBoxOffice = system.actorOf(BoxOffice.props, BoxOffice.name)
}

trait RestRoutes extends BoxOfficeApi
  with EventMarshalling {

  import akka.http.scaladsl.model.StatusCodes._

  def routes: Route = eventsRoute ~ eventRoute ~ ticketsRoute

  def eventsRoute =
    pathPrefix("events") {
      pathEndOrSingleSlash {
        get {
          // GET /events
          onSuccess(getEvents()) { events =>
            complete(OK, events)
          }
        }
      }
    }

  def eventRoute =
    pathPrefix("events" / Segment) { event =>
      pathEndOrSingleSlash {
        post {
          // POST /events/:event
          entity(as[EventDescription]) { ed =>
            //            调用createEvent方法活动，createEvent调用BoxOffice完成活动创建
            onSuccess(createEvent(event, ed.tickets)) {
              //              执行成功以201Created结束请求
              case BoxOffice.EventCreated(event) => complete(Created, event)
              case BoxOffice.EventExists =>
                val err = Error(s"$event event exists already.")
                //                如果活动无法创建，则以400BadRequest完成请求
                complete(BadRequest, err)
            }
          }
        } ~
          get {
            // GET /events/:event
            onSuccess(getEvent(event)) {
              _.fold(complete(NotFound))(e => complete(OK, e))
            }
          } ~
          delete {
            // DELETE /events/:event
            onSuccess(cancelEvent(event)) {
              _.fold(complete(NotFound))(e => complete(OK, e))
            }
          }
      }
    }


  def ticketsRoute =
    pathPrefix("events" / Segment / "tickets") { event =>
      post {
        pathEndOrSingleSlash {
          // POST /events/:event/tickets
          entity(as[TicketRequest]) { request =>
            onSuccess(requestTickets(event, request.tickets)) { tickets =>
              if (tickets.entries.isEmpty) complete(NotFound)
              else complete(Created, tickets)
            }
          }
        }
      }
    }
}

trait BoxOfficeApi {

  import BoxOffice._

  //  ActorSystem返回创建顶层Actor的地址，而不是Actor本身。这个地址称为ActorRef
  def createBoxOffice(): ActorRef

  implicit def executionContext: ExecutionContext

  implicit def requestTimeout: Timeout

  lazy val boxOffice = createBoxOffice()

  def createEvent(event: String, nrOfTickets: Int) =
    boxOffice.ask(CreateEvent(event, nrOfTickets))
      .mapTo[EventResponse]

  def getEvents() =
    boxOffice.ask(GetEvents).mapTo[Events]

  def getEvent(event: String) =
    boxOffice.ask(GetEvent(event))
      .mapTo[Option[Event]]

  def cancelEvent(event: String) =
    boxOffice.ask(CancelEvent(event))
      .mapTo[Option[Event]]

  def requestTickets(event: String, tickets: Int) =
    boxOffice.ask(GetTickets(event, tickets))
      .mapTo[TicketSeller.Tickets]
}
