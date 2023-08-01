package com.example.androidapp

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapp.databinding.FragmentCalendarBinding
import com.example.androidapp.databinding.FragmentInventoryBinding
import android.widget.CalendarView.OnDateChangeListener


class FragmentCalendar: Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    val expiring_list = mutableListOf<String>("Tomatoes", "Onions", "Cilantro")

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val title = binding.root.findViewById<TextView>(R.id.titleText)
        val expiringList = binding.root.findViewById<TextView>(R.id.expiringItems);
        val calendar = binding.root.findViewById<CalendarView>(R.id.calendar);

        calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->

            val curDate = (dayOfMonth.toString() + "-" + (month + 1) + "-" + year)

            title.text = curDate

            expiringList.text = "The ingredients expiring on this day are \n" +
                    "Tomatoes, onions, and cilantro "

        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}