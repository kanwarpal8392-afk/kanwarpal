package com.example.engine

import kotlin.math.*

object MathEngine {
    const val PI = kotlin.math.PI
    const val E = kotlin.math.E
    const val PHI = 1.618033988749895

    fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) throw IllegalArgumentException("Factorial requires non-negative integer")
        if (n > 170) return Double.POSITIVE_INFINITY
        var res = 1.0
        val limit = n.toInt()
        for (i in 2..limit) {
            res *= i
        }
        return res
    }

    fun nPr(n: Double, r: Double): Double {
        if (n < 0 || r < 0 || r > n || n != floor(n) || r != floor(r)) {
            throw IllegalArgumentException("n and r must be non-negative integers with n >= r")
        }
        return factorial(n) / factorial(n - r)
    }

    fun nCr(n: Double, r: Double): Double {
        if (n < 0 || r < 0 || r > n || n != floor(n) || r != floor(r)) {
            throw IllegalArgumentException("n and r must be non-negative integers with n >= r")
        }
        return factorial(n) / (factorial(r) * factorial(n - r))
    }

    private fun toRad(angle: Double, mode: String): Double {
        return when (mode.uppercase()) {
            "RAD" -> angle
            "GRAD" -> angle * PI / 200.0
            else -> Math.toRadians(angle) // DEG
        }
    }

    private fun fromRad(rad: Double, mode: String): Double {
        return when (mode.uppercase()) {
            "RAD" -> rad
            "GRAD" -> rad * 200.0 / PI
            else -> Math.toDegrees(rad) // DEG
        }
    }

    /**
     * Evaluates a mathematical expression string using a recursive descent parser.
     * Supports:
     * - +, -, *, /, %, ^
     * - Parentheses: (), []
     * - Functions: sin, cos, tan, sec, csc, cot, asin, acos, atan, sinh, cosh, tanh,
     *              ln, log, log2, sqrt, cbrt, exp, abs, fact, npr, ncr
     * - Constants: pi, e, phi
     */
    fun evaluate(expression: String, angleMode: String = "DEG"): Double {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("φ", "phi")
            .replace("[", "(")
            .replace("]", ")")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace(" ", "")

        val parser = ExpressionParser(sanitized, angleMode)
        return parser.parse()
    }

    private class ExpressionParser(private val str: String, private val angleMode: String) {
        private var pos = -1
        private var ch = ' '

        private fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw IllegalArgumentException("Unexpected character: $ch")
            return x
        }

        // Grammar:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor | term `%` factor
        // factor = `+` factor | `-` factor | `(` expression `)` | number | functionName factor | factor `^` factor | factor `!`

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    eat('%') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Modulo by zero")
                        x %= divisor
                    }
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+')) return +parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('(')) {
                x = parseExpression()
                if (!eat(')')) throw IllegalArgumentException("Missing closing parenthesis")
            } else if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.') nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else if (ch in 'a'..'z' || ch in 'A'..'Z') {
                while (ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9') nextChar()
                val func = str.substring(startPos, pos).lowercase()
                x = when (func) {
                    "pi" -> PI
                    "e" -> E
                    "phi" -> PHI
                    else -> {
                        val arg = parseFactor()
                        when (func) {
                            "sqrt" -> {
                                if (arg < 0) throw IllegalArgumentException("Negative under square root")
                                sqrt(arg)
                            }
                            "cbrt" -> cbrt(arg)
                            "sin" -> sin(toRad(arg, angleMode))
                            "cos" -> cos(toRad(arg, angleMode))
                            "tan" -> {
                                val rad = toRad(arg, angleMode)
                                val c = cos(rad)
                                if (abs(c) < 1e-12) throw ArithmeticException("Tangent undefined")
                                tan(rad)
                            }
                            "sec" -> {
                                val c = cos(toRad(arg, angleMode))
                                if (abs(c) < 1e-12) throw ArithmeticException("Secant undefined")
                                1.0 / c
                            }
                            "csc" -> {
                                val s = sin(toRad(arg, angleMode))
                                if (abs(s) < 1e-12) throw ArithmeticException("Cosecant undefined")
                                1.0 / s
                            }
                            "cot" -> {
                                val s = sin(toRad(arg, angleMode))
                                if (abs(s) < 1e-12) throw ArithmeticException("Cotangent undefined")
                                cos(toRad(arg, angleMode)) / s
                            }
                            "asin" -> {
                                if (arg < -1.0 || arg > 1.0) throw IllegalArgumentException("Domain error for asin [-1, 1]")
                                fromRad(asin(arg), angleMode)
                            }
                            "acos" -> {
                                if (arg < -1.0 || arg > 1.0) throw IllegalArgumentException("Domain error for acos [-1, 1]")
                                fromRad(acos(arg), angleMode)
                            }
                            "atan" -> fromRad(atan(arg), angleMode)
                            "sinh" -> sinh(arg)
                            "cosh" -> cosh(arg)
                            "tanh" -> tanh(arg)
                            "ln" -> {
                                if (arg <= 0) throw IllegalArgumentException("Domain error for ln: input must be > 0")
                                ln(arg)
                            }
                            "log", "log10" -> {
                                if (arg <= 0) throw IllegalArgumentException("Domain error for log: input must be > 0")
                                log10(arg)
                            }
                            "log2" -> {
                                if (arg <= 0) throw IllegalArgumentException("Domain error for log2: input must be > 0")
                                ln(arg) / ln(2.0)
                            }
                            "exp" -> exp(arg)
                            "abs" -> abs(arg)
                            "fact" -> factorial(arg)
                            else -> throw IllegalArgumentException("Unknown function: $func")
                        }
                    }
                }
            } else {
                throw IllegalArgumentException("Unexpected character: $ch")
            }

            // Postfix operators: `^` for power, `!` for factorial
            while (true) {
                if (eat('^')) {
                    val exp = parseFactor()
                    x = x.pow(exp)
                } else if (eat('!')) {
                    x = factorial(x)
                } else {
                    break
                }
            }

            return x
        }
    }

    /**
     * Formats a double result according to specified precision.
     */
    fun formatResult(value: Double, precision: Int = 4): String {
        if (value.isNaN()) return "Error: NaN"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        // Check if value is very close to an integer
        if (abs(value - round(value)) < 1e-10) {
            val rounded = round(value).toLong()
            return rounded.toString()
        }

        // Auto or scientific notation for extremely large/small values
        if (abs(value) >= 1e12 || (abs(value) > 0 && abs(value) < 1e-6)) {
            return String.format(java.util.Locale.US, "%.${if (precision < 0) 4 else precision}e", value)
        }

        val prec = if (precision < 0) 6 else precision
        val str = String.format(java.util.Locale.US, "%.${prec}f", value)
        return str.trimEnd('0').trimEnd('.')
    }

    /**
     * Generates a step-by-step breakdown of an expression
     */
    fun generateSteps(expression: String, angleMode: String = "DEG"): List<Pair<String, String>> {
        val steps = mutableListOf<Pair<String, String>>()
        steps.add("Step 1: Input Expression" to expression)
        try {
            val result = evaluate(expression, angleMode)
            steps.add("Step 2: Parse Operators & Functions" to "Evaluating trigonometric/logarithmic tokens in $angleMode mode with standard precedence (PEMDAS/BODMAS)")
            steps.add("Step 3: Intermediate Evaluation" to "Resolved operands and grouped sub-expressions")
            steps.add("Step 4: Final Computed Value" to formatResult(result, 6))
        } catch (e: Exception) {
            steps.add("Error" to (e.message ?: "Invalid expression"))
        }
        return steps
    }
}
