import com.example.kotlinactivites.Network.PaymentIntentResponse
import com.example.kotlinactivites.Network.PaymentMethodResponse
import com.example.kotlinactivites.Network.PaymentResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface PayMongoService {

    @Headers("Content-Type: application/json")
    @POST("v1/payment_intents")
    fun createPaymentIntent(@Body request: PaymentIntentRequest): Call<PaymentIntentResponse>

    @Headers("Content-Type: application/json")
    @POST("v1/payment_methods")
    fun createPaymentMethod(@Body request: PaymentMethodRequest): Call<PaymentMethodResponse>

    @Headers("Content-Type: application/json")
    @POST("v1/payments")
    fun attachPayment(@Body request: AttachPaymentRequest): Call<PaymentResponse>

}





data class PaymentIntentRequest(
    val data: PaymentIntentData
)

data class PaymentIntentData(
    val attributes: PaymentIntentAttributes
)

data class PaymentIntentAttributes(
    val amount: Int,  // Amount in cents (e.g., PHP 100.00 = 10000)
    val currency: String = "PHP",
    val payment_method_allowed: List<String> = listOf("gcash"),
    val capture_type: String = "automatic"
)

data class PaymentMethodRequest(
    val data: PaymentMethodData
)

data class PaymentMethodData(
    val attributes: PaymentMethodAttributes
)

data class PaymentMethodAttributes(
    val type: String = "gcash",
    val details: PaymentDetails
)

data class PaymentDetails(
    val phone: String  // User's phone number
)

data class AttachPaymentRequest(
    val data: AttachPaymentData
)

data class AttachPaymentData(
    val attributes: AttachPaymentAttributes
)

data class AttachPaymentAttributes(
    val payment_method: String,  // Payment method ID from `createPaymentMethod` response
    val client_key: String       // Client key from `createPaymentIntent` response
)
