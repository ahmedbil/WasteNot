package com.example.androidapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton



class ReceiptItemsAdapter(
    private val items: MutableList<Pair<String, Pair<Double, String>>>,
    private val onDeleteClickListener: (position: Int) -> Unit
) : RecyclerView.Adapter<ReceiptItemsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientNameEditText: EditText = itemView.findViewById(R.id.ingredientNameEditText)
        val ingredientQuantityEditText: EditText = itemView.findViewById(R.id.ingredientQuantityEditText)
        val ingredientUnitEditText: EditText = itemView.findViewById(R.id.ingredientUnitEditText)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
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
        holder.deleteButton.setOnClickListener {
            onDeleteClickListener.invoke(position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}


class EditReceiptResultsFragment : Fragment() {

    private lateinit var receiptItemsAdapter: ReceiptItemsAdapter
    private var receiptItems: MutableList<Pair<String, Pair<Double, String>>> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_edit_mode, container, false)

        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
        val addButton: MaterialButton = rootView.findViewById(R.id.addButton)
        val saveButton: MaterialButton = rootView.findViewById(R.id.saveButton)

        val flag = requireArguments().getBoolean("scannedItems")

        val list: ArrayList<Pair<String, Pair<Double, String>>>? = requireArguments().getSerializable("result") as ArrayList<Pair<String, Pair<Double, String>>>?

        Log.i("list", list.toString())
        if (list != null) {
            receiptItems = list
        }

        receiptItemsAdapter = ReceiptItemsAdapter(receiptItems) { position ->
            // Remove the ingredient at the specified position
            receiptItems.removeAt(position)
            receiptItemsAdapter.notifyItemRemoved(position)
        }

        recyclerView.adapter = receiptItemsAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        addButton.setOnClickListener {
            //saveChanges()
        }

        saveButton.setOnClickListener {
            //saveChanges()
        }

        return rootView
    }
}
