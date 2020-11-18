package weizhau.parkoukaby.Fragments

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import weizhau.parkoukaby.MainActivity
import weizhau.parkoukaby.R

class SettingsFragment : Fragment() {
    private lateinit var savePhoneButton: ImageButton
    private lateinit var sPref: SharedPreferences
    private lateinit var phoneEditText: EditText
    private lateinit var savedPhone: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        view.findViewById<ImageButton>(R.id.settings_back_button)
            .setOnClickListener {
                handleBackClick()
            }

        savedPhone = applyPhoneFromPreferences().toString()

        savePhoneButton = view.findViewById(R.id.save_phone_button)
        savePhoneButton.setOnClickListener { handleSavePhoneClick() }

        phoneEditText = view.findViewById(R.id.preferences_phone)
        applyTextWatcher(phoneEditText)

        return view
    }

    private fun handleSavePhoneClick() {
        sPref = activity!!.getPreferences(MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sPref.edit()
        editor.putString(MainActivity.PHONE, phoneEditText.text.toString())
        editor.apply()

        savedPhone = phoneEditText.text.toString()
    }

    private fun applyPhoneFromPreferences(): String? {
        sPref = activity!!.getPreferences(MODE_PRIVATE)
        return sPref.getString(MainActivity.PHONE, "")
    }

    private fun applyTextWatcher(view: EditText) {
        view.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (view.text.toString() != savedPhone) {
                    savePhoneButton.visibility = View.VISIBLE
                } else {
                    savePhoneButton.visibility = View.GONE
                }
            }
        })
    }

    private fun handleBackClick() {
        fragmentManager!!.popBackStack()
    }
}