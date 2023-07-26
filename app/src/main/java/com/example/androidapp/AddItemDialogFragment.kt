import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.androidapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddItemDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.add_item, container, false)

        //val itemNameEditText: TextInputEditText = rootView.findViewById(R.id.itemNameEditText)
        //val itemQuantityEditText: TextInputEditText = rootView.findViewById(R.id.itemQuantityEditText)
        val submitButton: MaterialButton = rootView.findViewById(R.id.add)
        val cancelButton: MaterialButton = rootView.findViewById(R.id.cancel)

        submitButton.setOnClickListener {
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return rootView
    }
}
