package com.example.trinetraai

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.trinetraai.authentication.Login
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class Profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var nameText: TextView
    private lateinit var emailText: TextView
    private lateinit var joinedOnText: TextView
    private lateinit var areaText: TextView
    private lateinit var postText: TextView
    private lateinit var logoutBtn: MaterialButton
    private lateinit var editProfileBtn: MaterialButton

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        nameText = findViewById(R.id.nameText)
        emailText = findViewById(R.id.emailText)
        joinedOnText = findViewById(R.id.joinedOnText)
        areaText = findViewById(R.id.areaText)
        postText = findViewById(R.id.postText)
        logoutBtn = findViewById(R.id.logoutButton)
        editProfileBtn = findViewById(R.id.editProfileButton)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        }

        userId = currentUser.uid
        emailText.text = currentUser.email

        val joinDate = currentUser.metadata?.creationTimestamp
        if (joinDate != null) {
            val formatted = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(joinDate))
            joinedOnText.text = "Joined on: $formatted"
        }

        loadProfile()

        logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        editProfileBtn.setOnClickListener {
            showEditProfileBottomSheet()
        }
    }

    private fun loadProfile() {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    nameText.text = doc.getString("name") ?: "Unknown"
                    areaText.text = "Assigned Area: ${doc.getString("assignedArea") ?: "N/A"}"
                    postText.text = "Post: ${doc.getString("post") ?: "N/A"}"
                }
            }
    }

    private fun showEditProfileBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.edit_profile, null)

        val editName = view.findViewById<TextInputEditText>(R.id.editName)
        val editEmail = view.findViewById<TextInputEditText>(R.id.editEmail)
        val editArea = view.findViewById<TextInputEditText>(R.id.editArea)
        val editPost = view.findViewById<TextInputEditText>(R.id.editPost)
        val saveBtn = view.findViewById<MaterialButton>(R.id.saveProfileButton)

        // Pre-fill current data
        editName.setText(nameText.text.toString())
        editEmail.setText(emailText.text.toString())
        editArea.setText(areaText.text.toString().replace("Assigned Area: ", ""))
        editPost.setText(postText.text.toString().replace("Post: ", ""))

        saveBtn.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val area = editArea.text.toString().trim()
            val post = editPost.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Name and Email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updates = mapOf(
                "name" to name,
                "assignedArea" to area,
                "post" to post
            )

            firestore.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener {
                    nameText.text = name
                    areaText.text = "Assigned Area: $area"
                    postText.text = "Post: $post"
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.setContentView(view)
        dialog.show()
    }
}
