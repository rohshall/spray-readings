package com.hahasolutions

import java.util.Date
import java.sql.Timestamp

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.session.{ Database, Session }

// Use the implicit threadLocalSession
//import Database.threadLocalSession

/**
 * Adopted from slick-examples
 */

case class DeviceType(id: Option[Int], name: String, version: String)

case class Device(id: Option[Int], mac_addr: String, device_type_id: Int, manufactured_at: Timestamp, registered_at: Option[Timestamp])

case class Reading(id: Option[Int], device_mac_addr: String, value: String, created_at: Timestamp)
   
trait ModelComponent {

  // Put an implicitSession in scope for database actions
  val db = Database.forURL("jdbc:postgresql:sd_ventures_development", 
    driver="org.postgresql.Driver", 
    user="sd_ventures", 
    password="")
  implicit val implicitSession = db.createSession

  object DeviceTypes extends Table[DeviceType]("device_types") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.NotNull)
    def version = column[String]("version")

    def * = id.? ~ name ~ version <> (DeviceType, DeviceType.unapply _)
    def forInsert = name ~ version <> (
      { t => DeviceType(None, t._1, t._2) }, 
      { (dt: DeviceType) => Some((dt.name, dt.version)) })
  }

  object Devices extends Table[Device]("devices") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def mac_addr = column[String]("mac_addr")
    def device_type_id = column[Int]("device_type_id")
    def manufactured_at = column[Timestamp]("manufactured_at")
    def registered_at = column[Option[Timestamp]]("registered_at")

    def * = id.? ~ mac_addr ~ device_type_id ~ manufactured_at ~ registered_at <> (Device, Device.unapply _)
    def forInsert = mac_addr ~ device_type_id ~ manufactured_at ~ registered_at <> (
      { t => Device(None, t._1, t._2, t._3, t._4) },
      { (d: Device) => Some((d.mac_addr, d.device_type_id, d.manufactured_at, d.registered_at)) })
   
    // A reified foreign key relation that can be navigated to create a join
    def deviceType = foreignKey("device_type_fk", device_type_id, DeviceTypes)(_.id)
  }

  object Readings extends Table[Reading]("readings"){
   
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def device_mac_addr = column[String]("device_mac_addr", O.NotNull)
    def value = column[String]("value", O.NotNull)
    def created_at = column[Timestamp]("created_at")

    def * = id.? ~ device_mac_addr ~ value ~ created_at <> (Reading, Reading.unapply _)
    def forInsert = device_mac_addr ~ value ~ created_at <> (
      { t => Reading(None, t._1, t._2, t._3) }, 
      { (r: Reading) => Some((r.device_mac_addr, r.value, r.created_at)) })
   
    // A reified foreign key relation that can be navigated to create a join
    def device = foreignKey("device_fk", device_mac_addr, Devices)(_.mac_addr)
  }

  def loadData() {
    val date = new Date
    val timestamp = new Timestamp(date.getTime)
    db withSession {
      // Insert some device types
      DeviceTypes.forInsert.insertAll(
        DeviceType(None, "NIBP device", "1.0"),
        DeviceType(None, "SpO2 device", "1.0")
      )

      // Insert some devices (using JDBC's batch insert feature, if supported by the DB)
      Devices.forInsert.insertAll(
        Device(None, "123456789012", 4, timestamp, None),
        Device(None, "923456789012", 4, timestamp, None),
        Device(None, "823456789012", 5, timestamp, None)
      )
    }
  }
  
  def getDevices() = {
    db withSession {
      val q3 = for {
        d <- Devices
        dt <- d.deviceType
      } yield (d.mac_addr.asColumnOf[String], dt.name.asColumnOf[String])

      q3.list
    }
  }

  def getAllReadings() = {
    db withSession {
      val q3 = for {
        r <- Readings
        d <- r.device
        dt <- d.deviceType
      } yield (r.value, d.mac_addr.asColumnOf[String], dt.name.asColumnOf[String])

      q3.list
    }
  }

  def getDeviceReadings(device_mac_addr: String) = {
    db withSession {
      val q3 = for {
        r <- Readings
        d <- r.device if d.mac_addr === device_mac_addr
        dt <- d.deviceType
      } yield (r.value, d.mac_addr.asColumnOf[String], dt.name.asColumnOf[String])

      q3.list
    }
  }

  def addDeviceReading(device_mac_addr: String, value: String) {
    db withSession {
      val date = new Date
      val timestamp = new Timestamp(date.getTime)
      val reading = Reading(None, device_mac_addr, value, timestamp)
      Readings.forInsert.insert(reading)
    }
  }
}
