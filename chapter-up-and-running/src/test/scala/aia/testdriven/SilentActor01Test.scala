package aia.testdriven

import akka.actor.{Actor, ActorSystem}
import akka.testkit.TestKit
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
  }
}

class SilentActor extends Actor {
  override def receive: Receive = {
    case msg =>
  }
}
