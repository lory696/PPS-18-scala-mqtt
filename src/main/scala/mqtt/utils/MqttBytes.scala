package mqtt.utils

import mqtt.utils.BitImplicits._

object MqttBytes { //TODO can be removed
  def encode(value: Seq[Byte]): Seq[Byte] = {
    value.length.bits.drop(16).toBytes ++ value
  }
  
  def decode(data: Seq[Byte]): Option[String] = ??? //TODO
}
