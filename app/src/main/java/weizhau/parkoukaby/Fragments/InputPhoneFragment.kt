package weizhau.parkoukaby.Fragments

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import weizhau.parkoukaby.R

class InputPhoneFragment(val viewPager: ViewPager2) : Fragment() {
    private val prefs = activity?.getSharedPreferences("weizhau.parkoukaby", Context.MODE_PRIVATE)
    private lateinit var savePhoneButton: Button
    private lateinit var phoneEditText: EditText
    private lateinit var skipButton: Button
    private val phoneRegex = Regex("\\+\\d{12}")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_input_phone, container, false)

        phoneEditText = view.findViewById(R.id.phone_edittext)

        savePhoneButton = view.findViewById(R.id.save_phone_button)
        savePhoneButton.setOnClickListener { savePhone(phoneEditText.text.toString()) }

        skipButton = view.findViewById(R.id.skip_phone_input_button)
        skipButton.setOnClickListener {
            switchToNextFragment()
        }

        return view
    }

    private fun savePhone(phone: String) {
        if (prefs != null) {
            if (phone.matches(phoneRegex)) {
           //     prefs.edit().putString("phone", phone).apply()
            } else {
                Toast.makeText(
                    context,
                    "not a phone number",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        savePhoneButton.isClickable = false
    }

    private fun switchToNextFragment() {
        viewPager.setCurrentItem(
            viewPager.currentItem + 1,
            true
        )
    }
}