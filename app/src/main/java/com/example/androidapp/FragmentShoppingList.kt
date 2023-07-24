package com.example.androidapp

import AddItemDialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.androidapp.databinding.FragmentShoppinglistBinding

// ADD LOGIC TO ADD QUANTITY WHILE ADDING

private var _binding: FragmentShoppinglistBinding? = null
val shopping_list = mutableListOf<String>("Beef", "Rice noodles", "Cilantro")
val shopping_list_quant = mutableListOf<String>("250gm", "200gm", "10gm")

// This property is only valid between onCreateView and
// onDestroyView.
private val binding get() = _binding!!

class AdapterShopping(private val shopping_list: MutableList<String>, private val shopping_list_quant: MutableList<String>) : RecyclerView.Adapter<AdapterShopping.MyViewHolder>() {

    private val checkedItems = mutableListOf<Int>()
    private var checkboxVisibility = View.INVISIBLE
    // This method creates a new ViewHolder object for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the layout for each item and return a new ViewHolder object
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_shopping, parent, false)
        return MyViewHolder(itemView)
    }

    // This method returns the total
    // number of items in the data set
    override fun getItemCount(): Int {
        return shopping_list.size
    }

    // This method binds the data to the ViewHolder object
    // for each item in the RecyclerView
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.ingredient.text = shopping_list[position]
        holder.quantity.text = shopping_list_quant[position]
        holder.checkBox.visibility = checkboxVisibility
    }

    fun toggleCheckBoxVisiblity() {
        if (checkboxVisibility == CheckBox.VISIBLE)
            checkboxVisibility = CheckBox.INVISIBLE
        else
            checkboxVisibility = CheckBox.VISIBLE
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Int> {
        return checkedItems
    }

    fun deleteItems(itemsToDelete: List<Int>) {
        val sortedItems = itemsToDelete.sortedDescending()
        sortedItems.forEach { position ->
            shopping_list.removeAt(position)
            shopping_list_quant.removeAt(position)
            notifyItemRemoved(position)
        }
        checkedItems.clear()
    }


    // This class defines the ViewHolder object for each item in the RecyclerView
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredient: TextView = itemView.findViewById(R.id.ingredient)
        val checkBox: CheckBox = itemView.findViewById(R.id.check)
        val quantity: TextView = itemView.findViewById(R.id.quantity)

        init {
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
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

class FragmentShoppingList : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShoppinglistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // getting the ingredientlist
        // Assign ingredientlist to ItemAdapter
        val itemAdapter = AdapterShopping(shopping_list, shopping_list_quant)
        // Set the LayoutManager that
        // this RecyclerView will use.
        val recyclerView: RecyclerView = view.findViewById(R.id.shopping_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // adapter instance is set to the
        // recyclerview to inflate the items.
        recyclerView.adapter = itemAdapter

        binding.add.setOnClickListener {
            showAddItemDialog()
            shopping_list.add("str")
            shopping_list_quant.add("100gm")
            itemAdapter.notifyDataSetChanged()
        }


        binding.select.setOnClickListener {
            var nextState = if (binding.delete.isVisible) View.GONE else View.VISIBLE
            binding.delete.visibility = nextState
            binding.addInv.visibility = nextState
            itemAdapter.toggleCheckBoxVisiblity()
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

    private fun showAddItemDialog() {
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val dialogFragment = AddItemDialogFragment()
        dialogFragment.show(fragmentManager, "AddItemDialog")
    }

}
