package com.example.androidapp
import android.graphics.Bitmap
import android.graphics.Point
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
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
                //Log.i("line",lineText)

                for (element in line.elements) {
                    val elementText = element.text
                    //val elementConfidence = element.confidence
                    val elementCornerPoints = element.cornerPoints
                    val elementFrame = element.boundingBox
                    //Log.i("line", element.angle.toString())

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
        Log.i("width", width.toString())
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

                    Log.i("topleft", topLeft.text)
                    Log.i("topLeft Point", "(${topLeft.cornerPoints?.get(0)!!.x.toString()}, ${topLeft.cornerPoints?.get(0)!!.y.toString()})")

                    var topLeftX = topLeft.cornerPoints?.get(0)!!.x.toDouble()
                    var topLeftY = topLeft.cornerPoints?.get(0)!!.y.toDouble()
                    val topRightX = topRight.cornerPoints?.get(0)!!.x.toDouble()
                    val topRightY = topRight.cornerPoints?.get(0)!!.y.toDouble()

                    var trX = topLeft.cornerPoints?.get(1)!!.x.toDouble()

                    var trY = topLeft.cornerPoints?.get(1)!!.y.toDouble()

                    var interpolant = (((width - trX) / (topLeftX - trX)) * (topLeftY)) + (((width - topLeftX) / (trX - topLeftX)) * (trY))

                    //interpolant = topLeftY
                    Log.i("interpolate", interpolant.toString())

                    val a = doubleArrayOf(topLeftX, topLeftY)
                    val b = doubleArrayOf(topRightX, topLeftY)

                    pointList.add(topLeft)
                    remainingPoints.remove(topLeft)

                    // Check if other points are in the top line formed by a and b
                    val remainingPointsToSearch = mutableListOf<Text.Element>()
                    for (point in remainingPoints) {
                        val pX = point.cornerPoints?.get(0)!!.x.toDouble()
                        val pY = point.cornerPoints?.get(0)!!.y.toDouble()
                        val p = doubleArrayOf(pX, pY)
                        val distance = pDistance(pX, pY, topLeftX, topLeftY, width.toDouble(), interpolant)
                        if (point.text == "23.99") {
                            Log.i("point", point.text)
                            Log.i("point Point", "(${point.cornerPoints?.get(0)!!.x.toString()}, ${point.cornerPoints?.get(0)!!.y.toString()})")
                            //pointList.add(point)
                        }
                        if (distance <= radius) {
                            Log.i("point", point.text)
                            Log.i("point Point", "(${point.cornerPoints?.get(0)!!.x.toString()}, ${point.cornerPoints?.get(0)!!.y.toString()})")
                            pointList.add(point)
                            if (pointList.size >= 2) {
                                pointList = pointList.sortedWith(compareBy { it.cornerPoints?.get(0)!!.x }).toMutableList()
                                //val left = pointList[pointList.size - 2]
                                //val right = pointList[pointList.size - 1]
                                //Log.i("left", left.text)
                                //Log.i("right", right.text)
                                //topLeftX = left.cornerPoints?.get(0)!!.x.toDouble()
                                //topLeftY = left.cornerPoints?.get(0)!!.y.toDouble()
                                //trX = right.cornerPoints?.get(0)!!.x.toDouble()
                                //trY = right.cornerPoints?.get(0)!!.y.toDouble()
                                //interpolant = linear_interpolate(pointList[0], pointList[pointList.size - 1], width)
                                interpolant = linear_interpolate(pointList[pointList.size-2], pointList[pointList.size-1], width)
                                Log.i("interpolate", interpolant.toString())
                                //interpolant = (((width - trX) / (topLeftX - trX)) * (topLeftY)) + (((width - topLeftX) / (trX - topLeftX)) * (trY))
                            }
                        } else {
                            remainingPointsToSearch.add(point)
                        }
                    }

                    val remainingPointsToSearch2 = mutableListOf<Text.Element>()

                    for (point in remainingPointsToSearch) {
                        val pX = point.cornerPoints?.get(0)!!.x.toDouble()
                        val pY = point.cornerPoints?.get(0)!!.y.toDouble()
                        val p = doubleArrayOf(pX, pY)
                        val distance = pDistance(pX, pY, topLeftX, topLeftY, width.toDouble(), interpolant)
                        if (point.text == "23.99") {
                            Log.i("point", point.text)
                            Log.i("point Point", "(${point.cornerPoints?.get(0)!!.x.toString()}, ${point.cornerPoints?.get(0)!!.y.toString()})")
                            //pointList.add(point)
                        }
                        if (distance <= radius) {
                            Log.i("point", point.text)
                            Log.i("point Point", "(${point.cornerPoints?.get(0)!!.x.toString()}, ${point.cornerPoints?.get(0)!!.y.toString()})")
                            pointList.add(point)
                            if (pointList.size >= 2) {
                                pointList = pointList.sortedWith(compareBy { it.cornerPoints?.get(0)!!.x }).toMutableList()
                                //val left = pointList[pointList.size - 2]
                                //val right = pointList[pointList.size - 1]
                                //Log.i("left", left.text)
                                //Log.i("right", right.text)
                                //topLeftX = left.cornerPoints?.get(0)!!.x.toDouble()
                                //topLeftY = left.cornerPoints?.get(0)!!.y.toDouble()
                                //trX = right.cornerPoints?.get(0)!!.x.toDouble()
                                //trY = right.cornerPoints?.get(0)!!.y.toDouble()
                                //interpolant = linear_interpolate(pointList[0], pointList[pointList.size - 1], width)
                                interpolant = linear_interpolate(pointList[0], getAveragePoint(pointList), width)
                                Log.i("interpolate", interpolant.toString())
                                //interpolant = (((width - trX) / (topLeftX - trX)) * (topLeftY)) + (((width - topLeftX) / (trX - topLeftX)) * (trY))
                            }
                        } else {
                            remainingPointsToSearch2.add(point)
                        }
                    }

                    pointList = pointList.sortedWith(compareBy { it.cornerPoints?.get(0)!!.x }).toMutableList()

                    var text = "";
                    for (element in pointList) {
                        text += element.cornerPoints?.get(0)!!.x.toString() + " "
                    }
                    //Log.i("list", text)
                    topLinePoints.add(pointList)
                    remainingPoints = remainingPointsToSearch2
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


    fun interpolate(f: List<Text.Element>, xi: Int, n: Int): Double {
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

    fun linear_interpolate(a : Text.Element, b : Text.Element, xi : Int) : Double {

        val x1 = a.cornerPoints?.get(0)!!.x.toDouble()
        val y1 = a.cornerPoints?.get(0)!!.y.toDouble()
        val x2 = b.cornerPoints?.get(0)!!.x.toDouble()
        val y2 = b.cornerPoints?.get(0)!!.y.toDouble()

        return y1 + (((xi-x1) * (y2-y1)) / (x2-x1))

    }

    fun linear_interpolate(a : Text.Element, b : Point, xi : Int) : Double {

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




}