package weizhau.parkoukaby.Fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import weizhau.parkoukaby.ItemClickListener
import weizhau.parkoukaby.MainActivity
import weizhau.parkoukaby.NumberPickerDialog
import weizhau.parkoukaby.R
import java.text.SimpleDateFormat
import java.util.*


class PaymentFragment : Fragment(), ItemClickListener, NumberPicker.OnValueChangeListener {

    companion object {
        private const val SEND_SMS_PERMISSION_REQUEST_CODE = 1
    }

    private lateinit var sumTextView: TextView
    private lateinit var carNumberEditText: EditText

    private var prefs: SharedPreferences? = null
    private lateinit var paidCarNumber: String

    private var chosenType: String = ""
    private var hoursToBePaid = 0

    private val paymentOptions = arrayListOf(
        "1 час" to "1 Br",
        "1-4 часа" to "1 Br / час",
        "весь день" to "5 Br"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = activity?.getSharedPreferences("weizhau.parkoukaby", Context.MODE_PRIVATE)

        paidCarNumber = prefs?.getString("paid_regplate", "").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        val backButton = view.findViewById<ImageButton>(R.id.back_from_payment_button)
        backButton.setOnClickListener { handleBackPress() }

        view.findViewById<RecyclerView>(R.id.payment_options_recyclerview)
            .apply {
                layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = PaymentOptionsRecyclerAdapter(
                    paymentOptions,
                    this@PaymentFragment
                )
            }

        sumTextView = view.findViewById(R.id.sum_textview)
        carNumberEditText = view.findViewById(R.id.car_number_edittext)
        carNumberEditText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(v)
            }
        }

        if (paidCarNumber != "") {
            carNumberEditText.setText(paidCarNumber)
            carNumberEditText.isEnabled = false
        }

        val payButton = view.findViewById<Button>(R.id.pay_button)
        payButton.setOnClickListener { handlePayPress() }

        return view
    }

    override fun onItemClick(type: String, cost: String) {
        chosenType = type
        when (type) {
            "1 час" -> {
                hoursToBePaid = 1
                sumTextView.text = cost
            }
            "1-4 часа" -> {
                showNumberPicker()
            }
            "весь день" -> {
                hoursToBePaid = 5
                sumTextView.text = cost
            }
        }
    }

    override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
        hoursToBePaid = newVal
        sumTextView.text = "${newVal} Br"
    }

    private fun showNumberPicker() {
        val newFragment = NumberPickerDialog(this)
        newFragment.show(fragmentManager!!, "number picker")
    }

    private fun handleBackPress() {
        fragmentManager?.popBackStack()
    }

    private fun transitToMainScreenPaid(hours: Int, number: String) {
        MainActivity.paidStatus = true

        val bundle = Bundle()
        bundle.putInt("paid_hours", hours)
        bundle.putString("paid_regplate", number)

        MainActivity.mainScreenFragment.arguments = bundle

        fragmentManager!!.beginTransaction()
            .replace(R.id.fragment_container, MainActivity.mainScreenFragment)
            .commit()
    }

    private fun handlePayPress() {
        val currentHours = getCurrentHours()
        val carNumber = carNumberEditText.getText().toString().toUpperCase(Locale.ROOT)

        if (checkIfPaymentTypePicked() && checkIfCarNumberPasses(carNumber)) {
            if (currentHours > 9 && currentHours < 18) {
                if (isPermissionGranted(Manifest.permission.SEND_SMS)) {
                    prefs?.edit()?.putString("paid_regplate", carNumber)?.apply()
                    val smsManager = SmsManager.getDefault()
                    /*when (chosenType) {
                        "1 час" -> {
                            smsManager.sendTextMessage(
                                "204",
                                null,
                                withoutLetters(carNumber),
                                null,
                                null
                            )
                        }
                        "1-4 часа" -> {
                            repeat(hoursToBePaid) {
                                smsManager.sendTextMessage(
                                    "204",
                                    null,
                                    withoutLetters(carNumber),
                                    null,
                                    null
                                )
                            }
                        }
                        "весь день" -> {
                            smsManager.sendTextMessage(
                                "204",
                                null,
                                "day $carNumber",
                                null,
                                null
                            )
                        }
                    }*/
                    transitToMainScreenPaid(hoursToBePaid, carNumber)
                } else {
                    ActivityCompat.requestPermissions(
                        activity as Activity,
                        arrayOf(Manifest.permission.SEND_SMS),
                        SEND_SMS_PERMISSION_REQUEST_CODE
                    )
                }
            } else {
                Toast.makeText(
                    context,
                    "Сейчас парковка бесплатна",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            ContextCompat.getSystemService(context!!, InputMethodManager::class.java)
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun withoutLetters(carNumber: String): String {
        return carNumber.substring(0, 4) + carNumber[6]
    }

    private fun checkIfPaymentTypePicked(): Boolean {
        if (chosenType != "") {
            return true
        } else {
            Toast.makeText(
                context,
                "Выберите время парковки",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
    }

    private fun checkIfCarNumberPasses(carNumber: String): Boolean {
        if (carNumber != "") {
            if (carNumber.matches(Regex("\\d{4}[A-Z]{2}\\d{1}"))) {
                return true
            } else {
                Toast.makeText(
                    context,
                    "Введите номер автомобиля в соответствии с шаблоном",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        } else {
            Toast.makeText(
                context,
                "Введите номер автомобиля",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
    }

    private fun getCurrentHours(): Int {
        val format = SimpleDateFormat("HH:mm:ss")
        return format.format(Calendar.getInstance().time)
            .substring(0, 2)
            .toInt()
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    inner class PaymentOptionsRecyclerAdapter(
        var options: ArrayList<Pair<String, String>>,
        val clickListener: ItemClickListener
    ) : RecyclerView.Adapter<PaymentOptionsRecyclerAdapter.PaymentOptionsViewHolder>() {

        inner class PaymentOptionsViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
            val optionText: TextView = view.findViewById(R.id.payment_hours_textview)
            val costText: TextView = view.findViewById(R.id.payment_cost_textview)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PaymentOptionsViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.payment_option_recycler_item, parent, false)

            return PaymentOptionsViewHolder(view)
        }

        override fun getItemCount() = options.size

        override fun onBindViewHolder(holder: PaymentOptionsViewHolder, position: Int) {
            holder.optionText.text = options[position].first
            holder.costText.text = options[position].second
            holder.itemView.setOnClickListener {
                clickListener.onItemClick(
                    holder.optionText.text.toString(),
                    holder.costText.text.toString()
                )
            }
        }
    }
}