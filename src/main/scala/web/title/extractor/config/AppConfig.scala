package web.title.extractor.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*
import pureconfig.ConfigSource
import cats.effect.kernel.Async
import cats.syntax.all._

final case class AppConfig(
    emberConfig: EmberConfig,
    httpConfig: HttpExtractorConfig,
    redisConfig: RedisConfig
) derives ConfigReader
