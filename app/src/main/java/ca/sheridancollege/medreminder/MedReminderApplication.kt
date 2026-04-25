package ca.sheridancollege.medreminder

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import ca.sheridancollege.medreminder.worker.DoseEventGenerationWorker
import ca.sheridancollege.medreminder.worker.DoseMissedDetectionWorker
import ca.sheridancollege.medreminder.worker.MedicationAlarmScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MedReminderApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        MedicationAlarmScheduler.createNotificationChannel(this)
        DoseEventGenerationWorker.scheduleOnAppStart(this)
        DoseEventGenerationWorker.scheduleDaily(this)
        DoseMissedDetectionWorker.scheduleImmediateRun(this)
        DoseMissedDetectionWorker.schedule(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
