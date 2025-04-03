package web.title.extractor.http.routes

import cats.effect._
import cats.Parallel
import org.http4s._
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import web.title.extractor.core.ExtractorService

/**
  * Главный API. Объединяет все роуты
  */
class HttpApi[F[_]: Async: Parallel: Logger] private (
    extractor: ExtractorService[F]
) {
  private val titleRoutes = TitleRoutes[F](extractor).routes

  val endpoints = Router(
    "/api" -> titleRoutes
  )

}

object HttpApi {
  def apply[F[_]: Async: Parallel: Logger](
      extractor: ExtractorService[F]
  ): HttpApi[F] = new HttpApi[F](extractor)
}
