package com.descinet.l0

import cats.data.NonEmptyList
import cats.effect.{IO, Resource}
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.option.catsSyntaxOptionId
import com.descinet.l0.custom_routes.CustomRoutes
import com.descinet.shared_data.LifecycleSharedFunctions
import com.descinet.shared_data.calculated_state.CalculatedStateService
import com.descinet.shared_data.deserializers.Deserializers
import com.descinet.shared_data.errors.Errors.valid
import com.descinet.shared_data.serializers.Serializers
import com.descinet.shared_data.types.Types._
import io.circe.{Decoder, Encoder}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.{EntityDecoder, HttpRoutes}
import org.tessellation.BuildInfo
import org.tessellation.currency.dataApplication._
import org.tessellation.currency.dataApplication.dataApplication._
import org.tessellation.currency.l0.CurrencyL0App
import org.tessellation.ext.cats.effect.ResourceIO
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.cluster.ClusterId
import org.tessellation.schema.semver.{MetagraphVersion, TessellationVersion}
import org.tessellation.security.hash.Hash
import org.tessellation.security.signature.Signed

import java.util.UUID

object Main
  extends CurrencyL0App(
    "descinet-currency-l0",
    "DeSciNet currency L0 node",
    ClusterId(UUID.fromString("517c3a05-9219-471b-a54c-21b7d72f4ae5")),
    metagraphVersion = MetagraphVersion.unsafeFrom(BuildInfo.version),
    tessellationVersion = TessellationVersion.unsafeFrom(BuildInfo.version)
  ) {
  private def makeBaseDataApplicationL0Service(
    calculatedStateService: CalculatedStateService[IO]
  ): BaseDataApplicationL0Service[IO] = BaseDataApplicationL0Service(new DataApplicationL0Service[IO, DeSciNetUpdate, DeSciNetOnChainState, DeSciNetCalculatedState] {
    override def genesis: DataState[DeSciNetOnChainState, DeSciNetCalculatedState] =
      DataState(
        DeSciNetOnChainState(
          exogenousVariables = Set.empty,
          measurements = Map.empty,
          models = Map.empty,
          targets = Map.empty,
          bounties = Map.empty,
          scores = Map.empty
        ),
        DeSciNetCalculatedState(
          exogenousVariables = Map.empty,
          measurements = Map.empty,
          models = Map.empty,
          targets = Map.empty,
          bounties = Map.empty,
          scores = Map.empty
        )
      )

    override def validateData(
      state  : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
      updates: NonEmptyList[Signed[DeSciNetUpdate]]
    )(implicit context: L0NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] =
      LifecycleSharedFunctions.validateData[IO](state, updates)

    override def validateUpdate(
      update: DeSciNetUpdate
    )(implicit context: L0NodeContext[IO]): IO[DataApplicationValidationErrorOr[Unit]] =
      valid.pure[IO]

    override def combine(
      state  : DataState[DeSciNetOnChainState, DeSciNetCalculatedState],
      updates: List[Signed[DeSciNetUpdate]]
    )(implicit context: L0NodeContext[IO]): IO[DataState[DeSciNetOnChainState, DeSciNetCalculatedState]] =
      LifecycleSharedFunctions.combine[IO](state, updates)

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
    ): IO[Array[Byte]] = IO(Serializers.serializeBlock(block)(dataEncoder.asInstanceOf[Encoder[DataUpdate]]))

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

    override def routes(implicit context: L0NodeContext[IO]): HttpRoutes[IO] =
      CustomRoutes[IO](calculatedStateService).public

    override def signedDataEntityDecoder: EntityDecoder[IO, Signed[DeSciNetUpdate]] =
      circeEntityDecoder

    override def getCalculatedState(implicit context: L0NodeContext[IO]): IO[(SnapshotOrdinal, DeSciNetCalculatedState)] =
      calculatedStateService.getCalculatedState.map(calculatedState => (calculatedState.ordinal, calculatedState.state))

    override def setCalculatedState(
      ordinal: SnapshotOrdinal,
      state  : DeSciNetCalculatedState
    )(implicit context: L0NodeContext[IO]): IO[Boolean] =
      calculatedStateService.setCalculatedState(ordinal, state)

    override def hashCalculatedState(
      state: DeSciNetCalculatedState
    )(implicit context: L0NodeContext[IO]): IO[Hash] =
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

  private def makeL0Service: IO[BaseDataApplicationL0Service[IO]] =
    CalculatedStateService.make[IO].map(makeBaseDataApplicationL0Service)

  override def dataApplication: Option[Resource[IO, BaseDataApplicationL0Service[IO]]] =
    makeL0Service.asResource.some

  // Todo: Add a custom reward function to distribute model and sample rewards in $DESCI. Examples:
  // https://github.com/Constellation-Labs/elpaca-metagraph/blob/main/modules/l0/src/main/scala/org/elpaca_metagraph/l0/Main.scala
  // https://github.com/Constellation-Labs/dor-metagraph/blob/main/metagraph/modules/l0/src/main/scala/com/my/dor_metagraph/l0/Main.scala
  // override def rewards(implicit sp: SecurityProvider[IO]): Option[Rewards[IO, CurrencySnapshotStateProof, CurrencyIncrementalSnapshot, CurrencySnapshotEvent]] = {
  //   val dailyBountyRewards = new DailyBountyRewards[IO]
  //   val analyticsBountyRewards = new AnalyticsBountyRewards[IO]
  //   val validatorNodes = new ValidatorNodesAPI[IO]

  //   DorRewards.make[IO](
  //     dailyBountyRewards,
  //     analyticsBountyRewards,
  //     validatorNodes
  //   ).some
  // }
}
