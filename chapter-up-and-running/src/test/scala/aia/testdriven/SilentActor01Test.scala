package aia.testdriven

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{MustMatchers, WordSpecLike}

/**
 * ${DESCRIPTION}
 *
 * @author chenmengjie@xiaomi.com
 * @create 10/24/2019
 **/
//从TestKit集成，提供测试用的actor系统
class SilentActor01Test extends TestKit(ActorSystem("testsystem")) with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {
  "A Silent Actor" must {
    //每个in 描述一个特定测试
    "change state when it receives a message,single threaded" in {
      fail("not implemented yet")
    }
    "change state when it receives a message,multi-threaded" in {
      fail("not implemented yet")
    }

    //    单线程测试内部状态
    "change internal state when it receives a message,single" in {
      //导入消息
      import SilentActor._

      //      为单线程测试创建
      val silentActor = TestActorRef[SilentActor]
      //向actor发送消息
      silentActor ! SilentMessage("whisper")
      //获取底层actor并检查其状态
      silentActor.underlyingActor.state must contain("whisper")
    }
  }
}

object SilentActor {

  case class SilentMessage(data: String)

  case class GetState(receiver: ActorRef)

}

class SilentActor extends Actor {

  import SilentActor._

  var internalState = Vector[String]()

  override def receive: Receive = {
    //    状态保留在向量中，每条消息报都添加到这个向量中
    case SilentMessage(data) => internalState = internalState :+ data
  }

  //  返回构件好的向量的状态方法
  def state = internalState
}
