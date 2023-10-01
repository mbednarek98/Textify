package master.mb.texify.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.core.content.FileProvider.getUriForFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import master.mb.texify.fileproviders.ComposeFileProvider


data class ImageUIModel(
    val imageUri: Uri? = null,
    val hasImage: Boolean = false
)

class ImageViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ImageUIModel())
    val uiState : StateFlow<ImageUIModel> = _uiState.asStateFlow()


    init {
        _uiState.value = ImageUIModel(null, false)
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.value = ImageUIModel(uri, uri != null)
    }


    fun onImageCaptured(success: Boolean) {
        when {
            _uiState.value.imageUri != null -> _uiState.value = ImageUIModel(_uiState.value.imageUri, success)
            else ->{
                _uiState.value = ImageUIModel(null, false)
            }
        }
    }

    fun onCameraClick(
        uri: Uri,
        cameraLauncher: ManagedActivityResultLauncher<Uri, Boolean>
    ) : Uri {
        _uiState.value = ImageUIModel(uri, false)
        cameraLauncher.launch(uri)
        return uri
    }


}