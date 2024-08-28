package com.descinet.shared_data.daemons.fetcher

import cats.effect.{Async, Resource}
import cats.syntax.all._
import fs2.io.net.Network
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.tessellation.node.shared.resources.MkHttpClient
import org.typelevel.ci.CIString
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import com.descinet.shared_data.types.Types._

object ExogenousVariableFetcher {
  def make[F[_]: Async](applicationConfig: ApplicationConfig, exogenousVariableId: ExogenousVariableId): ExogenousVariableFetcher[F] =
    new ExogenousVariableFetcher[F](applicationConfig, exogenousVariableId)
}

class ExogenousVariableFetcher[F[_]: Async](
  applicationConfig: ApplicationConfig,
  exogenousVariableId: ExogenousVariableId
) {
  private val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass(ExogenousVariableFetcher.getClass)

  case class FetchResponse(ordinal: Long, value: Double)

  def fetchValue(url: String): F[FetchResponse] = {
    val clientResource: Resource[F, Client[F]] = MkHttpClient.forAsync[F].newEmber(applicationConfig.http4s.client)

    clientResource.use { client =>
      val request = Request[F](
        method = Method.GET,
        uri = Uri.unsafeFromString(url)
      )

      client.expect[FetchResponse](request)(jsonOf[F, FetchResponse])
    }
  }

  def fetchExogenousVariableValue(variable: ExogenousVariable): F[Option[FetchResponse]] = {
    val urls = variable.l0NodeUrls.map(url => s"$url/${variable.dataApplicationUrlPath}")

    urls.foldLeft(Option.empty[FetchResponse].pure[F]) { (acc, url) =>
      acc.flatMap {
        case Some(value) => value.some.pure[F]
        case None =>
          fetchValue(url).attempt.flatMap {
            case Right(value) => value.some.pure[F]
            case Left(_) => none[FetchResponse].pure[F]
          }
      }
    }
  }

  def fetchAndBuildUpdates(exogenousVariableId: ExogenousVariableId): F[List[DeSciNetUpdate]] = {
    val exogenousVariable = applicationConfig.exogenousVariables.find(v => ExogenousVariableId(Hash(v)) == exogenousVariableId)

    exogenousVariable match {
      case Some(variable) =>
        fetchExogenousVariableValue(variable).map {
          case Some(FetchResponse(ordinal, value)) => List(NewMeasurement(exogenousVariableId, value))
          case None => List.empty[DeSciNetUpdate]
        }
      case None => List.empty[DeSciNetUpdate].pure[F]
    }
  }
}