package com.descinet.shared_data.types.codecs

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor, Json}
import com.descinet.shared_data.types.DataUpdates.DeSciNetUpdate
import org.tessellation.currency.dataApplication.DataUpdate

object DataUpdateCodec {
  implicit val dataUpdateEncoder: Encoder[DataUpdate] = {
    case event: DeSciNetUpdate => event.asJson
    case _ => Json.Null
  }

  implicit val dataUpdateDecoder: Decoder[DataUpdate] = (c: HCursor) => c.as[DeSciNetUpdate]
}
