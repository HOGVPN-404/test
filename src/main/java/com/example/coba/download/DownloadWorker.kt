package com.example.coba.download

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.net.URLConnection
import okhttp3.OkHttpClient
import okhttp3.Request

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: android.content.Context,
    @Assisted params: WorkerParameters,
    private val okHttpClient: OkHttpClient,
    private val notificationHelper: DownloadNotificationHelper
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val url = inputData.getString(KEY_URL).orEmpty()
        val filename = inputData.getString(KEY_FILENAME).orEmpty()

        if (url.isBlank() || filename.isBlank()) {
            return Result.failure(workDataOf(KEY_ERROR to "Data download tidak valid"))
        }

        setForeground(createForegroundInfo(filename, progress = 0, indeterminate = true))
        val notificationId = id.hashCode()

        return try {
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return Result.failure(workDataOf(KEY_ERROR to "HTTP ${response.code}"))
                }
                val body = response.body ?: return Result.failure(workDataOf(KEY_ERROR to "Body kosong"))
                val mimeType = URLConnection.guessContentTypeFromName(filename) ?: "application/octet-stream"
                val uri = createDownloadUri(filename, mimeType)
                    ?: return Result.failure(workDataOf(KEY_ERROR to "Tidak dapat menulis ke Downloads"))

                val totalBytes = body.contentLength().takeIf { it > 0L } ?: -1L
                var downloadedBytes = 0L
                var lastProgress = -1

                applicationContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    body.byteStream().use { inputStream ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            val read = inputStream.read(buffer)
                            if (read < 0) break
                            if (isStopped) {
                                return Result.failure(workDataOf(KEY_ERROR to "Download dibatalkan"))
                            }
                            outputStream.write(buffer, 0, read)
                            downloadedBytes += read

                            if (totalBytes > 0) {
                                val progress = ((downloadedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100)
                                if (progress != lastProgress) {
                                    setProgress(workDataOf(KEY_PROGRESS to progress))
                                    setForeground(createForegroundInfo(filename, progress, indeterminate = false))
                                    lastProgress = progress
                                }
                            }
                        }
                        outputStream.flush()
                    }
                } ?: return Result.failure(workDataOf(KEY_ERROR to "Output stream gagal dibuka"))

                markCompleted(uri)
                setProgress(workDataOf(KEY_PROGRESS to 100))
                notificationHelper.notifyFinished(notificationId, filename, true, "Download selesai")
                Result.success(workDataOf(KEY_OUTPUT_URI to uri.toString()))
            }
        } catch (e: Exception) {
            notificationHelper.notifyFinished(
                notificationId = notificationId,
                filename = filename,
                success = false,
                message = e.message ?: "Download gagal"
            )
            Result.failure(workDataOf(KEY_ERROR to (e.message ?: "Download gagal")))
        }
    }

    private fun createForegroundInfo(filename: String, progress: Int, indeterminate: Boolean): ForegroundInfo {
        val notification = notificationHelper.progressNotification(
            filename = filename,
            progress = progress,
            indeterminate = indeterminate
        )
        return ForegroundInfo(id.hashCode(), notification)
    }

    private fun createDownloadUri(filename: String, mimeType: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
        }
        return applicationContext.contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            values
        )
    }

    private fun markCompleted(uri: Uri) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val values = ContentValues().apply {
            put(MediaStore.Downloads.IS_PENDING, 0)
        }
        applicationContext.contentResolver.update(uri, values, null, null)
    }

    companion object {
        const val KEY_URL = "key_url"
        const val KEY_FILENAME = "key_filename"
        const val KEY_PROGRESS = "key_progress"
        const val KEY_OUTPUT_URI = "key_output_uri"
        const val KEY_ERROR = "key_error"
    }
}
