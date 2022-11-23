package util

import kotlinx.serialization.Serializable

@Serializable
class Stack<T> {

    var top = 0
    val elements = ArrayList<T>()

    fun push(element: T) {
        elements.add(element)
    }

    fun pop(): T? {
        if (elements.isNotEmpty()) {
            return elements.removeLast()
        }
        return null
    }

    fun isEmpty() = elements.isEmpty()
    fun isNotEmpty() = elements.isNotEmpty()
}
