package web.title.extractor.core

import cats.effect._
import cats.syntax.all._
import org.jsoup.Jsoup
import org.http4s.{Uri, Status}
import web.title.extractor.models.TitleResponse
import web.title.extractor.modules.AppDependencies
import java.util.concurrent.TimeoutException

/**
  * Обработчик URL для извлечения заголовков
  */
// TODO возможно перегружен и потребуется рефакторинг
// TODO расширение функционала поиска нужных данных 
class UrlProcessor[F[_]: Async](depends: AppDependencies[F]) {
  private val logger       = depends.logging
  private val client       = depends.client
  private val config       = depends.config
  private val errorHandler = depends.errorHandler

    /**
    * Парсит заголовок из HTML
    * @param html HTML контент
    * @return извлеченный заголовок в F[Option[String]]
    */
  private def parseTitle(html: String): F[Option[String]] = 
    val title = Option(Jsoup.parse(html).title()).filter(_.nonEmpty)
    logger.trace(s"Parsed title: $title").as(title)
  

/**
  * Преобразует URL в Uri + логирует ошибки валидации.
  */
  private def validateUrl(url: String): F[Uri] = 
    logger.debug(s"Validating URL: $url") *>
      Uri
        .fromString(url)
        .fold(
          e   => errorHandler.handleUrlValidationError(url, e),
          uri => logger.debug(s"URL validated successfully: $uri") *> uri.pure[F]
        )
  
/**
  * Проверяет разрешен ли домен URL согласно нашего списка из конфига
  * Сравнивает домен с allowedDomains.
  */
  private def isDomainAllowed(uri: Uri): F[Boolean] = 
    val host    = uri.host.map(_.value).getOrElse("")
    val allowed = config.allowedDomains.exists(allowed => host.endsWith(allowed))
    logger.debug(s"Checking if domain is allowed: $host") *>
      allowed.pure[F]
  
/**
  * Загружает содержимое страницы по HTTP:
  * 1. Проверка разрешен ли домен. Если да, то выполняет GET-запрос с обработкой ответов
  * 2. логирование таймаутов и ошибок
  */
  private def fetchBody(uri: Uri): F[String] =
    for {
      allowed <- isDomainAllowed(uri)
      result  <-
        if (allowed) {
          logger.info(s"Fetching content from allowed domain: ${uri.host.getOrElse("unknown")}") *>
            client
              .get(uri) { response =>
                response.status match {

                  case Status.Ok =>
                    response.bodyText.compile.string

                  case status if status.code >= 300 && status.code < 400 =>
                    logger.warn(s"Unexpected redirect status: $status") *>
                      s"Unexpected redirect: $status".pure[F]

                  case _ =>
                    val msg = s"HTTP Error: ${response.status}"
                    logger.warn(s"Failed to fetch content from $uri: $msg") *>
                      msg.pure[F]
                }
              }
              .handleErrorWith {
                case _: TimeoutException =>

                  logger.error(s"Request timeout for URI: $uri") *>
                    s"Request timeout for URI: $uri".pure[F]

                case e =>
                  logger.error(e)(s"Request failed for URI: $uri") *>
                    s"Request failed: ${e.getMessage}".pure[F]
              }
        } else {
          val msg = s"Domain not allowed: ${uri.host.getOrElse("unknown")}"
          logger.warn(msg) *> msg.pure[F]
        }
    } yield result

  /**
  * Основной метод обработки URL:
  * 1. Валидация
  * 2. Получение контента
  * 3. Парсинг заголовка
  * имеется обработка ошибок на каждом этапе
  * @return TitleResponse ответ с урлом и заголовком
  */
  def processUrl(url: String): F[TitleResponse] =
    (for {
      _     <- logger.info(s"Processing URL: $url")
      uri   <- validateUrl(url)
      body  <- fetchBody(uri)
      title <- parseTitle(body)
      _     <- logger.info(s"Successfully processed URL: $url")
    } yield TitleResponse(url, title))
      .handleErrorWith(e =>
        logger.error(e)(s"Error processing URL: $url") *>
          errorHandler
            .handleProcessingError(url, e)
            .map(errorMsg => TitleResponse(url, Some(errorMsg)))
      )
}

object UrlProcessor {
  def apply[F[_]: Async](depends: AppDependencies[F]): UrlProcessor[F] =
    new UrlProcessor[F](depends)
}
