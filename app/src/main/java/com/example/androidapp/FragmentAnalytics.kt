package com.example.androidapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
class FragmentAnalytics : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pieChart: PieChart = view.findViewById(R.id.pieChart)
        setupPieChart(pieChart)

        val barChart: BarChart = view.findViewById(R.id.barChart)
        setupBarChart(barChart)
    }

    private fun setupPieChart(pieChart: PieChart) {
        // Sample data for the pie chart
        val pieEntries = listOf(
            PieEntry(25f, "Category 1"),
            PieEntry(30f, "Category 2"),
            PieEntry(15f, "Category 3"),
            PieEntry(10f, "Category 4"),
            PieEntry(20f, "Category 5")
        )

        // Create a PieDataSet with the data and label it
        val dataSet = PieDataSet(pieEntries, "Sample Pie Chart")

        // Set up colors for the pie chart segments
        dataSet.colors = mutableListOf(
            Color.BLUE,
            Color.GREEN,
            Color.RED,
            Color.YELLOW,
            Color.MAGENTA
        )

        // Create a PieData object from the dataSet
        val pieData = PieData(dataSet)

        // Customize the PieChart
        pieChart.apply {
            data = pieData
            description.isEnabled = false
            setUsePercentValues(true)
            isDrawHoleEnabled = true
            holeRadius = 40f
            transparentCircleRadius = 50f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            legend.isEnabled = true
            animateY(1000)
        }

        pieChart.invalidate()
    }

    private fun setupBarChart(barChart: BarChart) {
        // Sample data for the bar chart
        val barEntries = listOf(
            BarEntry(0f, 40f),
            BarEntry(1f, 60f),
            BarEntry(2f, 80f),
            BarEntry(3f, 50f),
            BarEntry(4f, 30f)
        )

        // Create a BarDataSet with the data and label it
        val dataSet = BarDataSet(barEntries, "Sample Bar Chart")

        // Set up colors for the bar chart bars
        dataSet.colors = mutableListOf(
            Color.CYAN,
            Color.MAGENTA,
            Color.YELLOW,
            Color.GREEN,
            Color.RED
        )

        // Create a BarData object from the dataSet
        val barData = BarData(dataSet)

        // Customize the BarChart
        barChart.apply {
            data = barData
            description.isEnabled = false
            setFitBars(true)
            animateY(1000)
            legend.isEnabled = false
            xAxis.setDrawGridLines(false)
            axisLeft.setDrawGridLines(false)
            axisRight.setDrawGridLines(false)
        }

        barChart.invalidate()
    }

}
