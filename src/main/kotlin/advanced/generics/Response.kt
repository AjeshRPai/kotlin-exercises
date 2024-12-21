package advanced.generics.response

sealed class Response<out R, out E>
class Success<out R>(val value: R) : Response<R, Nothing>()
class Failure<out E>(val error: E) : Response<Nothing, E>()

fun usage() {
    val rs1 = Success(1)
    val rs2 = Success("ABC")
    val re1 = Failure(Error())
    val re2 = Failure("Error")

    val rs3: Success<Number> = rs1
    val rs4: Success<Any> = rs1
    val re3: Failure<Throwable> = re1
    val re4: Failure<Any> = re1

    val r1: Response<Int, Throwable> = rs1
    val r2: Response<Int, Throwable> = re1
}
