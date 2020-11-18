package weizhau.parkoukaby.Data

class ZonesResponse() {
    data class Response(
        val method: String,
        val response: List<Spot>,
        val status: String
    )

    data class Spot(
        val hours: String,
        val id: Int,
        val launch_date: Any,
        val map: List<List<List<Double>>>,
        val name: String,
        val sms_code: String,
        val week_days: String,
        val windowPos: List<List<Double>>
    )
}