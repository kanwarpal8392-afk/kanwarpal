package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Calcora", appName)
    }

    @Test
    fun `test MathEngine evaluation`() {
        val res1 = MathEngine.evaluate("25 * (15 + 8)")
        assertEquals(575.0, res1, 1e-6)

        val res2 = MathEngine.evaluate("sqrt(144) + 35")
        assertEquals(47.0, res2, 1e-6)

        val resTrig = MathEngine.evaluate("sin(90)", "DEG")
        assertEquals(1.0, resTrig, 1e-6)
    }

    @Test
    fun `test UnitConversionEngine`() {
        // 1 m to cm
        val cm = UnitConversionEngine.convert(1.0, "length", "m", "cm")
        assertEquals(100.0, cm, 1e-6)

        // 1 km to m
        val m = UnitConversionEngine.convert(1.0, "length", "km", "m")
        assertEquals(1000.0, m, 1e-6)

        // 0 C to F
        val f = UnitConversionEngine.convert(0.0, "temperature", "c", "f")
        assertEquals(32.0, f, 1e-6)
    }

    @Test
    fun `test FinanceEngine EMI`() {
        // Loan of 100,000 at 12% for 1 year (12 months)
        val emi = FinanceEngine.calculateEmi(100000.0, 12.0, 12)
        assertTrue(emi.monthlyEmi > 8000 && emi.monthlyEmi < 9500)
        assertTrue(emi.totalPayment > 100000.0)
    }

    @Test
    fun `test HealthEngine BMI`() {
        val bmiRes = HealthEngine.calculateBmi(180.0, 75.0)
        assertEquals("Normal Weight", bmiRes.category)
        assertTrue(abs(bmiRes.bmi - 23.148) < 0.1)
    }

    @Test
    fun `test QuadraticSolver`() {
        // x^2 - 5x + 6 = 0 -> roots are 3 and 2
        val quad = StatisticsEquationEngine.solveQuadratic(1.0, -5.0, 6.0)
        assertEquals("Two distinct real roots", quad.natureOfRoots)
        assertTrue(quad.root1.contains("3") || quad.root2.contains("3"))
    }
}
