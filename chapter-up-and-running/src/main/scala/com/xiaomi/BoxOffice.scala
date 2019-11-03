package com.xiaomi

import akka.actor._
import akka.util.Timeout

import scala.concurrent.Future

/**
 * @author chenmengjie@xiaomi.com
 * @create 10/23/19
 **/
object BoxOffice {
  def props(implicit timeout: Timeout) = Props(new BoxOffice)

  def name = "boxOffice"


  //  创建活动消息
  case class CreateEvent(name: String, tickets: Int)

  //  获取活动消息
  case class GetEvent(name: String)

  //  请求所有活动消息
  case object GetEvents

  case class GetTickets(event: String, tickets: Int)

  case class CancelEvent(name: String)

  case class Event(name: String, tickets: Int)

  //  描述活动列表的消息
  case class Events(events: Vector[Event])

  //  CreateEvent的响应消息
  sealed trait EventResponse

  case class EventCreated(event: Event) extends EventResponse

  case object EventExists extends EventResponse

}

class BoxOffice(implicit timeout: Timeout) extends Actor {

  import BoxOffice._
  import context._


  //  使用上下文创建TicketSeller
  def createTicketSeller(name: String) =
    context.actorOf(TicketSeller.props(name), name)

  def receive = {
    //创建票据的消息
    case CreateEvent(name, tickets) =>
      //      接收到create消息去创建，局部方法创建ticketSeller，向其添加票券并以eventcreate进行响应
      def create() = {
        val eventTickets = createTicketSeller(name)
        val newTickets = (1 to tickets).map { ticketId =>
          TicketSeller.Ticket(ticketId)
        }.toVector
        eventTickets ! TicketSeller.Add(newTickets)
        sender() ! EventCreated(Event(name, tickets))
      }

      //      创建EventCreate并响应，或以EventExists作为响应
      context.child(name).fold(create())(_ => sender() ! EventExists)

    //获取票券
    case GetTickets(event, tickets) =>
      //      如果找不到发送空的ticket消息
      def notFound() = sender() ! TicketSeller.Tickets(event)

      //局部方法，从找到的ticketseller购买
      def buy(child: ActorRef) =
        child.forward(TicketSeller.Buy(tickets))

      //执行notfind，或从找到的ticketseller购买，fold参数第一个ifEmpty 第二个参数表示获取到的情况
      context.child(event).fold(notFound())(buy)


    case GetEvent(event) =>
      def notFound() = sender() ! None

      def getEvent(child: ActorRef) = child forward TicketSeller.GetEvent

      context.child(event).fold(notFound())(getEvent)

    //      获取票券事件
    case GetEvents =>
      import akka.pattern.ask
      import akka.pattern.pipe

      //      局部方法查询活动的所有ticketseller
      def getEvents = context.children.map { child =>
        //        ask返回future，一个最终包含一个值的类型
        self.ask(GetEvent(child.path.name)).mapTo[Option[Event]]
      }

      def convertToEvents(f: Future[Iterable[Option[Event]]]) =
        f.map(_.flatten).map(l => Events(l.toVector))

      //      管道在future完成时，把其中的值发送给一个actor，这里GetEvent消息的发送者RestApi
      pipe(convertToEvents(Future.sequence(getEvents))) to sender()


    case CancelEvent(event) =>
      def notFound() = sender() ! None

      def cancelEvent(child: ActorRef) = child forward TicketSeller.Cancel

      context.child(event).fold(notFound())(cancelEvent)
  }
}
