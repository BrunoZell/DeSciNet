package com.descinet.shared_data.daemons

import cats.effect.Async
import cats.effect.std.Supervisor
import cats.syntax.all._
import com.comcast.ip4s.{Host, Port}
import fs2.io.net.Network
import com.descinet.shared_data.app.ApplicationConfig
import com.descinet.shared_data.calculated_state.CalculatedStateService
import com.descinet.shared_data.daemons.fetcher._
import org.http4s.client.Client
import org.tessellation.json.JsonSerializer
import org.tessellation.node.shared.domain.Daemon
import org.tessellation.node.shared.resources.MkHttpClient
import org.tessellation.schema.peer.P2PContext
import org.tessellation.security.SecurityProvider
import org.tessellation.security.key.ops.PublicKeyOps
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.security.KeyPair
import scala.concurrent.duration.FiniteDuration

trait DaemonApis[F[_]] {
  def spawnL1Daemons: F[Unit]

  def spawnL0Daemons(calculatedStateService: CalculatedStateService[F]): F[Unit]
}

object DaemonApis {
  def make[F[_] : Async : SecurityProvider : Supervisor : Network : JsonSerializer](
    config : ApplicationConfig,
    keypair: KeyPair
  ): DaemonApis[F] = new DaemonApis[F] {
    private val logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLoggerFromClass(DaemonApis.getClass)

    private def withHttpClient[A](useClient: Client[F] => F[A]): F[A] = {
      val httpClient = MkHttpClient.forAsync[F].newEmber(config.http4s.client)
      httpClient.use(useClient)
    }

    private def createP2PContext(keypair: KeyPair): P2PContext =
      P2PContext(Host.fromString(config.dataApi.ip).get, Port.fromInt(config.dataApi.port).get, keypair.getPublic.toId.toPeerId)

    override def spawnL1Daemons: F[Unit] =
      withHttpClient { client =>
        val publisher = Publisher.make[F](client, createP2PContext(keypair))
        val signer = Signer.make[F](keypair, publisher)

        logger.info("Spawning L1 daemons") >>
          spawnExolixDaemon(config, signer) >>
          spawnSimplexDaemon(config, signer) >>
          spawnIntegrationnetNodesOperatorsDaemon(config, signer)
      }

    override def spawnL0Daemons(calculatedStateService: CalculatedStateService[F]): F[Unit] =
      withHttpClient { client =>
        val publisher = Publisher.make[F](client, createP2PContext(keypair))
        val signer = Signer.make[F](keypair, publisher)

        logger.info("Spawning L0 daemons") >>
          spawnExogenousVariableDaemons(config, signer, calculatedStateService)
      }

    private def spawn(
      processor: Processor[F],
      idleTime : FiniteDuration
    ): Daemon[F] =
      Daemon.periodic[F](
        processor.execute,
        idleTime
      )

    private def spawnExogenousVariableDaemons(
      config: ApplicationConfig,
      signer: Signer[F],
      calculatedStateService: CalculatedStateService[F]
    ): F[Unit] = {
      val fetchers = config.exogenousVariables.map { variable =>
        val exogenousVariableId = ExogenousVariableId(Hash(variable))
        val fetcher = ExogenousVariableFetcher.make[F](config, exogenousVariableId)
        val processor = ExogenousVariableProcessor.make[F](fetcher, signer)
        spawn(processor, config.exogenousVariableDaemonIdleTime).start.void
      }

      fetchers.sequence_
    }
  }
}
