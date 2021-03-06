/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.en.job

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import nl.rijksoverheid.en.ExposureNotificationsRepository
import nl.rijksoverheid.en.notifier.NotificationsRepository
import java.util.concurrent.TimeUnit

private const val WORKER_ID = "exposure_reminder"
private const val REMINDER_INTERVAL_HOURS = 3L

class RemindExposureNotificationWorker(
    context: Context,
    workerParameters: WorkerParameters,
    private val exposureNotificationsRepository: ExposureNotificationsRepository,
    private val notificationsRepository: NotificationsRepository
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val exposure = exposureNotificationsRepository.getDaysSinceLastExposure()
        if (exposure != null) {
            notificationsRepository.showExposureNotification(exposure, true)
        } else {
            cancel(applicationContext)
        }
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RemindExposureNotificationWorker>(
                REMINDER_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setInitialDelay(REMINDER_INTERVAL_HOURS, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORKER_ID, ExistingPeriodicWorkPolicy.REPLACE, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORKER_ID)
        }
    }
}
