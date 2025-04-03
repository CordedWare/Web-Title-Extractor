package web.title.extractor.fixtures

import web.title.extractor.config.HttpExtractorConfig
import scala.concurrent.duration._

object TestData {
  val testConfig = HttpExtractorConfig(
    maxUrls        = 10,
    requestTimeout = 5.seconds,
    idleTimeout    = 5.seconds,
    maxPageSize     = "2MB",
    redirect       = 5,
    allowedDomains = List("example.com", "allowed.org")
  )

  val exampleUrl   = "https://example.com"
  val allowedUrl   = "https://allowed.org"
  val errorUrl     = "https://error.com"
  val noTitleUrl   = "https://notitle.com"
  val invalidUrl   = "htp://invalid url"
  val forbiddenUrl = "https://forbidden.com"

  val successUrls        = List(exampleUrl, allowedUrl)
  val urlsExceedingLimit = List("url1", "url2", "url3")
}
