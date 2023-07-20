package com.example.androidapp
import android.graphics.Bitmap
import android.graphics.Point
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ReceiptScanner {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private var diff = 0.0
    private var total = 0.0

    fun parseReceiptImage(imageBitmap: Bitmap?): Task<Text>? {
        val image = imageBitmap?.let { InputImage.fromBitmap(it, 0) }
        return image?.let {
            textRecognizer.process(it)
                .addOnSuccessListener { text ->
                    parseText(text, it.width)
                }
                .addOnFailureListener { e ->
                    Log.e("ReceiptParser", "Error parsing receipt image: ${e.message}", e)
                }
        }
    }

    private fun parseText(text: Text, width: Int) {
        val blocks = text.textBlocks

        val receiptLines = getSortedReceiptLineList(blocks)

        val receiptLinesSorted = extractReceiptBlocks(receiptLines, 10.0, width)

        printReceiptBlocks(receiptLinesSorted)

        Log.i("accuracy increase", ((diff / total) * 100).toString())

    }

    private fun getSortedReceiptLineList(blocks: List<Text.TextBlock>): List<Text.Element> {
        var lineList = mutableListOf<Text.Element>()

        for (block in blocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    lineList.add(element)
                }
            }
        }
        return lineList.sortedWith(compareBy({ it.cornerPoints?.get(0)!!.y }, { it.cornerPoints?.get(0)!!.x }));
    }

    fun sum(point : Point?) : Double {
        var value = 0.0
        if (point != null) {
            value = (point.x + point.y * (3)).toDouble();
            return value
        }
        return value
    }

    fun minus(point : Point?) : Double {
        var value = 0.0
        if (point != null) {
            value = (point.x - point.y).toDouble()
            return value
        }
        return value
    }

    fun updateAccuracyCheck(pointX : Double, pointY : Double, topLeftPointX : Double, topLeftPointY : Double, imgWidth : Double, interpolantDistance : Double) {
        val normal = pDistance(pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, topLeftPointY)
        diff += (normal - interpolantDistance)
        total += normal;
    }

    fun blockExtractInstrumentation(pointText : String, pointX : Double, pointY : Double, topLeftPointX : Double, topLeftPointY : Double, imgWidth : Double, interpolantDistance : Double) {
        Log.i("interpolate distance", interpolantDistance.toString())
        Log.i("non-distance", pDistance(pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, topLeftPointY).toString())
        Log.i("point", pointText)
        Log.i("point Point", "(${pointX}, ${pointY})")
    }

    fun extractReceiptBlock(points : MutableList<Text.Element>, block: MutableList<Text.Element>, imgWidth : Double, topLeftText : Text.Element, threshold : Double, interpolantOption : Int, isSecondPass : Boolean) : MutableList<Text.Element> {
        val remainingPoints= mutableListOf<Text.Element>()

        var topLeftPointX = block[0].cornerPoints?.get(0)!!.x.toDouble()
        var topLeftPointY = block[0].cornerPoints?.get(0)!!.y.toDouble()

        var imgWidthInterpolant = if (!isSecondPass) {
            val topRightPointX = block[0].cornerPoints?.get(1)!!.x.toDouble()
            val topRightPointY = block[0].cornerPoints?.get(1)!!.y.toDouble()
            val topLeftTextUpperPoints = listOf(Point(topLeftPointX.toInt(), topLeftPointY.toInt()), Point(topRightPointX.toInt(), topRightPointY.toInt()))

            lagrange_interpolate(topLeftTextUpperPoints, imgWidth, topLeftTextUpperPoints.size)

        } else {
            getInterpolantValueAtX(block, interpolantOption, imgWidth)
        }

        for (point in points) {
            val pointX = point.cornerPoints?.get(0)!!.x.toDouble()
            val pointY = point.cornerPoints?.get(0)!!.y.toDouble()
            val distance = pDistance(pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, imgWidthInterpolant)

            if (distance.toInt() <= threshold) {
                //thresh /= 1.2
                blockExtractInstrumentation(point.text, pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, distance)
                updateAccuracyCheck(pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, distance)
                block.add(point)

                val blockSize = block.size
                if (blockSize >= 2) {
                    block.sortWith(compareBy { it.cornerPoints?.get(0)!!.x })
                    imgWidthInterpolant = getInterpolantValueAtX(block, interpolantOption, imgWidth);
                    topLeftPointX = block[blockSize - 1].cornerPoints?.get(0)!!.x.toDouble()
                    topLeftPointY = block[blockSize - 1].cornerPoints?.get(0)!!.y.toDouble()
                }
            } else {
                remainingPoints.add(point)
            }
        }

        return remainingPoints
    }

    fun extractReceiptBlocks(points: List<Text.Element>, radius: Double, width : Int): List<List<Text.Element>> {
        val blocks = mutableListOf<List<Text.Element>>()
        var remainingPointsToGroup = points.toMutableList()

        while (remainingPointsToGroup.isNotEmpty()) {
            // Find top left point
            var block = mutableListOf<Text.Element>()
            val topLeftPoint = remainingPointsToGroup.minByOrNull {  sum(it.cornerPoints?.get(0)) }
            topLeftPoint?.let {topLeftText ->
                Log.i("topleft", topLeftText.text)
                Log.i("topLeft Point", "(${topLeftText.cornerPoints?.get(0)!!.x.toString()}, ${topLeftText.cornerPoints?.get(0)!!.y.toString()})")

                block.add(topLeftText)
                remainingPointsToGroup.remove(topLeftText)

                var threshold = radius;

                remainingPointsToGroup = extractReceiptBlock(remainingPointsToGroup, block, width.toDouble(), topLeftText, threshold, 1, false)

                if (block.size >= 2) {
                    remainingPointsToGroup = extractReceiptBlock(remainingPointsToGroup, block, width.toDouble(), topLeftText, threshold, 1, true)
                }

                block.sortWith(compareBy { it.cornerPoints?.get(0)!!.x })
                blocks.add(block)
            }
        }

        return blocks
    }

    fun getInterpolantValueAtX(block : List<Text.Element>, interpolantOption : Int, x : Double) : Double {
        val blockSize = block.size
        val leftMostPoint = block[0];
        val leftEndPoint = block[blockSize - 2]
        val rightEndPoint = block[blockSize - 1]

        when (interpolantOption) {
            1 -> {
                return linear_interpolate(leftMostPoint, rightEndPoint, x)
            }

            2 -> {
                return linear_interpolate(leftMostPoint, getAveragePoint(block), x)
            }

            3 -> {
                return linear_interpolate(leftEndPoint, rightEndPoint, x)
            }

            4 -> {
                return lagrange_interpolate_text(block, x, blockSize)
            }

            5 -> {
                val leftEndPointX = leftEndPoint.cornerPoints?.get(0)!!.x
                val leftEndPointY = leftEndPoint.cornerPoints?.get(0)!!.y
                val rightEndPointX = rightEndPoint.cornerPoints?.get(0)!!.x
                val rightEndPointY = rightEndPoint.cornerPoints?.get(0)!!.y
                val knownPoints = listOf(Point(leftEndPointX, leftEndPointY), Point(rightEndPointX, rightEndPointY))
                return lagrange_interpolate(knownPoints, x, knownPoints.size)
            }
        }

        return 0.0
    }
    fun pDistance(x : Double, y : Double, x1 : Double, y1 : Double, x2 : Double, y2 : Double) : Double {
        var A = x2 - x1;
        var B = y2 - y1;
        var C = y1 - y;
        var D = x1 - x;

        var AC = A * C;
        var DB = D * B;
        var diff = AC - DB;

        val AA = A * A;
        val BB = B * B;

        var squareSum = AA + BB;

        return Math.abs(diff) / Math.sqrt(squareSum)
    }


    /*fun pDistance(x : Double, y : Double, x1 : Double, y1 : Double, x2 : Double, y2 : Double) : Double {

        var A = x - x1;
        var B = y - y1;
        var C = x2 - x1;
        var D = y2 - y1;

        var dot = A * C + B * D;
        var len_sq = C * C + D * D;
        var param = -1.0;
        if (len_sq != 0.0) //in case of 0 length line
            param = dot / len_sq;

        var xx = 0.0
        var yy = 0.0

        if (param < 0) {
            xx = x1
            yy = y1
        }
        else if (param > 1) {
            xx = x2
            yy = y2
        }
        else {
            xx = (x1 + param * C)
            yy = (y1 + param * D)
        }

        var dx = x - xx;
        var dy = y - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }*/

    fun lagrange_interpolate(f: List<Point>, xi: Double, n: Int): Double {
        var result = 0.0 // Initialize result
        for (i in 0 until n) {
            // Compute individual terms of above formula
            var term: Double = f[i].y.toDouble()
            for (j in 0 until n) {
                val fjx = f[j].x
                val fix = f[i].x
                if (j != i) term *= ((xi - fjx) / (fix - fjx))
            }
            result += term
        }
        return result
    }


    fun lagrange_interpolate_text(f: List<Text.Element>, xi: Double, n: Int): Double {
        var result = 0.0 // Initialize result
        for (i in 0 until n) {
            // Compute individual terms of above formula
            var term: Double = f[i].cornerPoints?.get(0)!!.y.toDouble()
            for (j in 0 until n) {
                val fjx = f[j].cornerPoints?.get(0)!!.x.toDouble()
                val fix = f[i].cornerPoints?.get(0)!!.x.toDouble()
                if (j != i) term *= ((xi - fjx) / (fix - fjx))
            }

            // Add current term to result
            result += term
        }
        return result
    }

    fun linear_interpolate(a : Text.Element, b : Text.Element, xi : Double) : Double {

        val x1 = a.cornerPoints?.get(0)!!.x.toDouble()
        val y1 = a.cornerPoints?.get(0)!!.y.toDouble()
        val x2 = b.cornerPoints?.get(0)!!.x.toDouble()
        val y2 = b.cornerPoints?.get(0)!!.y.toDouble()

        return y1 + (((xi-x1) * (y2-y1)) / (x2-x1))

    }

    fun linear_interpolate(a : Text.Element, b : Point, xi : Double) : Double {

        val x1 = a.cornerPoints?.get(0)!!.x.toDouble()
        val y1 = a.cornerPoints?.get(0)!!.y.toDouble()
        val x2 = b.x.toDouble()
        val y2 = b.y.toDouble()

        return y1 + (((xi-x1) * (y2-y1)) / (x2-x1))

    }

    fun getAveragePoint(f: List<Text.Element>) : Point {
        val len = f.size
        var x = 0.0;
        var y = 0.0;

        f.forEach { text ->
            x += text.cornerPoints?.get(0)!!.x.toDouble()
            y += text.cornerPoints?.get(0)!!.y.toDouble()
        }

        x /= len;
        y /= len;

        return Point(x.toInt(), y.toInt())
    }
    fun googleGetReceiptBlocks(text : Text) : List<Text.TextBlock>  {
        var receiptLines = mutableListOf<Text.TextBlock>()
        val blocks = text.textBlocks
        for (block in blocks) {
            val blockText = block.text
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox
            receiptLines.add(block)
        }
        return receiptLines
    }


    /*fun printReceiptBlocks(blocks : List<Text.TextBlock>) {
        blocks.forEach {
            Log.i("block", it.text)
        }
    }*/

    fun printReceiptBlocks(blocks : List<List<Text.Element>>) {
        blocks.forEach {
            var text = ""
            it.forEach {
                text += it.text + " "
            }
            Log.i("line",text)
        }
    }
}