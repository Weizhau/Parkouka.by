package weizhau.parkoukaby

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment

class RationaleDialog : DialogFragment() {
    private var finishActivity = false
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val requestCode =
            arguments?.getInt(ARGUMENT_PERMISSION_REQUEST_CODE) ?: 0
        finishActivity =
            arguments?.getBoolean(ARGUMENT_FINISH_ACTIVITY) ?: false
        return AlertDialog.Builder(activity)
            .setMessage("R.string.permission_rationale_location")
            .setPositiveButton(android.R.string.ok) { dialog, which -> // After click on Ok, request the permission.
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    requestCode
                )
                // Do not finish the Activity while requesting permission.
                finishActivity = false
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (finishActivity) {
            Toast.makeText(
                activity,
                "R.string.permission_required_toast",
                Toast.LENGTH_SHORT
            ).show()
            activity?.finish()
        }
    }

    companion object {
        private const val ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode"
        private const val ARGUMENT_FINISH_ACTIVITY = "finish"

        fun newInstance(requestCode: Int, finishActivity: Boolean): RationaleDialog {
            val arguments = Bundle().apply {
                putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode)
                putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)
            }
            return RationaleDialog().apply {
                this.arguments = arguments
            }
        }
    }
}
