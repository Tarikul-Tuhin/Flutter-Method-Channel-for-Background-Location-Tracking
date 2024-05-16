package com.example.apprunbgrnd

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.plugin.common.EventChannel
import androidx.activity.result.contract.ActivityResultContracts

/*
class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.apprunbgrnd"

*/
/*       private fun checkPermissions(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION)
    }*//*


    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
          */
/*  Intent(applicationContext, LocationService::class.java).apply {
               action = LocationService.ACTION_START
               startService(this)
           }*//*


            if (call.method == "getBatteryLevel") {
                checkAndRequestPermissions()
              
                val batteryLevel = getBatteryLevel()

                if (batteryLevel != -1) {
                    result.success(batteryLevel)
                } else {
                    result.error("UNAVAILABLE", "Battery level not available.", null)
                }
            } else {
                result.notImplemented()
            }
        }
    }

    private fun getBatteryLevel(): Int {

       */
/* val serviceIntent = Intent(this, LocationService::class.java)
        serviceIntent.action = LocationService.ACTION_START
        startService(serviceIntent)*//*


      

        
        val batteryLevel: Int
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {

        

            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val intent = ContextWrapper(applicationContext).registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            batteryLevel = intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        }

        return 0
    }


        private fun checkAndRequestPermissions() {
                Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }

        
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val permissionsToRequest = mutableListOf<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        }
    }
}*/



// Reference video => https://www.youtube.com/watch?v=29FJc0W7gek

class MainActivity : FlutterFragmentActivity(),LocationCallback {
    private val REQUIRED_PERMISSIONS =  mutableListOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    ).apply {
        if(VERSION.SDK_INT >= VERSION_CODES.TIRAMISU){
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    private val networkEventChannel = "com.example.apprunbgrnd"
    private var attachEvent: EventChannel.EventSink? = null


    // registerActivity import issue resolved here => https://stackoverflow.com/questions/74139314/registerforactivityresult-wont-seem-to-import-kotlin
    fun <I, O> Activity.registerForActivityResult(
            contract: ActivityResultContract<I, O>,
            callback: ActivityResultCallback<O>
    ) = (this as ComponentActivity).registerForActivityResult(contract, callback)

    private val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
    ) { isGranted ->
        Log.i("isGranted ", isGranted.toString())
        if (isGranted.containsValue(false)) {
            Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_LONG).show()
        } else {
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            val isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
            )
            if (isEnabled) {
                Intent(applicationContext, LocationService::class.java).apply {
                    action = LocationService.ACTION_START
                    startService(this)
                }

                val serviceIntent = Intent(this, LocationService::class.java).apply {
                    action = LocationService.ACTION_START
                }
                startService(serviceIntent)
                bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
            } else {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                Toast.makeText(this@MainActivity, "Enable Location", Toast.LENGTH_LONG).show()
            }
        }

    }

    private var locationService: LocationService? = null
    private var isServiceBound = false

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            locationService?.setLocationCallback(this@MainActivity)
            isServiceBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            locationService = null
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        if(VERSION.SDK_INT >= VERSION_CODES.O){
            val channel = NotificationChannel("location", "Location", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, networkEventChannel).setStreamHandler(
                object: EventChannel.StreamHandler{
                    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                        Log.w("TAG_NAME", "Adding listener")
                        Log.w("TAG_NAME", "$events")
                        attachEvent = events
                    }

                    override fun onCancel(arguments: Any?) {
                        Log.w("TAG_NAME", "Cancelling listener")
                        attachEvent = null
                        println("StreamHandler - onCanceled: ")
                    }

                }
        )

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "locationPlatform").setMethodCallHandler {
            call, result ->
            when(call.method){
                "getLocation" -> {
                    println("getLocation invoked")
                    requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
                }

                "stopLocation" -> {
                    println("stopLocation invoked")
                    Intent(applicationContext, LocationService::class.java).apply {
                        action = LocationService.ACTION_STOP
                        startService(this)
                    }
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    override fun onLocationUpdated(latitude: Double, longitude: Double) {
        runOnUiThread {
            Log.i("attacheventis", "$attachEvent")
            Toast.makeText(this, "Lat lng $latitude $longitude", Toast.LENGTH_LONG).show()

            attachEvent?.success("$latitude $longitude")
        }
    }

}
