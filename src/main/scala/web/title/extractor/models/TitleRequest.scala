package web.title.extractor.models

import io.circe._
import io.circe.generic.semiauto._

case class TitleRequest(urls: List[String])

object TitleRequest {
  implicit val encoder: Encoder[TitleRequest] = deriveEncoder[TitleRequest]
  implicit val decoder: Decoder[TitleRequest] = deriveDecoder[TitleRequest]
}