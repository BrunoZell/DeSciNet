package com.descinet.data_l1

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.option.catsSyntaxOptionId
import com.descinet.shared_data.LifecycleSharedFunctions
import com.descinet.shared_data.calculated_state.CalculatedStateService
import com.descinet.shared_data.deserializers.Deserializers
import com.descinet.shared_data.errors.Errors.valid
import com.descinet.shared_data.serializers.Serializers
import com.descinet.shared_data.types.Types._
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication._
import org.tessellation.currency.dataApplication.dataApplication._
import org.tessellation.currency.l1.CurrencyL1App
import org.tessellation.ext.cats.effect.ResourceIO
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.{MetagraphVersion, TessellationVersion}
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.Signed

import java.util.UUID

object Main
  extends CurrencyL1App(
    "descinet-currency-data_l1",
    "DeSciNet currency data L1 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
    tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
  ) {
  private def makeBaseDataApplicationL1Service(
    calculatedStateService: CalculatedStateService[IO]
  ): BaseDataApplicationL1Service[IO] = BaseDataApplicationL1Service(new DataApplicationL1Service[IO, DeSciNetUpdate, DeSciNetOnChainState, DeSciNetCalculatedState] {
    override def validateData(
      state  : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
      updates: NonEmptyList[Signed[DeSciNetUpdate]]
    )(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] =
      valid.pure[IO]

    override def validateUpdate(
      update: DeSciNetUpdate
    )(implicit context: L1NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] =
      LifecycleSharedFunctions.validateUpdate[IO](update)

    override def combine(
      state  : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
      updates: List[Signed[DeSciNetUpdate]]
    )(implicit context: L1NodeContext[IO]): IO[DataState[DeSciNetOnChainState, DeSciNetCalculatedState]] =
      state.pure[IO]

    override def serializeState(
      state: DeSciNetOnChainState
    ): IO[Array[Byte]] =
      IO(Serializers.serializeState(state))

    override def serializeUpdate(
      update: DeSciNetUpdate
    ): IO[Array[Byte]] =
      IO(Serializers.serializeUpdate(update))

    override def serializeBlock(
      block: Signed[DataApplicationBlock]
    ): IO[Array[Byte]] =
      IO(Serializers.serializeBlock(block)(dataEncoder.asInstanceOf[Encoder[DataUpdate]]))

    override def deserializeState(
      bytes: Array[Byte]
    ): IO[Either[Throwable, DeSciNetOnChainState]] =
      IO(Deserializers.deserializeState(bytes))

    override def deserializeUpdate(
      bytes: Array[Byte]
    ): IO[Either[Throwable, DeSciNetUpdate]] =
      IO(Deserializers.deserializeUpdate(bytes))

    override def deserializeBlock(
      bytes: Array[Byte]
    ): IO[Either[Throwable, Signed[DataApplicationBlock]]] =
      IO(Deserializers.deserializeBlock(bytes)(dataDecoder.asInstanceOf[Decoder[DataUpdate]]))

    override def dataEncoder: Encoder[DeSciNetUpdate] =
      implicitly[Encoder[DeSciNetUpdate]]

    override def dataDecoder: Decoder[DeSciNetUpdate] =
      implicitly[Decoder[DeSciNetUpdate]]

    override def calculatedStateEncoder: Encoder[DeSciNetCalculatedState] =
      implicitly[Encoder[DeSciNetCalculatedState]]

    override def calculatedStateDecoder: Decoder[DeSciNetCalculatedState] =
      implicitly[Decoder[DeSciNetCalculatedState]]

    override def routes(implicit context: L1NodeContext[IO]): HttpRoutes[IO] =
      HttpRoutes.empty

    override def signedDataEntityDecoder: EntityDecoder[IO, Signed[DeSciNetUpdate]] =
      circeEntityDecoder

    override def getCalculatedState(implicit context: L1NodeContext[IO]): IO[(SnapshotOrdinal, DeSciNetCalculatedState)] =
      calculatedStateService.getCalculatedState.map(calculatedState => (calculatedState.ordinal, calculatedState.state))

    override def setCalculatedState(
      ordinal: SnapshotOrdinal,
      state  : DeSciNetCalculatedState
    )(implicit context: L1NodeContext[IO]): IO[Boolean] =
      calculatedStateService.setCalculatedState(ordinal, state)

    override def hashCalculatedState(
      state: DeSciNetCalculatedState
    )(implicit context: L1NodeContext[IO]): IO[Hash] =
      calculatedStateService.hashCalculatedState(state)

    override def serializeCalculatedState(
      state: DeSciNetCalculatedState
    ): IO[Array[Byte]] =
      IO(Serializers.serializeCalculatedState(state))

    override def deserializeCalculatedState(
      bytes: Array[Byte]
    ): IO[Either[Throwable, DeSciNetCalculatedState]] =
      IO(Deserializers.deserializeCalculatedState(bytes))
  })

  private def makeL1Service: IO[BaseDataApplicationL1Service[IO]] =
    CalculatedStateService.make[IO].map(makeBaseDataApplicationL1Service)

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL1Service[IO]]] =
    makeL1Service.asResource.some
}
