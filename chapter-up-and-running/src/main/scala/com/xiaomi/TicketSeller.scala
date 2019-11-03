package com.xiaomi

import akka.actor.{Actor, PoisonPill, Props}

/**
 * @author chenmengjie@xiaomi.com
 * @create 10/23/19
 **/
object TicketSeller {
  def props(event: String) = Props(new TicketSeller(event))

  case class Add(tickets: Vector[Ticket])

  case class Buy(tickets: Int)

  case class Ticket(id: Int)

  //默认空票券
  case class Tickets(event: String,
                     entries: Vector[Ticket] = Vector.empty[Ticket])

  case object GetEvent

  case object Cancel

}

class TicketSeller(event: String) extends Actor {

  import TicketSeller._

  //  ticket由BoxOffice创建，并持有票券列表
  var tickets = Vector.empty[Ticket]

  def receive = {
    case Add(newTickets) => tickets = tickets ++ newTickets
    case Buy(nrOfTickets) =>
      val entries = tickets.take(nrOfTickets)
      if (entries.size >= nrOfTickets) {
        sender() ! Tickets(event, entries)
        tickets = tickets.drop(nrOfTickets)
      } else sender() ! Tickets(event)
    case GetEvent => sender() ! Some(BoxOffice.Event(event, tickets.size))
    case Cancel =>
      sender() ! Some(BoxOffice.Event(event, tickets.size))
      self ! PoisonPill
  }
}
