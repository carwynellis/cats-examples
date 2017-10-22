package uk.carwynellis.cats.typeclasses.variance

import java.util.Date

import cats._
import cats.implicits._

/**
  * The Invariant type class is for functors that define an imap function with
  * the following type
  *
  *   def imap[A, B](fa: F[A])(f: A => B)(g: B => A): F[B]
  *
  * Every covariant (as well as contravariant) functor gives rise to an
  * invariant functor, by ignoring the g (or in case of contravariance, f)
  * function.
  *
  * Instances of Invariant include Semigroup and Monoid.
  *
  * See http://typelevel.org/cats/typeclasses/invariant.html
  */
object InvariantExample extends App {

  // Invariant instance for Semigroup

  // Say we have a Semigroup[Long] representing a UNIX timestamp which we want
  // to reuse to create a Semigroup[Date].

  // Semigroup does not form a covariant functor.

  // If Semigroup had an instance for the standard covariant functor type class
  // we could use map to apply a conversion function such as

  def longToDate(timestamp: Long): Date = new Date(timestamp)

  // However this is not enough to give us a Semigroup[Date].

  // A Semigroup[Date] should be able to combine two values of type Date. The
  // longToDate function does not help us at all because it only converts a
  // Long into a Date.

  // Semigroup does not form a contravariant functor.

  // If Semigroup had an instance for the contravariant functor type class we
  // could make use of contramap to apply the following conversion function.

  def dateToLong(date: Date): Long = date.getTime

  // Again we are faced with a problem when trying to get a Semigroup[Date]
  // from a Semigroup[Long]. As before, consider the case of wanting to combine
  // two Date values. We could use dateToLong to convert them into Longs and
  // use Semigroup[Long] to combine the values. However we cannot then turn
  // this into a Semigroup[Date] using contramap.

  // Semigroup does form an invariant functor.

  // From the above we can conclude that we need both map from Covariant, and
  // contramap from Contravarant.

  // There is already a type class for this, Invariant, which provides the
  // imap method.

  // We can now use imap to turn a Semigroup[Long] into a Semigroup[Date]
  // using the functions defined above as follows.

  implicit val semigroupDate: Semigroup[Date] =
    Semigroup[Long].imap(longToDate)(dateToLong)

  val EpochMilliseconds = 1451606400000L
  lazy val OneDayMilliseconds = 24 * 60 * 60 * 1000

  val someInstant = longToDate(EpochMilliseconds)
  val oneDay = longToDate(OneDayMilliseconds)

  val combined = someInstant |+| oneDay

  assert(combined == longToDate(EpochMilliseconds + OneDayMilliseconds))
}
