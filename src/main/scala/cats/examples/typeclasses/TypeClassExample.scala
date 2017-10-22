package cats.examples.typeclasses

/**
  * Introduction to typeclass pattern.
  *
  * See http://typelevel.org/cats/typeclasses.html
  */

/**
  * A type class to provide some context
  */
trait Show[A] {
  def show(f: A): String
}

object TypeClassExample extends App {

  /**
    * Note that using the implicitly keyword is equivalent to the following
    *
    * def log[A](a: A)(implicit s: Show[A]) = println(s.show(a))
    */
  def log[A: Show](a: A) = println(implicitly[Show[A]].show(a))

  implicit val stringShow = new Show[String] {
    def show(s: String) = s
  }

  implicit def optionShow[A](implicit sa: Show[A]) = new Show[Option[A]] {
    def show(oa: Option[A]): String = oa match {
      case None => "None"
      case Some(a) => "Some("+ sa.show(a) + ")"
    }
  }

  // Example invocations of log for the supported types.
  log("a string")
  log(Option("some string"))
  log(Option(Option("some some string")))

}
