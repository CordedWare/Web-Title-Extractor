package web.title.extractor.fixtures

import org.http4s.{Response, Status}
import cats.effect.IO
import fs2.Stream

object TestResponces {
  def successResponse(domain: String): Response[IO] = 
    Response(
      Status.Ok,
      body = Stream.emits(s"<html><title>$domain</title></html>".getBytes)
    )

  val noTitleResponse: Response[IO] = 
    Response(
      Status.Ok,
      body = Stream.emits("<html><head></head><body></body></html>".getBytes)
    )

  def forbiddenResponse(domain: String): Response[IO] =
    Response(
      Status.Forbidden,
      body = Stream.emits(s"Domain $domain not allowed".getBytes)
    )
}
