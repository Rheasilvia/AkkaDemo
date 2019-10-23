package com.xiaomi

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future


/**
 * @author chenmengjie@xiaomi.com
 * @create 10/23/19
 **/
object Main extends App with RequestTimeout {

  //从配置中获取主机和端口
  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val post = config.getInt("http.port")

  //bindAndHandler是异步的，需要隐式执行
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher

  val api = new Rest(system, requestTimeout(config)).routes

  implicit val materializer = ActorMaterializer()
  val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(api, host, post)

}

trait RequestTimeout {

  import scala.concurrent.duration._

  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}

