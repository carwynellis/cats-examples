package cats.examples.typeclasses

/**
  * Type classes are a powerful tool used in functional programming to enable
  * ad-hoc polymorphism, more commonly known as overloading. Where many
  * object-oriented languages leverage subtyping for polymorphic code,
  * functional programming tends towards a combination of parametric
  * polymorphism (think type parameters, like Java generics) and ad-hoc
  * polymorphism.
  *
  * See https://typelevel.org/cats/typeclasses.html
  */

object TypeClassExample extends App {

  // Example - collapsing a list

  // The  following code snippets show code that sums a list of integers,
  // concatenates a list of strings, and unions a list of sets.

  def sumInts(list: List[Int]): Int = list.foldRight(0)(_ + _)

  def concatStrings(list: List[String]): String = list.foldRight("")(_ ++ _)

  def unionSets[A](list: List[Set[A]]): Set[A] = list.foldRight(Set.empty[A])(_ union _)

  // All of these follow the same pattern: an initial value (0, empty string,
  // empty set) and a combining function (+, ++, union). We’d like to abstract
  // over this so we can write the function once instead of once for every type
  // so we pull out the necessary pieces into an interface.

  trait Monoid[A] {
    def empty: A
    def combine(x: A, y: A): A
  }

  // Implementation for Int
  val intAdditionMonoid: Monoid[Int] = new Monoid[Int] {
    def empty: Int = 0
    def combine(x: Int, y: Int): Int = x + y
  }

  // The name Monoid is taken from abstract algebra which specifies precisely
  // this kind of structure.

  // We can now write the functions above against this interface.

  def combineAll[A](list: List[A], A: Monoid[A]): A = list.foldRight(A.empty)(A.combine)

  // Type classes vs. subtyping

  // The definition above takes an actual monoid argument instead of doing the
  // usual object-oriented practice of using subtype constraints.

  // Subtyping
  def combineAll[A <: Monoid[A]](list: List[A]): A = ???

  // This has a subtle difference with the earlier explicit example. In order to
  // seed the foldRight with the empty value, we need to get a hold of it given
  // only the type A. Taking Monoid[A] as an argument gives us this by calling
  // the appropriate empty method on it. With the subtype example, the empty
  // method would be on a value of type Monoid[A] itself, which we are only
  // getting from the list argument. If list is empty, we have no values to work
  // with and therefore can’t get the empty value. Not to mention the oddity of
  // getting a constant value from a non-static object.

  // For another motivating difference, consider the simple pair type.

  final case class Pair[A, B](first: A, second: B)

  // Defining a Monoid[Pair[A, B]] depends on the ability to define a Monoid[A]
  // and Monoid[B], where the definition is point-wise, i.e. the first element
  // of the first pair combines with the first element of the second pair and
  // the second element of the first pair combines with the second element of
  // the second pair. With subtyping such a constraint would be encoded as
  // something like...

  final case class PairMonoid[A <: Monoid[A], B <: Monoid[B]](first: A, second: B) extends Monoid[Pair[A, B]] {
    def empty: Pair[A, B] = ???
    def combine(x: Pair[A, B], y: Pair[A, B]): Pair[A, B] = ???
  }

  // Not only is the type signature of Pair now messy but it also forces all
  // instances of Pair to have a Monoid instance, whereas Pair should be able to
  // carry any types it wants and if the types happens to have a Monoid instance
  // then so would it. We could try bubbling down the constraint into the methods
  // themselves...

  /**

  // The following does not compile - uncomment to see this
  final case class PairMonoid2[A, B](first: A, second: B) extends Monoid[Pair[A, B]] {
    def empty(implicit eva: A <:< Monoid[A], evb: B <:< Monoid[B]): Pair[A, B] = ???
    def combine(x: Pair[A, B], y: Pair[A, B])(implicit eva: A <:< Monoid[A], evb: B <:< Monoid[B]): Pair[A, B] = ???
  }

  **/

  // But now these don’t conform to the interface of Monoid due to the implicit constraints.

  // Implicit derivation

  // Note that a Monoid[Pair[A, B]] is derivable given Monoid[A] and Monoid[B]:

  def deriveMonoidPair[A, B](A: Monoid[A], B: Monoid[B]): Monoid[Pair[A, B]] =
    new Monoid[Pair[A, B]] {
      def empty: Pair[A, B] = Pair(A.empty, B.empty)

      def combine(x: Pair[A, B], y: Pair[A, B]): Pair[A, B] =
        Pair(A.combine(x.first, y.first), B.combine(x.second, y.second))
    }

  // One of the most powerful features of type classes is the ability to do this
  // kind of derivation automatically. We can do this through Scala’s implicit
  // mechanism.

  object Pair {
    implicit def tuple2Instance[A, B](implicit A: Monoid[A], B: Monoid[B]): Monoid[Pair[A, B]] =
      new Monoid[Pair[A, B]] {
        def empty: Pair[A, B] = Pair(A.empty, B.empty)

        def combine(x: Pair[A, B], y: Pair[A, B]): Pair[A, B] =
          Pair(A.combine(x.first, y.first), B.combine(x.second, y.second))
      }
  }

  // We also change any functions that have a Monoid constraint on the type
  // parameter to take the argument implicitly, and any instances of the type
  // class to be implicit.

  implicit val intAdditionMonoidImplicit: Monoid[Int] = new Monoid[Int] {
    def empty: Int = 0
    def combine(x: Int, y: Int): Int = x + y
  }

  def combineAll2[A](list: List[A])(implicit A: Monoid[A]): A = list.foldRight(A.empty)(A.combine)

  // Now we can also combineAll a list of Pairs so long as Pair’s type
  // parameters themselves have Monoid instances.

  implicit val stringMonoid: Monoid[String] = new Monoid[String] {
    def empty: String = ""
    def combine(x: String, y: String): String = x ++ y
  }

  import Pair.tuple2Instance

  assert(
    combineAll2(List(Pair(1, "hello"), Pair(2, " "), Pair(3, "world"))) == Pair(6, "hello world")
  )

  // A note on syntax

  // In many cases, including the combineAll function above, the implicit
  // arguments can be written with syntactic sugar.

  def combineAll3[A : Monoid](list: List[A]): A = ???

  // While nicer to read as a user, it comes at a cost for the implementer.

  def combineAll4[A : Monoid](list: List[A]): A =
    list.foldRight(implicitly[Monoid[A]].empty)(implicitly[Monoid[A]].combine)

  // For this reason, many libraries that provide type classes provide a utility
  // method on the companion object of the type class, usually under the name
  // apply, that skirts the need to call implicitly everywhere.

  object Monoid {
    def apply[A : Monoid]: Monoid[A] = implicitly[Monoid[A]]
  }

  def combineAll5[A : Monoid](list: List[A]): A =
    list.foldRight(Monoid[A].empty)(Monoid[A].combine)

  // Cats uses simulacrum for defining type classes which will auto-generate
  // such an apply method.

  // Laws

  // Conceptually, all type classes come with laws. These laws constrain
  // implementations for a given type and can be exploited and used to reason
  // about generic code.

  // For instance, the Monoid type class requires that combine be associative
  // and empty be an identity element for combine. That means the following
  // equalities should hold for any choice of x, y, and z.

  /**

  combine(x, combine(y, z)) = combine(combine(x, y), z)
  combine(x, id) = combine(id, x) = x

  **/

  // With these laws in place, functions parametrized over a Monoid can leverage
  // them for say, performance reasons. A function that collapses a List[A] into
  // a single A can do so with foldLeft or foldRight since combine is assumed to
  // be associative, or it can break apart the list into smaller lists and
  // collapse in parallel, such as

  val list = List(1, 2, 3, 4, 5)
  val (left, right) = list.splitAt(2)

  // Imagine the following two operations run in parallel
  val sumLeft = combineAll4(left)
  assert(sumLeft == 3)

  val sumRight = combineAll4(right)
  assert(sumRight == 12)

  // Now gather the results
  val result = Monoid[Int].combine(sumLeft, sumRight)
  assert(result == 15)

}
