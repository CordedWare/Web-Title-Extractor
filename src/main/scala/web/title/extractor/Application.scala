package web.title.extractor

import cats.effect._
import com.comcast.ip4s._
import org.http4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import web.title.extractor.config.AppConfig
import web.title.extractor.core.{ExtractorService, LoggingService, UrlProcessor}
import web.title.extractor.http.routes.HttpApi
import web.title.extractor.config.syntax.loadF
import org.http4s.client.middleware.FollowRedirect
import web.title.extractor.modules.AppDependencies
import web.title.extractor.config.RedisConfig
import web.title.extractor.modules.RedisCache

object Application extends IOApp {

  private given rootLogger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private def initApp(config: AppConfig): Resource[IO, Server] = {
    for {
      // TODO добавление функционала при добавлении новых требований:
      // TODO 1.Интеграция с базой данных, 
      // TODO 2.Аутентификация и авторизация, 
      // TODO 3.Подтверждение email при регистрации, 
      // TODO 4.Кэширование
      client     <- EmberClientBuilder.default[IO]
        .withTimeout(config.httpConfig.requestTimeout)
        .withIdleConnectionTime(config.httpConfig.idleTimeout)
        .build
        .map(FollowRedirect(config.httpConfig.redirect, _ => true))
      redisCache <- RedisCache[IO](config.redisConfig)
      depends    =  AppDependencies[IO](client, config.httpConfig, LoggingService(rootLogger), redisCache)
      processor  =  new UrlProcessor[IO](depends)
      service    =  new ExtractorService[IO](depends, processor)
      httpApi    =  HttpApi[IO](service).endpoints.orNotFound
      server     <- EmberServerBuilder.default[IO]
        .withHost(config.emberConfig.host)
        .withPort(config.emberConfig.port)
        .withHttpApp(httpApi)
        .build
    } yield server
  }

  override def run(args: List[String]): IO[ExitCode] = {

    given Logger[IO] = rootLogger

    for {
      config <- ConfigSource.default.loadF[IO, AppConfig]
      _      <- rootLogger.info(s"Loaded config: $config")
      _      <- initApp(config).use { server =>
        rootLogger.info(s"Server started at ${server.address}") *>
          IO.never
      }
    } yield ExitCode.Success
  }

}
// запуск контейнера с бэкендом (не забудьте провериь порты)
// docker-compose up -d --build
