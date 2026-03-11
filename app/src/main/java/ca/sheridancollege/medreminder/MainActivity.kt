package ca.sheridancollege.medreminder

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import ca.sheridancollege.medreminder.data.datastore.UserPreferencesDataStore
import ca.sheridancollege.medreminder.domain.usecase.MarkAsTakenUseCase
import ca.sheridancollege.medreminder.navigation.AppNavigation
import ca.sheridancollege.medreminder.ui.theme.MedReminderTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferences: UserPreferencesDataStore
    @Inject lateinit var markAsTakenUseCase: MarkAsTakenUseCase

    private var nfcAdapter: NfcAdapter? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Read initial dark theme synchronously so there's no flash on startup
        val initialDarkTheme = runBlocking { userPreferences.darkTheme.first() }

        setContent {
            // Collect as state — updates instantly when toggled in Settings
            val darkTheme by userPreferences.darkTheme.collectAsState(initial = initialDarkTheme)

            MedReminderTheme(darkTheme = darkTheme, dynamicColor = false) {
                AppNavigation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            tag?.let {
                val tagId = it.id.joinToString("") { byte -> "%02x".format(byte) }
                handleNfcTag(tagId)
            }
        }
    }

    private fun handleNfcTag(tagId: String) {
        lifecycleScope.launch {
            Toast.makeText(this@MainActivity, "NFC Tag: $tagId", Toast.LENGTH_SHORT).show()
        }
    }
}
