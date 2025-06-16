package com.example.trinetraai.Authentication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.trinetraai.PresetData.ZoneData
import com.example.trinetraai.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val postField = findViewById<TextInputEditText>(R.id.editTextPost)
        val locationField = findViewById<TextInputEditText>(R.id.editTextLocation)



        //TODO( complete this)
        val postOptions = arrayOf("Constable", "Head Constable", "SI", "Inspector", "ACP", "DCP")
        val zoneOptions = ZoneData.zoneList


        postField.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.DarkAlertDialog) // Apply dark style
                .setTitle("Select Post")
                .setItems(postOptions) { dialog, which ->
                    postField.setText(postOptions[which])
                }
                .show()
        }

        locationField.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this, R.style.DarkAlertDialog)
                .setTitle("Select Zone")
                .setItems(zoneOptions) { dialog, which ->
                    locationField.setText(zoneOptions[which])
                }
            builder.show()
        }

    }
}