package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import org.json.JSONArray
import org.json.JSONObject

class Paymentpage : AppCompatActivity() {

    private lateinit var paymentsClient: PaymentsClient
    private lateinit var roomPriceTextView: TextView
    private val loadPaymentDataRequestCode = 991
    private var roomPrice: String = "0.00" // Default price

    private var roomId: Int = 0
    private var name: String? = null
    private var maxOccupants: Int = 0
    private var roomImageURL: String? = null
    private var startDate: String? = null
    private var endDate: String? = null
    private var numberofoccupants: Int = 1
    private var numberofrooms: Int = 1
    private var totalPrice: Double = 0.0 // Store total price of selected rooms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.paymentpage)



        // TextView to display room price
        roomPriceTextView = findViewById(R.id.room_price_text)

        // Get room price from the Intent passed
        fetchRoomPriceFromIntent()

        Log.d("CheckPayment", "Total Price:$totalPrice ")
        // Retrieve other necessary values from the intent
        retrieveBookingDetails()
        // Initialize Google Pay API client
        paymentsClient = createPaymentsClient()

        Log.d("PaymentPage3", "ROOM_ID: $roomId")
        Log.d("PaymentPage3", "ROOM_TYPE: $name")
        Log.d("PaymentPage3", "DATES: $startDate to $endDate")
        Log.d("PaymentPage3", "ROOM_IMAGE_URL: $roomImageURL")
        Log.d("PaymentPage3", "NUMBER_OF_OCCUPANTS: $numberofoccupants")
        Log.d("PaymentPage3", "NUMBER_OF_ROOMS: $numberofrooms")

        //val numberofoccupants = intent.getIntExtra("numberofoccupants", 0) // Default value is 0
        Log.d("Numberofoccupantss", "NumberofOccupantscheck:$numberofoccupants")




        // Check if Google Pay is available
        checkGooglePayAvailability()
        Log.d("CheckPayment", "Total Pssrice:$totalPrice ")
        // Set up the button click listener
        val payButton: Button = findViewById(R.id.googlepaybutton)
        payButton.setOnClickListener {
            requestPayment() // Initiates Google Pay payment flow
        }
    }

    private fun createPaymentsClient(): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST) // Change to ENVIRONMENT_PRODUCTION in production
            .build()
        return Wallet.getPaymentsClient(this, walletOptions)
    }

    private fun fetchRoomPriceFromIntent() {
        // Retrieve the selected rooms list from the Intent
        val selectedRooms: ArrayList<Room>? = intent.getParcelableArrayListExtra("selectedRooms")

        // Check if selected rooms are not null and not empty
        if (selectedRooms != null && selectedRooms.isNotEmpty()) {
            // Log the received rooms and their prices
            for (room in selectedRooms) {
                Log.d("RoomDetails", "Received room: Name: ${room.name}, Price: ${room.price}")
            }

            // Calculate the total price
            totalPrice = calculateTotalPrice(selectedRooms)
            // Display the total price in the UI
            roomPriceTextView.text = "Total Price: $$totalPrice"

            Log.d("RoomDetails", "Total price for selected rooms: $totalPrice")
        } else {
            Toast.makeText(this, "Failed to fetch room price from intent", Toast.LENGTH_SHORT).show()
        }
    }

    // Retrieve booking details passed from the previous page
    private fun retrieveBookingDetails() {
        // Retrieve the list of selected rooms from the intent
        val selectedRooms: ArrayList<Room>? = intent.getParcelableArrayListExtra("selectedRooms")

        // Ensure the list is not null or empty
        if (selectedRooms != null && selectedRooms.isNotEmpty()) {
            val room = selectedRooms[0] // Assuming you are working with the first selected room
            roomId = room.id // Get the room ID
            name = room.name // Get the room type
            maxOccupants = room.maxoccupants // Get max occupants
            roomImageURL = room.imageURL // Get room image URL
        }

        // Retrieve other booking details
        startDate = intent.getStringExtra("startDate") // Get start date
        endDate = intent.getStringExtra("endDate") // Get end date
        numberofoccupants = intent.getIntExtra("numberofoccupants", 1) // Default value: 1
        numberofrooms = intent.getIntExtra("NUMBER_OF_ROOMS", 1) // Default value: 1
        Log.d("Checck", "CheckNumber: $numberofoccupants ")
    }




    // Calculate total price by multiplying room price with number of rooms
    private fun calculateTotalPrice(selectedRooms: ArrayList<Room>): Double {
        var totalPrice = 0.0
        for (room in selectedRooms) {
            // Multiply room price by the number of rooms selected
            totalPrice += room.price.toDouble() * room.numberofrooms // Adjust price calculation
        }
        Log.d("RoomDetails", "Calculated total price: $totalPrice")
        return totalPrice
    }

    private fun checkGooglePayAvailability() {
        val isReadyToPayJson = getIsReadyToPayRequest()
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())

        paymentsClient.isReadyToPay(request)
            .addOnCompleteListener { task ->
                try {
                    if (task.getResult(ApiException::class.java) == true) {
                        findViewById<Button>(R.id.googlepaybutton).isEnabled = true
                    } else {
                        Toast.makeText(this, "Google Pay is not available", Toast.LENGTH_LONG).show()
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
    }

    private fun requestPayment() {
        Log.d("CheckPayment", "Check payment: $totalPrice ")
        val paymentDataRequestJson = getPaymentDataRequest()
        if (paymentDataRequestJson == null) return

        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(request),
            this,
            loadPaymentDataRequestCode
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            loadPaymentDataRequestCode -> {
                when (resultCode) {
                    RESULT_OK -> {
                        data?.let {
                            val paymentData = PaymentData.getFromIntent(data)
                            handlePaymentSuccess(paymentData)
                        }
                    }
                    RESULT_CANCELED -> {
                        Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_LONG).show()
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                        Toast.makeText(this, "Error: ${status?.statusMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun handlePaymentSuccess(paymentData: PaymentData?) {
        val paymentInformation = paymentData?.toJson() ?: return
        val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")

        val token = paymentMethodData
            .getJSONObject("tokenizationData")
            .getString("token")

        showPaymentSuccessDialog()
    }

    private fun showPaymentSuccessDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Payment Successful")
        builder.setMessage("Your payment was processed successfully. Thank you for booking!")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(this, MyBookings::class.java)
            intent.putExtra("ROOM_ID", roomId)
            intent.putExtra("ROOM_TYPE", name)
            intent.putExtra("START_DATE", startDate)  // Pass startDate
            intent.putExtra("END_DATE", endDate)      // Pass endDate
            intent.putExtra("ROOM_IMAGE_URL", roomImageURL)
            intent.putExtra("numberofoccupants", numberofoccupants)
            intent.putExtra("NUMBER_OF_ROOMS", numberofrooms)


            Log.d("PaymentPage1", "ROOM_ID: $roomId")
            Log.d("PaymentPage1", "ROOM_TYPE: $name")
            Log.d("PaymentPage1", "DATES: $startDate to $endDate")
            Log.d("PaymentPage1", "ROOM_IMAGE_URL: $roomImageURL")
            Log.d("PaymentPage1", "NUMBER_OF_OCCUPANTS: $numberofoccupants")
            Log.d("PaymentPage1", "NUMBER_OF_ROOMS: $numberofrooms")



            startActivity(intent)
            finish()
        }
        builder.setCancelable(false)
        builder.create().show()
    }

    private fun getIsReadyToPayRequest(): JSONObject {
        return JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().apply {
                put(getBaseCardPaymentMethod())
            })
        }
    }

    private fun getPaymentDataRequest(): JSONObject? {
        return JSONObject().apply {
            put("apiVersion", 2)
            put("apiVersionMinor", 0)
            put("allowedPaymentMethods", JSONArray().apply {
                put(getBaseCardPaymentMethod())
            })
            put("transactionInfo", JSONObject().apply {
                put("totalPrice", totalPrice.toString()) // Use the updated total price
                put("totalPriceStatus", "FINAL")
                put("currencyCode", "USD") // Adjust currency as needed
            })
            put("merchantInfo", JSONObject().apply {
                put("merchantName", "Example Merchant")
            })
        }
    }

    private fun getBaseCardPaymentMethod(): JSONObject {
        return JSONObject().apply {
            put("type", "CARD")
            put("parameters", JSONObject().apply {
                put("allowedCardNetworks", JSONArray().apply {
                    put("VISA")
                    put("MASTERCARD")
                })
                put("allowedAuthMethods", JSONArray().apply {
                    put("PAN_ONLY")
                    put("CRYPTOGRAM_3DS")
                })
            })
            put("tokenizationSpecification", JSONObject().apply {
                put("type", "PAYMENT_GATEWAY")
                put("parameters", JSONObject().apply {
                    put("gateway", "example") // Replace 'example' with your actual gateway
                    put("gatewayMerchantId", "exampleMerchantId") // Replace with real ID
                })
            })
        }
    }
}

