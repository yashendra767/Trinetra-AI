package com.example.trinetraai.drawer_activities.about

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.trinetraai.R
import com.example.trinetraai.drawer_activities.about.TeamPagerAdapter
import com.example.trinetraai.drawer_activities.about.ZoomOutPageTransformer
import com.google.android.material.button.MaterialButton

class About : AppCompatActivity() {

    private lateinit var btnMeetTeam: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        btnMeetTeam = findViewById(R.id.btn_meet_team)
        val aboutDescription = findViewById<TextView>(R.id.about_description)

        val aboutText = """
    Trinetra AI is a <b>prototype crime mapping and predictive policing application</b> built to assist law enforcement with data-driven insights. It visualizes <b>FIRs</b> through <b>heatmaps</b>, predicts <b>crime trends</b> using <b>machine learning</b>, and generates <b>optimal patrol routes</b> to improve safety.
    <br><br>
    The current version uses <b>dummy FIR and zone data from New Delhi</b> and is designed to scale seamlessly when provided with <b>real-time and larger datasets</b>.
    <br><br>
    The <b>ML models</b> are trained manually on a limited dataset to ensure all features are functional. Future versions will support <b>automated learning and continuous improvement</b> as data increases.
    <br><br>
    Our goal is to empower policing with <b>AI-backed decisions</b> for enhanced <b>women’s safety</b>, <b>real-time response</b>, and <b>smart patrol planning</b>.
""".trimIndent()

        aboutDescription.text = Html.fromHtml(aboutText, Html.FROM_HTML_MODE_LEGACY)

        btnMeetTeam.setOnClickListener {
            showTeamDialog()
        }
    }

    private fun showTeamDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_team, null)
        val viewPager = dialogView.findViewById<ViewPager2>(R.id.team_view_pager)

        val teamMembers = listOf(
            TeamMember(
                "Aryan Sharma",
                "Package Developer",
                "Developed roadmap, Crime trend Prediction, Heatmap, managed Firebase.",
                R.drawable.aryan
            ),
            TeamMember(
                "Yashendra Awasthi",
                "Android Developer",
                "Designed UI and implemented patrol routes and core app logic.",
                R.drawable.yashendra
            ),
            TeamMember(
                "Aayush Gujjar",
                "Backend Engineer",
                "Handled Auth, data preprocessing and ML backend.",
                R.drawable.aayush
            )
        )

        viewPager.adapter = TeamPagerAdapter(teamMembers)
        viewPager.setPageTransformer(ZoomOutPageTransformer())

        val dialog = AlertDialog.Builder(this, R.style.RoundedDialog)
            .setView(dialogView)
            .create()

        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setDimAmount(0.7f)
        dialog.show()

    }

}