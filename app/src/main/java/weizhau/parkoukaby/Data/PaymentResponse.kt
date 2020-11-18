package weizhau.parkoukaby.Data

data class PaymentResponse(
    val request: Request,
    val response: Response
)

data class Request(
    val regplate: String,
    val status: String,
    val type: String
)

data class Response(
    val is_paid: Boolean,
    val tariff_id: Int,
    val valid_from: String,
    val valid_till: String,
    val zone_id: Int
)