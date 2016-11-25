package uk.carwynellis.cats.typeclasses.functors

import cats._
import cats.implicits._

import scala.concurrent.Future

/**
  * Traverse extends the Functor typeclass with a new function, traverse.
  *
  * Broadly speaking, traverse applies a function, f: A => G[B] to some functor
  * F[A] accumulating the result in functor G[B].
  *
  * See http://typelevel.org/cats/typeclasses/traverse.html
  */

object TraverseExample extends App {

  // In functional programming we typically eoncode effects as data types, for
  // example Option encodes a value that may not be present.

  // These effects tend to appear in functions that operate on a single
  // argument, for example parsing a String into an Int. Consider the following
  // examples.

  // Parsing a string may or may not succeed and result in Some value.
  def parseInt(s: String): Option[Int] = ???

  // Validating a login will Either succeed or fail with some error.
  trait Credentials
  trait AuthenticationError

  def validateCredentials(c: Credentials): Either[AuthenticationError, Unit] = ???

  // Retrieving a user profile will eventually return some data.
  trait Profile
  trait User

  def getUserProfile(u: User): Future[Profile] = ???

  // Now we have a simple function for fetching a user profile, it can be used
  // to fetch profiles for a list of users via map.
  def getUserProfiles(users: List[User]): List[Future[Profile]] = users map { getUserProfile }

  // However the result is a List of Future which is awkward to work with.
  // Future provides a traverse method which takes a List[Future[_]] and
  // returns a Future[List[_]] but this is a very specific implementation.

  // We can apply this idea more generally using the traverse method from the
  // Traverse type class.

  // Thus we could convert a List[String] or validate credentials for a List
  // of users.

  

}
