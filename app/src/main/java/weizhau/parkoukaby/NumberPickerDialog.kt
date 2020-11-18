package weizhau.parkoukaby

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment

class NumberPickerDialog(val valueChangeListener: NumberPicker.OnValueChangeListener) :
    DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val numberPicker = NumberPicker(activity)
        numberPicker.minValue = 1
        numberPicker.maxValue = 4

        return AlertDialog.Builder(activity)
            .setView(/*R.layout.number_picker_dialog_layout*/numberPicker)
            .setPositiveButton("Принять", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    valueChangeListener.onValueChange(
                        numberPicker,
                        numberPicker.getValue(), numberPicker.getValue()
                    )
                }
            })
            .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}