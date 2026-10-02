package com.example.engine

import kotlin.math.pow

data class EmiResult(
    val monthlyEmi: Double,
    val totalInterest: Double,
    val totalPayment: Double,
    val principalRatio: Float,
    val interestRatio: Float,
    val yearlySchedule: List<YearlyAmortization>
)

data class YearlyAmortization(
    val year: Int,
    val principalPaid: Double,
    val interestPaid: Double,
    val totalPaid: Double,
    val remainingBalance: Double
)

data class GstResult(
    val baseAmount: Double,
    val gstAmount: Double,
    val totalAmount: Double,
    val cgst: Double,
    val sgst: Double
)

object FinanceEngine {
    const val DISCLAIMER = "Calculations are mathematical estimates based on the values entered by the user. Actual rates, taxes, fees and financial outcomes may differ. This tool does not provide financial advice."

    fun calculateEmi(principal: Double, annualRatePercent: Double, tenureMonths: Int): EmiResult {
        if (principal <= 0 || tenureMonths <= 0) {
            return EmiResult(0.0, 0.0, 0.0, 0f, 0f, emptyList())
        }
        val monthlyRate = (annualRatePercent / 12.0) / 100.0
        val emi = if (monthlyRate == 0.0) {
            principal / tenureMonths
        } else {
            val factor = (1.0 + monthlyRate).pow(tenureMonths)
            (principal * monthlyRate * factor) / (factor - 1.0)
        }

        val totalPayment = emi * tenureMonths
        val totalInterest = totalPayment - principal

        // Generate yearly amortization
        var balance = principal
        val schedule = mutableListOf<YearlyAmortization>()
        var yearPrincipal = 0.0
        var yearInterest = 0.0
        val years = (tenureMonths + 11) / 12

        for (m in 1..tenureMonths) {
            val interestMonth = balance * monthlyRate
            val principalMonth = emi - interestMonth
            balance = (balance - principalMonth).coerceAtLeast(0.0)
            yearPrincipal += principalMonth
            yearInterest += interestMonth

            if (m % 12 == 0 || m == tenureMonths) {
                val yr = (m + 11) / 12
                schedule.add(
                    YearlyAmortization(
                        year = yr,
                        principalPaid = yearPrincipal,
                        interestPaid = yearInterest,
                        totalPaid = yearPrincipal + yearInterest,
                        remainingBalance = balance
                    )
                )
                yearPrincipal = 0.0
                yearInterest = 0.0
            }
        }

        val principalRatio = if (totalPayment > 0) (principal / totalPayment).toFloat() else 1f
        val interestRatio = if (totalPayment > 0) (totalInterest / totalPayment).toFloat() else 0f

        return EmiResult(
            monthlyEmi = emi,
            totalInterest = totalInterest,
            totalPayment = totalPayment,
            principalRatio = principalRatio,
            interestRatio = interestRatio,
            yearlySchedule = schedule
        )
    }

    fun calculateGst(amount: Double, ratePercent: Double, isInclusive: Boolean): GstResult {
        return if (isInclusive) {
            val base = amount / (1.0 + (ratePercent / 100.0))
            val gst = amount - base
            val halfGst = gst / 2.0
            GstResult(base, gst, amount, halfGst, halfGst)
        } else {
            val gst = amount * (ratePercent / 100.0)
            val total = amount + gst
            val halfGst = gst / 2.0
            GstResult(amount, gst, total, halfGst, halfGst)
        }
    }

    fun calculateCompoundInterest(
        principal: Double,
        ratePercent: Double,
        timeYears: Double,
        compoundingPerYear: Int = 1 // 1=annual, 4=quarterly, 12=monthly
    ): Pair<Double, Double> {
        val n = compoundingPerYear.coerceAtLeast(1)
        val rate = ratePercent / 100.0
        val totalAmount = principal * (1.0 + (rate / n)).pow(n * timeYears)
        val totalInterest = totalAmount - principal
        return totalAmount to totalInterest
    }

    fun calculateSimpleInterest(principal: Double, ratePercent: Double, timeYears: Double): Pair<Double, Double> {
        val interest = (principal * ratePercent * timeYears) / 100.0
        val total = principal + interest
        return total to interest
    }

    fun calculateDiscount(originalPrice: Double, discountPercent: Double, extraDiscountPercent: Double = 0.0): Triple<Double, Double, Double> {
        val firstCut = originalPrice * (discountPercent / 100.0)
        val afterFirst = originalPrice - firstCut
        val secondCut = afterFirst * (extraDiscountPercent / 100.0)
        val finalPrice = afterFirst - secondCut
        val totalSavings = originalPrice - finalPrice
        val effectivePercentage = if (originalPrice > 0) (totalSavings / originalPrice) * 100.0 else 0.0
        return Triple(finalPrice, totalSavings, effectivePercentage)
    }

    fun calculateProfitLoss(costPrice: Double, sellingPrice: Double): Triple<Double, Double, Boolean> {
        val diff = sellingPrice - costPrice
        val isProfit = diff >= 0
        val percent = if (costPrice > 0) (kotlin.math.abs(diff) / costPrice) * 100.0 else 0.0
        return Triple(kotlin.math.abs(diff), percent, isProfit)
    }

    fun calculateInflation(currentAmount: Double, inflationRate: Double, years: Double): Pair<Double, Double> {
        val futureAmount = currentAmount * (1.0 + (inflationRate / 100.0)).pow(years)
        val purchasingPowerLoss = ((futureAmount - currentAmount) / futureAmount) * 100.0
        return futureAmount to purchasingPowerLoss
    }
}
