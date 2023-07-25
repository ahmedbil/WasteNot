package com.example.androidapp
import android.graphics.Bitmap
import android.graphics.Point
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.core.text.isDigitsOnly
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.*

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

    //@ExperimentalGetImage
    fun parseReceiptMediaImage(imageProxy: ImageProxy?): Task<Text>? {
        val image = imageProxy?.let { InputImage.fromMediaImage(it.image!!, imageProxy.imageInfo.rotationDegrees) }
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

        val cleanReceiptLinesSorted = cleanedReceiptLines(receiptLinesSorted)

        printReceiptBlocks(cleanReceiptLinesSorted)

        Log.i("accuracy increase", ((diff / total) * 100).toString())

    }

    fun satisfiesRule(word : String) : Boolean {
        var isSatisfied = true;

        val fourConsecCharPattern = Regex("(\\w)\\1{3}")

        val size = word.length

        if (size > 40) {
            isSatisfied = false;
        }

        if (word.isDigitsOnly() && (size > 3)) {
            isSatisfied = false;
        }

        if (!word.isDigitsOnly() && (size < 3)) {
            isSatisfied = false;
        }

        if (fourConsecCharPattern.containsMatchIn(word)) {
            isSatisfied = false;
        }

        if (word.contains("*")) {
            isSatisfied = false;
        }

        val alphaNumericWord = word.filter { it.isLetterOrDigit() }
        val alphaNumericDigits = alphaNumericWord.filter { it.isDigit()}

        if (alphaNumericDigits.length > 5) {
            isSatisfied = false;
        }

        val lettersCount = alphaNumericWord.count { it.isLetter() }
        val numbersCount = alphaNumericWord.count { it.isDigit() }
        var ratio = 0.0
        if (lettersCount > 0) {
            ratio = (numbersCount / lettersCount).toDouble()
        }

        if (ratio > 0.5) {
            isSatisfied = false;
        }

        return isSatisfied
    }

    fun removeReceiptMetaData(receipt : List<List<Text.Element>>) : List<List<Text.Element>> {
        val indexToRemoveMetaData = mutableListOf<Int>()

        var blocks = receipt

        val constantWords = listOf<String>("TOTAL", "SUBTOTAL", "TAX")

        var index = 0;
        blocks.forEach { block ->
            for (word in block) {
                if (constantWords.any { word.text.contains(it) }) {
                    indexToRemoveMetaData.add(index)
                }
            }

            index += 1;
        }

        if (indexToRemoveMetaData.isNotEmpty()) {
            val minRemoveIndex = indexToRemoveMetaData.min()
            blocks = blocks.slice(0 until minRemoveIndex)
        }

        return blocks
    }

    private fun cleanedReceiptLines(receipt : List<List<Text.Element>>) : List<List<Text.Element>> {

        var blocks = mutableListOf<List<Text.Element>>()

        val receiptWithoutMetaData = removeReceiptMetaData(receipt)

        receiptWithoutMetaData.forEach {block ->
            var cleanBlock = mutableListOf<Text.Element>()

            for (word in block) {
                if (satisfiesRule(word.text)) {
                    cleanBlock.add(word)
                }
            }

            if (cleanBlock.isNotEmpty()) {
                blocks.add(cleanBlock)
            }
        }

        return blocks;
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

            if (point.text == "2.29") {
                blockExtractInstrumentation(point.text, pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, distance)
            }

            if (distance.toInt() <= threshold) {
                //thresh /= 1.2
                //Log.i("confidence", point.confidence.toString())
                //blockExtractInstrumentation(point.text, pointX, pointY, topLeftPointX, topLeftPointY, imgWidth, distance)
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
                    remainingPointsToGroup = extractReceiptBlock(remainingPointsToGroup, block, width.toDouble(), topLeftText, threshold, 2, true)
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


    fun printOriginalReceiptBlocks(blocks : List<Text.TextBlock>) {
        blocks.forEach {
            Log.i("block", it.text)
        }
    }

    fun printReceiptBlocks(blocks : List<List<Text.Element>>) {
        blocks.forEach {
            var text = ""
            it.forEach {
                text += it.text + " "
            }
            Log.i("line",text)

            //val regexPattern ="(r\"\\b[A-Z.]+ [A-Z./]+ [A-Z.]+\\b|\\b[A-Z.]+ [0-9,]* [A-Z.%]*\\b|\\b[A-Z.!]+ [A-Z.]+\\b|\\b[A-Z]+\\b\")"

            //val regexPattern = "(r\"\\d{1,2},\\d{2}[ ]\")"

            //val inputText = "Your input text goes here."

            //val regex = Regex(regexPattern)
            //val matches = regex.findAll(text).map { it.value }.toList()
            //Log.i("match", matches.toString())
            //matches.forEach {
                //Log.i("match", it.toString())
            //}
        }
    }

    fun getTuples(text: String): List<Pair<Double, String>> {
        var products = mutableListOf<Pair<Double, String>>()
        var produs_crt = 0
        var pret_crt = 0
        var nume_crt = 0
        var started =
            false  // all the text from the upper part of the receipt is unnecessary and it should be skipped
        val lines = text.split("\n")
        Log.i("lines", lines.toString())
        for (line in lines) {
            if ("total" == line.toLowerCase(Locale.getDefault())
                || "*" in line.toLowerCase(Locale.getDefault())
            ) {
                break
            }

            if ("x " in line.toLowerCase(Locale.getDefault())) {
                val trimmedLine = line.drop(line.toLowerCase(Locale.getDefault()).indexOf("x ") + 2)
                if (trimmedLine.isEmpty()) {
                    continue
                }
                val words = trimmedLine.split(' ')
                if (words.isNotEmpty()) {
                    if (words[0].toDoubleOrNull() != null) {
                        val nr = words[0].toDouble()
                        if (pret_crt == produs_crt) {
                            products.add(Pair(nr, ""))
                            produs_crt += 1
                            pret_crt += 1
                        } else {
                            products[pret_crt] = Pair(nr, products[pret_crt].second)
                            pret_crt += 1
                        }
                        started = true
                    }
                }
            } else if (started
                && !line[0].isDigit()
                && "discount" !in line.toLowerCase(Locale.getDefault())
                && "total" !in line.toLowerCase(Locale.getDefault())
                && "lei" != line.toLowerCase(Locale.getDefault())
                && "lel" != line.toLowerCase(Locale.getDefault())
            ) {  // the text recognizer might confuse i for l

                if (nume_crt == produs_crt) {
                    products.add(Pair(0.toDouble(), line))
                    produs_crt += 1
                    nume_crt += 1
                } else {
                    products[nume_crt] = Pair(products[nume_crt].first, line)
                    nume_crt += 1
                }
            }
        }
        return products
    }
}