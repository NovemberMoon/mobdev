package io.github.mobdev

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvDisplay = findViewById<TextView>(R.id.tvDisplay)
        val tvError = findViewById<TextView>(R.id.tvError)

        viewModel.displayText.observe(this) { text ->
            tvDisplay.text = text
        }

        viewModel.error.observe(this) { errorText ->
            if (errorText != null) {
                tvError.text = errorText
                tvError.visibility = View.VISIBLE
            } else {
                tvError.visibility = View.GONE
            }
        }

        val digitIds = listOf(
            R.id.btn0,
            R.id.btn1,
            R.id.btn2,
            R.id.btn3,
            R.id.btn4,
            R.id.btn5,
            R.id.btn6,
            R.id.btn7,
            R.id.btn8,
            R.id.btn9
        )
        for (id in digitIds) {
            findViewById<Button>(id).setOnClickListener { btn ->
                viewModel.onDigit((btn as Button).text.toString())
            }
        }

        findViewById<Button>(R.id.btnPlus).setOnClickListener { viewModel.onOperator("+") }
        findViewById<Button>(R.id.btnMinus).setOnClickListener { viewModel.onOperator("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { viewModel.onOperator("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { viewModel.onOperator("÷") }

        findViewById<Button>(R.id.btnDot).setOnClickListener { viewModel.onDot() }
        findViewById<Button>(R.id.btnEqual).setOnClickListener { viewModel.onEqual() }
        findViewById<Button>(R.id.btnC).setOnClickListener { viewModel.onClear() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { viewModel.onDelete() }
    }
}