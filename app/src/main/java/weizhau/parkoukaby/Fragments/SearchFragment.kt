package weizhau.parkoukaby.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import weizhau.parkoukaby.AdressClickListener
import weizhau.parkoukaby.Data.ZonesResponse
import weizhau.parkoukaby.MainActivity
import weizhau.parkoukaby.R

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SearchFragment : Fragment(), AdressClickListener {
    private val dataset = MainActivity.zonesResponse
    private val adressesList = getAdressesList(dataset!!)
    private val namesToCoordsMap = bindNamesToCoordinates(dataset!!)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val nearestRecyclerView = view
            .findViewById<RecyclerView>(R.id.nearest_recyclerview)
            .apply {
                layoutManager = LinearLayoutManager(context)
                adapter = NearestRecyclerAdapter(adressesList, this@SearchFragment)
            }

        val backButton = view.findViewById<ImageButton>(R.id.back_from_search_button)
        backButton.setOnClickListener { fragmentManager?.popBackStack() }

        val nearestTextView = view.findViewById<TextView>(R.id.nearest_text_view)

        val searchEditText = view.findViewById<EditText>(R.id.adress_edit_text)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (searchEditText.text.toString() != "") {
                    nearestTextView.setText(R.string.search_result_text)
                } else {
                    nearestTextView.setText(R.string.nearest_textview)
                }

                val filteredList = ArrayList(
                    formatListToSearched(
                        searchEditText
                            .text
                            .toString(),
                        adressesList
                    )
                )
                nearestRecyclerView.adapter =
                    NearestRecyclerAdapter(filteredList, this@SearchFragment)
            }
        })
        return view
    }

    private fun getAdressesList(zonesResponse: ZonesResponse.Response): ArrayList<String> {
        val adressesList = ArrayList<String>()
        for (spot in zonesResponse.response.listIterator()) {
            adressesList.add(spot.name)
        }
        return adressesList
    }

    private fun bindNamesToCoordinates(
        dataset: ZonesResponse.Response
    ): HashMap<String, List<Double>> {
        val map = HashMap<String, List<Double>>()
        for (spot in dataset.response) {
            map.put(spot.name, spot.windowPos[0])
        }

        return map
    }

    fun formatListToSearched(searchText: String, list: ArrayList<String>): List<String> {
        return list.filter { it.toLowerCase(Locale.ROOT).contains(searchText) }
    }

    override fun onAdressClick(coords: List<Double>) {
        val bundle = Bundle()
        bundle.putDoubleArray("coordinates", coords.toDoubleArray())
        val mainScreenFragment = MainScreenFragment()
        mainScreenFragment.arguments = bundle

        fragmentManager!!.beginTransaction()
            .replace(
                R.id.fragment_container,
                mainScreenFragment
            )
            .commit()
    }

    inner class NearestRecyclerAdapter(
        var adressesSet: ArrayList<String>,
        val clickListener: AdressClickListener
    ) : RecyclerView.Adapter<NearestRecyclerAdapter.NearestViewHolder>() {

        inner class NearestViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(R.id.nearest_recycler_item)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearestViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.nearest_recycler_item, parent, false)

            return NearestViewHolder(view)
        }

        override fun getItemCount() = adressesSet.size

        override fun onBindViewHolder(holder: NearestViewHolder, position: Int) {
            val name = adressesSet[position]
            holder.text.text = name
            holder.itemView.setOnClickListener {
                clickListener.onAdressClick(namesToCoordsMap[name]!!)
            }
        }
    }
}