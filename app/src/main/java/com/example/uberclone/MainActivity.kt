package com.example.uberclone

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.uberclone.Util.logger
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit private var map: GoogleMap


    val REQUEST_CODE = 1
    var ERROR_DIALAOG_CODE = 2
    var GPS_REQUEST_CODE = 3
    var LOCATION_PERMISSION_CODE = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (isServiceOk()) {
            if (isGPSEnabled()) {
                if (!isLocationPersmissionEnabled()) {
                    requestLoactionPermission()
                } else {
                    initMap()
                }
            }
        }

    }

    fun initMap() {
        var mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this);
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let {
            map = googleMap

        }
    }

    private fun isLocationPersmissionEnabled(): Boolean {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun requestLoactionPermission() {
        val persmission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        ActivityCompat.requestPermissions(this, persmission, GPS_REQUEST_CODE)
    }

    fun isServiceOk(): Boolean {
        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MainActivity)
        if (available == ConnectionResult.SUCCESS) {
            logger("Service is available and is working")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            logger("an error occur but it can be resolve")
            val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this@MainActivity, available, ERROR_DIALAOG_CODE)
            dialog.show()
        } else {
            Toast.makeText(this, "You can't mak map request", Toast.LENGTH_SHORT).show()
        }
        return false
    }

    fun isGPSEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (providerEnabled) {
            return true
        } else {
            val alertDialog: AlertDialog = AlertDialog.Builder(this)
                    .setTitle("GPS Permission")
                    .setMessage("Please enable the GPS")
                    .setPositiveButton("YES") { dialog, which ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivityForResult(intent, REQUEST_CODE)
                    }
                    .setCancelable(false)
                    .show()
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (requestCode == REQUEST_CODE) {
            if (providerEnabled) {
                Toast.makeText(this, "GPS is enabled", Toast.LENGTH_SHORT).show()
            } else {
                isGPSEnabled();
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (LOCATION_PERMISSION_CODE == requestCode && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            requestLoactionPermission()
        }
    }
}
