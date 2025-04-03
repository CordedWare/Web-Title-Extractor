package web.title.extractor.core

import cats.effect.Sync
import org.typelevel.log4cats.Logger

final class LoggingService[F[_]] private (logger: Logger[F]) {
  def info(msg: String): F[Unit]                = logger.info(msg)
  def debug(msg: String): F[Unit]               = logger.debug(msg)
  def warn(msg: String): F[Unit]                = logger.warn(msg)
  def error(msg: String): F[Unit]               = logger.error(msg)
  def error(e: Throwable)(msg: String): F[Unit] = logger.error(e)(msg)
  def trace(msg: String): F[Unit]               = logger.trace(msg)
}

object LoggingService {
  def apply[F[_]](logger: Logger[F]): LoggingService[F] = new LoggingService(logger)
}
