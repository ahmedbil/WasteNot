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

private var _binding: FragmentInventoryBinding? = null
val ingredient_list = mutableListOf<String>("Beef", "Rice noodles", "Quinoa", "Carrot")

// This property is only valid between onCreateView and
// onDestroyView.
private val binding get() = _binding!!

class Adapter(private val ingredient_list: MutableList<String>) : RecyclerView.Adapter<Adapter.MyViewHolder>() {

    private val checkedItems = mutableListOf<Int>()
    private var checkboxVisibility = View.INVISIBLE
    // This method creates a new ViewHolder object for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the layout for each item and return a new ViewHolder object
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        val viewHolder = MyViewHolder(itemView)
        viewHolder.ingredient.setOnLongClickListener {
            toggleCheckBoxVisiblity()
            binding.delete.visibility = if (binding.delete.isVisible) View.GONE else View.VISIBLE
            return@setOnLongClickListener true
        }
        return viewHolder
    }

    // This method returns the total
    // number of items in the data set
    override fun getItemCount(): Int {
        return ingredient_list.size
    }

    // This method binds the data to the ViewHolder object
    // for each item in the RecyclerView
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.ingredient.text = ingredient_list[position]
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
            print(position)
            ingredient_list.removeAt(position)
            notifyItemRemoved(position)
        }
        checkedItems.clear()
    }


    // This class defines the ViewHolder object for each item in the RecyclerView
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredient: TextView = itemView.findViewById(R.id.ingredient)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox)

        init {
            ingredient.setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }
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

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentInventory.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentInventory : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // getting the ingredientlist
        // Assign ingredientlist to ItemAdapter
        val itemAdapter = Adapter(ingredient_list)
        // Set the LayoutManager that
        // this RecyclerView will use.
        val recyclerView: RecyclerView = view.findViewById(R.id.ingredient_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // adapter instance is set to the
        // recyclerview to inflate the items.
        recyclerView.adapter = itemAdapter

        binding.ingredientNameField.setEndIconOnClickListener {
            val str = binding.ingredientNameField.editText?.text.toString()
            if(str.isNotEmpty()) {
                ingredient_list.add(str)
                binding.ingredientNameField.editText?.text?.clear()
                itemAdapter.notifyDataSetChanged()
            }
        }

        binding.delete.setOnClickListener {
            val selectedItems = itemAdapter.getSelectedItems()
            itemAdapter.deleteItems(selectedItems)
            itemAdapter.toggleCheckBoxVisiblity()
            binding.delete.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
