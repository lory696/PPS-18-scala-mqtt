package mqtt.builder.packets

import mqtt.builder.CommonBuilders._
import mqtt.builder.{Builder, IdentityBuilder}
import mqtt.model.Packet.Pingreq

/**
 * Provide the structure of Pingreq packet as referred in chapter 3.12.
 */
case object PingreqStructure extends IdentityBuilder[Pingreq] {
  override val builder: Builder[Pingreq] =
    controlPacketType(12) :: (4 zeros) :: remainingLength //3.12.1
}
