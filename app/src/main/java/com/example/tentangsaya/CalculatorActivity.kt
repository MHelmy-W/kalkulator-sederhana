package com.example.tentangsaya

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CalculatorActivity : AppCompatActivity() {
    private lateinit var tvResult: TextView
    private var expression: String = ""
    private lateinit var mediaPlayerClick: MediaPlayer
    private lateinit var mediaPlayerOperator: MediaPlayer
    private lateinit var mediaPlayerClear: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        tvResult = findViewById(R.id.tvResult)

        mediaPlayerClick = MediaPlayer.create(this, R.raw.bonksfx)
        mediaPlayerOperator = MediaPlayer.create(this, R.raw.pew_pewsfx)
        mediaPlayerClear = MediaPlayer.create(this, R.raw.vine_boomsfx)

        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
            R.id.btn8, R.id.btn9
        )

        val operatorButtons = listOf(
            R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide
        )

        numberButtons.forEach { id ->
            findViewById<Button>(id)?.setOnClickListener {
                expression += findViewById<Button>(id)?.text.toString()
                tvResult.text = expression
                playSound(mediaPlayerClick)
            }
        }

        operatorButtons.forEach { id ->
            findViewById<Button>(id)?.setOnClickListener {
                val op = findViewById<Button>(id)?.text.toString()
                if (expression.isNotEmpty() && !"+-*/".contains(expression.last())) {
                    expression += op
                    tvResult.text = expression
                    playSound(mediaPlayerOperator)
                }
            }
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            expression = ""
            tvResult.text = "0"
            playSound(mediaPlayerClear)
        }

        findViewById<Button>(R.id.btnEqual).setOnClickListener {
            try {
                val result = evaluateExpression(expression)
                tvResult.text = result.toString()
                expression = result.toString()
            } catch (e: Exception) {
                tvResult.text = "Error"
                expression = ""
            }
        }

        findViewById<Button>(R.id.btnExit).setOnClickListener {
            finish()
        }
    }

    private fun playSound(mediaPlayer: MediaPlayer?) {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.seekTo(0)
            } else {
                it.start()
            }
        }
    }

    private fun evaluateExpression(exp: String): Double {
        return try {
            eval(exp)
        } catch (e: Exception) {
            Double.NaN
        }
    }

    private fun eval(expression: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expression.length) expression[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    when {
                        eat('+'.code) -> x += parseTerm()
                        eat('-'.code) -> x -= parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    when {
                        eat('*'.code) -> x *= parseFactor()
                        eat('/'.code) -> {
                            val divisor = parseFactor()
                            if (divisor == 0.0) throw ArithmeticException("Division by zero")
                            x /= divisor
                        }
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDoubleOrNull() ?: throw RuntimeException("Invalid number format")
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                return x
            }
        }.parse()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayerClick.release()
        mediaPlayerOperator.release()
        mediaPlayerClear.release()
    }
}
