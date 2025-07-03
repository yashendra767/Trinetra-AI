package com.example.trinetraai

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.trinetraai.bottom_fragments.HeatmapDashboard
import com.example.trinetraai.bottom_fragments.PatrolRoutes
import com.example.trinetraai.bottom_fragments.TrendAnalyser.TrendAnalyser
import com.example.trinetraai.bottom_fragments.WomenSafety
import com.example.trinetraai.drawer_activities.about.About
import com.example.trinetraai.drawer_activities.AllFIRsActivity
import com.example.trinetraai.drawer_activities.AllZones
import com.example.trinetraai.drawer_activities.Notifications
import com.example.trinetraai.drawer_activities.Settings
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LandingDashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private var profileDialog: AlertDialog? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landing_dashboard)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        val bottomNav = findViewById<me.ibrahimsn.lib.SmoothBottomBar>(R.id.adminBottomNav)


        setSupportActionBar(toolbar)

        replaceTheFragment(HeatmapDashboard())

        bottomNav.setOnItemSelectedListener {
            when (it) {
                0 -> replaceTheFragment(HeatmapDashboard())
                1 -> replaceTheFragment(TrendAnalyser())
                2 -> replaceTheFragment(PatrolRoutes())
                3 -> replaceTheFragment(WomenSafety())
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                showProfileDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_profile, null)

        val dialogName = dialogView.findViewById<TextView>(R.id.dialogName)
        val dialogEmail = dialogView.findViewById<TextView>(R.id.dialogEmail)
        val dialogProfileButton = dialogView.findViewById<TextView>(R.id.dialogProfileButton)

        val dialogNotifications = dialogView.findViewById<LinearLayout>(R.id.dialogNotifications)
        val dialogSettings = dialogView.findViewById<LinearLayout>(R.id.dialogSettings)
        val dialogAllFIRs = dialogView.findViewById<LinearLayout>(R.id.dialogAllFIRs)
        val dialogAllZones = dialogView.findViewById<LinearLayout>(R.id.dialogAllZones)
        val dialogAbout = dialogView.findViewById<LinearLayout>(R.id.dialogAbout)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            dialogEmail.text = currentUser.email ?: "No Email"
            FirebaseFirestore.getInstance().collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        dialogName.text = document.getString("name") ?: "No Name"
                    }
                }
        }

        profileDialog = AlertDialog.Builder(this, com.google.android.material.R.style.ThemeOverlay_Material3_Dialog)
            .setView(dialogView)
            .create()
        profileDialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        profileDialog?.window?.setDimAmount(0.7f)
        profileDialog?.window?.decorView?.elevation = 8f

        dialogProfileButton.setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
            profileDialog?.dismiss()
        }

        dialogNotifications.setOnClickListener {
            startActivity(Intent(this, Notifications::class.java))
            profileDialog?.dismiss()
        }

        dialogSettings.setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
            profileDialog?.dismiss()
        }

        dialogAllFIRs.setOnClickListener {
            startActivity(Intent(this, AllFIRsActivity::class.java))
            profileDialog?.dismiss()
        }

        dialogAllZones.setOnClickListener {
            startActivity(Intent(this, AllZones::class.java))
            profileDialog?.dismiss()
        }

        dialogAbout.setOnClickListener {
            startActivity(Intent(this, About::class.java))
            profileDialog?.dismiss()
        }

        profileDialog?.show()
    }



    override fun onBackPressed() {
        super.onBackPressed()
        if (profileDialog?.isShowing == true) {
            profileDialog?.dismiss()
        } else {
            moveTaskToBack(true)
        }
    }



    private fun replaceTheFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment)
            .commit()
    }

}

