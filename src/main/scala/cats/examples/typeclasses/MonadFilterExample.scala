package cats.examples.typeclasses

import cats._
import cats.implicits._

/**
  * The MonadFilter type class provides a Monad with an additional method
  * which allows us to create an empty value.
  *
  * This then allows us to add a filter method to the monad which is used when
  * pattern matching or for guards in for comprehensions.
  */
object MonadFilterExample extends App {

  assert(MonadFilter[List].empty == List.empty)

  assert(MonadFilter[List].mapFilter(List(1,2))((a: Int) => Option(a + 2)) == List(3,4))

  assert(MonadFilter[List].mapFilter(List.empty)((a: Int) => Option(a + 2)) == List.empty)

}
