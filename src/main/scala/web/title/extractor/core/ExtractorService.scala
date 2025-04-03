package web.title.extractor.core

import cats.effect._
import cats.Parallel
import cats.syntax.all._
import web.title.extractor.models.{TitleRequest, TitleResponse}
import web.title.extractor.modules.AppDependencies

/**
  * Сервис извлечения заголовков
  */
class ExtractorService[F[_]: Async](
    depends: AppDependencies[F],
    urlProcessor: UrlProcessor[F]
) {
  private val logger       = depends.logging
  private val config       = depends.config
  private val errorHandler = depends.errorHandler
  private val cache        = depends.redisCache

 /**
  * Извлекает заголовки страниц для списка URL с кэшированием результатов.
  * 
  * @param request Список URL как источник поиска по ним
  * @return Список ответов:
  *  [
  *    {
  *      "url": "https://example.com",
  *      "title": "Example Domain"
  *    },
  *    {
  *      "url": "https://scala-lang.org",
  *      "title": "The Scala Programming Language"
  *    }
  *  ]
  * @throws IllegalArgumentException при превышении maxUrls
  */
  def extractTitles(request: TitleRequest)(implicit P: Parallel[F]): F[List[TitleResponse]] = {

    request.urls.size match {

      case count if count > depends.config.maxUrls =>
          errorHandler.handleMaxUrlsExceeded(count, config.maxUrls)

      case count =>
        logger.info(s"Processing $count URLs") *>
          request.urls.parTraverse { url =>
            cache
              .cached(url) {
                urlProcessor.processUrl(url).map(_.title.getOrElse("No title"))
              }
              .map { title =>
                TitleResponse(url, Some(title))
              }
          }
    }
  }

}

object ExtractorService {
  def apply[F[_]: Async](
      depends: AppDependencies[F],
      urlProcessor: UrlProcessor[F]
  ): ExtractorService[F] = {
    new ExtractorService[F](depends, urlProcessor)
  }
}
