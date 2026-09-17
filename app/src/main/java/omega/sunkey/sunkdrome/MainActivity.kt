package omega.sunkey.sunkdrome

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import omega.sunkey.sunkdrome.server.MediaScanner
import omega.sunkey.sunkdrome.server.ServerService
import omega.sunkey.sunkdrome.ui.theme.SunkdromeTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SunkdromeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    MainScreen(
                        modifier = Modifier.padding(16.dp).fillMaxSize().wrapContentSize(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var started by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startService(context)
            started = true
        }
    } // i shall merge start and scan into one later
    val tempReq = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { gra ->
        if (gra) {
            startScan(context)
        }
    }
    var music by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                perm
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    Column(modifier = modifier) {
        Button(
            onClick = {
                if(!started && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else if(!started) {
                    startService(context)
                    started = true
                } else {
                    stopService(context)
		            started = false
                }
            }
        ) {
            if (!started) Text("Start") else Text("Stop")
        }
        Button(
            onClick = {
                if(music) {
                    startScan(context)
                } else {
                    tempReq.launch(perm)
                }
            }
        ) {
            if (music) Text("Scan") else Text("Grant storage permission")
        }
    }
}

fun startScan(context: Context) {
    val req = OneTimeWorkRequestBuilder<MediaScanner>().build()
    WorkManager.getInstance(context).enqueue(req)
}
fun startService(context: Context) {
    val intent = Intent(context, ServerService::class.java)
    context.startForegroundService(intent)
}

fun stopService(context: Context) {
    val intent = Intent(context, ServerService::class.java)
    context.stopService(intent)
}
