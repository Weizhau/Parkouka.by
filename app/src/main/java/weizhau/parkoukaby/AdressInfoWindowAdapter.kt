package weizhau.parkoukaby

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class AdressInfoWindowAdapter(val context: Context) :
    GoogleMap.InfoWindowAdapter {

    private var view: View

    init {
        view = LayoutInflater.from(context).inflate(R.layout.parking_info_window, null)
    }

    fun renderWindow(marker: Marker) {


        val nameTextView = view.findViewById<TextView>(R.id.parking_name_textview)
        nameTextView.text = marker.title

        val daysHoursTextView = view.findViewById<TextView>(R.id.parking_days_hours_textview)
        when (marker.snippet.substringBefore('|')) {
            "12345" -> daysHoursTextView.text = "пн-пт ${marker.snippet.substringAfter('|')}"
            "1234567" -> daysHoursTextView.text = "пн-вс ${marker.snippet.substringAfter('|')}"
        }

        val costTextView = view.findViewById<TextView>(R.id.parking_cost_textview)
        costTextView.text = "1 Br/ч"
    }

    override fun getInfoContents(marker: Marker): View {
        renderWindow(marker)
        return view
    }

    override fun getInfoWindow(marker: Marker): View {
        renderWindow(marker)
        return view
    }
}