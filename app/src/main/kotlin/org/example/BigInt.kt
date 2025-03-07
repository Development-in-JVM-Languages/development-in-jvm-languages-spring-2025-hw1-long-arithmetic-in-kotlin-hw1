class BigInt(private val value: String) : Comparable<BigInt> {

    private var digits: List<Int>
    private var sign: Int

    init {
        require(value.matches(Regex("-?\\d+"))) { "Invalid input format: $value" }
        val numPart = if (value.startsWith("-")) value.substring(1) else value
        require(numPart.isNotEmpty()) { "Invalid input format: $value" }
        digits = numPart.map { it.toString().toInt() }
        sign = when {
            digits.all { it == 0 } -> 0
            value.startsWith("-") -> -1
            else -> 1
        }
        normalize()
    }

    private fun normalize() {
        var normalizedDigits = digits
        while (normalizedDigits.size > 1 && normalizedDigits[0] == 0) {
            normalizedDigits = normalizedDigits.drop(1)
        }
        digits = normalizedDigits
    }

    // Addition
    operator fun plus(other: BigInt): BigInt {
        if (sign == 0) return other
        if (other.sign == 0) return this
        return if (sign == other.sign) {
            BigInt(addDigits(digits, other.digits).joinToString("").let {
                if (sign == -1) "-$it" else it
            })
        } else {
            if (this.abs() > other.abs()) {
                BigInt(subtractDigits(digits, other.digits).joinToString("").let {
                    if (sign == -1) "-$it" else it
                })
            } else {
                BigInt(subtractDigits(other.digits, digits).joinToString("").let {
                    if (other.sign == -1) "-$it" else it
                })
            }
        }
    }

    private fun addDigits(a: List<Int>, b: List<Int>): List<Int> {
        val result = mutableListOf<Int>()
        var carry = 0
        var i = a.size - 1
        var j = b.size - 1
        while (i >= 0 || j >= 0 || carry > 0) {
            val sum = (if (i >= 0) a[i--] else 0) + (if (j >= 0) b[j--] else 0) + carry
            result.add(sum % 10)
            carry = sum / 10
        }
        return result.reversed()
    }

    private fun subtractDigits(a: List<Int>, b: List<Int>): List<Int> {
        val result = mutableListOf<Int>()
        var borrow = 0
        var i = a.size - 1
        var j = b.size - 1
        while (i >= 0) {
            val diff = a[i--] - (if (j >= 0) b[j--] else 0) - borrow
            if (diff < 0) {
                result.add(diff + 10)
                borrow = 1
            } else {
                result.add(diff)
                borrow = 0
            }
        }
        return result.reversed()
    }

    operator fun plus(other: Int): BigInt = this + BigInt(other.toString())
    operator fun plus(other: Short): BigInt = this + BigInt(other.toString())
    operator fun plus(other: Byte): BigInt = this + BigInt(other.toString())

    // Subtraction
    operator fun minus(other: BigInt): BigInt = this + (-other)
    operator fun minus(other: Int): BigInt = this - BigInt(other.toString())
    operator fun minus(other: Short): BigInt = this - BigInt(other.toString())
    operator fun minus(other: Byte): BigInt = this - BigInt(other.toString())

    // Unary minus
    operator fun unaryMinus(): BigInt = when (sign) {
        -1 -> BigInt(digits.joinToString(""))
        1 -> BigInt("-${digits.joinToString("")}")
        else -> this
    }

    // Multiplication
    operator fun times(other: BigInt): BigInt {
        if (sign == 0 || other.sign == 0) return BigInt("0")
        val resultDigits = multiplyDigits(digits, other.digits)
        val resultSign = if (sign == other.sign) 1 else -1
        return BigInt(resultDigits.joinToString("").let {
            if (resultSign == -1) "-$it" else it
        })
    }

    private fun multiplyDigits(a: List<Int>, b: List<Int>): List<Int> {
        val result = MutableList(a.size + b.size) { 0 }
        for (i in a.indices.reversed()) {
            for (j in b.indices.reversed()) {
                val product = a[i] * b[j] + result[i + j + 1]
                result[i + j + 1] = product % 10
                result[i + j] += product / 10
            }
        }
        while (result.size > 1 && result[0] == 0) {
            result.removeAt(0)
        }
        return result
    }

    operator fun times(other: Int): BigInt = this * BigInt(other.toString())
    operator fun times(other: Short): BigInt = this * BigInt(other.toString())
    operator fun times(other: Byte): BigInt = this * BigInt(other.toString())

    // Division
    operator fun div(other: BigInt): BigInt {
        require(other.sign != 0) { "Division by zero" }
        if (sign == 0) return BigInt("0")
        val (quotientDigits, _) = divideDigits(digits, other.digits)
        val resultSign = if (sign == other.sign) 1 else -1
        return BigInt(quotientDigits.joinToString("").let {
            if (resultSign == -1) "-$it" else it
        })
    }

    private fun divideDigits(a: List<Int>, b: List<Int>): Pair<List<Int>, List<Int>> {
        if (compareDigits(a, b) < 0) return Pair(listOf(0), a)
        val quotient = mutableListOf<Int>()
        var remainder = mutableListOf<Int>()
        var dividend = a.toMutableList()
        for (i in a.indices) {
            remainder.add(dividend[i])
            while (remainder.size > 1 && remainder[0] == 0) {
                remainder.removeAt(0)
            }
            if (compareDigits(remainder, b) < 0) {
                quotient.add(0)
                continue
            }
            var quotientDigit = 0
            while (compareDigits(remainder, b) >= 0) {
                remainder = subtractDigits(remainder, b).toMutableList()
                quotientDigit++
            }
            quotient.add(quotientDigit)
        }
        while (quotient.size > 1 && quotient[0] == 0) {
            quotient.removeAt(0)
        }
        return Pair(quotient, remainder)
    }

    operator fun div(other: Int): BigInt = this / BigInt(other.toString())
    operator fun div(other: Short): BigInt = this / BigInt(other.toString())
    operator fun div(other: Byte): BigInt = this / BigInt(other.toString())

    // Modulo
    operator fun rem(other: BigInt): BigInt {
        require(other.sign != 0) { "Division by zero" }
        if (sign == 0) return BigInt("0")
        val (_, remainderDigits) = divideDigits(digits, other.digits)
        return BigInt(remainderDigits.joinToString("").let {
            if (sign == -1) "-$it" else it
        })
    }

    operator fun rem(other: Int): BigInt = this % BigInt(other.toString())
    operator fun rem(other: Short): BigInt = this % BigInt(other.toString())
    operator fun rem(other: Byte): BigInt = this % BigInt(other.toString())

    // Exponentiation
    fun pow(exp: BigInt): BigInt {
        if (exp.sign == 0) return BigInt("1")
        if (sign == 0) return BigInt("0")
        if (exp.sign == -1) throw IllegalArgumentException("Negative exponents are not supported")
        var result = BigInt("1")
        var base = this
        var exponent = exp
        while (exponent.sign != 0) {
            if (exponent.digits.last() % 2 == 1) {
                result *= base
            }
            base *= base
            exponent /= BigInt("2")
        }
        return result
    }

    fun pow(exp: Int): BigInt = pow(BigInt(exp.toString()))
    fun pow(exp: Short): BigInt = pow(BigInt(exp.toString()))
    fun pow(exp: Byte): BigInt = pow(BigInt(exp.toString()))

    // Sign function
    fun sign(): Int = sign

    // Absolute value
    fun abs(): BigInt = if (sign == -1) -this else this

    // String representation
    override fun toString(): String = when (sign) {
        -1 -> "-${digits.joinToString("")}"
        0 -> "0"
        else -> digits.joinToString("")
    }

    // Comparison
    override operator fun compareTo(other: BigInt): Int {
        if (sign != other.sign) return sign.compareTo(other.sign)
        if (sign == 0) return 0
        return compareDigits(digits, other.digits) * sign
    }

    operator fun compareTo(other: Int): Int = compareTo(BigInt(other.toString()))
    operator fun compareTo(other: Short): Int = compareTo(BigInt(other.toString()))
    operator fun compareTo(other: Byte): Int = compareTo(BigInt(other.toString()))

    private fun compareDigits(a: List<Int>, b: List<Int>): Int {
        if (a.size > b.size) return 1
        if (a.size < b.size) return -1
        for (i in a.indices) {
            if (a[i] > b[i]) return 1
            if (a[i] < b[i]) return -1
        }
        return 0
    }

    // Equality
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigInt) return false
        return sign == other.sign && digits == other.digits
    }

    // Hash code
    override fun hashCode(): Int = digits.hashCode() + sign.hashCode()
}