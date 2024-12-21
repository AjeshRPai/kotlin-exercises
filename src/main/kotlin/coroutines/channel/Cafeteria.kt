package coroutines.channel.cafeteria

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CoffeeType { ESPRESSO, LATTE }
class Milk
class GroundCoffee

sealed class Coffee

class Espresso(ground: GroundCoffee) : Coffee() {
    override fun toString(): String = "Espresso"
}

class Latte(milk: Milk, espresso: Espresso) : Coffee() {
    override fun toString(): String = "Latte"
}

suspend fun main() = coroutineScope {
    val listOfBaristas = listOf("Alice","Bob","Celine","Dave")
    val orderSendChannel = Channel<CoffeeType>(Channel.UNLIMITED)
    val coffeeReadyChannel = Channel<Coffee>(Channel.UNLIMITED)

    launch {
        for (order in orderSendChannel) {
            for (barista in listOfBaristas) {
                val coffee = makeCoffee(order, barista)
                coffeeReadyChannel.send(coffee)
            }
        }
    }

    launch {  for(coffee in coffeeReadyChannel){
        println("$coffee is ready")
    }
        }



    println("Welcome to Dream Coffee!")
    println("Press E to get espresso, L to get latte.")
    while (true) {
        val type = when (readlnOrNull()) {
            "E" -> CoffeeType.ESPRESSO
            "L" -> CoffeeType.LATTE
            else -> continue
        }
        println("Order for $type sent")
        orderSendChannel.send(type)
    }
}

private suspend fun makeCoffee(order: CoffeeType, baristaName: String): Coffee {
    val groundCoffee = groundCoffee(baristaName)
    val espresso = makeEspresso(groundCoffee, baristaName)
    return when (order) {
        CoffeeType.ESPRESSO -> espresso
        CoffeeType.LATTE -> {
            val milk = brewMilk(baristaName)
            Latte(milk, espresso)
        }
    }
}

suspend fun groundCoffee(baristaName: String): GroundCoffee {
    println("$baristaName: Grinding coffee...")
    delay(3000)
    return GroundCoffee()
}

suspend fun brewMilk(baristaName: String): Milk {
    println("$baristaName: Brewing milk...")
    delay(3000)
    return Milk()
}

suspend fun makeEspresso(ground: GroundCoffee, baristaName: String): Espresso {
    println("$baristaName: Making espresso...")
    delay(3000)
    return Espresso(ground)
}
