package akka.japi

/**
 * A Function interface. Used to create first-class-functions is Java (sort of).
 */
trait Function[T,R] {
  def apply(param: T): R
}

/** A Procedure is like a Function, but it doesn't produce a return value
 */
trait Procedure[T] {
  def apply(param: T): Unit
}

/**
 * An executable piece of code that takes no parameters and doesn't return any value.
 */
trait SideEffect {
  def apply: Unit
}

/**
 * This class represents optional values. Instances of <code>Option</code>
 * are either instances of case class <code>Some</code> or it is case
 * object <code>None</code>.
 * <p>
 * Java API
 */
sealed abstract class Option[A] extends java.lang.Iterable[A] {
  import scala.collection.JavaConversions._

  def get: A
  def isEmpty: Boolean
  def isDefined = !isEmpty
  def asScala: scala.Option[A]
  def iterator = if (isEmpty) Iterator.empty else Iterator.single(get)
}

object Option {
  /**
   * <code>Option</code> factory that creates <code>Some</code>
   */
  def some[A](v: A): Option[A] = Some(v)

  /**
   * <code>Option</code> factory that creates <code>None</code>
   */
  def none[A] = None.asInstanceOf[Option[A]]

  /**
   * <code>Option</code> factory that creates <code>None</code> if
   * <code>v</code> is <code>null</code>, <code>Some(v)</code> otherwise.
   */
  def option[A](v: A): Option[A] = if (v == null) none else some(v)

  /**
   * Class <code>Some[A]</code> represents existing values of type
   * <code>A</code>.
   */
  final case class Some[A](v: A) extends Option[A] {
    def get = v
    def isEmpty = false
    def asScala = scala.Some(v)
  }

  /**
   * This case object represents non-existent values.
   */
  private case object None extends Option[Nothing] {
    def get = throw new NoSuchElementException("None.get")
    def isEmpty = true
    def asScala = scala.None
  }

  implicit def java2ScalaOption[A](o: Option[A]): scala.Option[A] = o.asScala
  implicit def scala2JavaOption[A](o: scala.Option[A]): Option[A] = option(o.get)
}