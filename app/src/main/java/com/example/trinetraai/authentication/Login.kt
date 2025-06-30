package com.example.trinetraai.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.trinetraai.LandingDashboard
import com.example.trinetraai.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val emailEditText = findViewById<TextInputEditText>(R.id.loginEmail)
        val passEditText = findViewById<TextInputEditText>(R.id.loginPass)
        val loginButton = findViewById<MaterialButton>(R.id.buttonLogin)
        val signUpText = findViewById<TextView>(R.id.tVSignUpDirect)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passEditText.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Invalid email format"
                return@setOnClickListener
            }

            if (password.length < 6) {
                passEditText.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            loginButton.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener

                    firestore.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LandingDashboard::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "User profile not found in Firestore.", Toast.LENGTH_SHORT).show()
                                loginButton.isEnabled = true
                            }
                        }
                }
                .addOnFailureListener {
                    loginButton.isEnabled = true
                    Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        signUpText.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }
    }
}
