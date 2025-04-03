package web.title.extractor.modules

import cats.effect._
import cats.syntax.all._
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.Stdout._
import dev.profunktor.redis4cats.RedisCommands
import scala.concurrent.duration.FiniteDuration
import web.title.extractor.config.RedisConfig

/**
  * @param redis клиент Redis (кэш)
  * @param ttl   время жизни данных в кэше
  */
class RedisCache[F[_]: Async](
    redis: RedisCommands[F, String, String],
    ttl: FiniteDuration
) {
  /**
    * Получает значение по ключу
    * @param  key Ключ для поиска
    * @return Возвращает F[Option[String]] 
    *         - Some если ключ существует и значение найдено
    *         - None если ключ не существует или истек TTL
    */
  def get(key: String): F[Option[String]] = redis.get(key)

  /**
    * Устанавливает TTL (Срок годности ключа кэша)
    * @param key   Ключ для сохранения
    * @param value Значение для сохранения
    * @return      возвращает F[Unit] при успехе сохраненич или ошибку
    */
  def set(key: String, value: String): F[Unit] = redis.setEx(key, value, ttl).void

    /**
    * Проверяет кэш на наличие. 
    * Если же его нет, то передат через функцию @param fetch и сохраняет в кэш
    * @param key   Ключ для поиска или сохранения
    * @param fetch Функция для вычисления значения (если значение отсутствует в кэше)
    * @return Значение в эффекте F
    *         - если значение есть в кэше, то возвращает его
    *         - если значения нет - вычисляет и сохраняет сновым TTL
    *         - или вернет ошибку
    */
  def cached(key: String)(fetch: => F[String]): F[String] = get(key).flatMap {
    case Some(cachedValue) => cachedValue.pure[F]
    case None              => fetch.flatTap(value => set(key, value))
  }

}

object RedisCache {
  def apply[F[_]: Async](config: RedisConfig): Resource[F, RedisCache[F]] =
    Redis[F].utf8(config.uri).map { redis =>
      new RedisCache[F](redis, config.cacheTtl)
    }
}
