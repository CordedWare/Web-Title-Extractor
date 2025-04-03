package web.title.extractor.http.responses

import cats.effect._
import cats.syntax.all._
import org.typelevel.log4cats.Logger
import org.http4s.Uri
import web.title.extractor.config.HttpExtractorConfig
import web.title.extractor.models.TitleResponse
import java.util.concurrent.TimeoutException
import web.title.extractor.core.LoggingService

/**
  * Обработчик ошибок для сервиса извлечения заголовков.
  */
class ExtractorErrorHandler[F[_]](logging: LoggingService[F])(implicit async: Async[F]) {

  /**
    * Обрабатывает ошибку валидации URL.
    */
  def handleUrlValidationError(url: String, error: Throwable): F[Uri] =
    logging.warn(s"Invalid URL: $url - ${error.getMessage}") >>
      async.raiseError(new IllegalArgumentException(s"Invalid URL: $url"))

  /**
    * Обрабатывает ошибки загрузки страницы:(рантайм и остальные)
    */
  def handleFetchError(uri: Uri, error: Throwable): F[String] = error match {
    case _: TimeoutException =>
      logging.warn(s"Timeout while fetching $uri") *>
        s"Timeout while fetching URL".pure[F]

    case e =>
      logging.error(e)(s"Failed to fetch content from $uri") *>
        s"Request failed: ${e.getMessage}".pure[F]
  }

  /**
    * Обрабатывает ошибки парсинга заголовка.
    */
  def handleProcessingError(url: String, error: Throwable): F[String] =
    logging.error(error)(s"Error processing URL $url") >>
      s"Processing error: ${error.getMessage}".pure[F]

  /**
    * Обрабатывает превышение лимита URL.
    */
  def handleMaxUrlsExceeded(received: Int, max: Int): F[List[TitleResponse]] =
    logging.error(s"Max URLs ($max) exceeded. Received: $received") >>
      async.raiseError(new IllegalArgumentException(s"Max URLs ($max) exceeded"))
}