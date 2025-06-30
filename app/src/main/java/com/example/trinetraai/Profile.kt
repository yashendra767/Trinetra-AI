package com.example.trinetraai

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.trinetraai.authentication.Login
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class Profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val nameText = findViewById<TextView>(R.id.nameText)
        val emailText = findViewById<TextView>(R.id.emailText)
        val joinedOnText = findViewById<TextView>(R.id.joinedOnText)
        val areaText = findViewById<TextView>(R.id.areaText)
        val postText = findViewById<TextView>(R.id.postText)
        val logoutBtn = findViewById<MaterialButton>(R.id.logoutButton)
        val editProfileBtn = findViewById<MaterialButton>(R.id.editProfileButton)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        emailText.text = currentUser.email
        val joinDate = currentUser.metadata?.creationTimestamp
        if (joinDate != null) {
            val formatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(joinDate))
            joinedOnText.text = "Joined on: $formatted"
        }

        val uid = currentUser.uid
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    nameText.text = doc.getString("name") ?: "Unknown"
                    postText.text = "Post: ${doc.getString("post") ?: "N/A"}"
                    areaText.text = "Assigned Area: ${doc.getString("assignedArea") ?: "N/A"}"
                } else {
                    Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        // Logout button functionality
        logoutBtn.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        editProfileBtn.setOnClickListener {
            Toast.makeText(this, "Edit profile baaki hai", Toast.LENGTH_SHORT).show()
        }
    }
}
