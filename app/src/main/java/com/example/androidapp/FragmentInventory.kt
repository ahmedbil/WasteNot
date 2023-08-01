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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.androidapp.databinding.FragmentInventoryBinding

private var _binding: FragmentInventoryBinding? = null

// This property is only valid between onCreateView and
// onDestroyView.
private val binding get() = _binding!!

class Adapter() : RecyclerView.Adapter<Adapter.MyViewHolder>() {

    private lateinit var viewHolder: MyViewHolder

    private val checkedItems = mutableListOf<Item>()

    private var checkboxVisibility = View.INVISIBLE

    val items = mutableListOf<Item>()
    // This method creates a new ViewHolder object for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the layout for each item and return a new ViewHolder object
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false)

        viewHolder = MyViewHolder(itemView)

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
        return items.size
    }

    // This method binds the data to the ViewHolder object
    // for each item in the RecyclerView
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.ingredient.text = items[position].getName()
        //holder.quantity.text = ingredient_list_quant[position]
        holder.checkBox.visibility = checkboxVisibility

        if (holder.checkBox.visibility == View.INVISIBLE) {
            holder.checkBox.isChecked = false
        }

    }

    fun submitList(newData: List<Item>) {
        checkedItems.clear()

        items.clear()

        items.addAll(newData)

        notifyDataSetChanged()
    }

    fun toggleCheckBoxVisiblity() {
        checkboxVisibility = if (checkboxVisibility == CheckBox.VISIBLE)
            CheckBox.INVISIBLE
        else
            CheckBox.VISIBLE

        checkedItems.clear()

        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Item> {
        return checkedItems
    }

    fun deleteItems(itemsToDelete: List<Item>) {
        itemsToDelete.forEach { item ->
            notifyItemRemoved(items.indexOfFirst { it.getId() == item.getId() })
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
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (isChecked) {
                        checkedItems.add(items[position])
                    } else {
                        checkedItems.remove(items[position])
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

    fun showPopup(itemSelected: String) {


        println("Show popup called")


        val popUpCardView = layoutInflater.inflate(R.layout.inventory_item_popup,
            null)


        popUpCardView.focusable = View.FOCUSABLE

        //popUpCardView.text

        //val popupTextView = popUpCardView.findViewById<TextView>(R.id.textView)




        val window = PopupWindow(popUpCardView, LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, true)

        window.showAtLocation(view, Gravity.CENTER, 0, 100)



//        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
//        builder.setTitle("More info about item")
//        builder.setMessage("Quantity: 300 g")
//
//        builder.setMessage("Purchase Date: January 1, 2019")
//        builder.setMessage("Estimated expiry date: March 15, 2019")
//
//
//
//        builder.setPositiveButton("Close",
//            DialogInterface.OnClickListener { dialog, which -> // Do something when the button is clicked
//                //retVal = 1
//                dialog.dismiss()
//            })



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var inventoryViewModel = ViewModelProvider(requireActivity())[InventoryViewModel::class.java]

        val itemAdapter = Adapter()

        val recyclerView: RecyclerView = view.findViewById(R.id.ingredient_list)

        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = itemAdapter

        recyclerView.addOnItemTouchListener(

            RecyclerTouchListener(
                requireContext(),
                recyclerView,
                object : RecyclerTouchListener.ClickListener {
                    override fun onClick(view: View, position: Int) {
                        println("ShowPopup called from onClick listener ")
                        showPopup("potatoes")
                        // Handle click event
                    }

                    override fun onLongClick(view: View, position: Int) {
                        // Handle long click event
                    }
                }
            )
        )

        binding.scanItems.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentReceiptScanner()).commit()
        }

        binding.addItem.setOnClickListener {
            showAddItemDialog()
        }

        binding.deleteItems.setOnClickListener {
            val selectedItems = itemAdapter.getSelectedItems()
            inventoryViewModel.deleteItems(selectedItems)
            //itemAdapter.deleteItems(selectedItems)
            itemAdapter.toggleCheckBoxVisiblity()
            itemAdapter.updateView()
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

        inventoryViewModel.fetchItems()

        inventoryViewModel.getItems().observe(viewLifecycleOwner, Observer { itemAdapter.submitList(it) })

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
