package pl.mateusz.wawreszuk.lfsrgenerator

import io.reactivex.Observable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class GeneratorLFSR(polynomial: String, private val interval: Long) {

    private val polynomial = ArrayList<Int>()
    private val register: Array<Boolean>

    init {
        val random = Random(System.currentTimeMillis())
        register = Array(polynomial.length) { random.nextBoolean() }
        register[0] = true
        register[register.size - 1] = false
        val chars = polynomial.toCharArray()
        for (i in chars.indices) {
            if (chars[i] == '1') {
                this.polynomial.add(i)
            } else if (chars[i] != '0') {
                throw WrongKeyFormatException(R.string.shouldBeBinaryFormat)
            }
        }
        if (polynomial.lastOrNull() != '1') throw WrongKeyFormatException(R.string.lastDigitShouldBeOne)
    }

    fun generateObservable(): Observable<Boolean> =
        Observable.interval(interval, interval, TimeUnit.MILLISECONDS)
            .map {
                var newBit = register[polynomial[0]]
                for (i in 1 until polynomial.size) newBit = newBit.xor(register[polynomial[i]])
                val outBit = register.last()
                for (i in register.size - 1 downTo 1) register[i] = register[i - 1]
                register[0] = newBit
                outBit
            }

}