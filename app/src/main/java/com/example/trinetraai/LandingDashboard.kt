package com.example.trinetraai

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.trinetraai.bottom_fragments.HeatmapDashboard
import com.example.trinetraai.bottom_fragments.PatrolRoutes
import com.example.trinetraai.bottom_fragments.TrendAnalyser
import com.example.trinetraai.bottom_fragments.WomenSafety
import com.example.trinetraai.drawer_fragments.AllFIRsFragment
import com.example.trinetraai.drawer_fragments.Notifications
import com.example.trinetraai.drawer_fragments.Settings
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp

class LandingDashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle


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
        navView = findViewById(R.id.adminNavView)
        val bottomNav = findViewById<me.ibrahimsn.lib.SmoothBottomBar>(R.id.adminBottomNav)

        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        replaceTheFragment(HeatmapDashboard())

        bottomNav.setOnItemSelectedListener {
            when (it) {
                0 -> replaceTheFragment(HeatmapDashboard())
                1 -> replaceTheFragment(TrendAnalyser())
                2 -> replaceTheFragment(PatrolRoutes())
                3 -> replaceTheFragment(WomenSafety())
            }
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_notification -> replaceTheFragment(Notifications())
                R.id.nav_Settings -> replaceTheFragment(Settings())
                R.id.nav_allFirs -> replaceTheFragment(AllFIRsFragment())
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, Profile::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    private fun replaceTheFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.adminFragmentContainer, fragment)
            .commit()
    }

}

