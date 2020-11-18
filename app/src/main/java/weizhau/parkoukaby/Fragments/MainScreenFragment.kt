package weizhau.parkoukaby.Fragments

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_main.*
import weizhau.parkoukaby.*
import weizhau.parkoukaby.Data.ZonesResponse
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainScreenFragment : Fragment(),
    OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private var permissionDenied = false

    private lateinit var map: GoogleMap
    private lateinit var paymentButton: Button
    private lateinit var timer: CountDownTimer
    private lateinit var timerTextView: TextView
    private lateinit var paidTillTextView: TextView
    private lateinit var paymentStatusView: View
    private lateinit var prolongButton: Button
    private lateinit var numberTextView: TextView
    private lateinit var searchButton: Button
    private lateinit var sideMenuButton: ImageButton
    private lateinit var getPositionButton: ImageButton

    private var textColorIsBlack = true
    private var paidCarNumber: String = ""
    private var paidTill: String = ""
    private var timerRunning = false
    private var secondsUntilTimerFinish: Long = 0

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (MainActivity.paidStatus) {
            if (MainActivity
                    .paymentResponse!!
                    .response
                    .is_paid
            ) {
                paidCarNumber = MainActivity
                    .paymentResponse!!
                    .request
                    .regplate

                val timeFromResponse = MainActivity
                    .paymentResponse!!
                    .response
                    .valid_till
                    .substringAfter('T', "")
                    .substringBefore('Z')

                paidTill = TimeUtility(timeFromResponse).sumTime(3)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val mapView = view.findViewById<MapView>(R.id.mapView)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        paymentButton = view.findViewById(R.id.payment_button)
        paymentButton.setOnClickListener { handlePaymentPress() }

        prolongButton = view.findViewById(R.id.prolong_payment_button)
        prolongButton.setOnClickListener { handlePaymentPress() }

        paymentStatusView = view.findViewById(R.id.payment_status_view)

        numberTextView = view.findViewById(R.id.payment_status_number)
        paidTillTextView = view.findViewById(R.id.payment_status_paid_till)

        searchButton = view.findViewById(R.id.search_button)
        searchButton.setOnClickListener { handleSearchPress() }

        sideMenuButton = view.findViewById(R.id.side_menu_button)
        sideMenuButton.setOnClickListener { handleSideMenuPress() }

        getPositionButton = view.findViewById(R.id.get_position_button)
        getPositionButton.setOnClickListener { onLocationButtonClick() }

        timerTextView = view.findViewById(R.id.payment_status_countdown)

        setIfPaymentInfoPassed()

        if (MainActivity.paidStatus) {
            paymentButton.visibility = View.INVISIBLE
            paymentStatusView.visibility = View.VISIBLE

            numberTextView.setText(paidCarNumber)

            paidTillTextView.text = paidTill

            startTimer(
                TimeUtility(getCurrentTime()).findTimeBetweenInMillis(paidTill)
            )
        } else {
            paymentStatusView.visibility = View.INVISIBLE
            paymentButton.visibility = View.VISIBLE
        }

        return view
    }

    private fun setIfPaymentInfoPassed() {
        if (arguments != null) {
            if (!arguments!!.isEmpty) {
                paidCarNumber = arguments!!.getString("paid_regplate", "")
                arguments!!.remove("paid_regplate")
                val hours = arguments!!.getInt("paid_hours")
                arguments!!.remove("paid_hours")

                if (hours != 5) {
                    if (paidTill != "") {
                        paidTill = TimeUtility(paidTill)
                            .sumTime(hours)
                    } else {
                        paidTill = TimeUtility(getCurrentTime())
                            .sumTime(hours)
                    }
                } else {
                    paidTill = "18:00"
                }

                switchButtonAndStat()
            }
        }
    }

    private fun switchButtonAndStat() {
        if (paymentButton.visibility == View.VISIBLE) {
            paymentButton.visibility = View.INVISIBLE
            paymentStatusView.visibility = View.VISIBLE
        } else {
            paymentButton.visibility = View.VISIBLE
            paymentStatusView.visibility = View.INVISIBLE
        }
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            map = googleMap

            googleMap.uiSettings.isCompassEnabled = false
            googleMap.uiSettings.isMyLocationButtonEnabled = false

            enableMyLocation()
            drawSpots(MainActivity.zonesResponse!!.response)
        }
    }

    private fun drawSpots(spots: List<ZonesResponse.Spot>) {
        val polygonList = ArrayList<Polygon>()
        val polygonToSpotMap = HashMap<Polygon, ZonesResponse.Spot>()

        for (spot in spots) {
            val polygon = map.addPolygon(setupPolygon(spot))

            polygonList.add(polygon)
            polygonToSpotMap.put(polygon, spot)

            map.setInfoWindowAdapter(context?.let { AdressInfoWindowAdapter(it) })

            map.setOnPolygonClickListener {
                val marker = addZoneMarker(polygonToSpotMap[it]!!)
                marker.showInfoWindow()
            }

            map.setOnInfoWindowCloseListener {
                it.remove()
            }
        }

        if (arguments != null) {
            //todo need to be refactored
            if (!arguments!!.isEmpty) {
                val passedCoordinates = arguments?.getDoubleArray("coordinates")
                arguments?.remove("coordinates")
                doOnCoordinatesPassed(passedCoordinates, polygonList, polygonToSpotMap)
            } else {
                map.moveCamera(
                    CameraUpdateFactory
                        .newLatLngZoom(LatLng(53.8991248, 27.5546675), 13.0F)
                )
            }
        } else {
            map.moveCamera(
                CameraUpdateFactory
                    .newLatLngZoom(LatLng(53.8991248, 27.5546675), 13.0F)
            )
        }
    }

    private fun setupPolygon(spot: ZonesResponse.Spot): PolygonOptions {
        val polygonOptions = PolygonOptions()
            .clickable(true)

        if (MainActivity.paidStatus) {
            polygonOptions
                .strokeColor(Color.GREEN)
                .fillColor(Color.GREEN)
        } else {
            polygonOptions
                .strokeColor(Color.RED)
                .fillColor(Color.RED)
        }

        for (point in spot.map[0].listIterator()) {
            polygonOptions.add(LatLng(point[0], point[1]))
        }

        return polygonOptions
    }

    private fun doOnCoordinatesPassed(
        coordinates: DoubleArray?,
        polygonList: ArrayList<Polygon>,
        polygonToSpotMap: HashMap<Polygon, ZonesResponse.Spot>
    ) {
        if (coordinates != null) {
            map.animateCamera(
                CameraUpdateFactory
                    .newLatLngZoom(
                        LatLng(coordinates[0], coordinates[1]),
                        13.0F
                    )
            )

            for (polygon in polygonList) {
                val item = polygonToSpotMap[polygon]

                if (item!!.windowPos[0] == coordinates.toList()) {
                    val marker = addZoneMarker(item)
                    marker.showInfoWindow()
                }
            }
        }
    }

    private fun addZoneMarker(zone: ZonesResponse.Spot): Marker {
        return map.addMarker(
            MarkerOptions()
                .title(zone.name)
                .snippet("${zone.week_days}|${zone.hours}")
                .position(
                    LatLng(
                        zone.windowPos[0][0],
                        zone.windowPos[0][1]
                    )
                )
        )
    }

    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
            == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            //  requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), )
            requestPermission(
                this.activity as AppCompatActivity, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
    }

    private fun handleSearchPress() {
        fragmentManager!!.beginTransaction()
            .replace(
                R.id.fragment_container,
                SearchFragment()
            )
            .addToBackStack(null)
            .commit()
    }

    private fun handleSideMenuPress() {
        val mainDrawer = view!!.findViewById<DrawerLayout>(R.id.side_menu_drawer)
        mainDrawer.openDrawer(Gravity.LEFT)

        val navigationView: NavigationView = view!!.findViewById(R.id.side_menu)
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.settings_menu_item -> {
                    fragmentManager!!.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            SettingsFragment()
                        )
                        .addToBackStack(null)
                        .commit()
                }
            }
            mainDrawer.closeDrawer(Gravity.LEFT)
            true
        }


    }

    private fun handlePaymentPress() {
        fragmentManager!!.beginTransaction()
            .replace(
                R.id.fragment_container,
                PaymentFragment()
            )
            .addToBackStack(null)
            .commit()
    }

    private fun onLocationButtonClick() {
        val locationManager =
            activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        val provider = locationManager.getBestProvider(criteria, false)

        if (ContextCompat
                .checkSelfPermission(
                    context!!,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat
                .checkSelfPermission(
                    context!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), 13F
                    )
                )
            }

        } else {
            ActivityCompat.requestPermissions(
                activity as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                1
            )
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    private fun requestPermission(
        activity: AppCompatActivity, requestId: Int,
        permission: String, finishActivity: Boolean
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            RationaleDialog.newInstance(requestId, finishActivity)
                .show(activity.supportFragmentManager, "dialog")
        } else {
            // Location permission has not been granted yet, request it.
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                requestId
            )
        }
    }

    private fun isPermissionGranted(
        grantPermissions: Array<String>,
        grantResults: IntArray,
        permission: String
    ): Boolean {
        for (i in grantPermissions.indices) {
            if (permission == grantPermissions[i]) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }

    private fun startTimer(timeInMillis: Long) {
        if (timerRunning) {
            timer.cancel()
        }
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onFinish() {
                switchButtonAndStat()
                clearPaidNumber()
                MainActivity.paidStatus = false
                timerRunning = false
            }

            override fun onTick(millisUntilFinished: Long) {
                secondsUntilTimerFinish = millisUntilFinished / 1000
                updateTimerText()

                //600000 = 10 minutes until end
                if (millisUntilFinished < 600000) {
                    updateTimerTextColor()
                }
            }
        }
        timer.start()
        timerRunning = true
    }

    private fun clearPaidNumber() {
        val prefs = activity?.getSharedPreferences("weizhau.parkoukaby", Context.MODE_PRIVATE)
        prefs?.edit()?.remove("paid_regplate")?.apply()
    }

    private fun updateTimerText() {
        val hours = secondsUntilTimerFinish / 3600
        val minutes = secondsUntilTimerFinish % 3600 / 60
        val seconds = secondsUntilTimerFinish % 3600 % 60

        timerTextView.text = "$hours:$minutes:$seconds"
    }

    private fun updateTimerTextColor() {
        if (textColorIsBlack) {
            timerTextView.setTextColor(
                ContextCompat
                    .getColor(context!!, R.color.colorRed)
            )
            textColorIsBlack = false
        } else {
            timerTextView.setTextColor(
                ContextCompat
                    .getColor(context!!, R.color.colorDarkGrey)
            )
            textColorIsBlack = true
        }
    }

    private fun getCurrentTime(): String {
        val format = SimpleDateFormat("HH:mm:ss")
        return format.format(Calendar.getInstance().time)
    }
}