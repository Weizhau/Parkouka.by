package weizhau.parkoukaby.Fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import weizhau.parkoukaby.FragmentRelations
import weizhau.parkoukaby.R

class InputNumbersFragment(val waiter: FragmentRelations.Waiter) : Fragment() {
    private val numberList = ArrayList<String>()
    private val prefs = activity?.getSharedPreferences("weizhau.parkoukaby", Context.MODE_PRIVATE)
    private val numbersRecyclerAdapter = NumbersRecyclerAdapter()
    private lateinit var numberEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_input_numbers, container, false)

        numberEditText = view.findViewById(R.id.number_edittext)

        val saveNumberButton = view.findViewById<ImageButton>(R.id.save_number_button)
        saveNumberButton.setOnClickListener {
            saveNumber(numberEditText.text.toString())
        }

        val saveAllNumberButton = view.findViewById<Button>(R.id.save_all_numbers_button)
        saveAllNumberButton.setOnClickListener {
            saveAllNumbers()
        }

        val skipButton = view.findViewById<Button>(R.id.skip_number_input_button)
        skipButton.setOnClickListener {
            waiter.doOnFirstSetupFinish()
        }

        val numbersRecyclerView = view.findViewById<RecyclerView>(R.id.saved_numbers_recyclerview)
        numbersRecyclerView.apply {
            adapter = numbersRecyclerAdapter
            layoutManager = LinearLayoutManager(context)
        }

        return view
    }

    private fun saveNumber(number: String) {
        if (number.matches(Regex("\\d{4}[A-Z]{2}\\d{1}"))) {
            if (numberList.size < 11) {
                numberList.add(number)
                numbersRecyclerAdapter.add(number)
                numberEditText.text.clear()
            } else {
                Toast.makeText(
                    context,
                    "you can add up to 10 numbers",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                "not a valid number",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveAllNumbers() {
        if (prefs != null) {
           // prefs.edit().putStringSet("car_numbers", numberList.toSet()).apply()
        }
    }

    class NumbersRecyclerAdapter :
        RecyclerView.Adapter<NumbersRecyclerAdapter.NumbersRecyclerViewHolder>() {
        val numberSet = ArrayList<String>()

        inner class NumbersRecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val number = view.findViewById<TextView>(R.id.number_textview)
        }

        fun add(number: String) {
            numberSet.add(number)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): NumbersRecyclerViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.number_recycler_item, parent, false)
            return NumbersRecyclerViewHolder(view)
        }

        override fun getItemCount() = numberSet.size

        override fun onBindViewHolder(holder: NumbersRecyclerViewHolder, position: Int) {
            holder.number.text = numberSet[position]
        }
    }
}