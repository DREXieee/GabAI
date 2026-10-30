package com.gabai.presentation.ui.preevaluation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gabai.data.local.repository.AuthSessionStore
import com.gabai.data.remote.api.RemotePreEvaluationRepositoryImpl
import com.gabai.data.remote.api.RetrofitClient
import com.gabai.domain.model.PreEvaluationQuestions
import com.gabai.domain.model.PreEvaluationResult
import com.gabai.domain.repository.PreEvaluationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

// step 0 is the intro; steps 1..QUESTION_COUNT are the questions, PHQ-9 first.
data class PreEvaluationUiState(
    val isChecking: Boolean = true,
    val step: Int = 0,
    // One entry per question, null until answered.
    val answers: List<Int?> = List(PreEvaluationQuestions.ALL.size) { null },
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val result: PreEvaluationResult? = null,
    // Answered before (e.g. on the website): go straight to the chat.
    val alreadyDone: Boolean = false
) {
    val questionCount: Int get() = answers.size
    val questionIndex: Int get() = step - 1
}

class PreEvaluationViewModel(
    private val repository: PreEvaluationRepository,
    private val authStore: AuthSessionStore
) : ViewModel() {

    private val _state = MutableStateFlow(PreEvaluationUiState())
    val state: StateFlow<PreEvaluationUiState> = _state.asStateFlow()

    init {
        checkAlreadyDone()
    }

    // A fresh sign-in says whether it's needed; only a session saved before
    // the pre-evaluation existed has to ask the backend. If that check fails,
    // the questions are shown anyway: answering twice does no harm.
    private fun checkAlreadyDone() {
        viewModelScope.launch {
            if (authStore.session.first()?.needsPreEvaluation == null) {
                val done = runCatching { repository.getLatest() != null }.getOrDefault(false)
                if (done) {
                    authStore.markPreEvaluationDone()
                    _state.update { it.copy(alreadyDone = true) }
                }
            }
            _state.update { it.copy(isChecking = false) }
        }
    }

    fun start() = _state.update { it.copy(step = 1) }

    fun answer(value: Int) = _state.update { state ->
        state.copy(answers = state.answers.toMutableList().also { it[state.questionIndex] = value }, error = null)
    }

    fun back() = _state.update { it.copy(step = (it.step - 1).coerceAtLeast(0), error = null) }

    fun next() {
        val current = _state.value
        if (current.answers[current.questionIndex] == null) return
        if (current.step < current.questionCount) {
            _state.update { it.copy(step = it.step + 1) }
        } else {
            submit()
        }
    }

    private fun submit() {
        val answers = _state.value.answers.map { it ?: return }
        val phq9Count = PreEvaluationQuestions.PHQ9.size
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }
            try {
                val result = repository.submit(answers.take(phq9Count), answers.drop(phq9Count))
                authStore.markPreEvaluationDone()
                _state.update { it.copy(isSubmitting = false, result = result) }
            } catch (e: HttpException) {
                _state.update { it.copy(isSubmitting = false, error = "Couldn't save your answers (${e.code()}). Please try again.") }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isSubmitting = false, error = "Couldn't reach GabAI. Check your connection and try again.")
                }
            }
        }
    }
}

class PreEvaluationViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val context = appContext.applicationContext
        val repository = RemotePreEvaluationRepositoryImpl(RetrofitClient.getApiService(context))
        @Suppress("UNCHECKED_CAST")
        return PreEvaluationViewModel(repository, AuthSessionStore(context)) as T
    }
}
