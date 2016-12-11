package uk.carwynellis.cats.datatypes

import cats._
import cats.data.Const
import cats.implicits._

import scala.language.higherKinds

/**
  * At first glance, Const seems like a strange data type. It has two type
  * parameters, yet only stores values of the first type. What possible use is
  * it? As it turns out, it does have its uses which serve as a nice example of
  * the consistency and elegance of functional programming.
  *
  * See
  *
  *   http://typelevel.org/cats/datatypes/const.html
  *   http://functional-wizardry.blogspot.co.uk/2014/02/lens-implementation-part-1.html
  *
  */
object ConstExample extends App {

  // Thinking about Const

  // The Const data type can be thought of similarly to the const function, but
  // as a data type.

  def const[A, B](a: A)(b: => B): A = a

  // The const function takes two arguments and simply returns the first
  // argument, ignoring the second.

  final case class ConstDT[A, B](getConst: A)

  // The Const data type takes two type parameters but only ever stores a value
  // of the first type. The second type parameter is referred to as a 'phantom'
  // type since it is not used.

  // Why do we care?

  // It would seem that Const gives us no benefit over a data type that would
  // simply not have the second type parameter. However, while we don't
  // directly use the second type parameter, its existence becomes useful in
  // certain contexts.

  // Example 1 - Lens

  // The following is heavily inspired by Julian Truffaut's blog post on
  // Monocle, a fully-fledged optics library in Scala.

  // Types that contain other types are common across many programming
  // paradigms. It is of course desirable in many cases to get out members of
  // other types, or to set them. In traditional object-oriented programming
  // this is handled by getter and setter methods on the outer object. In
  // functional programming, a popular solution is to use a lens.

  // A lens can be thought of as a first class getter/setter. A Lens[S, A] is a
  // data type that knows how to get an A out of an S, or set an A in an S.

  trait Lens[S, A] {

    def get(s: S): A

    def set(s: S, a: A): S

    def modify(s: S)(f: A => A): S = set(s, f(get(s)))

  }

  // It can be useful to have effectful modifications as well. Perhaps our
  // modification can fail, expressed using Option, or return multiple values
  // using List.

  trait Lens2[S, A] {

    def get(s: S): A

    def set(s: S, a: A): S

    def modify(s: S)(f: A => A): S = set(s, f(get(s)))

    def modifyOption(s: S)(f: A => Option[A]): Option[S] =
      f(get(s)).map(a => set(s, a))

    def modifyList(s: S)(f: A => List[A]): List[S] =
      f(get(s)).map(a => set(s,a))

  }

  // Notice that both modifyOption and modifyList share the same
  // implementation. If we look closely, the only thing we need is a map
  // operation on the data type. Being good functinal programmers, we abstract.

  trait Lens3[S, A] {

    def get(s: S): A

    def set(s: S, a: A): S

    def modify(s: S)(f: A => A): S = set(s, f(get(s)))

    def modifyF[F[_] : Functor](s: S)(f: A => F[A]): F[S] =
      f(get(s)).map(a => set(s, a))

  }

  // We can redefine modify in terms of modifyF by using cats.Id. We can also
  // treat set as a modification that simply ignores the current value. Due to
  // these modifications however, we must leave modifyF abstract, since having
  // it defined in terms of set would lead to an infinite loop.

  trait Lens4[S, A] {

    def get(s: S): A

    def set(s: S, a: A): S = modify(s)(_ => a)

    def modify(s: S)(f: A => A): S = modifyF[Id](s)(f)

    def modifyF[F[_] : Functor](s: S)(f: A => F[A]): F[S]

  }

  // What about get? Certainly we can't define get in terms of the others which
  // modify an existing value. Let's give it a shot anyway.

  // Looking at modifyF we have an S we can pass in. The tricky part will be
  // the A => F[A] and then somehow getting an A out of F[S]. If we imagine F
  // to be a type level constant function however, we could imagine it would
  // simply take any type and return some other constant type, an A perhaps.
  // This suggests our F is a Const.

  // We then take a look at the fact that modifyF takes an F[_], a type
  // constructor that takes a single type parameter. Const takes two, so we
  // must fix one. The function returns an F[S], but we want an A, which implies
  // we have the first type parameter fixed to A and leave the second one free
  // for the function to fill in as it wants.

  // Substituting in Const[A, _] wherever we see F[_], the function wants an
  // A => Const[A, A] and will give us back a Const[A, S]. Looking at the
  // definition of Const, we see that we only ever have a value of the first
  // type parameter and completely ignore the second. Therefore, we can treat
  // any Const[X, Y] value as equivalent to X (plus or minus some wrapping into
  // Const). This leaves us with needing a function A => A. Given the type, the
  // only thing we can do is to take an A and return it right back (lifted into
  // Const).

  // Before we plug and play however, note that modifyF has a Functor constraint
  // on F[_]. This means we need to define a Functor instance for Const, where
  // the first type parameter is fixed.

  // Note: the example below assumes usage of the kind-projector compiler plugin
  // and will not compile if it is not being used in a project.

  implicit def constFunctor[X]: Functor[Const[X, ?]] =
    new Functor[Const[X, ?]] {
      // Recall Const[X, A] ~= X, so the function is not of any use to us
      def map[A, B](fa: Const[X, A])(f: A => B): Const[X, B] =
        Const(fa.getConst)
    }

  // Now that's taken care of, lets substitute and see what happens!

  trait Lens5[S, A] {

    def get(s: S): A = {
      val storedValue = modifyF[Const[A, ?]](s)(a => Const(a))
      storedValue.getConst
    }

    def set(s: S, a: A): S = modify(s)(_ => a)

    def modify(s: S)(f: A => A): S = modifyF[Id](s)(f)

    def modifyF[F[_] : Functor](s: S)(f: A => F[A]): F[S]

  }

  // It works! We get a Const[A, S] out on the other side, and we simply just
  // retrieve the A value stored inside.

  // What’s going on here?

  // We can treat the effectful “modification” we are doing as a store
  // operation - we take an A and store it inside a Const. Knowing only F[_]
  // has a Functor instance, it can only map over the Const which will do
  // nothing to the stored value. After modifyF is done getting the new S, we
  // retrieve the stored A value and we’re done!

}
