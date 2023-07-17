package com.example.androidapp
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class ReceiptScanner {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

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
        //var stopParsing = false;

        //Log.i("width", width.toString())

        val blocks = text.textBlocks


        val receiptLines = getSortedReceiptLineList(blocks)

        //receiptLinesSorted.forEach{ Log.i(it.text,"x: ${it.boundingBox?.exactCenterX()}, y: ${it.boundingBox?.exactCenterY()}") }

        val receiptLinesSorted = findTopLine(receiptLines, 10.0, width)

        receiptLinesSorted.forEach {
            var text = ""
            it.forEach {
                text += it.text + " "
            }
            Log.i("line",text)
        }

        /*for (block in blocks) {
            val blockText = block.text
            //val blockConfidence = block.
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox

            for (line in block.lines) {
                val lineText = line.text
                val lineConfidence = line.confidence
                val lineCornerPoints = line.cornerPoints
                val lineFrame = line.boundingBox
                Log.i("line",lineText)

                for (element in line.elements) {
                    val elementText = element.text
                    //val elementConfidence = element.confidence
                    val elementCornerPoints = element.cornerPoints
                    val elementFrame = element.boundingBox
                    //Log.i("line", element.)
                }
            }
        }*/
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
        return lineList.sortedWith(compareBy({ it.boundingBox?.exactCenterY() }, { it.boundingBox?.exactCenterX() }));
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

    fun crossProduct(u: DoubleArray, v: DoubleArray): DoubleArray {
        return doubleArrayOf(u[0] * v[1] - u[1] * v[0]);
    }

    fun DoubleArray.subtract(other: DoubleArray): DoubleArray {
        return DoubleArray(size) { index -> this[index] - other[index] }
    }

    fun DoubleArray.norm(): Double {
        var sum = 0.0
        for (value in this) {
            sum += value.pow(2)
        }
        return sqrt(sum)
    }

    fun findTopLine(points: List<Text.Element>, radius: Double, width : Int): List<List<Text.Element>> {
        val topLinePoints = mutableListOf<List<Text.Element>>()
        var remainingPoints = points.toMutableList()

        while (remainingPoints.isNotEmpty()) {
            // Find top left point
            var pointList = mutableListOf<Text.Element>()
            val topLeftPoint = remainingPoints.minByOrNull {  sum(it.cornerPoints?.get(0)) }
            topLeftPoint?.let {topLeft ->
                // Find top right point
                val topRightPoint = remainingPoints.maxByOrNull { minus(it.cornerPoints?.get(0)) }
                topRightPoint?.let { topRight ->
                    //topLinePoints.add(it)

                    //Log.i("topleft", topLeft.cornerPoints?.get(0).toString())
                    //Log.i("topRight", topRight.cornerPoints?.get(0).toString())

                    val topLeftX = topLeft.cornerPoints?.get(0)!!.x.toDouble()
                    val topLeftY = topLeft.cornerPoints?.get(0)!!.y.toDouble()
                    val topRightX = topRight.cornerPoints?.get(0)!!.x.toDouble()
                    val topRightY = topRight.cornerPoints?.get(0)!!.y.toDouble()

                    val a = doubleArrayOf(topLeftX, topLeftY)
                    val b = doubleArrayOf(topRightX, topLeftY)

                    // Check if other points are in the top line formed by a and b
                    val remainingPointsToSearch = mutableListOf<Text.Element>()
                    for (point in remainingPoints) {
                        val pX = point.cornerPoints?.get(0)!!.x.toDouble()
                        val pY = point.cornerPoints?.get(0)!!.y.toDouble()
                        val p = doubleArrayOf(pX, pY)
                        val distance = pDistance(pX, pY, topLeftX, topLeftY, width.toDouble(), topLeftY)
                        if (distance <= radius) {
                            pointList.add(point)
                        } else {
                            remainingPointsToSearch.add(point)
                        }
                    }
                    pointList = pointList.sortedWith(compareBy { it.cornerPoints?.get(0)!!.x }).toMutableList()

                    var text = "";
                    for (element in pointList) {
                        text += element.cornerPoints?.get(0)!!.x.toString() + " "
                    }
                    //Log.i("list", text)
                    topLinePoints.add(pointList)
                    remainingPoints = remainingPointsToSearch
                }
            }
        }

        return topLinePoints
    }


    fun pDistance(x : Double, y : Double, x1 : Double, y1 : Double, x2 : Double, y2 : Double) : Double {

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
    }




}