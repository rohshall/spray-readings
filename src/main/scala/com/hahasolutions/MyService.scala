package com.hahasolutions

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport.sprayJsonMarshaller
import spray.httpx.SprayJsonSupport.sprayJsonUnmarshaller
import DefaultJsonProtocol._
  


// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}

// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService with ModelComponent {

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            val devices = getDevices()
            <html>
              <body>
                <h1>Devices Available</h1>
                <ul>
                { devices.map( d => <li>{d._1 ++ " " ++ d._2}</li> ) }
                </ul>
              </body>
            </html>
          }
        }
      }
    } ~
    path("readings") {
      get {
        complete {
          val readings = getAllReadings()
          readings
        }
      }
    } ~
    path("devices" / PathElement / "readings") { dev_mac_addr:String =>
      get {
        complete {
          val readings = getDeviceReadings(dev_mac_addr)
          readings
        }
      } ~
      post {
        entity(as[Map[String, String]]) { readingMap =>
          complete { 
            addDeviceReading(dev_mac_addr, readingMap("value"))
            Map("status" -> "ok")
          }
        }
      }
    }
}
