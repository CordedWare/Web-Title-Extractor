package web.title.extractor.modules

import cats.effect.Async
import org.http4s.client.Client
import web.title.extractor.config.HttpExtractorConfig
import web.title.extractor.core.LoggingService
import web.title.extractor.modules.RedisCache
import web.title.extractor.http.responses.ExtractorErrorHandler

final case class AppDependencies[F[_]: Async](
    client: Client[F],
    config: HttpExtractorConfig,
    logging: LoggingService[F],
    redisCache: RedisCache[F]
) {
  val errorHandler: ExtractorErrorHandler[F] = new ExtractorErrorHandler[F](logging)
}
