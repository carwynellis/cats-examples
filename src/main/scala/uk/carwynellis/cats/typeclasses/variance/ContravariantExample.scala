package uk.carwynellis.cats.typeclasses.variance

/**
  * The Contravariant type class is for functions that define a contramap with
  * the following type
  *
  *   def contramap[A,B](fa: F[A])(f: B => A): F[B]
  *
  * It looks like a covariant map, but with the transformation f: A => B
  * reversed.
  *
  * See http://typelevel.org/cats/typeclasses/contravariant.html
  */
object ContravariantExample extends App {

}
