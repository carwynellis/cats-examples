package uk.carwynellis.cats.datatypes


/**
  * Validated provides a means to accumulate validation errors so all errors
  * for a given set of inputs can be reported in a single result.
  *
  * See http://typelevel.org/cats/datatypes/validated.html
  */
object ValidatedExample extends App {

  // Imagine you are filling out a web form to signup for an account. You input
  // your username and password and submit. Response comes back saying your
  // username can’t have dashes in it, so you make some changes and resubmit.
  // Can’t have special characters either. Change, resubmit. Passwords need to
  // have at least one capital letter. Change, resubmit. Password needs to have
  // at least one number.

  // Or perhaps you’re reading from a configuration file. One could imagine the
  // configuration library you’re using returns a scala.util.Try, or maybe a
  // scala.util.Either. Your parsing may look something like:

  {
    import scala.util.Try

    val fakeConfig = Map(
      "endpoint" -> "www.example.com",
      "port"     -> "this is not an int"
    )

    def config[T](s: String): Try[T] = Try {
      fakeConfig(s).asInstanceOf[T]
    }

    case class ConnectionParams(endpoint: String, port: Int)

    val invalidKey = for {
      url  <- config[String]("url")
      port <- config[Int]("port")
    } yield ConnectionParams(url, port)

    assert(invalidKey.isFailure)

    val invalidPort = for {
      url  <- config[String]("endpoint")
      port <- config[Int]("port")
    } yield ConnectionParams(url, port)

    assert(invalidPort.isFailure)
  }

  // You run your program and it says key “url” not found, turns out the key was
  // “endpoint”. So you change your code and re-run. Now it says the “port” key
  // was not a well-formed integer.

  // It would be nice to have all of these errors be reported simultaneously.
  // That the username can’t have dashes can be validated separately from it not
  // having special characters, as well as from the password needing to have
  // certain requirements. A misspelled (or missing) field in a config can be
  // validated separately from another field not being well-formed.

  // Enter Validated.

  // Parallel validation

  // Our goal is to report any and all errors across independent bits of data.
  // For instance, when we ask for several pieces of configuration, each
  // configuration field can be validated separately from one another. How then
  // do we enforce that the data we are working with is independent? We ask for
  // both of them up front.

  // As our running example, we will look at config parsing. Our config will be
  // represented by a Map[String, String]. Parsing will be handled by a Read
  // type class - we provide instances just for String and Int for brevity.

  trait Read[A] {
    def read(s: String): Option[A]
  }

  object Read {
    def apply[A](implicit A: Read[A]): Read[A] = A

    implicit val stringRead: Read[String] =
      new Read[String] { def read(s: String): Option[String] = Some(s) }

    implicit val intRead: Read[Int] =
      new Read[Int] {
        def read(s: String): Option[Int] =
          if (s.matches("-?[0-9]+")) Some(s.toInt)
          else None
      }
  }

  // Then we enumerate our errors - when asking for a config value, one of two
  // things can go wrong: the field is missing, or it is not well-formed with
  // regards to the expected type.

  sealed abstract class ConfigError
  final case class MissingConfig(field: String) extends ConfigError
  final case class ParseError(field: String) extends ConfigError

  // We need a data type that can represent either a successful value (a parsed
  // configuration), or an error.

  {
    sealed abstract class Validated[+E, +A]

    object Validated {

      final case class Valid[+A](a: A) extends Validated[Nothing, A]

      final case class Invalid[+E](e: E) extends Validated[E, Nothing]

    }

  }

  // Note - the example continues using the cats Validated implementation.
  import cats.data.Validated
  import cats.data.Validated.{Invalid, Valid}

  case class Config(map: Map[String, String]) {
    def parse[A : Read](key: String): Validated[ConfigError, A] =
      map.get(key) match {
        case None        => Invalid(MissingConfig(key))
        case Some(value) =>
          Read[A].read(value) match {
            case None    => Invalid(ParseError(key))
            case Some(a) => Valid(a)
          }
      }
  }

  // Everything is in place to write the parallel validator. Recall that we can
  // only do parallel validation if each piece is independent. How do we enforce
  // the data is independent? By asking for all of it up front. Let’s start with
  // two pieces of data.

  def parallelValidate1[E, A, B, C](v1: Validated[E, A], v2: Validated[E, B])(f: (A, B) => C): Validated[E, C] =
    (v1, v2) match {
      case (Valid(a), Valid(b)) => Valid(f(a, b))
      case (Valid(_), i@Invalid(_)) => i
      case (i@Invalid(_), Valid(_)) => i
      case (Invalid(e1), Invalid(e2)) => ???
    }

  // We’ve run into a problem. In the case where both have errors, we want to
  // report both. But we have no way of combining the two errors into one error!
  // Perhaps we can put both errors into a List, but that seems needlessly
  // specific - clients may want to define their own way of combining errors.

  // How then do we abstract over a binary operation? The Semigroup type class
  // captures this idea.

  import cats.Semigroup

  def parallelValidate2[E : Semigroup, A, B, C](v1: Validated[E, A], v2: Validated[E, B])(f: (A, B) => C): Validated[E, C] =
    (v1, v2) match {
      case (Valid(a), Valid(b))       => Valid(f(a, b))
      case (Valid(_), i@Invalid(_))   => i
      case (i@Invalid(_), Valid(_))   => i
      case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
    }

  // Perfect! But.. going back to our example, we don’t have a way to combine
  // ConfigErrors. But as clients, we can change our Validated values where the
  // error can be combined, say, a List[ConfigError]. It is more common however
  // to use a NonEmptyList[ConfigError] - the NonEmptyList statically guarantees
  // we have at least one value, which aligns with the fact that if we have an
  // Invalid, then we most certainly have at least one error. This technique is
  // so common there is a convenient method on Validated called toValidatedNel
  // that turns any Validated[E, A] value to a Validated[NonEmptyList[E], A].
  // Additionally, the type alias ValidatedNel[E, A] is provided.

  // Time to parse.

  import cats.SemigroupK
  import cats.data.NonEmptyList
  import cats.implicits._

  case class ConnectionParams(endpoint: String, port: Int)

  val config = Config(
    Map(
      "endpoint" -> "127.0.0.1",
      "port"     -> "not an int"
    )
  )

  implicit val nelSemigroup: Semigroup[NonEmptyList[ConfigError]] =
    SemigroupK[NonEmptyList].algebra[ConfigError]

  implicit val readString: Read[String] = Read.stringRead
  implicit val readInt: Read[Int] = Read.intRead

  // Any and all errors are reported!

  val v1 = parallelValidate2(
    config.parse[String]("url").toValidatedNel,
    config.parse[Int]("port").toValidatedNel
  )(ConnectionParams.apply)

  assert(v1 == Invalid(NonEmptyList.of(MissingConfig("url"), ParseError("port"))))

  val v2 = parallelValidate2(
    config.parse[String]("endpoint").toValidatedNel,
    config.parse[Int]("port").toValidatedNel
  )(ConnectionParams.apply)

  assert(v2 == Invalid(NonEmptyList.of(ParseError("port"))))

  val validConfig = Config(
    Map(
      "endpoint" -> "127.0.0.1",
      "port"     -> "1234"
    )
  )

  val v3 = parallelValidate2(
    validConfig.parse[String]("endpoint").toValidatedNel,
    validConfig.parse[Int]("port").toValidatedNel
  )(ConnectionParams.apply)

  assert(v3 == Valid(ConnectionParams("127.0.0.1", 1234)))
}
