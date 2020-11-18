package weizhau.parkoukaby

import android.content.Context
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import weizhau.parkoukaby.Data.PaymentResponse
import weizhau.parkoukaby.Data.ZonesResponse
import weizhau.parkoukaby.Fragments.FirstLaunchFragment
import weizhau.parkoukaby.Fragments.MainScreenFragment
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

class MainActivity : AppCompatActivity(), FragmentRelations.Waiter {
    companion object {
        const val PHONE = "phone"
        var paidStatus = false
        var zonesResponse: ZonesResponse.Response? = null
        var paymentResponse: PaymentResponse? = null
            //TODO use interface for activity-fragment communication instead of static fragment field
        lateinit var mainScreenFragment: MainScreenFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = this.getSharedPreferences("weizhau.parkoukaby", Context.MODE_PRIVATE)
        if (prefs.getBoolean("firstrun", true)) {
            prefs.edit().putBoolean("firstrun", false).apply()
            startFirstLaunchFragment()
        } else {
            getResponseAndStartMainFragment()
        }
    }

    private fun startFirstLaunchFragment() {
        supportFragmentManager.beginTransaction()
            .add(
                R.id.fragment_container,
                FirstLaunchFragment()
            ).commit()
    }

    private fun getResponseAndStartMainFragment() {
        Thread {
            zonesResponse = getParkingInfo()
            paymentResponse = getPaymentInfo()

            if (zonesResponse != null
                && paymentResponse != null
            ) {
                paidStatus = checkPaidStatusOnLoad()
                mainScreenFragment = MainScreenFragment()

                supportFragmentManager.beginTransaction()
                    .add(
                        R.id.fragment_container,
                        mainScreenFragment
                    ).commit()
            } else {
                runOnUiThread {
                    AlertDialog.Builder(this)
                        .setMessage(
                            "Не удается подключиться к серверу. " +
                                    "Проверьте Ваше интернет-соединение"
                        )
                        .setNeutralButton(
                            "Ok",
                            { dialog, which -> getResponseAndStartMainFragment() })
                        .create()
                        .show()
                }
            }
        }.start()
    }

    private fun checkPaidStatusOnLoad(): Boolean {
        val isPaid = paymentResponse!!.response.is_paid
        if (!isPaid) {
            val prefs = this.getSharedPreferences("weizhau.parkoukaby", Context.MODE_PRIVATE)
            prefs.edit().remove("paid_regplate").apply()
        }
        return isPaid
    }

    @WorkerThread
    fun getParkingInfo(): ZonesResponse.Response? {
        val httpURLConnection = URL("https://parkouka.by/api/public/zone_info")
            .openConnection() as HttpURLConnection
        try {
            if (httpURLConnection.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }
        } catch (exception: UnknownHostException) {
            return null
        }

        val streamReader = InputStreamReader(httpURLConnection.inputStream)

        var text = ""
        streamReader.use {
            text = it.readText()
        }

        val gson = Gson()
        val response = gson.fromJson(text, ZonesResponse.Response::class.java)

        httpURLConnection.disconnect()

        return response
    }

    @WorkerThread
    fun getPaymentInfo(): PaymentResponse? {
        val prefs = this.getSharedPreferences("weizhau.parkoukaby", Context.MODE_PRIVATE)
        val carNumber = prefs.getString("paid_regplate", "0000AA0")

        val httpURLConnection = URL(
            "https://parkouka.by/api/" +
                    "parking/check?partner_api_key=app_test&zone_id=21&regplate=$carNumber"
        ).openConnection() as HttpURLConnection

        try {
            if (httpURLConnection.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }
        } catch (exception: UnknownHostException) {
            return null
        }

        val streamReader = InputStreamReader(httpURLConnection.inputStream)

        var text = ""
        streamReader.use {
            text = it.readText()
        }

        val paymentResponse = Gson().fromJson(text, PaymentResponse::class.java)

        httpURLConnection.disconnect()

        return paymentResponse
    }

    //todo bad name
    override fun doOnFirstSetupFinish() {
        getResponseAndStartMainFragment()
    }
}


