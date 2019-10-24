package aia.testdriven

import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Suite}


/**
 * 所有测试完成后停止系统
 *
 * @author chenmengjie@xiaomi.com
 * @create 10/24/2019
 **/
trait StopSystemAfterAll extends BeforeAndAfterAll {
  this: TestKit with Suite =>
  override protected def afterAll() {
    super.afterAll()
    shutdown(system)
  }

}
