package web.title.extractor

import pureconfig.ConfigSource
import cats.effect.kernel.Async
import pureconfig.ConfigReader
import cats.implicits._
import pureconfig.error.ConfigReaderException
import scala.reflect.ClassTag
import pureconfig.generic.derivation.default._

import cats.effect.IO

package object config {
  implicit class ConfigSourceOps(source: ConfigSource) {
    def loadF[F[_]: Async, A](implicit
        reader: ConfigReader[A],
        tag: ClassTag[A]
    ): F[A] = source.load[A].leftMap(ConfigReaderException(_)).liftTo[F]
  }
}
