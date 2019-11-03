package com.xiaomi

import spray.json._

/**
 * @author chenmengjie@xiaomi.com
 * @create 10/23/19
 **/
//RestApi中使用的活动消息
//包含活动初始票数消息
case class EventDescription(tickets: Int) {
  require(tickets > 0)
}

//包含需求票数的消息
case class TicketRequest(tickets: Int) {
  require(tickets > 0)
}

//包含错误的消息
case class Error(message: String)


trait EventMarshalling extends DefaultJsonProtocol {

  import com.xiaomi.BoxOffice._

  implicit val eventDescriptionFormat = jsonFormat1(EventDescription)
  implicit val eventFormat = jsonFormat2(Event)
  implicit val eventsFormat = jsonFormat1(Events)
  implicit val ticketRequestFormat = jsonFormat1(TicketRequest)
  implicit val ticketFormat = jsonFormat1(TicketSeller.Ticket)
  implicit val ticketsFormat = jsonFormat2(TicketSeller.Tickets)
  implicit val errorFormat = jsonFormat1(Error)
}
