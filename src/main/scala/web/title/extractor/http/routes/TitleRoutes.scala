package web.title.extractor.http.routes

import cats.effect._
import cats.syntax.all._
import cats.Parallel
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityCodec._
import web.title.extractor.models.{TitleRequest, TitleResponse}
import web.title.extractor.core.ExtractorService
import org.http4s.client.Client
import web.title.extractor.config.HttpExtractorConfig
import org.http4s.server.Router

/**
  * Роут для извлечения заголовков (для поискового сервиса)
  */
class TitleRoutes[F[_]: Async: Parallel](
    extractorService: ExtractorService[F]
) extends Http4sDsl[F] {

  val fetchTitles: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "titles" =>
      for {
        urls     <- req.as[TitleRequest]
        titles   <- extractorService.extractTitles(urls)
        response <- Ok(titles)
      } yield response
  }

  val routes = Router(
    "/extractor" -> fetchTitles
  )

}

object TitleRoutes {
  def apply[F[_]: Async: Parallel](extractorService: ExtractorService[F]): TitleRoutes[F] =
    new TitleRoutes(extractorService)
}
