package cats.examples.typeclasses.monads

import cats._
import cats.implicits._

import scala.annotation.tailrec

/**
  * Monad extends the Applicative type class with a new function, flatten.
  *
  * Flatten takes a value in nested contexts, e.g. F[F[A]] and combines them
  * into a single context, e.g. F[A].
  *
  * See http://typelevel.org/cats/typeclasses/monad.html
  */
object MonadExample extends App {

  // The name flatten should remind you of functions of the same name in many
  // classes in the standard library.
  assert(Option(Option(1)).flatten == Option(1))

  assert(Option(None).flatten == None)

  assert(List(List(1), List(2,3)).flatten == List(1,2,3))

  // If Applicative is present and flatten is 'well behaved', extending the
  // Applicative into a Monad is trivial. To provide evidence that a type
  // belongs in the Monad type class, cats requires us to provide an
  // implementation of pure, (which can be reused from Applicative), and
  // flatmap.

  // We can use flatten to define flatMap since flatMap is just map followed by
  // flatten. Conversely flatten is just flatMap using the identity function.

  implicit def optionMonad(implicit app: Applicative[Option]) =
    new Monad[Option] {
      // Define flatMap using the flatten method from Option
      override def flatMap[A,B](fa: Option[A])(f: A => Option[B]): Option[B] =
        app.map(fa)(f).flatten

      // Delegate to pure from Applicative.
      override def pure[A](a: A): Option[A] = app.pure(a)

      // We must also implement tailRecM from cats.FlatMap
      @tailrec
      override def tailRecM[A,B](init: A)(fn: A => Option[Either[A,B]]): Option[B] =
        fn(init) match {
          case None => None
          case Some(Right(b)) => Some(b)
          case Some(Left(a)) => tailRecM(a)(fn)
        }
    }

  // flatMap is often considered to be the core function of Monad and cats
  // follows this tradition by providing implementations of flatten and map
  // derived from flatMap and pure.

  // Monadic recursion is common enough that cats requires tailRecM to be
  // implemented, which must provide a stack safe implementation.

  // Additionally flatMap has special significance in Scala since for
  // comprehensions use flatMap to chain together operations in a Monadic
  // context.

  // For example the following for-comprehension...
  for {
    x <- Some(1)
    y <- Some(2)
  } yield x + y

  // ...is equivalent to
  Some(1) flatMap { x =>
    Some(2) map { y =>
      x + y
    }
  }

  // Monad also provides the ability to choose later operations in a sequence
  // according to the results of earlier ones. This is provided by ifM which
  // lifts an if statement into the monadic context.

  val ifTrue = List(1,2)
  val ifFalse = List(3,4)

  assert(Monad[List].ifM(List(true, false, true))(ifTrue, ifFalse)
    == List(1,2,3,4,1,2))

  // Unlike Applicative and Functor, not all Monad instances compose.
  // More formally, for Monads A[_] and B[_] it follows that B[A[_]] may not be
  // a Monad.

  // However in many common cases, Monads do compose.

  // One way of expressing this is to provide an implementation defining how
  // to compose some outer Monad F[_] with a specific inner Monad, for example
  // Option[_] as in the example below.
  case class OptionT[F[_], A](value: F[Option[A]])

  implicit def OptionTMonad[F[_]](implicit F: Monad[F]) =
    new Monad[OptionT[F, ?]] {

      override def pure[A](a: A): OptionT[F, A] = OptionT(F.pure(Some(a)))

      override def flatMap[A,B](fa: OptionT[F,A])(f: A => OptionT[F,B]): OptionT[F,B] =
        OptionT {
          F.flatMap(fa.value) {
            case None => F.pure(None)
            case Some(a) => f(a).value
          }
        }

      override def tailRecM[A,B](a: A)(f: A => OptionT[F, Either[A,B]]): OptionT[F,B] =
        OptionT {
          F.tailRecM(a)(a0 => F.map( f(a0).value) {
            case None => Either.right[A, Option[B]](None)
            case Some(b0) => b0.map(Some(_))
          })
        }
    }

}
