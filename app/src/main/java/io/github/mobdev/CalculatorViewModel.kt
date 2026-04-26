package io.github.mobdev

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.round

class CalculatorViewModel : ViewModel() {

    private val _displayText = MutableLiveData("0")
    val displayText: LiveData<String> = _displayText

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private var firstOperand: Double = 0.0
    private var currentOperator: String = ""
    private var isNewInput: Boolean = true

    fun onDigit(digit: String) {
        _error.value = null
        val currentText = _displayText.value ?: "0"

        if (isNewInput) {
            _displayText.value = digit
            isNewInput = false
        } else {
            when (currentText) {
                "0" -> {
                    if (digit != "0") _displayText.value = digit
                }

                "-0" -> {
                    if (digit != "0") _displayText.value = "-$digit"
                }

                else -> {
                    _displayText.value = currentText + digit
                }
            }
        }
    }

    fun onDot() {
        _error.value = null
        val currentText = _displayText.value ?: "0"

        if (isNewInput) {
            _displayText.value = "0."
            isNewInput = false
        } else if (!currentText.contains(".")) {
            if (currentText == "-") {
                _displayText.value = "-0."
            } else {
                _displayText.value = "$currentText."
            }
        }
    }

    fun onOperator(op: String) {
        _error.value = null

        if (isNewInput && op == "-") {
            _displayText.value = "-"
            isNewInput = false
            return
        }

        val currentVal = _displayText.value?.toDoubleOrNull() ?: return

        if (currentOperator.isNotEmpty() && !isNewInput) {
            calculateInternal(currentVal)
        } else {
            firstOperand = currentVal
        }

        currentOperator = op
        isNewInput = true
    }

    fun onEqual() {
        val currentVal = _displayText.value?.toDoubleOrNull() ?: return
        if (currentOperator.isNotEmpty()) {
            calculateInternal(currentVal)
            currentOperator = ""
            isNewInput = true
        }
    }

    fun onDelete() {
        _error.value = null
        if (isNewInput) return

        val currentText = _displayText.value ?: "0"
        if (currentText.length > 1) {
            _displayText.value = currentText.dropLast(1)
        } else {
            _displayText.value = "0"
            isNewInput = true
        }
    }

    fun onClear() {
        firstOperand = 0.0
        currentOperator = ""
        isNewInput = true
        _displayText.value = "0"
        _error.value = null
    }

    private fun calculateInternal(secondOperand: Double) {
        var result = 0.0
        when (currentOperator) {
            "+" -> result = firstOperand + secondOperand
            "-" -> result = firstOperand - secondOperand
            "×" -> result = firstOperand * secondOperand
            "÷" -> {
                if (secondOperand == 0.0) {
                    _error.value = "Ошибка: Деление на ноль"
                    return
                }
                result = firstOperand / secondOperand
            }
        }

        val roundedResult = round(result * 100000000.0) / 100000000.0
        firstOperand = roundedResult

        if (roundedResult % 1.0 == 0.0) {
            _displayText.value = roundedResult.toLong().toString()
        } else {
            _displayText.value = roundedResult.toString()
        }
    }
}