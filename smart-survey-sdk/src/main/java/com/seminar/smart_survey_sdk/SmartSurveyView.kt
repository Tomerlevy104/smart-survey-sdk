package com.seminar.smart_survey_sdk

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView


class SmartSurveyView : FrameLayout {

    private lateinit var tvSurveyTitle: MaterialTextView
    private lateinit var questionsContainer: LinearLayoutCompat
    private lateinit var btnSubmit: MaterialButton
    private var listener: SmartSurveyListener? = null
    private var currentSurveyId: String? = null

    // Constructor I - Called when inflating from code
    constructor(context: Context) : super(context) {
        initView(context)
    }

    // Constructor II - Called when inflating from XML
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    // Constructor III - Called when inflating from XML with style
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    private fun initView(context: Context) {
        removeAllViews()

        val root = LayoutInflater.from(context).inflate(
            R.layout.view_smart_survey,
            this,
            true
        )

        tvSurveyTitle = root.findViewById(R.id.tvSurveyTitle)
        questionsContainer = root.findViewById(R.id.questionsContainer)
        btnSubmit = root.findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener { submit() }

        // Placeholder text for now
        tvSurveyTitle.text = "Smart Survey (SDK UI Loaded)"
    }

    fun setListener(listener: SmartSurveyListener?) {
        this.listener = listener
    }

    fun loadSurvey(surveyId: String) {
        try {
            SmartSurvey.requireConfig()

            currentSurveyId = surveyId
            tvSurveyTitle.text = "Loading survey: $surveyId"

            // TODO: Retrofit GET + render questions

            tvSurveyTitle.text = "Survey loaded: $surveyId"
            listener?.onSurveyLoaded(surveyId)
        } catch (t: Throwable) {
            tvSurveyTitle.text = "Error: ${t.message}"
            listener?.onError(t)
        }
    }

    fun submit() {
        val surveyId = currentSurveyId
        if (surveyId == null) {
            val err = IllegalStateException("No survey loaded. Call loadSurvey(surveyId) first.")
            tvSurveyTitle.text = "Error: ${err.message}"
            listener?.onError(err)
            return
        }

        try {
            SmartSurvey.requireConfig()

            tvSurveyTitle.text = "Submitting survey: $surveyId"

            // TODO: Retrofit POST + payload

            tvSurveyTitle.text = "Survey submitted: $surveyId"
            listener?.onSurveySubmitted(surveyId)
        } catch (t: Throwable) {
            tvSurveyTitle.text = "Error: ${t.message}"
            listener?.onError(t)
        }
    }
}
