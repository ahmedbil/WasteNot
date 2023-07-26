package com.example.androidapp

import AddItemDialogFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.example.androidapp.databinding.FragmentInventoryBinding

private var _binding: FragmentInventoryBinding? = null
val ingredient_list = mutableListOf<String>("Beef", "Rice noodles", "Cilantro")
val ingredient_list_quant = mutableListOf<String>("250gm", "200gm", "10gm")

// This property is only valid between onCreateView and
// onDestroyView.
private val binding get() = _binding!!

class Adapter(private val ingredient_list: MutableList<String>, private val ingredient_list_quant: MutableList<String>) : RecyclerView.Adapter<Adapter.MyViewHolder>() {

    private val checkedItems = mutableListOf<Int>()
    private var checkboxVisibility = View.INVISIBLE
    // This method creates a new ViewHolder object for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the layout for each item and return a new ViewHolder object
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)
        val viewHolder = MyViewHolder(itemView)
        viewHolder.ingredient.setOnLongClickListener {
            toggleCheckBoxVisiblity()
            updateView()
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
        //holder.quantity.text = ingredient_list_quant[position]
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
            ingredient_list.removeAt(position)
            ingredient_list_quant.removeAt(position)
            notifyItemRemoved(position)
        }
        checkedItems.clear()
    }

    fun updateView() {
        binding.deleteItems.visibility = if (binding.deleteItems.isVisible) View.GONE else View.VISIBLE
        binding.editItems.visibility = if (binding.editItems.isVisible) View.GONE else View.VISIBLE
        binding.scanItems.isClickable = !binding.scanItems.isClickable
        binding.scanItems.alpha = if (binding.scanItems.isClickable) 1F else 0.2F
        binding.addItem.isClickable = !binding.addItem.isClickable
        binding.addItem.alpha = if (binding.addItem.isClickable) 1F else 0.2F
    }


    // This class defines the ViewHolder object for each item in the RecyclerView
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredient: TextView = itemView.findViewById(R.id.ingredient)
        val checkBox: CheckBox = itemView.findViewById(R.id.check)
        //val quantity: TextView = itemView.findViewById(R.id.quantity)

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
        val itemAdapter = Adapter(ingredient_list, ingredient_list_quant)
        // Set the LayoutManager that
        // this RecyclerView will use.
        val recyclerView: RecyclerView = view.findViewById(R.id.ingredient_list)
        recyclerView.layoutManager = LinearLayoutManager(context)
        // adapter instance is set to the
        // recyclerview to inflate the items.
        recyclerView.adapter = itemAdapter


        binding.scanItems.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentReceiptScanner()).commit()
        }


        binding.addItem.setOnClickListener {
            showAddItemDialog()
        }

        binding.deleteItems.setOnClickListener {
            val selectedItems = itemAdapter.getSelectedItems()
            itemAdapter.deleteItems(selectedItems)
            itemAdapter.toggleCheckBoxVisiblity()
            binding.deleteItems.visibility = View.GONE
        }

        binding.editItems.setOnClickListener{
            val selectedItems = itemAdapter.getSelectedItems()
            val editModeFragment = EditModeFragment.newInstance(true,
                selectedItems as ArrayList<Int>
            )
            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, editModeFragment)
                .addToBackStack(null) // Optional: Add to the back stack if you want to navigate back
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showAddItemDialog() {
        val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
        val dialogFragment = AddItemDialogFragment()

        dialogFragment.dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogFragment.show(fragmentManager, "AddItemDialog")
    }
}
