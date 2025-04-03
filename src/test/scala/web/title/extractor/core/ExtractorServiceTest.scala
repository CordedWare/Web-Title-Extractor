package web.title.extractor.core

import cats.effect.{IO, Resource}
import cats.effect.unsafe.implicits.global
import cats.implicits._
import org.http4s.client.Client
import org.http4s.{Response, Status, Uri}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger
import web.title.extractor.fixtures.TestData
import web.title.extractor.fixtures.TestResponces
import web.title.extractor.models.{TitleResponse, TitleRequest}
import web.title.extractor.modules.{AppDependencies, RedisCache}

import scala.concurrent.duration._

class ExtractorServiceTest extends AnyWordSpec with Matchers {
  import TestData._
  import TestResponces._

  implicit val logger: Logger[IO] = NoOpLogger[IO]

  private def createTestDeps(client: Client[IO], cache: RedisCache[IO]) = AppDependencies[IO](
    client = client,
    config = testConfig,
    logging = LoggingService(logger),
    redisCache = cache
  )

  private def createMockCache(getResult: Option[String] = None) =
    new RedisCache[IO](null, 1.minute) {
      override def get(key: String): IO[Option[String]]      = IO.pure(getResult)
      override def set(key: String, value: String): IO[Unit] = IO.unit
      override def cached(key: String)(fetch: => IO[String]): IO[String] =
        getResult.map(IO.pure).getOrElse(fetch.flatTap(v => set(key, v)))
    }

  private def createMockClient() = Client[IO] { req =>
    val domain = req.uri.host.map(_.value).getOrElse("unknown")

    domain match {
      case "error.com" =>
        Resource.eval(IO.raiseError(new Exception(s"Simulated error for $domain")))
      case "notitle.com"                              => Resource.eval(IO.pure(noTitleResponse))
      case d if testConfig.allowedDomains.contains(d) => Resource.eval(IO.pure(successResponse(d)))
      case _ => Resource.eval(IO.pure(forbiddenResponse(domain)))
    }
  }

  "ExtractorService" should {
    "корректно извлекать заголовки для нескольких URL" in {
      val mockClient = createMockClient()
      val mockCache  = createMockCache()
      val processor  = new UrlProcessor[IO](createTestDeps(mockClient, mockCache))
      val service    = new ExtractorService[IO](createTestDeps(mockClient, mockCache), processor)

      val request = TitleRequest(successUrls :+ forbiddenUrl)
      val result  = service.extractTitles(request).unsafeRunSync()

      result should have size 3
      result should contain(TitleResponse(exampleUrl, Some("example.com")))
      result should contain(TitleResponse(allowedUrl, Some("allowed.org")))
      result.find(_.url == forbiddenUrl).get.title.get should include(
        "Domain forbidden.com not allowed"
      )
    }

    "обрабатывать пустой список URL" in {
      val mockClient = createMockClient()
      val mockCache  = createMockCache()
      val processor  = new UrlProcessor[IO](createTestDeps(mockClient, mockCache))
      val service    = new ExtractorService[IO](createTestDeps(mockClient, mockCache), processor)

      val request = TitleRequest(Nil)
      val result  = service.extractTitles(request).unsafeRunSync()

      result shouldBe empty
    }

    "возвращать ошибку при превышении лимита URL" in {
      val smallConfig = testConfig.copy(maxUrls = 2)
      val mockClient  = createMockClient()
      val mockCache   = createMockCache()
      val deps        = createTestDeps(mockClient, mockCache).copy(config = smallConfig)
      val processor   = new UrlProcessor[IO](deps)
      val service     = new ExtractorService[IO](deps, processor)

      val request = TitleRequest(urlsExceedingLimit)
      val result  = service.extractTitles(request).attempt.unsafeRunSync()

      result.isLeft shouldBe true
      result.left.get.getMessage should include("Max URLs (2) exceeded")
    }

    "использовать кэш при повторных запросах" in {
      val mockClient = createMockClient()
      val mockCache  = createMockCache()
      val processor  = new UrlProcessor[IO](createTestDeps(mockClient, mockCache))
      val service    = new ExtractorService[IO](createTestDeps(mockClient, mockCache), processor)

      val firstResult = service.extractTitles(TitleRequest(List(exampleUrl))).unsafeRunSync()
      firstResult shouldBe List(TitleResponse(exampleUrl, Some("example.com")))

      val secondResult = service.extractTitles(TitleRequest(List(exampleUrl))).unsafeRunSync()
      secondResult shouldBe List(TitleResponse(exampleUrl, Some("example.com")))
    }

    "обрабатывать URL с ошибками" in {
      val mockClient = createMockClient()
      val mockCache  = createMockCache()
      val processor  = new UrlProcessor[IO](createTestDeps(mockClient, mockCache))
      val service    = new ExtractorService[IO](createTestDeps(mockClient, mockCache), processor)

      val request = TitleRequest(List(errorUrl))
      val result  = service.extractTitles(request).unsafeRunSync()

      result should have size 1
      result.head.title shouldBe Some("Processing error: Simulated error for error.com")
    }

    "корректно обрабатывать невалидные URL" in {
      val mockClient = createMockClient()
      val mockCache  = createMockCache()
      val processor  = new UrlProcessor[IO](createTestDeps(mockClient, mockCache))
      val service    = new ExtractorService[IO](createTestDeps(mockClient, mockCache), processor)

      val request = TitleRequest(List(invalidUrl))
      val result  = service.extractTitles(request).unsafeRunSync()

      result.head.title shouldBe Some("Processing error: Invalid URL: htp://invalid url")
    }

    "возвращать None для страниц без title" in {
      val mockClient = createMockClient()
      val mockCache  = createMockCache()
      val processor  = new UrlProcessor[IO](createTestDeps(mockClient, mockCache))
      val service    = new ExtractorService[IO](createTestDeps(mockClient, mockCache), processor)

      val request = TitleRequest(List(noTitleUrl))
      val result  = service.extractTitles(request).unsafeRunSync()

      result.head.title shouldBe None
    }
  }
}
