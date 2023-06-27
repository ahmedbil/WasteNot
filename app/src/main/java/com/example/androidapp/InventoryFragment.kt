package com.example.androidapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapp.databinding.FragmentInventoryBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private var _binding: FragmentInventoryBinding? = null
val ingredientlist= mutableListOf<String>()

// This property is only valid between onCreateView and
// onDestroyView.
private val binding get() = _binding!!

class Adapter(private val ingrlist: MutableList<String>) : RecyclerView.Adapter<Adapter.MyViewHolder>() {

    private val checkedItems = mutableListOf<Int>()
    private var checkboxVisibility = View.GONE
    // This method creates a new ViewHolder object for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the layout for each item and return a new ViewHolder object
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        return MyViewHolder(itemView)
    }

    // This method returns the total
    // number of items in the data set
    override fun getItemCount(): Int {
        return ingrlist.size
    }

    // This method binds the data to the ViewHolder object
    // for each item in the RecyclerView
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.ingredient.text = ingrlist[position]

        holder.checkBox.visibility = checkboxVisibility
        /*binding.select.setOnClickListener {
            var nextState = if (holder.checkBox.isVisible) View.GONE else View.VISIBLE
            holder.checkBox.visibility = nextState
        }*/
    }

    fun setCheckboxVisibility(visibility: Int) {
        checkboxVisibility = visibility
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Int> {
        return checkedItems
    }

    fun deleteItems(itemsToDelete: List<Int>) {
        val sortedItems = itemsToDelete.sortedDescending()
        sortedItems.forEach { position ->
            print(position)
            ingrlist.removeAt(position)
            checkedItems.removeAt(0)
            notifyItemRemoved(position)
        }
    }


    // This class defines the ViewHolder object for each item in the RecyclerView
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredient: TextView = itemView.findViewById(R.id.ingredient)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)

        init {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (isChecked) {
                        checkedItems.add(position)
                    } else {
                        checkedItems.remove(position)
                    }
                }
            }
        }
    }
}

/**
 * A simple [Fragment] subclass.
 * Use the [InventoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class InventoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        ingredientlist.add("Beef")
        ingredientlist.add("Cilantro")
        ingredientlist.add("Rice Noodles")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment InventoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                InventoryFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // getting the ingredientlist
        // Assign ingredientlist to ItemAdapter
        val itemAdapter=Adapter(ingredientlist)
        // Set the LayoutManager that
        // this RecyclerView will use.
        val recyclerView:RecyclerView=view.findViewById(R.id.ingredient_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // adapter instance is set to the
        // recyclerview to inflate the items.
        recyclerView.adapter = itemAdapter

        binding.addi.setOnClickListener {
            var str = binding.addIngredient.text.toString()
            if(str.isNotEmpty()) {
                ingredientlist.add(str)
                itemAdapter.notifyDataSetChanged()
            }
        }

        binding.select.setOnClickListener {
            var nextState = if (binding.delete.isVisible) View.GONE else View.VISIBLE
            binding.delete.visibility = nextState
            itemAdapter.setCheckboxVisibility(nextState)
        }

        binding.delete.setOnClickListener{
            val selectedItems = itemAdapter.getSelectedItems()
            itemAdapter.deleteItems(selectedItems)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}