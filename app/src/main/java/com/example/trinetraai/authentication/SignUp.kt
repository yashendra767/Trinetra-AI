package com.example.trinetraai.authentication

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.trinetraai.presetData.PostData
import com.example.trinetraai.presetData.ZoneData
import com.example.trinetraai.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class SignUp : AppCompatActivity() {

    private lateinit var postField : TextInputEditText
    private lateinit var locationField : TextInputEditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)


        postField = findViewById<TextInputEditText>(R.id.editTextPost)
        locationField = findViewById<TextInputEditText>(R.id.editTextLocation)

        val postOptions = PostData.postList
        val zoneOptions = ZoneData.zoneList

        postField.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.DarkAlertDialog)
                .setTitle("Select Post")
                .setItems(postOptions) { dialog, which ->
                    postField.setText(postOptions[which])
                }
                .show()
        }
        locationField.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this, R.style.DarkAlertDialog)
                .setTitle("Assigned Area")
                .setItems(zoneOptions) { dialog, which ->
                    locationField.setText(zoneOptions[which])
                }
            builder.show()
        }

    }
}