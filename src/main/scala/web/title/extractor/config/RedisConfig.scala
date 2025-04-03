package web.title.extractor.config

import scala.concurrent.duration.FiniteDuration
import pureconfig.ConfigReader
import pureconfig.generic.derivation.default._

case class RedisConfig(
    uri: String,
    cacheTtl: FiniteDuration
) derives ConfigReader
