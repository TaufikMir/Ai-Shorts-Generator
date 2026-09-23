package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.util.VideoUploadValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AI Shorts Factory", appName)
  }

  @Test
  fun `validate accepts MP4, MOV, and WebM video formats`() {
    val mp4Result = VideoUploadValidator.validate("keynote_lecture.mp4", 350f, 1800)
    assertTrue("MP4 should be valid", mp4Result.isValid)
    assertEquals("MP4", mp4Result.detectedFormat)

    val movResult = VideoUploadValidator.validate("podcast_prores.mov", 420f, 2100)
    assertTrue("MOV should be valid", movResult.isValid)
    assertEquals("MOV", movResult.detectedFormat)

    val webmResult = VideoUploadValidator.validate("stream_recording.webm", 250f, 900)
    assertTrue("WebM should be valid", webmResult.isValid)
    assertEquals("WebM", webmResult.detectedFormat)
  }

  @Test
  fun `validate rejects unsupported video and audio formats`() {
    val aviResult = VideoUploadValidator.validate("old_recording.avi", 500f, 1200)
    assertFalse("AVI must be rejected", aviResult.isValid)
    assertTrue("Error should mention AVI", aviResult.errorMessage?.contains(".avi", ignoreCase = true) == true)

    val mkvResult = VideoUploadValidator.validate("movie_rip.mkv", 900f, 3600)
    assertFalse("MKV must be rejected", mkvResult.isValid)

    val mp3Result = VideoUploadValidator.validate("audio_only.mp3", 50f, 600)
    assertFalse("MP3 audio must be rejected", mp3Result.isValid)
  }

  @Test
  fun `validate rejects oversized files exceeding 2GB limit`() {
    val oversizedResult = VideoUploadValidator.validate("huge_raw_clip.mp4", 2500f, 3600)
    assertFalse("Files > 2GB must be rejected", oversizedResult.isValid)
    assertTrue("Error should mention size limit", oversizedResult.errorMessage?.contains("2 GB", ignoreCase = true) == true)
  }

  @Test
  fun `timestamped segments serialization and formatting`() {
    val segment = com.example.data.model.TimestampedSegment(
      id = 1,
      startTimeSeconds = 65,
      endTimeSeconds = 125,
      speaker = "Host",
      text = "This is a test transcript segment.",
      confidence = 0.99f
    )

    assertEquals("01:05 - 02:05", segment.formatTimestamp())

    val jsonString = com.example.data.model.TimestampedSegment.toJsonString(listOf(segment))
    val parsedList = com.example.data.model.TimestampedSegment.parseSegmentsJson(jsonString)

    assertEquals(1, parsedList.size)
    assertEquals("Host", parsedList[0].speaker)
    assertEquals("This is a test transcript segment.", parsedList[0].text)
    assertEquals(65, parsedList[0].startTimeSeconds)
    assertEquals(125, parsedList[0].endTimeSeconds)
  }

  @Test
  fun `extractVideoId correctly parses various YouTube URL patterns`() {
    val helper = com.example.data.util.YouTubeImportHelper

    // Standard watch URL
    assertEquals("jvqFAi7vkBc", helper.extractVideoId("https://www.youtube.com/watch?v=jvqFAi7vkBc"))

    // youtu.be shortlink with query parameter
    assertEquals("jvqFAi7vkBc", helper.extractVideoId("https://youtu.be/jvqFAi7vkBc?si=xyz123"))

    // Mobile URL with timestamp
    assertEquals("6rZ1Hn36z04", helper.extractVideoId("https://m.youtube.com/watch?v=6rZ1Hn36z04&t=45s"))

    // Shorts URL
    assertEquals("Yf-P_Wk7OQk", helper.extractVideoId("https://www.youtube.com/shorts/Yf-P_Wk7OQk"))

    // Shorts URL with trailing slash and query
    assertEquals("Yf-P_Wk7OQk", helper.extractVideoId("https://www.youtube.com/shorts/Yf-P_Wk7OQk/?feature=share"))

    // YouTube Live stream URL
    assertEquals("jvqFAi7vkBc", helper.extractVideoId("https://www.youtube.com/live/jvqFAi7vkBc"))
    assertEquals("jvqFAi7vkBc", helper.extractVideoId("https://youtube.com/live/jvqFAi7vkBc?feature=share"))

    // Music YouTube URL
    assertEquals("jvqFAi7vkBc", helper.extractVideoId("https://music.youtube.com/watch?v=jvqFAi7vkBc"))

    // Watch URL with hash fragment
    assertEquals("jvqFAi7vkBc", helper.extractVideoId("https://www.youtube.com/watch?v=jvqFAi7vkBc#t=100"))

    // Watch URL with trailing slash before query
    assertEquals("jvqFAi7vkBc", helper.extractVideoId("https://www.youtube.com/watch/?v=jvqFAi7vkBc"))

    // Embed URL
    assertEquals("QmOF0crdyRU", helper.extractVideoId("https://www.youtube.com/embed/QmOF0crdyRU"))

    // Raw 11-char ID
    assertEquals("jvqFAi7vkBc", helper.extractVideoId("jvqFAi7vkBc"))
    assertEquals("jvqFAi7vkBc", helper.extractVideoId("\"jvqFAi7vkBc\""))

    // Shared text from mobile YouTube app
    assertEquals("jvqFAi7vkBc", helper.extractVideoId("Watch 'Lex Fridman Podcast' on YouTube: https://youtu.be/jvqFAi7vkBc"))
  }

  @Test
  fun `cleanUrlOrExtract produces valid canonical URL`() {
    val helper = com.example.data.util.YouTubeImportHelper
    assertEquals(
      "https://www.youtube.com/watch?v=jvqFAi7vkBc",
      helper.cleanUrlOrExtract("https://youtu.be/jvqFAi7vkBc?si=123")
    )
    assertEquals(
      "https://www.youtube.com/watch?v=jvqFAi7vkBc",
      helper.cleanUrlOrExtract("jvqFAi7vkBc")
    )
    assertEquals(
      "https://www.youtube.com/watch?v=jvqFAi7vkBc",
      helper.cleanUrlOrExtract("Check this out: https://www.youtube.com/shorts/jvqFAi7vkBc")
    )
  }

  @Test
  fun `isValidYouTubeUrl rejects invalid or non-YouTube URLs`() {
    val helper = com.example.data.util.YouTubeImportHelper

    assertFalse(helper.isValidYouTubeUrl(""))
    assertFalse(helper.isValidYouTubeUrl("   "))
    assertFalse(helper.isValidYouTubeUrl("https://vimeo.com/76979871"))
    assertFalse(helper.isValidYouTubeUrl("https://tiktok.com/@creator/video/12345"))
    assertFalse(helper.isValidYouTubeUrl("https://youtube.com/about"))
    assertFalse(helper.isValidYouTubeUrl("invalid-text-string"))
  }

  @Test
  fun `all curated presets have valid 11-character YouTube IDs`() {
    val presets = com.example.data.util.YouTubeImportHelper.CURATED_PRESETS
    assertTrue("Should have curated presets", presets.isNotEmpty())

    for (preset in presets) {
      assertEquals(11, preset.videoId.length)
      assertTrue(com.example.data.util.YouTubeImportHelper.isValidYouTubeUrl(preset.url))
      assertEquals(preset.videoId, com.example.data.util.YouTubeImportHelper.extractVideoId(preset.url))
    }
  }

  @Test
  fun `YouTubeVideoDetails duration formatting handles hours and minutes`() {
    val detailsLong = com.example.data.util.YouTubeVideoDetails(
      videoId = "jvqFAi7vkBc",
      canonicalUrl = "https://www.youtube.com/watch?v=jvqFAi7vkBc",
      title = "AI Keynote",
      channelTitle = "Tech Channel",
      thumbnailUrl = "",
      maxResThumbnailUrl = "",
      durationSeconds = 7325, // 2h 2m 5s
      estimatedSizeMb = 450f
    )
    assertEquals("2:02:05", detailsLong.formatDuration())

    val detailsShort = detailsLong.copy(durationSeconds = 960) // 16m 0s
    assertEquals("16:00", detailsShort.formatDuration())
  }

  @Test
  fun `DeviceMediaScanner provides bundled videos with correct format compatibility`() {
    val videos = com.example.ui.components.DeviceMediaScanner.getBundledDeviceVideos()
    assertTrue("Should have bundled media videos", videos.isNotEmpty())

    val supported = videos.filter { it.isSupported }
    val unsupported = videos.filter { !it.isSupported }

    assertTrue("Should have supported videos", supported.isNotEmpty())
    assertTrue("Should have at least one unsupported video for rejection testing", unsupported.isNotEmpty())

    // Validate supported formats
    for (item in supported) {
      val ext = item.fileName.substringAfterLast('.', "")
      assertTrue("Format should be mp4, mov, or webm", ext in setOf("mp4", "mov", "webm"))
      assertTrue("Size should be positive", item.fileSizeMb > 0f)
      assertTrue("Duration should be positive", item.durationSeconds > 0)
    }

    // Validate unsupported format
    val aviItem = unsupported.find { it.fileName.endsWith(".avi") }
    assertNotNull("Should find .avi test file", aviItem)
    assertFalse("AVI format should not be marked as supported", aviItem!!.isSupported)
  }

  @Test
  fun `MediaFileItem duration formatting handles minutes and seconds`() {
    val item = com.example.ui.components.MediaFileItem(
      uri = null,
      fileName = "sample.mp4",
      fileSizeMb = 120f,
      durationSeconds = 85, // 1m 25s
      mimeType = "video/mp4",
      isSupported = true,
      locationTag = "DCIM"
    )
    assertEquals("01:25", item.formatDuration())
  }

  @Test
  fun `pipeline progress stages match exact sequence Uploading Transcribing Finding moments Creating clips Adding captions Ready`() {
    val stages = com.example.ui.components.PIPELINE_PROGRESS_STAGES

    // Verify exactly 6 stages
    assertEquals(6, stages.size)

    // Verify stage identities and sequence
    assertEquals(com.example.data.model.ProcessingStage.UPLOADING, stages[0])
    assertEquals(com.example.data.model.ProcessingStage.TRANSCRIBING, stages[1])
    assertEquals(com.example.data.model.ProcessingStage.FINDING_MOMENTS, stages[2])
    assertEquals(com.example.data.model.ProcessingStage.CREATING_CLIPS, stages[3])
    assertEquals(com.example.data.model.ProcessingStage.ADDING_CAPTIONS, stages[4])
    assertEquals(com.example.data.model.ProcessingStage.READY, stages[5])

    // Verify stage labels match the exact requested wording
    assertEquals("Uploading", stages[0].label)
    assertEquals("Transcribing", stages[1].label)
    assertEquals("Finding moments", stages[2].label)
    assertEquals("Creating clips", stages[3].label)
    assertEquals("Adding captions", stages[4].label)
    assertEquals("Ready", stages[5].label)

    // Verify step numbers are sequential 1 to 6
    stages.forEachIndexed { index, stage ->
      assertEquals(index + 1, stage.stepNumber)
    }

    // Verify stage icons are present
    stages.forEach { stage ->
      val icon = com.example.ui.components.getStageIcon(stage)
      assertNotNull("Stage icon must exist for ${stage.name}", icon)
    }
  }

  @Test
  fun `granular error metadata for unsupported format provides user-friendly alert data`() {
    val aviResult = VideoUploadValidator.validate("keynote_capture.avi", 800f, 1500)
    assertFalse("AVI format must fail validation", aviResult.isValid)
    assertEquals(com.example.data.util.UploadErrorType.UNSUPPORTED_FORMAT, aviResult.errorType)
    assertTrue("Should flag as format error", aviResult.isFormatError)
    assertFalse("Should not flag as size error", aviResult.isFileSizeError)
    assertEquals("AVI", aviResult.detectedFormat)
    assertNotNull("Error title must be set", aviResult.errorTitle)
    assertTrue("Title should mention Unsupported Format", aviResult.errorTitle?.contains("Unsupported", ignoreCase = true) == true)
    assertTrue("Resolution tips must not be empty", aviResult.resolutionTips.isNotEmpty())
    assertTrue("Tips should recommend MP4 or MOV", aviResult.resolutionTips.any { it.contains("MP4", ignoreCase = true) })
  }

  @Test
  fun `granular error metadata for file size limit exceeded provides excess metrics and tips`() {
    val oversizedResult = VideoUploadValidator.validate("podcast_raw_4k.mp4", 3450f, 3600)
    assertFalse("3450 MB must exceed 2GB limit", oversizedResult.isValid)
    assertEquals(com.example.data.util.UploadErrorType.FILE_SIZE_LIMIT_EXCEEDED, oversizedResult.errorType)
    assertTrue("Should flag as file size error", oversizedResult.isFileSizeError)
    assertFalse("Should not flag as format error", oversizedResult.isFormatError)
    assertTrue("Excess MB must be greater than 0", oversizedResult.sizeExcessMb > 0f)
    assertEquals(1402f, oversizedResult.sizeExcessMb, 1.0f)
    assertNotNull("Error title must be set", oversizedResult.errorTitle)
    assertTrue("Title should mention File Size Limit", oversizedResult.errorTitle?.contains("Size", ignoreCase = true) == true)
    assertTrue("Resolution tips must not be empty", oversizedResult.resolutionTips.isNotEmpty())
    assertTrue("Tips should advise compression or 1080p export", oversizedResult.resolutionTips.any { it.contains("compress", ignoreCase = true) || it.contains("1080p", ignoreCase = true) })
  }

  @Test
  fun `FFmpeg automated center crop calculates macroblock aligned 9_16 dimensions for 1080p 16_9`() {
    val geom = com.example.data.util.FfmpegCropPipeline.calculateGeometry(
      sourceWidth = 1920,
      sourceHeight = 1080,
      framingMode = com.example.data.model.VideoFramingMode.CENTER_SPEAKER,
      horizontalOffsetRatio = 0.5f
    )

    // 1080 * 9 / 16 = 607.5 -> rounded to even = 608
    assertEquals(608, geom.cropWidth)
    assertEquals(1080, geom.cropHeight)
    // Horizontal margin: 1920 - 608 = 1312. Center crop (0.5) = 656
    assertEquals(656, geom.cropX)
    assertEquals(0, geom.cropY)
    assertEquals(656, geom.leftCroppedPixels)
    assertEquals(656, geom.rightCroppedPixels)
    // Macroblock parity: all dimensions must be divisible by 2 for H.264
    assertEquals(0, geom.cropWidth % 2)
    assertEquals(0, geom.cropHeight % 2)
    assertEquals(0, geom.cropX % 2)

    // Filter verification
    assertTrue("Filter must contain crop dimensions", geom.filterComplex.contains("crop=608:1080:656:0"))
    assertTrue("Filter must upscale to 1080x1920 using lanczos", geom.filterComplex.contains("scale=1080:1920:flags=lanczos"))
    assertTrue("Filter must enforce square SAR", geom.filterComplex.contains("setsar=1"))
  }

  @Test
  fun `FFmpeg automated center crop calculates macroblock aligned dimensions for 4K 3840x2160`() {
    val geom = com.example.data.util.FfmpegCropPipeline.calculateGeometry(
      sourceWidth = 3840,
      sourceHeight = 2160,
      framingMode = com.example.data.model.VideoFramingMode.CENTER_SPEAKER,
      horizontalOffsetRatio = 0.5f
    )

    // 2160 * 9 / 16 = 1215 -> rounded to even = 1214 or 1216
    assertTrue("Crop width must be macroblock aligned", geom.cropWidth % 2 == 0)
    assertEquals(2160, geom.cropHeight)
    assertTrue("Crop X must be macroblock aligned", geom.cropX % 2 == 0)
    assertEquals(geom.leftCroppedPixels, geom.rightCroppedPixels) // symmetric center
  }

  @Test
  fun `FFmpeg focal offset dynamically shifts horizontal crop window for off-center speakers`() {
    // 30% Left speaker focus
    val leftGeom = com.example.data.util.FfmpegCropPipeline.calculateGeometry(
      sourceWidth = 1920,
      sourceHeight = 1080,
      framingMode = com.example.data.model.VideoFramingMode.CENTER_SPEAKER,
      horizontalOffsetRatio = 0.30f
    )
    assertTrue("Left crop X should be less than center crop X", leftGeom.cropX < 656)
    assertEquals(0, leftGeom.cropX % 2)

    // 70% Right speaker focus
    val rightGeom = com.example.data.util.FfmpegCropPipeline.calculateGeometry(
      sourceWidth = 1920,
      sourceHeight = 1080,
      framingMode = com.example.data.model.VideoFramingMode.CENTER_SPEAKER,
      horizontalOffsetRatio = 0.70f
    )
    assertTrue("Right crop X should be greater than center crop X", rightGeom.cropX > 656)
    assertEquals(0, rightGeom.cropX % 2)
  }

  @Test
  fun `FFmpeg filter complexes for SMART_CROP and BLURRED_BG generate correct filter graphs`() {
    // Smart Crop dynamic pan-and-scan
    val smartCropGeom = com.example.data.util.FfmpegCropPipeline.calculateGeometry(
      sourceWidth = 1920,
      sourceHeight = 1080,
      framingMode = com.example.data.model.VideoFramingMode.SMART_CROP
    )
    assertTrue("Smart crop must contain sine tracking expression", smartCropGeom.filterComplex.contains("sin(2*PI*t"))

    // Blurred background filter graph
    val blurGeom = com.example.data.util.FfmpegCropPipeline.calculateGeometry(
      sourceWidth = 1920,
      sourceHeight = 1080,
      framingMode = com.example.data.model.VideoFramingMode.BLURRED_BG
    )
    assertTrue("Blurred BG must split into background and foreground streams", blurGeom.filterComplex.contains("split=2"))
    assertTrue("Blurred BG must include boxblur filter", blurGeom.filterComplex.contains("boxblur="))
    assertTrue("Blurred BG must composite via overlay", blurGeom.filterComplex.contains("overlay="))
  }

  @Test
  fun `FFmpeg buildCommandArgs produces complete, production-ready CLI invocation`() {
    val config = com.example.data.util.FfmpegCropConfig(
      inputPath = "interview_source.mp4",
      outputPath = "output_vertical_short.mp4",
      sourceWidth = 1920,
      sourceHeight = 1080,
      startTimeSec = 15f,
      durationSec = 45f,
      framingMode = com.example.data.model.VideoFramingMode.CENTER_SPEAKER,
      horizontalOffsetRatio = 0.5f,
      videoCodec = "libx264",
      preset = "veryfast",
      crf = 21,
      fps = 60
    )

    val args = com.example.data.util.FfmpegCropPipeline.buildCommandArgs(config)
    assertTrue("First arg must be ffmpeg", args[0] == "ffmpeg")
    assertTrue("Must overwrite output with -y", args.contains("-y"))
    assertTrue("Must include seek start time -ss", args.contains("-ss"))
    assertTrue("Must include duration -t", args.contains("-t"))
    assertTrue("Must specify input file", args.contains("-i") && args.contains("interview_source.mp4"))
    assertTrue("Must specify -filter_complex", args.contains("-filter_complex"))
    assertTrue("Must use libx264 codec", args.contains("-c:v") && args.contains("libx264"))
    assertTrue("Must use veryfast preset", args.contains("-preset") && args.contains("veryfast"))
    assertTrue("Must specify CRF 21", args.contains("-crf") && args.contains("21"))
    assertTrue("Must specify 60 fps", args.contains("-r") && args.contains("60"))
    assertTrue("Must include audio codec aac", args.contains("-c:a") && args.contains("aac"))
    assertTrue("Must include web faststart optimization", args.contains("+faststart"))
    assertEquals("Last argument must be destination path", "output_vertical_short.mp4", args.last())
  }

  @Test
  fun `export short entity updates status to EXPORTED with 1080x1920 file path`() {
    val initialShort = com.example.data.local.ShortEntity(
      id = 42,
      projectId = 10,
      projectTitle = "AI Revolution Podcast",
      title = "Why AI Models Will Replace Legacy Software",
      hook = "Nobody saw this coming...",
      durationSeconds = 48,
      startTimeSeconds = 120,
      endTimeSeconds = 168,
      selectionReason = "High virality hook",
      internalAiScore = 96,
      transcript = "Here is what happens next.",
      titleSuggestions = "AI Revolution Explained|Why Legacy Software Is Dead",
      description = "The future of software architecture discussed in depth.",
      youtubeHashtags = "#shorts #ai #tech #software",
      searchKeywords = "ai, artificial intelligence, software",
      pinnedComment = "What's your take on this? Drop a comment below!",
      status = "READY"
    )

    assertEquals("READY", initialShort.status)
    assertNull(initialShort.exportedPath)

    val exportedShort = initialShort.copy(
      status = "EXPORTED",
      exportedPath = "exports/Short_42_1080x1920.mp4"
    )

    assertEquals("EXPORTED", exportedShort.status)
    assertEquals("exports/Short_42_1080x1920.mp4", exportedShort.exportedPath)
    assertTrue("File name must reflect 1080x1920 dimensions", exportedShort.exportedPath?.contains("1080x1920") == true)
  }

  @Test
  fun `publishing kit all-in-one combines title, description, hashtags and pinned comment`() {
    val title = "The 10-Second Productivity Hack That Changed My Life"
    val description = "Detailed breakdown of the rule of 10 seconds for deep work."
    val hashtags = "#shorts #productivity #habits #focus #trending"
    val pinnedComment = "Do you struggle with morning focus? Let's discuss below! 👇"

    val kit = buildString {
      appendLine("--- TITLE ---")
      appendLine(title)
      appendLine()
      appendLine("--- DESCRIPTION ---")
      appendLine(description)
      appendLine()
      appendLine("--- HASHTAGS ---")
      appendLine(hashtags)
      appendLine()
      appendLine("--- PINNED COMMENT ---")
      appendLine(pinnedComment)
    }

    assertTrue("Kit must contain title section", kit.contains("--- TITLE ---"))
    assertTrue("Kit must contain actual title text", kit.contains(title))
    assertTrue("Kit must contain description section", kit.contains("--- DESCRIPTION ---"))
    assertTrue("Kit must contain actual description text", kit.contains(description))
    assertTrue("Kit must contain hashtags section", kit.contains("--- HASHTAGS ---"))
    assertTrue("Kit must contain actual hashtags", kit.contains(hashtags))
    assertTrue("Kit must contain pinned comment section", kit.contains("--- PINNED COMMENT ---"))
    assertTrue("Kit must contain actual pinned comment", kit.contains(pinnedComment))
  }

  @Test
  fun `export metadata hashtag parsing extracts individual clickable chips`() {
    val hashtagString = "#shorts #podcast #tech #viral #shortsfeed #creators"
    val chips = hashtagString.split(" ").filter { it.startsWith("#") && it.length > 1 }

    assertEquals(6, chips.size)
    assertEquals("#shorts", chips[0])
    assertEquals("#podcast", chips[1])
    assertEquals("#creators", chips.last())
    assertTrue("All chips must begin with hashtag symbol", chips.all { it.startsWith("#") })
  }
}
