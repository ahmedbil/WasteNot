package com.example.androidapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton



class ReceiptItemsAdapter(private val items: MutableList<Pair<String, Pair<Double, String>>>) : RecyclerView.Adapter<ReceiptItemsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientNameEditText: EditText = itemView.findViewById(R.id.ingredientNameEditText)
        val ingredientQuantityEditText: EditText = itemView.findViewById(R.id.ingredientQuantityEditText)
        val ingredientUnitEditText: EditText = itemView.findViewById(R.id.ingredientUnitEditText)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        init {
            deleteButton.setOnClickListener {
                items.removeAt(absoluteAdapterPosition);
                notifyItemRemoved(absoluteAdapterPosition);
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.ingredientNameEditText.setText(item.first)
        holder.ingredientQuantityEditText.setText(""+item.second.first)
        holder.ingredientUnitEditText.setText(item.second.second)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}


class EditReceiptResultsFragment : Fragment() {

    private lateinit var receiptItemsAdapter: ReceiptItemsAdapter
    private var receiptItems: MutableList<Pair<String, Pair<Double, String>>> = mutableListOf()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_edit_mode, container, false)

        var inventoryViewModel = ViewModelProvider(requireActivity())[InventoryViewModel::class.java]

        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
        val addButton: MaterialButton = rootView.findViewById(R.id.add_items)
        val cancelButton: MaterialButton = rootView.findViewById(R.id.cancel_items)

        var flag = requireArguments().getBoolean("scannedItems")
        var list = requireArguments().getSerializable("result") as ArrayList<Pair<String, Pair<Double, String>>>?

        if (list != null) {
            receiptItems = list
        }

        receiptItemsAdapter = ReceiptItemsAdapter(receiptItems)

        recyclerView.adapter = receiptItemsAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        addButton.setOnClickListener {
            inventoryViewModel.addReceiptItems(receiptItems)
            navigateToInventoryPage()
        }

        cancelButton.setOnClickListener {
            navigateToInventoryPage()
        }

        return rootView
    }

    fun navigateToInventoryPage() {
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentInventory()).commit()
    }
}
