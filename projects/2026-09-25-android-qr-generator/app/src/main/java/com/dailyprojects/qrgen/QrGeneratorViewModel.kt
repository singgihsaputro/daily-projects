package com.dailyprojects.qrgen

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QrUiState(
    val input: String = "",
    val qrBitmap: ImageBitmap? = null,
    val presets: List<QrPreset> = emptyList(),
    val history: List<QrHistoryEntry> = emptyList(),
    val error: String? = null,
)

private const val MAX_HISTORY = 8
private const val MAX_INPUT_LENGTH = 500
private const val MATRIX_SIZE = 480

class QrGeneratorViewModel(private val repository: QrHistoryRepository) : ViewModel() {

    private val writer = QRCodeWriter()

    private val _uiState = MutableStateFlow(QrUiState())
    val uiState: StateFlow<QrUiState> = _uiState.asStateFlow()

    private var nextId: Int

    init {
        val fixture = repository.loadFixture()
        nextId = (fixture.history.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0) + 1
        val startingText = fixture.presets.firstOrNull()?.content.orEmpty()
        _uiState.value = QrUiState(input = startingText, presets = fixture.presets, history = fixture.history)
        generate()
    }

    fun setInput(text: String) {
        _uiState.value = _uiState.value.copy(input = text.take(MAX_INPUT_LENGTH))
    }

    fun applyPreset(preset: QrPreset) {
        setInput(preset.content)
        generate()
    }

    fun generate() {
        val text = _uiState.value.input.trim()
        if (text.isEmpty()) {
            _uiState.value = _uiState.value.copy(qrBitmap = null, error = "Enter some text or a URL first.")
            return
        }

        val bitmap = try {
            encode(text)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(qrBitmap = null, error = "Could not encode that text: ${e.message}")
            return
        }

        val entry = QrHistoryEntry(id = (nextId++).toString(), content = text, time = currentTimeLabel())
        _uiState.value = _uiState.value.copy(
            qrBitmap = bitmap,
            history = (listOf(entry) + _uiState.value.history).take(MAX_HISTORY),
            error = null,
        )
    }

    private fun encode(text: String): ImageBitmap {
        val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M)
        val matrix: BitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, MATRIX_SIZE, MATRIX_SIZE, hints)
        val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.RGB_565)
        for (x in 0 until matrix.width) {
            for (y in 0 until matrix.height) {
                bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap.asImageBitmap()
    }

    private fun currentTimeLabel(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}
