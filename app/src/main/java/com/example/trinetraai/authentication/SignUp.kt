package com.example.trinetraai.authentication


import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trinetraai.LandingDashboard
import com.example.trinetraai.R
import com.example.trinetraai.presetData.PostData
import com.example.trinetraai.presetData.ZoneData
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var postList: List<String>
    private lateinit var zoneList: List<String>

    private lateinit var nameField: TextInputEditText
    private lateinit var emailField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var postField: AutoCompleteTextView
    private lateinit var areaField: AutoCompleteTextView
    private lateinit var signupButton: MaterialButton
    private lateinit var tVLoginReDirect: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        nameField = findViewById(R.id.signupName)
        emailField = findViewById(R.id.signupEmail)
        passwordField = findViewById(R.id.signupPass)
        postField = findViewById(R.id.editTextPostSignup)
        areaField = findViewById(R.id.editTextLocationSignup)
        signupButton = findViewById(R.id.buttonSignup)
        tVLoginReDirect = findViewById(R.id.tVLoginDirect)

        postList = PostData.postList.toList()
        zoneList = ZoneData.zoneList.toList()
        tVLoginReDirect.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        setupDropdowns()

        signupButton.setOnClickListener {
            registerUser()
        }
    }

    private fun setupDropdowns() {
        val postAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, postList)
        postAdapter.setDropDownViewResource(R.layout.spinner_item_white)
        postField.setAdapter(postAdapter)
        postField.setOnClickListener { postField.showDropDown() }

        val areaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, zoneList)
        areaAdapter.setDropDownViewResource(R.layout.spinner_item_white)
        areaField.setAdapter(areaAdapter)
        areaField.setOnClickListener { areaField.showDropDown() }
    }

    private fun registerUser() {
        val name = nameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()
        val post = postField.text.toString().trim()
        val area = areaField.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || post.isEmpty() || area.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.error = "Invalid email format"
            return
        }

        if (password.length < 6) {
            passwordField.error = "Password must be at least 6 characters"
            return
        }

        signupButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val userMap = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "post" to post,
                    "assignedArea" to area,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("users").document(uid)
                    .set(userMap)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LandingDashboard::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        signupButton.isEnabled = true
                        Toast.makeText(this, "Failed to save user data: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                signupButton.isEnabled = true
                Toast.makeText(this, "Sign up failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
