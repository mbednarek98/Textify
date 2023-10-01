package master.mb.texify.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.deepl.api.DeepLException
import com.deepl.api.Translator
import com.deepl.api.TranslatorOptions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import master.mb.texify.BuildConfig
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

data class TranslationUiModel(
    val imageUri: Uri? = null,
    val captureText: String = "",
    val translatedText : String = ""
)

class TranslationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TranslationUiModel())
    val uiState: StateFlow<TranslationUiModel> = _uiState.asStateFlow()

    init {
        _uiState.value = TranslationUiModel(null, "", "")
    }

    @OptIn(ExperimentalPermissionsApi::class)
    fun getTextToShowGivenPermissions(
        permissions: List<PermissionState>,
        shouldShowRationale: Boolean
    ): String {
        val revokedPermissionsSize = permissions.size
        if (revokedPermissionsSize == 0) return ""

        val textToShow = StringBuilder()

        for (i in permissions.indices) textToShow.append(permissions[i].permission + "\n")

        textToShow.append(if (revokedPermissionsSize == 1) "\n\nThis permission is" else "\n\nThese permissions are")
        textToShow.append(
            if (shouldShowRationale) {
                " important.\nPlease grant all of them for the app to function properly."
            } else {
                " denied.\nThe app cannot function without them."
            }
        )
        return textToShow.toString()
    }


    fun changeImageValue(uri: Uri? = null, text: String = "", translatedText: String = "") {
        _uiState.value = TranslationUiModel(uri, text, translatedText)
    }


    fun getTextFromImage(context: Context) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image: InputImage
        try {
            image = InputImage.fromFilePath(context, uiState.value.imageUri!!)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val revisedVisionText = visionText.text.replace("\n", " ")
                    changeImageValue(_uiState.value.imageUri, revisedVisionText)
                    if(revisedVisionText.isNotEmpty() || revisedVisionText.length > 2) translateTextAsync(visionText.text)
                }
                .addOnFailureListener { e ->
                    Log.d("Textify_OVR", "Error: ${e.message}")
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }


    }



    private fun translateTextAsync(text: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val translator = Translator(BuildConfig.DEEPL_API_KEY)
            val res = translator.translateText(text, null, "EN-US")
            Log.d("Translate", res.text.toString())
            val revisedVisionText = res.text.replace("\n", " ")
            _uiState.value = TranslationUiModel(_uiState.value.imageUri, _uiState.value.captureText, revisedVisionText)
        }
    }
}