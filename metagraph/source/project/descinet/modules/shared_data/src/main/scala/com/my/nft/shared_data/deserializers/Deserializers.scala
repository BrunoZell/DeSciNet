package com.descinet.shared_data.deserializers

import com.descinet.shared_data.types.Types._
import io.circe.Decoder
import io.circe.jawn.decode
import org.tessellation.currency.dataApplication.DataUpdate
import org.tessellation.currency.dataApplication.dataApplication.DataApplicationBlock
import org.tessellation.security.signature.Signed

import java.nio.charset.StandardCharsets

object Deserializers {
  private def deserialize[A: Decoder](
    bytes: Array[Byte]
  ): Either[Throwable, A] =
    decode[A](new String(bytes, StandardCharsets.UTF_8))

  def deserializeUpdate(
    bytes: Array[Byte]
  ): Either[Throwable, DeSciNetUpdate] =
    deserialize[DeSciNetUpdate](bytes)

  def deserializeState(
    bytes: Array[Byte]
  ): Either[Throwable, DeSciNetOnChainState] =
    deserialize[DeSciNetOnChainState](bytes)

  def deserializeBlock(
    bytes: Array[Byte]
  )(implicit e: Decoder[DataUpdate]): Either[Throwable, Signed[DataApplicationBlock]] =
    deserialize[Signed[DataApplicationBlock]](bytes)

  def deserializeCalculatedState(
    bytes: Array[Byte]
  ): Either[Throwable, DeSciNetCalculatedState] =
    deserialize[DeSciNetCalculatedState](bytes)
}