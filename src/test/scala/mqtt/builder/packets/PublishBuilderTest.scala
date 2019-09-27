package mqtt.builder.packets

import mqtt.model.Packet.{ApplicationMessage, Publish}
import mqtt.model.QoS.{QoS0, QoS1, QoS2}
import mqtt.utils.Bit
import mqtt.utils.BitImplicits._


class PublishBuilderTest extends PacketBuilderTester {
  assertBuild(Map[Publish, Seq[Bit]](
    Publish(duplicate = false, 0, ApplicationMessage(retain = false, QoS0, "a/b", Seq[Byte](1, 2, 3))) ->
      Seq(
        0, 0, 1, 1, 0, 0, 0, 0, //dup, qos | qos, retain
        0, 0, 0, 0, 1, 0, 0, 0, //remaining length
        0, 0, 0, 0, 0, 0, 0, 0, //Topic name MSB
        0, 0, 0, 0, 0, 0, 1, 1, //Topic name LSB
        0, 1, 1, 0, 0, 0, 0, 1, //Topic name 'a'
        0, 0, 1, 0, 1, 1, 1, 1, //Topic name '/'
        0, 1, 1, 0, 0, 0, 1, 0, //Topic name 'b'
        0, 0, 0, 0, 0, 0, 0, 1, //Payload
        0, 0, 0, 0, 0, 0, 1, 0, //Payload
        0, 0, 0, 0, 0, 0, 1, 1, //Payload
      ),
    Publish(duplicate = true, 1234, ApplicationMessage(retain = false, QoS1, "a/b", Seq[Byte](1, 2, 3))) ->
      Seq(
        0, 0, 1, 1, 1, 0, 1, 0, //dup, qos | qos, retain
        0, 0, 0, 0, 1, 0, 1, 0, //remaining length
        0, 0, 0, 0, 0, 0, 0, 0, //Topic name MSB
        0, 0, 0, 0, 0, 0, 1, 1, //Topic name LSB
        0, 1, 1, 0, 0, 0, 0, 1, //Topic name 'a'
        0, 0, 1, 0, 1, 1, 1, 1, //Topic name '/'
        0, 1, 1, 0, 0, 0, 1, 0, //Topic name 'b'
        0, 0, 0, 0, 0, 1, 0, 0, //Packet id MSB
        1, 1, 0, 1, 0, 0, 1, 0, //Packet id LSB
        0, 0, 0, 0, 0, 0, 0, 1, //Payload
        0, 0, 0, 0, 0, 0, 1, 0, //Payload
        0, 0, 0, 0, 0, 0, 1, 1, //Payload
      ),
    Publish(duplicate = true, 1234, ApplicationMessage(retain = true, QoS2, "a/b", Seq.empty)) ->
      Seq(
        0, 0, 1, 1, 1, 1, 0, 1, //dup, qos | qos, retain
        0, 0, 0, 0, 0, 1, 1, 1, //remaining length
        0, 0, 0, 0, 0, 0, 0, 0, //Topic name MSB
        0, 0, 0, 0, 0, 0, 1, 1, //Topic name LSB
        0, 1, 1, 0, 0, 0, 0, 1, //Topic name 'a'
        0, 0, 1, 0, 1, 1, 1, 1, //Topic name '/'
        0, 1, 1, 0, 0, 0, 1, 0, //Topic name 'b'
        0, 0, 0, 0, 0, 1, 0, 0, //Packet id MSB
        1, 1, 0, 1, 0, 0, 1, 0, //Packet id LSB
      ),
    Publish(duplicate = true, 1234, ApplicationMessage(retain = true, QoS2, "a/b", (0 until 123).map(_.toByte))) ->
      Seq[Bit](
        0, 0, 1, 1, 1, 1, 0, 1, //dup, qos | qos, retain
        1, 0, 0, 0, 0, 0, 1, 0, //remaining length
        0, 0, 0, 0, 0, 0, 0, 1, //remaining length
        0, 0, 0, 0, 0, 0, 0, 0, //Topic name MSB
        0, 0, 0, 0, 0, 0, 1, 1, //Topic name LSB
        0, 1, 1, 0, 0, 0, 0, 1, //Topic name 'a'
        0, 0, 1, 0, 1, 1, 1, 1, //Topic name '/'
        0, 1, 1, 0, 0, 0, 1, 0, //Topic name 'b'
        0, 0, 0, 0, 0, 1, 0, 0, //Packet id MSB
        1, 1, 0, 1, 0, 0, 1, 0, //Packet id LSB
      ).++((0 until 123).map(_.toByte).toBitsSeq) //payload
  ))
}
