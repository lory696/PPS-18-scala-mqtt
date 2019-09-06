package mqtt.broker

import mqtt.broker.SampleInstances._
import mqtt.model.Packet.ConnectReturnCode.{ConnectionAccepted, IdentifierRejected, UnacceptableProtocolVersion}
import mqtt.model.Packet._
import mqtt.model.QoS.QoS0
import org.scalatest.{Assertion, FunSuite}

import scala.concurrent.duration.Duration


class TestConnectPacketHandler extends FunSuite {
  //TODO add test for presistent flag
  //TODO will message publication test?
  
  def checkConnectReturnCode(packet: Connect, returnCode: ConnectReturnCode, searchInPendingTransmission: Boolean): Assertion = {
    val bs1 = ConnectPacketHandler.handle(bs0, packet, sample_socket_0)
    if(searchInPendingTransmission) {
      bs1.sessionFromClientID(sample_id_0).fold(fail)(s => {
        s.pendingTransmission.find{ case Connack(_, `returnCode`) => true }.fold(fail)(_ => succeed)
      })
    } else {
      bs1.closing.get(sample_socket_0).fold(fail)(seq => {
        seq.find { case Connack(_, `returnCode`) => true }.fold(fail)(_ => succeed)
      })
    }
  }
  
  test("Sending a connect packet with and unsupported protocol version should disconnect") {
    val packet = sample_connect_packet_0.copy(protocol = Protocol("MQTT", 3))
    checkConnectReturnCode(packet, UnacceptableProtocolVersion, searchInPendingTransmission = false)
  }
  
  test("Sending a connect packet with a bad client identifier should disconnect") {
    val packet = sample_connect_packet_0.copy(clientId = "")
    checkConnectReturnCode(packet, IdentifierRejected, searchInPendingTransmission = false)
  }
  
  test("Sending a legit connect packet should respond with ack 0") {
    checkConnectReturnCode(sample_connect_packet_0, ConnectionAccepted, searchInPendingTransmission = true)
  }
  
  test("Sending a connect packet with CleanSession 0 should clear the session") {
    val packet = sample_connect_packet_0.copy(cleanSession = true)
    val bs1 = bs0.setSession(sample_id_0, sample_session_0)
    val bs2 = ConnectPacketHandler.handle(bs1, packet, sample_socket_0)
    bs2.sessionFromClientID(sample_id_0).fold(fail)(s => {
      assert(s.subscriptions.isEmpty)
    })
  }
  
  def checkCleanSession(cleanSession: Boolean, SessionPresent: Boolean, SessionWasPresent: Boolean): Assertion = {
    val packet = sample_connect_packet_0.copy(cleanSession = cleanSession)
    val bs1 = if (SessionWasPresent) bs0.setSession(sample_id_0, sample_session_0) else bs0
    val bs2 = ConnectPacketHandler.handle(bs1, packet, sample_socket_0)
    bs2.sessionFromClientID(sample_id_0).fold(fail)(s => {
      s.pendingTransmission.find{ case Connack(SessionPresent, ConnectionAccepted) => true }.fold(fail)(_ => succeed)
    })
  }
  
  test("Sending a connect packet with CleanSession 1 should reply with Session Present 0") {
    checkCleanSession(cleanSession = true, SessionPresent = false, SessionWasPresent = true)
  }
  
  test("Sending a connect packet with CleanSession 0 should reply with Session Present 1 if session was present") {
    checkCleanSession(cleanSession = false, SessionPresent = true, SessionWasPresent = true)
  }
  
  test("Sending a connect packet with CleanSession 0 should reply with Session Present 0 if session was not present") {
    checkCleanSession(cleanSession = false, SessionPresent = false, SessionWasPresent = false)
  }
  
  test("A Connect from a new socket should disconnect the old one") {
    val bs1 = ConnectPacketHandler.handle(bs0, sample_connect_packet_0, sample_socket_0)
    val bs2 = ConnectPacketHandler.handle(bs1, sample_connect_packet_0, sample_socket_1)
    assert(bs2.closing.get(sample_socket_0).isDefined)
  }
  
  test("A Connect from a new socket should replace the old one") {
    val bs1 = ConnectPacketHandler.handle(bs0, sample_connect_packet_0, sample_socket_0)
    val bs2 = ConnectPacketHandler.handle(bs1, sample_connect_packet_0, sample_socket_1)
    bs2.sessionFromClientID(sample_id_0).fold(fail)(s => {
      s.socket.fold(fail)(sk => assert(sk == sample_socket_1))
    })
  }
  
  test("The will message should be saved") {
    val packet = sample_connect_packet_0.copy(willMessage = Option(ApplicationMessage(retain = false, QoS0, sample_topic_0, Seq())))
    val bs1 = ConnectPacketHandler.handle(bs0, packet, sample_socket_0)
    bs1.sessionFromClientID(sample_id_0).fold(fail)(s => {
      s.socket.fold(fail)(sk => sk.willMessage.fold(fail)(m => assert(m.topic == sample_topic_0)))
    })
  }
  
  test("The keep alive should be saved") {
    val packet = sample_connect_packet_0.copy(keepAlive = Duration(10, "minutes"))
    val bs1 = ConnectPacketHandler.handle(bs0, packet, sample_socket_0)
    bs1.sessionFromClientID(sample_id_0).fold(fail)(s => {
      assert(s.keepAlive == Duration(10, "minutes"))
    })
  }
  
  test("Connecting two times should disconnect") {
    val bs1 = ConnectPacketHandler.handle(bs0, sample_connect_packet_0, sample_socket_0)
    val bs2 = ConnectPacketHandler.handle(bs1, sample_connect_packet_0, sample_socket_0)
    assert(bs2.closing.get(sample_socket_0).isDefined)
  }
}
