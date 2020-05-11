package com.example.uberclone

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.uberclone.Util.logger
import com.example.uberclone.Util.showToast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit private var map: GoogleMap


    var ERROR_DIALAOG_CODE = 2
    var GPS_REQUEST_CODE = 3
    var LOCATION_PERMISSION_CODE = 4
    var STORAGE_PERMISSION_CODE = 5
    var zoom_level: Float = 14f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (isServiceOk()) {
            if (!checkLocationPermission()) {
                requestLocationPermission()
            }
            if (!checkStoaragePermission()) {
                requestStoragePermission()
            }
            initMap()
        }
        floatingActionButton.setOnClickListener {
            geoCodingFromAddress()
        }

    }

    fun geoCodingFromAddress() {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addressList: MutableList<Address> = geocoder.getFromLocationName("Mumbai", 5)
        if (addressList.isNotEmpty()) {
            addressList.forEach {
                val latLng = LatLng(it.latitude, it.longitude)
                map.addMarker(MarkerOptions().position(latLng))
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                logger("${it.locality} ${it.getAddressLine(it.maxAddressLineIndex)} ")
            }
        }
    }

    fun initMap() {
        var mapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this);
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let {
            map = googleMap
            map.isMyLocationEnabled=true
            map.addMarker(
                MarkerOptions().title("Hello").position(LatLng(22.233969, 84.782612))
                    .draggable(true)
            )
            var cameraUpdate: CameraUpdate =
                CameraUpdateFactory.newLatLngZoom(LatLng(22.233969, 84.782612), zoom_level)
            map.animateCamera((cameraUpdate), object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    map.mapType = GoogleMap.MAP_TYPE_HYBRID
                    var latLngBounds =
                        LatLngBounds(LatLng(22.229034, 84.766047), LatLng(22.235203, 84.779179))
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 14))
//                    map.setLatLngBoundsForCameraTarget(latLngBounds)

                }

                override fun onCancel() {

                }

            })

        }
    }

    fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    fun checkStoaragePermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }


    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_CODE
        )
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }


    fun isServiceOk(): Boolean {
        val available =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MainActivity)
        if (available == ConnectionResult.SUCCESS) {
            logger("Service is available and is working")
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            logger("an error occur but it can be resolve")
            val dialog = GoogleApiAvailability.getInstance()
                .getErrorDialog(this@MainActivity, available, ERROR_DIALAOG_CODE)
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
                    startActivityForResult(intent, GPS_REQUEST_CODE)
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
        if (requestCode == GPS_REQUEST_CODE) {
            if (providerEnabled) {
                Toast.makeText(this, "GPS is enabled", Toast.LENGTH_SHORT).show()
            } else {
                isGPSEnabled();
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissions.forEach { s ->
            logger(s)
        }
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    showToast("Please enable location permission")
                }
            }
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    showToast("Please enable storage permission")
                }
            }


        }
    }
}
