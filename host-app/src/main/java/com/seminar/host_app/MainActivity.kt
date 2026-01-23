package com.seminar.host_app

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.seminar.smart_survey_sdk.SmartSurvey
import com.seminar.smart_survey_sdk.SmartSurveyListener
import com.seminar.smart_survey_sdk.SmartSurveyView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    private lateinit var surveyOverlay: View
    private lateinit var dimBg: View
    private lateinit var btnOpenSurvey: View
    private lateinit var btnCloseSurvey: View
    private lateinit var surveyContainer: FrameLayout

    private var surveyView: SmartSurveyView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate UI from XML
        setContentView(R.layout.activity_main)
        // Init SDK (the only "must have" part)
        SmartSurvey.init(
            context = applicationContext,
            baseUrl = "https://survey-sdk-server.onrender.com/",
            apiKey = "SSK_faf36299-194f-4d27-b936-6d2948e6ee09"
        )

        // Bind views
        surveyOverlay = findViewById(R.id.surveyOverlay)
        dimBg = findViewById(R.id.dimBg)
        btnOpenSurvey = findViewById(R.id.btnOpenSurvey)
        btnCloseSurvey = findViewById(R.id.btnCloseSurvey)
        surveyContainer = findViewById(R.id.surveyContainer)

        // Clicks
        btnOpenSurvey.setOnClickListener { openSurvey() }
        btnCloseSurvey.setOnClickListener { closeSurvey() }

        // Close the survey when clicking the background
        dimBg.setOnClickListener { closeSurvey() }
    }

    private fun openSurvey() {
        surveyOverlay.visibility = View.VISIBLE

        // Create once, reuse
        if (surveyView == null) {
            surveyView = SmartSurveyView(this).apply {
                setListener(object : SmartSurveyListener {
                    override fun onSurveyLoaded(surveyId: String) {
                        Toast.makeText(
                            this@MainActivity,
                            "Loaded survey",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onSurveySubmitted(surveyId: String) {
                        Toast.makeText(this@MainActivity, "Survey submitted", Toast.LENGTH_SHORT)
                            .show()
                        closeSurvey()
                    }

                    override fun onError(error: Throwable) {
                        closeSurvey()
//                        Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        Toast.makeText(
                            this@MainActivity,
                            "Error loading survey",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                })
            }
            surveyContainer.addView(
                surveyView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
        // Load survey each time you open
        surveyView?.loadRandomSurvey()
    }

    private fun closeSurvey() {
        surveyOverlay.visibility = View.GONE
    }
}
