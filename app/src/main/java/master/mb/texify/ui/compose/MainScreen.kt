package master.mb.texify.ui.compose

import android.content.Context
import android.graphics.drawable.shapes.Shape
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import master.mb.texify.ui.theme.*
import master.mb.texify.R
import master.mb.texify.fileproviders.ComposeFileProvider
import master.mb.texify.ui.theme.Shapes
import master.mb.texify.viewmodels.TranslationViewModel
import master.mb.texify.ui.theme.TexifyTheme
import master.mb.texify.viewmodels.ImageViewModel

// TODO: JavaDoc for this function

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(mainViewModel: TranslationViewModel = viewModel()) {


    val multiplePermissionsState = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            rememberMultiplePermissionsState(
                listOf(
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.INTERNET,
                )
            )
        }
        else -> {
            rememberMultiplePermissionsState(
                listOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.INTERNET,
                )
            )
        }
    }


    val uiState by mainViewModel.uiState.collectAsState()


    when {
        multiplePermissionsState.allPermissionsGranted -> {
            MainLayout(
                excText = uiState.captureText,
                tranText = uiState.translatedText,
                onUriChange = {mainViewModel.changeImageValue(uri = it)},
                onImageChange = {mainViewModel.getTextFromImage(context = it)})
        }
        else -> CameraDeniedLayout(
            multiplePermissionsState = multiplePermissionsState
        ) { mainViewModel.getTextToShowGivenPermissions(
                multiplePermissionsState.revokedPermissions,
                multiplePermissionsState.shouldShowRationale
            )
        }

    }
}



// TODO: JavaDoc for this function

@Composable
fun MainLayout(imageViewModel: ImageViewModel = viewModel(),
               excText: String,
               tranText: String,
               onUriChange : (Uri) -> Unit,
               onImageChange: (Context)  -> Unit) {
    val uiState by imageViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            imageViewModel.onImageSelected(uri)
            if (uri != null) {
                onUriChange(uri)
                onImageChange(context)
            }

        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            imageViewModel.onImageCaptured(success)
            onImageChange(context)
        }
    )


    Scaffold(topBar = {   TopAppBar(title = {
        Text("Textify")
    }, actions = {
        IconButton(
            modifier = Modifier.padding(12.dp),
            onClick = { /*TODO: To SettingsActivity*/ }) {
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings"
            )
        }

    })} ,
        floatingActionButton = {
            Column {
                ExtendedFloatingActionButton(
                    backgroundColor = Purple500,
                    onClick = {  val uri = ComposeFileProvider.getImageUri(context)
                        onUriChange(uri)
                        imageViewModel.onCameraClick(uri,cameraLauncher) },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera_24),
                            contentDescription = "Camera"
                        )
                    },
                    text = { Text("Camera") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExtendedFloatingActionButton(
                    backgroundColor = Purple500,
                    onClick = { imagePicker.launch("image/*") },
                    icon = {
                        Icon(  painter = painterResource(id = R.drawable.ic_library_add_24),
                            contentDescription = "Library"
                        )
                    },
                    text = { Text("Library ") }
                )
            }

        }
        , content = {


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .height(1000.dp)
            ) {
                Box(modifier = Modifier
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(32.dp)).size(350.dp)){
                    if (uiState.hasImage && uiState.imageUri != null) {
                        AsyncImage(
                            model = uiState.imageUri,
                            modifier = Modifier
                                .background(color = Color.LightGray)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .size(350.dp),
                            contentDescription = "Selected Image"
                        )
                    }

                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(fontWeight = FontWeight.Bold,
                    text = "Extracted from Image")

                Box(modifier = Modifier
                    .width(350.dp)
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)){
                    Text(modifier = Modifier.align(Alignment.Center),
                        text = excText)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(fontWeight = FontWeight.Bold,
                    text = "Translated Text")

                Box(modifier = Modifier
                    .width(350.dp)
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)){
                    Text(modifier = Modifier.align(Alignment.Center),
                        text = tranText)
                }
                Spacer(modifier = Modifier.height(32.dp))

            } })
}





// TODO: JavaDoc for this function

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraDeniedLayout(
    multiplePermissionsState: MultiplePermissionsState,
    getShowPermissionText: () -> String
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp),
            text = getShowPermissionText())

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { multiplePermissionsState.launchMultiplePermissionRequest() }) {
            Text("Request permission")
        }
    }
}




/**
 * Displays a camera preview using the provided CameraX API.
 * Uses 'AndroidView' to integrate CameraPreview as there isn't a build in implementation for it in Android Compose.
 *
 * @param modifier The modifier for styling and positioning the camera preview.
 * @param cameraSelector The camera selector to choose the desired camera (default is the back camera).
 * @param scaleType The scale type for the preview view (default is FILL_CENTER).
 */

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context).apply { this.scaleType = scaleType } }

    AndroidView(
        modifier = modifier.size(130.dp),
        factory = { previewView }
    ) {
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", exc)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }
}

/**
 * Displays a settings button.
 *
 * @param modifier The modifier to be applied to the settings button.
 */

@Composable
fun SettingsButton(modifier: Modifier){
        IconButton(
            modifier = modifier.padding(12.dp),
            onClick = { /*TODO: To SettingsActivity*/ }) {
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = Icons.Default.Settings,
                contentDescription = "Icon",
                tint = Color.Black,
            )
        }
}



