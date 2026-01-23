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

    private val viewJob =
        SupervisorJob() // SupervisorJob is a Job that allows you to run several coroutines together, so that if one fails, the others are not canceled.
    private val viewScope =
        CoroutineScope(Dispatchers.Main + viewJob) // viewScope is a CoroutineScope that runs coroutines on the Main thread, and is managed by viewJob.

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
        // Placeholder text
        tvSurveyTitle.text = context.getString(R.string.smart_survey)
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
        tvSurveyTitle.text = context.getString(R.string.loading_survey)
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
                tvSurveyTitle.text = context.getString(R.string.error, t.message)
                listener?.onError(t)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Load random survey function
    fun loadRandomSurvey() {
        SmartSurvey.requireConfig()

        showLoading()
        tvSurveyTitle.text = context.getString(R.string.loading_survey)
        viewScope.launch {
            try {
                val survey = withContext(Dispatchers.IO) {
                    ApiClient.surveyApi().getRandomSurvey() // Sending the request to Server
                }
                currentSurveyId = survey.id
                renderSurvey(survey)
                hideLoading()
                listener?.onSurveyLoaded(survey.id)
            } catch (t: Throwable) {
                hideLoading()
                tvSurveyTitle.text = context.getString(R.string.error, t.message)
                listener?.onError(t)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Submit function
    internal fun submit() {
        val survey = currentSurvey
        val surveyId = currentSurveyId

        if (survey == null || surveyId.isNullOrBlank()) {
            val err =
                IllegalStateException("No survey loaded. Call loadSurveyById() / loadRandomSurvey() first.")
            tvSurveyTitle.text = context.getString(R.string.additional_error, err.message)
            listener?.onError(err)
            return
        }

        // Client-side validation - all questions must be answered
        val missing = survey.questions
            .map { it.id }
            .filter { id -> answers[id].orEmpty().trim().isBlank() }

        if (missing.isNotEmpty()) {
            val err = IllegalStateException("Please answer all questions before submitting")
            tvSurveyTitle.text = context.getString(R.string.please_answer_all_questions)
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
        tvSurveyTitle.text = context.getString(R.string.submitting)

        viewScope.launch {
            try {
                SmartSurvey.requireConfig()

                val response = withContext(Dispatchers.IO) {
                    ApiClient.surveyResponseApi()
                        .submitSurveyResponse(payload) // Sending the request to Server
                }

                hideLoading()
                tvSurveyTitle.text = context.getString(R.string.survey_submitted)
                listener?.onSurveySubmitted(surveyId) // Successful response

            } catch (t: Throwable) { // Failed response
                hideLoading()
                tvSurveyTitle.text = context.getString(R.string.error, t.message)
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

        survey.questions.forEach { que ->
            renderQuestion(que)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Routes for questions options
    private fun renderQuestion(que: QuestionDto) {
        when (que.type) {
            QuestionType.TEXT -> renderTextQuestion(que)
            QuestionType.SINGLE_CHOICE -> renderSingleChoiceQuestion(que)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Render TEXT question
    private fun renderTextQuestion(que: QuestionDto) {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.item_question_text, questionsContainer, false)

        val tvPrompt = itemView.findViewById<MaterialTextView>(R.id.tvPrompt)
        val etAnswer = itemView.findViewById<TextInputEditText>(R.id.etAnswer)

        tvPrompt.text = que.prompt

        etAnswer.addTextChangedListener {
            answers[que.id] = it?.toString().orEmpty()
        }

        questionsContainer.addView(itemView)
    }

    //////////////////////////////////////////////////////////////////////////////////
    // Render SINGLE CHOICE question
    private fun renderSingleChoiceQuestion(que: QuestionDto) {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.item_question_single_choice, questionsContainer, false)

        val tvPrompt = itemView.findViewById<MaterialTextView>(R.id.tvPrompt)
        val rgOptions = itemView.findViewById<RadioGroup>(R.id.rgOptions)

        tvPrompt.text = que.prompt
        rgOptions.removeAllViews()

        val opts = que.options.orEmpty()

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
                answers[que.id] = selected
            } else {
                answers.remove(que.id)
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
