import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.androidapp.InventoryViewModel
import com.example.androidapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddItemDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.add_item, container, false)


        var inventoryViewModel = ViewModelProvider(requireActivity())[InventoryViewModel::class.java]

        val submitButton: MaterialButton = rootView.findViewById(R.id.add)
        val cancelButton: MaterialButton = rootView.findViewById(R.id.cancel)

        submitButton.setOnClickListener {
            val itemName =  rootView.findViewById<EditText>(R.id.edit_item_name).text.toString()
            val itemQuantity = rootView.findViewById<EditText>(R.id.edit_item_quantity).text.toString()
            val itemQuantityUnit = rootView.findViewById<EditText>(R.id.edit_item_unit).text.toString()
            val itemPurchaseDate = rootView.findViewById<EditText>(R.id.edit_item_purchase_date).text.toString()
            val itemExpiryDate = rootView.findViewById<EditText>(R.id.edit_item_expiry_date).text.toString()

            if (canAddItem(itemName, itemQuantity, itemQuantityUnit, itemPurchaseDate, itemExpiryDate)) {
                inventoryViewModel.addItem(itemName, itemQuantity.toDouble(), itemQuantityUnit, itemPurchaseDate, itemExpiryDate)
                dismiss()
            }
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return rootView
    }

    fun canAddItem( name : String, quantity : String, unit : String,  purchase : String,  expiry : String ) : Boolean {
        var canAdd = true

        if (name == "" || quantity == "" || unit == "" || purchase == "" || expiry == "") {
            canAdd = false
        }

        return canAdd
    }
}
