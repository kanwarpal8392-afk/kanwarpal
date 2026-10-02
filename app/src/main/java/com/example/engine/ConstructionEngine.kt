package com.example.engine

data class ConcreteMixResult(
    val wetVolumeM3: Double,
    val dryVolumeM3: Double,
    val cementBags50kg: Double,
    val sandVolumeM3: Double,
    val aggregateVolumeM3: Double,
    val totalVolumeWithWasteM3: Double
)

data class TileResult(
    val roomAreaM2: Double,
    val tileAreaM2: Double,
    val netTiles: Int,
    val totalTilesWithWaste: Int,
    val extraTiles: Int,
    val estimatedBoxes: Int
)

data class PaintResult(
    val totalWallAreaM2: Double,
    val netPaintableAreaM2: Double,
    val litresPerCoat: Double,
    val totalLitres: Double,
    val recommendedPurchaseLitres: Double
)

object ConstructionEngine {
    const val DISCLAIMER = "Construction quantities are estimates. Actual material requirements can vary based on site conditions, material specifications, workmanship and project design."

    fun calculateConcrete(
        lengthM: Double,
        widthM: Double,
        depthM: Double,
        cementParts: Double = 1.0,
        sandParts: Double = 2.0,
        aggregateParts: Double = 4.0,
        wastePercent: Double = 5.0
    ): ConcreteMixResult {
        val wetVol = lengthM * widthM * depthM
        val wetWithWaste = wetVol * (1.0 + (wastePercent / 100.0))
        val dryVol = wetWithWaste * 1.54 // standard dry factor

        val totalParts = cementParts + sandParts + aggregateParts
        val cementVol = (cementParts / totalParts) * dryVol
        // 1 m3 cement ≈ 1440 kg. 1 bag = 50 kg -> 1440 / 50 ≈ 28.8 bags per m3
        val cementBags = cementVol * 28.8
        val sandVol = (sandParts / totalParts) * dryVol
        val aggregateVol = (aggregateParts / totalParts) * dryVol

        return ConcreteMixResult(
            wetVolumeM3 = wetVol,
            dryVolumeM3 = dryVol,
            cementBags50kg = cementBags,
            sandVolumeM3 = sandVol,
            aggregateVolumeM3 = aggregateVol,
            totalVolumeWithWasteM3 = wetWithWaste
        )
    }

    fun calculateTiles(
        roomLengthM: Double,
        roomWidthM: Double,
        tileLengthCm: Double,
        tileWidthCm: Double,
        wastePercent: Double = 10.0,
        tilesPerBox: Int = 10
    ): TileResult {
        val roomArea = roomLengthM * roomWidthM
        val tileArea = (tileLengthCm / 100.0) * (tileWidthCm / 100.0)
        if (tileArea <= 0) return TileResult(0.0, 0.0, 0, 0, 0, 0)

        val netTiles = kotlin.math.ceil(roomArea / tileArea).toInt()
        val totalTiles = kotlin.math.ceil(netTiles * (1.0 + (wastePercent / 100.0))).toInt()
        val extra = totalTiles - netTiles
        val boxes = kotlin.math.ceil(totalTiles.toDouble() / tilesPerBox.toDouble()).toInt()

        return TileResult(
            roomAreaM2 = roomArea,
            tileAreaM2 = tileArea,
            netTiles = netTiles,
            totalTilesWithWaste = totalTiles,
            extraTiles = extra,
            estimatedBoxes = boxes
        )
    }

    fun calculatePaint(
        wallLengthM: Double,
        wallHeightM: Double,
        numberOfWalls: Int = 4,
        deductionAreaM2: Double = 3.5, // e.g. door and windows
        coveragePerLitreM2: Double = 10.0,
        coats: Int = 2
    ): PaintResult {
        val grossWallArea = wallLengthM * wallHeightM * numberOfWalls
        val netArea = (grossWallArea - deductionAreaM2).coerceAtLeast(0.0)
        val litresPerCoat = if (coveragePerLitreM2 > 0) netArea / coveragePerLitreM2 else 0.0
        val totalLitres = litresPerCoat * coats
        // 10% contingency buffer
        val recommendedLitres = kotlin.math.ceil(totalLitres * 1.1)

        return PaintResult(
            totalWallAreaM2 = grossWallArea,
            netPaintableAreaM2 = netArea,
            litresPerCoat = litresPerCoat,
            totalLitres = totalLitres,
            recommendedPurchaseLitres = recommendedLitres
        )
    }
}
