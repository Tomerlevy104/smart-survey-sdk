package com.seminar.smart_survey_sdk

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.seminar.smart_survey_sdk.dto.AnswerDto
import com.seminar.smart_survey_sdk.dto.QuestionDto
import com.seminar.smart_survey_sdk.dto.QuestionType
import com.seminar.smart_survey_sdk.dto.SurveyDto
import com.seminar.smart_survey_sdk.dto.SurveyResponseDto
import com.seminar.smart_survey_sdk.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SmartSurveyView : FrameLayout {

    private lateinit var tvSurveyTitle: MaterialTextView
    private lateinit var questionsContainer: LinearLayoutCompat
    private lateinit var btnSubmit: MaterialButton
    private var listener: SmartSurveyListener? = null
    private var currentSurveyId: String? = null
    private lateinit var progressLoading: com.google.android.material.progressindicator.CircularProgressIndicator
    private lateinit var scrollContent: android.widget.ScrollView
    private lateinit var loadingOverlay: View


    private val answers: MutableMap<String, String> = mutableMapOf()
    private var currentSurvey: SurveyDto? = null

    private val viewJob = SupervisorJob()
    private val viewScope = CoroutineScope(Dispatchers.Main + viewJob)

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewJob.cancel()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    // Constructor I - Called when inflating from code
    constructor(context: Context) : super(context) {
        initView(context)
    }

    // Constructor II - Called when inflating from XML
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    // Constructor III - Called when inflating from XML with style
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Initialize view
    private fun initView(context: Context) {
        removeAllViews()

        val view = LayoutInflater.from(context).inflate(
            R.layout.view_smart_survey,
            this,
            true
        )

        findViews(view)
        setupListeners()
        // Placeholder text for now
        tvSurveyTitle.text = "Smart Survey (SDK UI Loaded)"
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Find views
    private fun findViews(view: View) {
        tvSurveyTitle = view.findViewById(R.id.tvSurveyTitle) // Survey title
        questionsContainer = view.findViewById(R.id.questionsContainer) // Questions container
        btnSubmit = view.findViewById(R.id.btnSubmit) // Submit button
        loadingOverlay = view.findViewById(R.id.loadingOverlay) // Gray screen for loading
        progressLoading = view.findViewById(R.id.progressLoading) // Loading spinner
        scrollContent = view.findViewById(R.id.scrollContent) // Scrollable content
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Setup listeners
    private fun setupListeners() {
        btnSubmit.setOnClickListener { submit() }
    }

    fun setListener(listener: SmartSurveyListener?) {
        this.listener = listener
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Load survey by id function
    fun loadSurveyById(surveyId: String) {
        SmartSurvey.requireConfig()
        currentSurveyId = surveyId

        showLoading()
        tvSurveyTitle.text = "Loading survey..."
        viewScope.launch {
            try {
                val survey = withContext(Dispatchers.IO) {
                    ApiClient.surveyApi().getSurveyById(surveyId) // Call to server
                }
                renderSurvey(survey)
                hideLoading()
                listener?.onSurveyLoaded(surveyId)
            } catch (t: Throwable) {
                hideLoading()
                tvSurveyTitle.text = "Error: ${t.message}"
                listener?.onError(t)
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Load survey by id function
    fun loadRandomSurvey() {
        SmartSurvey.requireConfig()

        showLoading()
        tvSurveyTitle.text = "Loading survey..."
        viewScope.launch {
            try {
                val survey = withContext(Dispatchers.IO) {
                    ApiClient.surveyApi().getRandomSurvey() // // Sending the request to Server
                }
                currentSurveyId = survey.id
                renderSurvey(survey)
                hideLoading()
                listener?.onSurveyLoaded(survey.id)
            } catch (t: Throwable) {
                hideLoading()
                tvSurveyTitle.text = "Error: ${t.message}"
                listener?.onError(t)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Submit function
    fun submit() {
        val survey = currentSurvey
        val surveyId = currentSurveyId

        if (survey == null || surveyId.isNullOrBlank()) {
            val err = IllegalStateException("No survey loaded. Call loadSurveyById() / loadRandomSurvey() first.")
            tvSurveyTitle.text = "Error: ${err.message}"
            listener?.onError(err)
            return
        }

        // Client-side validation: all questions must be answered
        val missing = survey.questions
            .map { it.id }
            .filter { it.isNullOrBlank().not() }
            .filter { qId -> answers[qId].isNullOrBlank() }

        if (missing.isNotEmpty()) {
            val err = IllegalStateException("Please answer all questions before submitting.")
            tvSurveyTitle.text = "Please answer all questions."
            listener?.onError(err)
            return
        }

        // Build payload
        val payload = SurveyResponseDto(
            surveyId = surveyId,
            answers = answers.map { (questionId, value) ->
                AnswerDto(questionId = questionId, value = value)
            }
        )

        showLoading()
        tvSurveyTitle.text = "Submitting..."

        viewScope.launch {
            try {
                SmartSurvey.requireConfig()

                val responseId = withContext(Dispatchers.IO) {
                    ApiClient.surveyResponseApi().submitSurveyResponse(payload) // Sending the request to Server
                }

                hideLoading()
                tvSurveyTitle.text = "Survey submitted!"
                listener?.onSurveySubmitted(surveyId) // Successful response

            } catch (t: Throwable) { // Failed response
                hideLoading()
                tvSurveyTitle.text = "Error: ${t.message}"
                listener?.onError(t)
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    private fun renderSurvey(survey: SurveyDto) {
        currentSurvey = survey
        currentSurveyId = survey.id
        tvSurveyTitle.text = survey.title
        questionsContainer.removeAllViews()
        answers.clear()

        survey.questions.forEach { q ->
            renderQuestion(q)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Routes for questions options
    private fun renderQuestion(q: QuestionDto) {
        when (q.type) {
            QuestionType.TEXT -> renderTextQuestion(q)
            QuestionType.SINGLE_CHOICE -> renderSingleChoiceQuestion(q)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Render TEXT question
    private fun renderTextQuestion(q: QuestionDto) {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.item_question_text, questionsContainer, false)

        val tvPrompt = itemView.findViewById<MaterialTextView>(R.id.tvPrompt)
        val etAnswer = itemView.findViewById<TextInputEditText>(R.id.etAnswer)

        tvPrompt.text = q.prompt

        etAnswer.addTextChangedListener {
            answers[q.id] = it?.toString().orEmpty()
        }

        questionsContainer.addView(itemView)
    }


    //////////////////////////////////////////////////////////////////////////////////
    // Render SINGLE CHOICE question
    private fun renderSingleChoiceQuestion(q: QuestionDto) {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.item_question_single_choice, questionsContainer, false)

        val tvPrompt = itemView.findViewById<MaterialTextView>(R.id.tvPrompt)
        val rgOptions = itemView.findViewById<RadioGroup>(R.id.rgOptions)

        tvPrompt.text = q.prompt
        rgOptions.removeAllViews()

        val opts = q.options.orEmpty()

        opts.forEachIndexed { index, option ->
            val rb = RadioButton(context).apply {
                id = generateViewId() // safe unique id
                text = option
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = if (index == 0) 0 else 6
                }
            }
            rgOptions.addView(rb)
        }

        rgOptions.setOnCheckedChangeListener { group, checkedId ->
            val selected = group.findViewById<RadioButton>(checkedId)?.text?.toString()
            if (!selected.isNullOrBlank()) {
                answers[q.id] = selected
            } else {
                answers.remove(q.id)
            }
        }

        questionsContainer.addView(itemView)
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Loading indicator
    private fun showLoading() {
        loadingOverlay.visibility = VISIBLE
        progressLoading.visibility = VISIBLE
        scrollContent.visibility = INVISIBLE
        btnSubmit.isEnabled = false
    }

    private fun hideLoading() {
        loadingOverlay.visibility = GONE
        progressLoading.visibility = GONE
        scrollContent.visibility = VISIBLE
        btnSubmit.isEnabled = true
    }
    //////////////////////////////////////////////////////////////////////////////////
}
