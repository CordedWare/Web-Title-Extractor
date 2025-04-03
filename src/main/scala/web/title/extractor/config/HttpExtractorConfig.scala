package web.title.extractor.config

import scala.concurrent.duration.FiniteDuration
import pureconfig.*
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.derivation.default.*
import cats.effect.Async
import cats.syntax.all._
import scala.reflect.ClassTag

case class HttpExtractorConfig(
    maxUrls: Int,
    requestTimeout: FiniteDuration,
    idleTimeout: FiniteDuration,
    maxPageSize: String,
    redirect: Int,
    allowedDomains: List[String]
) derives ConfigReader
