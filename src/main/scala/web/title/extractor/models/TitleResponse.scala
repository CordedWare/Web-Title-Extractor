package web.title.extractor.models

import io.circe._
import io.circe.generic.semiauto._

case class TitleResponse(url: String, title: Option[String])

object TitleResponse {
  implicit val encoder: Encoder[TitleResponse] = deriveEncoder[TitleResponse]
  implicit val decoder: Decoder[TitleResponse] = deriveDecoder[TitleResponse]
}
