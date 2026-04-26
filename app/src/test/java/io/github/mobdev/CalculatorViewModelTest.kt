package io.github.mobdev

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CalculatorViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun addition_isCorrect() {
        val viewModel = CalculatorViewModel()

        viewModel.onDigit("2.2")
        viewModel.onOperator("+")
        viewModel.onDigit("2")
        viewModel.onEqual()

        assertEquals("4.2", viewModel.displayText.value)
    }

    @Test
    fun subtraction_isCorrect() {
        val viewModel = CalculatorViewModel()

        viewModel.onDigit("5")
        viewModel.onOperator("-")
        viewModel.onDigit("7.9")
        viewModel.onEqual()

        assertEquals("-2.9", viewModel.displayText.value)
    }

    @Test
    fun multiplication_isCorrect() {
        val viewModel = CalculatorViewModel()

        viewModel.onDigit("4")
        viewModel.onOperator("×")
        viewModel.onDigit("3.4")
        viewModel.onEqual()

        assertEquals("13.6", viewModel.displayText.value)
    }

    @Test
    fun division_isCorrect() {
        val viewModel = CalculatorViewModel()

        viewModel.onDigit("9")
        viewModel.onOperator("÷")
        viewModel.onDigit("2")
        viewModel.onEqual()

        assertEquals("4.5", viewModel.displayText.value)
    }

    @Test
    fun divisionByZero_showsError() {
        val viewModel = CalculatorViewModel()

        viewModel.onDigit("5")
        viewModel.onOperator("÷")
        viewModel.onDigit("0")
        viewModel.onEqual()

        assertEquals("Ошибка: Деление на ноль", viewModel.error.value)
    }

    @Test
    fun delete_isCorrect() {
        val viewModel = CalculatorViewModel()

        viewModel.onDigit("123")
        viewModel.onDelete()
        assertEquals("12", viewModel.displayText.value)

        viewModel.onDelete()
        viewModel.onDelete()
        assertEquals("0", viewModel.displayText.value)
    }

    @Test
    fun clear_isCorrect() {
        val viewModel = CalculatorViewModel()

        viewModel.onDigit("5")
        viewModel.onClear()

        assertEquals("0", viewModel.displayText.value)
    }

    @Test
    fun chainedOperations_isCorrect() {
        val viewModel = CalculatorViewModel()

        viewModel.onDigit("10")
        viewModel.onOperator("×")
        viewModel.onDigit("2")
        viewModel.onOperator("+")
        viewModel.onDigit("5")
        viewModel.onEqual()

        assertEquals("25", viewModel.displayText.value)
    }

    @Test
    fun negativeNumber_firstOperand_isCorrect() {
        val viewModel = CalculatorViewModel()

        viewModel.onOperator("-")
        viewModel.onDigit("5")
        viewModel.onOperator("+")
        viewModel.onDigit("3")
        viewModel.onEqual()

        assertEquals("-2", viewModel.displayText.value)
    }

    @Test
    fun negativeNumber_secondOperand_isCorrect() {
        val viewModel = CalculatorViewModel()

        viewModel.onDigit("5")
        viewModel.onOperator("×")
        viewModel.onOperator("-")
        viewModel.onDigit("3")
        viewModel.onEqual()

        assertEquals("-15", viewModel.displayText.value)
    }
}