package ca.sheridancollege.medreminder

import android.app.Application
import androidx.work.Configuration
import ca.sheridancollege.medreminder.worker.DailyResetWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MedReminderApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerConfiguration: Configuration

    override val workManagerConfiguration: Configuration
        get() = workerConfiguration

    override fun onCreate() {
        super.onCreate()
        DailyResetWorker.schedule(this)
    }
}