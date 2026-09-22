package com.example.ui.screens

import android.net.Uri
import android.view.DragEvent
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Warning
import com.example.data.util.YouTubeVideoDetails
import com.example.ui.components.UploadErrorAlertDialog
import com.example.ui.components.VideoFilePickerBottomSheet
import com.example.ui.components.VideoProcessingProgressIndicator
import com.example.ui.components.YouTubeImportSection
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ProcessingStage
import com.example.data.repository.ShortsRepository
import com.example.data.util.SupportedFormatInfo
import com.example.data.util.VideoUploadValidator
import com.example.data.util.VideoValidationResult
import com.example.ui.theme.BgDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurpleGlow
import com.example.ui.theme.RedError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

private val AmberWarning = Color(0xFFF59E0B)

data class SampleVideoPreset(
    val title: String,
    val fileName: String,
    val sizeMb: Float,
    val durationSeconds: Int,
    val category: String,
    val description: String,
    val isSupportedFormat: Boolean = true
)

enum class IngestSourceTab {
    FILE_UPLOAD,
    YOUTUBE_LINK
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateShortScreen(
    pipelineStatus: ShortsRepository.CreationPipelineStatus,
    onStartUploadAndProcess: (String, Float, Int, String?, String?) -> Unit,
    onImportYouTubeVideo: ((YouTubeVideoDetails, String?) -> Unit)? = null,
    onResetPipeline: () -> Unit,
    onCancelUpload: () -> Unit = {},
    onViewGeneratedProject: (Long) -> Unit
) {
    val context = LocalContext.current

    var ingestSourceTab by remember { mutableStateOf(IngestSourceTab.FILE_UPLOAD) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("masterclass_ai_workflows.mp4") }
    var selectedFileSizeMb by remember { mutableFloatStateOf(412.0f) }
    var selectedDurationSec by remember { mutableStateOf(1920) }
    var customProjectTitle by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf<SampleVideoPreset?>(null) }
    var isDragOverZone by remember { mutableStateOf(false) }
    var showFormatInfoDialog by remember { mutableStateOf(false) }
    var showFilePickerBottomSheet by remember { mutableStateOf(false) }
    var showErrorAlertDialog by remember { mutableStateOf(false) }

    // Validation state
    var validationResult by remember {
        mutableStateOf<VideoValidationResult?>(
            VideoUploadValidator.validate("masterclass_ai_workflows.mp4", 412.0f, 1920)
        )
    }

    val samplePresets = remember {
        listOf(
            SampleVideoPreset(
                title = "The Future of Autonomous AI Agents",
                fileName = "future_ai_agents_keynote.mp4",
                sizeMb = 412.0f,
                durationSeconds = 1920,
                category = "TECH / AI",
                description = "High energy keynote on multimodal models, robotics & autonomous agents",
                isSupportedFormat = true
            ),
            SampleVideoPreset(
                title = "Zero to \$100k/Month Bootstrapped SaaS",
                fileName = "founder_podcast_ep18.mov",
                sizeMb = 580.5f,
                durationSeconds = 2400,
                category = "PODCAST",
                description = "Deep dive founder interview about pricing, customer retention & scaling",
                isSupportedFormat = true
            ),
            SampleVideoPreset(
                title = "Pro Gaming Tournament Finals Highlights",
                fileName = "tournament_grand_finals.webm",
                sizeMb = 720.0f,
                durationSeconds = 3100,
                category = "GAMING",
                description = "Clutch 1v4 comeback rounds, shouting reactions & hilarious commentary",
                isSupportedFormat = true
            ),
            SampleVideoPreset(
                title = "Studio Camera Raw Stream (Test Format Rejection)",
                fileName = "studio_multicam_archive.avi",
                sizeMb = 850.0f,
                durationSeconds = 2700,
                category = "UNSUPPORTED FORMAT TEST",
                description = "Tests validation rejection for non-supported AVI format (.avi)",
                isSupportedFormat = false
            ),
            SampleVideoPreset(
                title = "4K Cinema DCI Raw Footage (Test Size Limit)",
                fileName = "cinema_raw_master_session.mp4",
                sizeMb = 3450.0f,
                durationSeconds = 5400,
                category = "OVERSIZED FILE TEST",
                description = "Tests validation rejection for files exceeding the 2.0 GB (2,048 MB) maximum upload limit",
                isSupportedFormat = false
            )
        )
    }

    // Video File Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            selectedPreset = null
            val result = VideoUploadValidator.extractFromUri(context, uri)
            validationResult = result
            selectedFileName = result.fileName
            selectedFileSizeMb = result.fileSizeMb
            selectedDurationSec = result.durationSeconds
            if (!result.isValid) {
                showErrorAlertDialog = true
            }
            if (customProjectTitle.isBlank() && result.isValid) {
                customProjectTitle = result.fileName.substringBeforeLast('.').replace("_", " ").replace("-", " ")
            }
        }
    }

    // Function to process a dropped or selected file
    fun applyFileSelection(
        uri: Uri?,
        fileName: String,
        fileSizeMb: Float,
        durationSeconds: Int,
        mimeType: String? = null
    ) {
        selectedVideoUri = uri
        selectedPreset = null
        val result = if (uri != null) {
            VideoUploadValidator.extractFromUri(context, uri)
        } else {
            VideoUploadValidator.validate(fileName, fileSizeMb, durationSeconds, mimeType)
        }
        validationResult = result
        selectedFileName = result.fileName
        selectedFileSizeMb = result.fileSizeMb
        selectedDurationSec = result.durationSeconds
        if (!result.isValid) {
            showErrorAlertDialog = true
        }
        if (customProjectTitle.isBlank() && result.isValid) {
            customProjectTitle = result.fileName.substringBeforeLast('.').replace("_", " ").replace("-", " ")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pipeline Progress View
        when (pipelineStatus) {
            is ShortsRepository.CreationPipelineStatus.Running -> {
                item {
                    PipelineProgressCard(
                        status = pipelineStatus,
                        onCancelUpload = onCancelUpload
                    )
                }
            }

            is ShortsRepository.CreationPipelineStatus.Success -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("creation_success_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GreenSuccess)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(GreenSuccess.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = GreenSuccess,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Shorts Generated Successfully!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Created ${pipelineStatus.shortsGenerated} vertical 9:16 Shorts with viral hooks, captions, and SEO ready.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { onViewGeneratedProject(pipelineStatus.projectId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("open_project_button")
                                ) {
                                    Text("Open Project Studio", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = onResetPipeline,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Upload Another", color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            is ShortsRepository.CreationPipelineStatus.Error -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("creation_error_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedError)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = RedError,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Processing Failed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RedError
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = pipelineStatus.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (pipelineStatus.wasCreditRefunded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x3310B981)
                                ) {
                                    Text(
                                        text = "🛡️ 100% Credit Protected: Your credits were not charged.",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GreenSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onResetPipeline,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Try Again", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            ShortsRepository.CreationPipelineStatus.Idle -> {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Upload & Create Shorts",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (ingestSourceTab == IngestSourceTab.FILE_UPLOAD)
                                    "Drag and drop your long-form video or pick from storage"
                                else
                                    "Paste any YouTube video or podcast URL for AI short extraction",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        IconButton(
                            onClick = { showFormatInfoDialog = true },
                            modifier = Modifier.testTag("format_info_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Format specifications",
                                tint = CyanGlow
                            )
                        }
                    }
                }

                // Source Mode Segmented Switcher (File Upload vs YouTube Link)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceDark)
                            .border(1.dp, BorderDark, RoundedCornerShape(14.dp))
                            .padding(4.dp)
                    ) {
                        // Tab 1: File Upload
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { ingestSourceTab = IngestSourceTab.FILE_UPLOAD }
                                .testTag("tab_file_upload"),
                            color = if (ingestSourceTab == IngestSourceTab.FILE_UPLOAD) CyanGlow else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = if (ingestSourceTab == IngestSourceTab.FILE_UPLOAD) Color.Black else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "File Upload",
                                    fontWeight = FontWeight.Bold,
                                    color = if (ingestSourceTab == IngestSourceTab.FILE_UPLOAD) Color.Black else TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Tab 2: YouTube Link
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { ingestSourceTab = IngestSourceTab.YOUTUBE_LINK }
                                .testTag("tab_youtube_link"),
                            color = if (ingestSourceTab == IngestSourceTab.YOUTUBE_LINK) Color(0xFFFF0000) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartDisplay,
                                    contentDescription = null,
                                    tint = if (ingestSourceTab == IngestSourceTab.YOUTUBE_LINK) Color.White else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "YouTube Link",
                                    fontWeight = FontWeight.Bold,
                                    color = if (ingestSourceTab == IngestSourceTab.YOUTUBE_LINK) Color.White else TextSecondary,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (ingestSourceTab == IngestSourceTab.YOUTUBE_LINK) Color.White.copy(alpha = 0.25f) else Color(0x33FF0000)
                                ) {
                                    Text(
                                        text = "NEW",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (ingestSourceTab == IngestSourceTab.YOUTUBE_LINK) Color.White else Color(0xFFFF4444)
                                    )
                                }
                            }
                        }
                    }
                }

                if (ingestSourceTab == IngestSourceTab.YOUTUBE_LINK) {
                    item {
                        YouTubeImportSection(
                            onImportConfirmed = { details, title ->
                                onImportYouTubeVideo?.invoke(details, title)
                            }
                        )
                    }
                } else {
                    // Drag & Drop Zone Card
                    item {
                        VideoDropZone(
                            isHovering = isDragOverZone,
                            onHoverChange = { isDragOverZone = it },
                            onFileDropped = { uri, name, sizeMb, durSec, mime ->
                                applyFileSelection(uri, name, sizeMb, durSec, mime)
                            },
                            onBrowseClicked = { showFilePickerBottomSheet = true }
                        )
                    }

                    // Format Validation Status & Details Card
                    validationResult?.let { result ->
                        item {
                            VideoValidationCard(
                                result = result,
                                onClear = {
                                    validationResult = null
                                    selectedVideoUri = null
                                    selectedPreset = null
                                },
                                onPickAnother = { showFilePickerBottomSheet = true },
                                onViewAlertDetails = { showErrorAlertDialog = true }
                            )
                        }
                    }

                    // Project Title Input (only if valid video is selected)
                    if (validationResult?.isValid == true) {
                        item {
                            OutlinedTextField(
                                value = customProjectTitle,
                                onValueChange = { customProjectTitle = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("project_title_input"),
                                label = { Text("Project Title (Optional)") },
                                placeholder = { Text("e.g. Scaling Our SaaS to \$1M ARR") },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanGlow,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SurfaceCard,
                                    unfocusedContainerColor = SurfaceCard
                                )
                            )
                        }
                    }

                    // Action Launch Button (enabled only when format is valid)
                    item {
                        val canProceed = validationResult?.isValid == true
                        Button(
                            onClick = {
                                if (canProceed) {
                                    onStartUploadAndProcess(
                                        selectedFileName,
                                        selectedFileSizeMb,
                                        selectedDurationSec,
                                        customProjectTitle.ifBlank { null },
                                        selectedVideoUri?.toString()
                                    )
                                }
                            },
                            enabled = canProceed,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_processing_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanGlow,
                                contentColor = Color.Black,
                                disabledContainerColor = Color(0xFF1E2640),
                                disabledContentColor = TextMuted
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (canProceed) Color.Black else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (canProceed) "Upload Video & Generate Shorts" else "Select Valid Video to Continue",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Interactive Sample Videos & Test Section
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Or Test With Sample Videos (Click or Drag)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select a preset or drag it into the drop zone above to test validation:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    items(samplePresets) { preset ->
                        val isSelected = selectedFileName == preset.fileName
                        DraggablePresetCard(
                            preset = preset,
                            isSelected = isSelected,
                            onSelect = {
                                selectedPreset = preset
                                applyFileSelection(
                                    uri = null,
                                    fileName = preset.fileName,
                                    fileSizeMb = preset.sizeMb,
                                    durationSeconds = preset.durationSeconds,
                                    mimeType = null
                                )
                                if (preset.isSupportedFormat) {
                                    customProjectTitle = preset.title
                                }
                            },
                            onDroppedIntoTarget = {
                                selectedPreset = preset
                                applyFileSelection(
                                    uri = null,
                                    fileName = preset.fileName,
                                    fileSizeMb = preset.sizeMb,
                                    durationSeconds = preset.durationSeconds,
                                    mimeType = null
                                )
                                if (preset.isSupportedFormat) {
                                    customProjectTitle = preset.title
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Format Specifications Dialog
    if (showFormatInfoDialog) {
        SupportedFormatsDialog(onDismiss = { showFormatInfoDialog = false })
    }

    // Video File Picker Bottom Sheet
    if (showFilePickerBottomSheet) {
        VideoFilePickerBottomSheet(
            onDismiss = { showFilePickerBottomSheet = false },
            onFileSelected = { uri, fileName, fileSizeMb, durationSeconds, mimeType ->
                applyFileSelection(uri, fileName, fileSizeMb, durationSeconds, mimeType)
            }
        )
    }

    // Granular Upload Error Alert Dialog
    if (showErrorAlertDialog && validationResult != null && !validationResult!!.isValid) {
        UploadErrorAlertDialog(
            result = validationResult!!,
            onDismiss = { showErrorAlertDialog = false },
            onPickAnother = {
                showErrorAlertDialog = false
                showFilePickerBottomSheet = true
            },
            onShowFormatSpecs = {
                showErrorAlertDialog = false
                showFormatInfoDialog = true
            }
        )
    }
}

/**
 * Drag and Drop Zone supporting both Android Native System DragEvents and In-App Dragging.
 */
@Composable
fun VideoDropZone(
    isHovering: Boolean,
    onHoverChange: (Boolean) -> Unit,
    onFileDropped: (Uri?, String, Float, Int, String?) -> Unit,
    onBrowseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isHovering) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onBrowseClicked() }
            .testTag("upload_dropzone_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovering) Color(0xFF13203C) else SurfaceDark
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isHovering) 2.5.dp else 1.5.dp,
            brush = if (isHovering) {
                Brush.linearGradient(listOf(CyanGlow, PurpleGlow, PinkAccent))
            } else {
                Brush.linearGradient(listOf(CyanGlow.copy(alpha = 0.7f), PurpleGlow.copy(alpha = 0.4f)))
            }
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Android View listener for system Drag & Drop (multi-window, files app, desktop emulator drag)
            AndroidView(
                modifier = Modifier.matchParentSize(),
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        setOnDragListener { _, event ->
                            when (event.action) {
                                DragEvent.ACTION_DRAG_STARTED -> true
                                DragEvent.ACTION_DRAG_ENTERED, DragEvent.ACTION_DRAG_LOCATION -> {
                                    onHoverChange(true)
                                    true
                                }
                                DragEvent.ACTION_DRAG_EXITED -> {
                                    onHoverChange(false)
                                    true
                                }
                                DragEvent.ACTION_DROP -> {
                                    onHoverChange(false)
                                    val clipData = event.clipData
                                    if (clipData != null && clipData.itemCount > 0) {
                                        val item = clipData.getItemAt(0)
                                        val uri = item.uri
                                        val text = item.text?.toString()
                                        if (uri != null) {
                                            val validation = VideoUploadValidator.extractFromUri(ctx, uri)
                                            onFileDropped(
                                                uri,
                                                validation.fileName,
                                                validation.fileSizeMb,
                                                validation.durationSeconds,
                                                validation.mimeType
                                            )
                                        } else if (!text.isNullOrBlank()) {
                                            val cleanName = text.substringAfterLast('/').substringAfterLast('\\')
                                            onFileDropped(null, cleanName, 350.0f, 1800, null)
                                        }
                                    }
                                    true
                                }
                                DragEvent.ACTION_DRAG_ENDED -> {
                                    onHoverChange(false)
                                    true
                                }
                                else -> false
                            }
                        }
                    }
                }
            )

            // Visual Drop Zone Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            if (isHovering) Brush.radialGradient(listOf(CyanGlow.copy(alpha = 0.4f), Color.Transparent))
                            else Brush.radialGradient(listOf(Color(0x3300F2FE), Color.Transparent))
                        )
                        .border(1.dp, if (isHovering) CyanGlow else BorderDark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Drop video",
                        tint = if (isHovering) CyanGlow else TextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isHovering) "Drop Video File Here to Validate" else "Drag & Drop Video File Here",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isHovering) CyanGlow else TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "or click anywhere to browse local device storage",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Supported Format Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormatPill(label = "MP4", isRecommended = true)
                    FormatPill(label = "MOV", isRecommended = false)
                    FormatPill(label = "WebM", isRecommended = false)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Maximum file size: 2 GB • Max duration: 3 hours",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onBrowseClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceElevated,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanGlow.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("browse_files_button")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanGlow)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open File Picker", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FormatPill(label: String, isRecommended: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isRecommended) Color(0x3300F2FE) else Color(0xFF232A42),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isRecommended) CyanGlow.copy(alpha = 0.5f) else BorderDark
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (isRecommended) CyanGlow else GreenSuccess,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

/**
 * Card displaying validation results:
 * - If valid: shows format verification, size, duration, codecs
 * - If invalid: shows clear rejection banner, why it's not accepted, and how to fix
 */
@Composable
fun VideoValidationCard(
    result: VideoValidationResult,
    onClear: () -> Unit,
    onPickAnother: () -> Unit,
    onViewAlertDetails: () -> Unit = {}
) {
    if (result.isValid) {
        // Valid video card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("validation_success_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GreenSuccess.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "✓ FORMAT VERIFIED: ${result.detectedFormat}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = GreenSuccess
                            )
                        }
                    }
                    IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.fileName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format("%.1f", result.fileSizeMb)} MB • ${result.durationSeconds / 60}m ${result.durationSeconds % 60}s • 1080p compatible",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                result.technicalDetails?.let { details ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚡ $details",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    } else {
        val isFileSize = result.isFileSizeError
        val errorThemeColor = if (isFileSize) AmberWarning else RedError
        val containerBg = if (isFileSize) Color(0xFF26190E) else Color(0xFF2B1214)

        // Invalid video rejection banner with granular error handling
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("validation_error_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = containerBg),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, errorThemeColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with distinct error badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isFileSize) Icons.Default.Storage else Icons.Default.Warning,
                        contentDescription = "Upload Error",
                        tint = errorThemeColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.errorTitle ?: (if (isFileSize) "File Size Exceeded" else "Format Rejected: ${result.detectedFormat}"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = errorThemeColor
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Explanatory message
                Text(
                    text = result.errorMessage ?: "This file cannot be processed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )

                // Granular metrics breakdown
                if (isFileSize) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33000000),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Current Size", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    text = "${String.format("%.1f", result.fileSizeMb)} MB",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = AmberWarning
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Allowed Limit", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    text = "2,048 MB (2 GB)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenSuccess
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Over Limit", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    text = "+${String.format("%.0f", result.sizeExcessMb)} MB",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = RedError
                                )
                            }
                        }
                    }
                } else if (result.isFormatError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Supported Formats Matrix
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33000000),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text("✓ MP4", color = GreenSuccess, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("✓ MOV", color = GreenSuccess, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("✓ WebM", color = GreenSuccess, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("✕ ${result.detectedFormat}", color = RedError, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                result.technicalDetails?.let { tip ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💡 $tip",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onViewAlertDetails,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = errorThemeColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("view_solutions_button")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("View Solution & Tips", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onPickAnother,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.testTag("choose_another_video_button")
                    ) {
                        Text("Pick Another", color = TextPrimary, style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = onClear,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark)
                    ) {
                        Text("Clear", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

/**
 * Interactive Draggable Preset Card.
 */
@Composable
fun DraggablePresetCard(
    preset: SampleVideoPreset,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDroppedIntoTarget: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(preset.fileName) {
                detectDragGestures(
                    onDragEnd = {
                        // If dragged up towards dropzone (negative Y), trigger drop!
                        if (offsetY < -80f) {
                            onDroppedIntoTarget()
                        }
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x * 0.4f
                        offsetY += dragAmount.y * 0.4f
                    }
                )
            }
            .clickable { onSelect() }
            .testTag("preset_${preset.fileName}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E2640) else SurfaceCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) CyanGlow else if (!preset.isSupportedFormat) RedError.copy(alpha = 0.5f) else BorderDark
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (!preset.isSupportedFormat) Color(0x33EF4444)
                        else if (isSelected) CyanGlow
                        else Color(0x333B82F6)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (!preset.isSupportedFormat) Icons.Default.Warning else Icons.Default.Movie,
                    contentDescription = null,
                    tint = if (!preset.isSupportedFormat) RedError else if (isSelected) Color.Black else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = preset.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (!preset.isSupportedFormat) RedError else TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${preset.category} • ${preset.fileName} • ${preset.sizeMb.toInt()} MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Drag handle hint
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag into drop zone",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = CyanGlow)
            }
        }
    }
}

/**
 * Rich Pipeline and Video Processing Status Indicator Card.
 * Displays the progress stages of video processing:
 * 'Uploading → Transcribing → Finding moments → Creating clips → Adding captions → Ready'
 * with clear loading animations.
 */
@Composable
fun PipelineProgressCard(
    status: ShortsRepository.CreationPipelineStatus.Running,
    onCancelUpload: () -> Unit
) {
    VideoProcessingProgressIndicator(
        status = status,
        onCancelUpload = onCancelUpload
    )
}

/**
 * Dialog showing supported video formats specifications and reasons.
 */
@Composable
fun SupportedFormatsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VideoFile, contentDescription = null, tint = CyanGlow)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Supported Video Formats", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "AI Shorts Factory requires specific video containers to support rapid frame indexing and neural speech-to-text alignment:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                VideoUploadValidator.SUPPORTED_FORMATS.forEach { format ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E2640),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${format.name} (${format.extension})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanGlow
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Codecs: ${format.codecs}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary
                            )
                            Text(
                                text = format.recommendation,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x33EF4444),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✕ Formats like .avi, .mkv, .wmv, and .flv are NOT supported due to non-standard moov atoms and lossy subtitle sync.",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = RedError
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyanGlow, contentColor = Color.Black)
            ) {
                Text("Got it")
            }
        },
        containerColor = SurfaceDark,
        textContentColor = TextPrimary
    )
}
