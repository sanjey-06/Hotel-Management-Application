package com.example.hotelbooking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Searchingpage : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var roomsRecyclerView: RecyclerView
    private lateinit var roomsAdapter: RoomsAdapter
    private val roomList = mutableListOf<Room>()

    private var startDate: String? = null
    private var endDate: String? = null
    private var adults: Int = 1
    private var children: Int = 0
    private var rooms: Int = 1 // Default starting room value should be 1

    private lateinit var numberOfRoomsSelectedTextView: TextView
    private var totalRoomsSelected: Int = 0 // Ensure it starts from 0
    private var totalPrice: Int = 0 // Track the total price of selected rooms
    private val selectedRooms = mutableListOf<Room>() // Store selected rooms for passing to PaymentPage
    private var maxRoomsAllowed: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.searching_page)

        Log.d("SearchingPage", "Adults: $adults, Children: $children, Rooms: $rooms")

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("rooms")

        // Get the data passed from Booknowpage
        startDate = intent.getStringExtra("startDate")
        endDate = intent.getStringExtra("endDate")
        adults = intent.getIntExtra("adults", 1)
        children = intent.getIntExtra("children", 0)
        rooms = intent.getIntExtra("rooms", 1)

        // Set maxRoomsAllowed to the value passed from BookNow page
        maxRoomsAllowed = rooms

        Log.d("SearchingPage", "Received details: StartDate=$startDate, EndDate=$endDate, Adults=$adults, Children=$children, Rooms=$rooms")

        // Initialize RecyclerView
        roomsRecyclerView = findViewById(R.id.roomsRecyclerView)
        roomsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize TextView to display the number of rooms selected
        numberOfRoomsSelectedTextView = findViewById(R.id.numberofroomsselected)



        // Initialize RoomsAdapter and pass callbacks, along with maxRoomsAllowed
        roomsAdapter = RoomsAdapter(roomList, startDate, endDate,
            updateRoomCount = { totalRooms ->
                totalRoomsSelected = totalRooms
                numberOfRoomsSelectedTextView.text = "Total Rooms: $totalRoomsSelected"
                updateTotalPrice() // Update the total price whenever the room count changes
            },
            onSelectedRoomsChanged = { updatedSelectedRooms ->
                selectedRooms.clear()
                selectedRooms.addAll(updatedSelectedRooms)
                Log.d("SearchingPage", "Updated selected rooms: $selectedRooms")
                updateTotalPrice() // Update the price whenever the selected rooms list changes
            },
            maxRoomsAllowed = maxRoomsAllowed, // Pass the maxRoomsAllowed value here

        )
        roomsRecyclerView.adapter = roomsAdapter


        // Fetch rooms data from Firebase
        fetchRooms()

        // Initialize "Book Now" button
        val bookNowButton: Button = findViewById(R.id.book_now_button)
        bookNowButton.setOnClickListener {
            if (totalRoomsSelected == 0) {
                Toast.makeText(this, "Please select at least one room to proceed.", Toast.LENGTH_SHORT).show()
            } else {
                checkLoginStatusAndProceed()
            }
        }

        // Initialize other buttons and bottom navigation
        val createAccountButton: Button = findViewById(R.id.createAccountButton)
        val loginButton: Button = findViewById(R.id.loginButton)
        setupBottomNavigation()
        setupLoginButtons(loginButton, createAccountButton)
    }

    // Check if the user is logged in, and navigate accordingly
    private fun checkLoginStatusAndProceed() {
        val currentUser = auth.currentUser // Get the current logged-in user

        if (currentUser == null) {
            // If the user is not logged in, navigate to the Login page
            navigateToLoginPage()
        } else {
            // If the user is logged in, navigate directly to the Payment page with selected rooms
            navigateToPaymentPage()
        }
    }

    private fun navigateToLoginPage() {

        // Show a toast informing the user to log in
        Toast.makeText(this, "You need to log in to book rooms.", Toast.LENGTH_SHORT).show()
        // Navigate to the Login page
        val loginIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("isComingFromBookNow", true)  // Indicate that the login is coming from BookNow
            putExtra("startDate", startDate)
            putExtra("endDate", endDate)
            putExtra("rooms", rooms)
            putExtra("adults", adults)
            putExtra("children", children)
            putExtra("totalPrice",totalPrice)
            putParcelableArrayListExtra("selectedRooms", ArrayList(selectedRooms))
            Log.d("TotalPrice", "Total Price: $totalPrice")

        }
        startActivityForResult(loginIntent, REQUEST_CODE_LOGIN)
    }

    private fun navigateToPaymentPage() {
        // Calculate the total price for selected rooms
        val totalPrice = selectedRooms.sumOf { it.price * it.numberofrooms }

        Log.d("RoomDetails", "Navigating to PaymentPage with selected rooms: $selectedRooms")
        Log.d("RoomDetails", "Total price: $totalPrice")  // Log the total price to debug
        // Ensure at least one room is selected
        if (selectedRooms.isEmpty()) {
            Toast.makeText(this, "Please select at least one room.", Toast.LENGTH_SHORT).show()
            return
        }

        // Log the room details before passing to PaymentPage
        selectedRooms.forEach { room ->
            Log.d("SearchingPage", "Selected Room: ${room.name}, Quantity: ${room.numberofrooms}, Total Price: ${room.price * room.numberofrooms}")
        }



        // Create an Intent to pass data to the PaymentPage
        val intent = Intent(this, Paymentpage::class.java)

        // Pass the selectedRooms list (ParcelableArrayList)
        intent.putParcelableArrayListExtra("selectedRooms", ArrayList(selectedRooms))

        // Pass other booking details like dates and number of adults/children
        intent.putExtra("startDate", startDate)
        intent.putExtra("endDate", endDate)
        intent.putExtra("adults", adults)
        intent.putExtra("children", children)
        intent.putExtra("numberofoccupants", (adults +children))

        // Pass the total price to the PaymentPage
        intent.putExtra("totalPrice", totalPrice)

        // Start the PaymentPage activity
        startActivity(intent)
    }

    // Handle result from login page (if necessary)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOGIN && resultCode == RESULT_OK) {
            // If the login is successful, directly navigate to the Payment page
            navigateToPaymentPage()
        }
    }

    private fun fetchRooms() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                roomList.clear()

                // Check if the user has selected 2 or more rooms
                val displayAllRooms = rooms >= 2 // `rooms` is passed from the BookNow page


                for (roomSnapshot in snapshot.children) {
                    val roomName = roomSnapshot.key ?: "Unknown Room"

                    for (subrooms in roomSnapshot.children) {
                        val map = subrooms.getValue() as? Map<String, Any> ?: continue

                        val room = Room(
                            id = (map["id"] as? Long)?.toInt() ?: 0,
                            name = roomName,
                            maxoccupants = (map["maxoccupants"] as? Long)?.toInt() ?: 0,
                            price = (map["price"] as? Long)?.toInt() ?: 0,
                            bookings = map["bookings"] as? String ?: "",
                            imageURL = map["imageURL"] as? String ?: "",
                            numberofoccupants = adults + children,
                            numberofrooms = 0 // Start with 0 rooms selected
                        )
                        Log.d("SearchingPage", "Room price for $roomName: ${room.price}") // Log the price here

                        val totalOccupants = adults + children
                        if (displayAllRooms || room.maxoccupants >= totalOccupants) {
                            roomList.add(room)
                        }
                    }
                }

                roomsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Searchingpage, "Failed to load rooms: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTotalPrice() {
        totalPrice = selectedRooms.sumOf { it.price * it.numberofrooms }
        Log.d("SearchingPage", "Updated total price: $totalPrice")
    }

    // This function is responsible for navigating to the PaymentPage
//    private fun navigateToPaymentPage() {
//        // Calculate the total price for selected rooms
//        val totalPrice = selectedRooms.sumOf { it.price * it.numberofrooms }
//
//        Log.d("RoomDetails", "Navigating to PaymentPage with selected rooms: $selectedRooms")
//        Log.d("RoomDetails", "Total price: $totalPrice")  // Log the total price to debug
//        // Ensure at least one room is selected
//        if (selectedRooms.isEmpty()) {
//            Toast.makeText(this, "Please select at least one room.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Log the room details before passing to PaymentPage
//        selectedRooms.forEach { room ->
//            Log.d("SearchingPage", "Selected Room: ${room.name}, Quantity: ${room.numberofrooms}, Total Price: ${room.price * room.numberofrooms}")
//        }
//
//        // Create an Intent to pass data to the PaymentPage
//        val intent = Intent(this, Paymentpage::class.java)
//
//        // Pass the selectedRooms list (ParcelableArrayList)
//        intent.putParcelableArrayListExtra("selectedRooms", ArrayList(selectedRooms))
//
//        // Pass other booking details like dates and number of adults/children
//        intent.putExtra("startDate", startDate)
//        intent.putExtra("endDate", endDate)
//        intent.putExtra("adults", adults)
//        intent.putExtra("children", children)
//
//        // Pass the total price to the PaymentPage
//        intent.putExtra("totalPrice", totalPrice)
//
//        // Start the PaymentPage activity
//        startActivity(intent)
//    }

    private fun setupBottomNavigation() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomnavbar)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_1 -> {
                    val intent = Intent(this, Homepage::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    true
                }
                R.id.page_2 -> {
                    val intent = Intent(this, MyBookings::class.java)
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupLoginButtons(loginButton: Button, createAccountButton: Button) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            confirmUserLoggedInAndFetchName(currentUser.uid, loginButton, createAccountButton)
        } else {
            loginButton.text = "Login"
            createAccountButton.visibility = View.VISIBLE
        }

        createAccountButton.setOnClickListener { navigateToSignup() }
        loginButton.setOnClickListener {
            if (auth.currentUser == null) navigateToLogin()
            else showPopupMenu(loginButton)
        }
    }

    private fun confirmUserLoggedInAndFetchName(userId: String, loginButton: Button, createAccountButton: Button) {
        database.root.child("users").child(userId).child("firstName").get()
            .addOnSuccessListener { snapshot ->
                val firstName = snapshot.value as? String
                if (firstName != null) {
                    loginButton.text = firstName
                    createAccountButton.visibility = View.GONE
                } else {
                    handleInvalidSession(loginButton, createAccountButton)
                }
            }
            .addOnFailureListener {
                handleInvalidSession(loginButton, createAccountButton)
            }
    }

    private fun handleInvalidSession(loginButton: Button, createAccountButton: Button) {
        auth.signOut()
        loginButton.text = "Login"
        createAccountButton.visibility = View.VISIBLE
    }

    private fun showPopupMenu(view: Button) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_signout -> {
                    auth.signOut()
                    Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Homepage::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun navigateToSignup() {
        val intent = Intent(this, SigninActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    companion object {
        const val REQUEST_CODE_LOGIN = 1001
    }
}
