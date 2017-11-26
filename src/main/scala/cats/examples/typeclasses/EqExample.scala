package cats.examples.typeclasses

/**
  * Eq is an alternative to the standard Java equals method.
  *
  * It is defined by a single method eqv as follows
  *
  *   def eqv(x: A, y: A): Boolean
  *
  */
object EqExample extends App {

  // In Scala it's possible to compare any two values using == (which desugars
  // to Java equals). This is because the type signature of equals uses Any
  // (Java's Object) to compare two values. Thus we can compare two completely
  // unrelated types without generating a compiler error.

  // The Scala compiler may issue a warning in some cases, but not all, which
  // can lead to weird bugs.

  // For example the following code will raise a warning at compile time...

  // 42 == "Hello"

  // ...but the following will compile without a warning.

  "Hellp" == 42

  // Ideally Scala should never let us compare two types that will never be
  // equal.

  // As you can probably see in the type signature of eqv, it is impossible to
  // compare two values of different types, eliminating these types of bugs
  // altogether.

  // The Eq syntax package also offers some handy symbolic operators.

  import cats.implicits._

  assert(1 === 1)

  assert("Hello" =!= "Worled")

  // Implementing Eq instances yourself for every type might seem like a lot of
  // work for the slight gains of type safety. However there are two options to
  // reduce the work involved.

  // Firstly we can use helper functions provided with Eq.

  // Using Eq.fromUniversalEquals which defers to == we can implement an Eq
  // instance for a custom type as follows...

  import cats.kernel.Eq

  case class Foo(a: Int, b: String)

  implicit val eqFoo: Eq[Foo] = Eq.fromUniversalEquals

  assert(Foo(1, "Bar") === Foo(1, "Bar"))

  assert(Foo(1, "Bar") =!= Foo(2, "Baz"))

  // An alternative is to use Kittens, a library providing type class
  // derivation for cats. See https://github.com/milessabin/kittens
}
