package variants

abstract class Variant<T> {

    abstract fun modify(target: T)

}
