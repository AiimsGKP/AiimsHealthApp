package com.example.aiimshealthapp

import android.app.ProgressDialog
import android.graphics.Rect
import android.location.GpsStatus
import android.location.Location
import android.os.Bundle
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import com.example.aiimshealthapp.databinding.ActivityHealthFacitlityLocationActivtiyBinding
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.net.URL

class HealthFacitlityLocationActivtiy : AppCompatActivity(), MapListener, GpsStatus.Listener {

    private lateinit var mMap: MapView
    private lateinit var controller: IMapController
    private lateinit var mMyLocationOverlay: MyLocationNewOverlay
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private val TAG = "CHECK_RESPONSE"
    private val grasshopper_key = "aeabf753-2f47-493a-b937-426d307f24cb"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHealthFacitlityLocationActivtiyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load configuration
        Configuration.getInstance().load(applicationContext, getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE))

        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.setMultiTouchControls(true)

        // Check for location permissions
        if (checkLocationPermissions()) {
            setupMap()
        } else {
            requestLocationPermissions()
        }
    }

    private fun setupMap() {
        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMap)
        controller = mMap.controller

        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true

        // Set a listener for when the first location fix is obtained
        mMyLocationOverlay.runOnFirstFix {
            runOnUiThread {
                // Center the map on the user's location
                controller.setCenter(mMyLocationOverlay.myLocation)
                controller.animateTo(mMyLocationOverlay.myLocation)
            }
        }

        controller.setZoom(15.0)  // Set a reasonable zoom level
        addMarkers()
        mMap.overlays.add(mMyLocationOverlay)
        mMap.addMapListener(this)
    }

    private fun addMarkers() {
        val locations = listOf(
            GeoPoint(26.747019373623484, 83.44502853615273) to "Delhi",
            GeoPoint(19.0760, 72.8777) to "Mumbai",
            GeoPoint(13.0827, 80.2707) to "Chennai"
        )



        for ((geoPoint, title) in locations) {
            val marker = Marker(mMap)
            marker.position = geoPoint
            marker.title = title
            marker.icon = resources.getDrawable(R.drawable.ic_marker)

            marker.setOnMarkerClickListener { _, _ ->
                // Handle directions when the marker is clicked
                Toast.makeText(this, "Marker Clicked!!", Toast.LENGTH_SHORT).show()
                if (mMyLocationOverlay.myLocation != null) {
                    val startPoint = GeoPoint(mMyLocationOverlay.myLocation.latitude, mMyLocationOverlay.myLocation.longitude)
                    showDirections(startPoint, geoPoint)
                } else {
                    Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show()
                }
                true
            }

            mMap.overlays.add(marker)
        }
    }

    private fun showDirections(startPoint: GeoPoint, endPoint: GeoPoint) {
        // This is a simplified example; you should use a proper routing API to get the route

        val url = "https://graphhopper.com/api/1/route?point=${startPoint.latitude},${startPoint.longitude}&point=${endPoint.latitude},${endPoint.longitude}&vehicle=car&key=$grasshopper_key"
        Log.i(TAG, url)
        // You can use an AsyncTask or a Coroutine to make this network call
        Thread {
            try {
                val result = URL(url).readText() // Fetch the route data
                Log.i(TAG, result)
                // Parse the result to get the route points (this part will vary depending on your chosen API)
                val routePoints = parseRoute(result)
                Log.i(TAG, routePoints.toString())

                runOnUiThread {
                    // Display the route on the map
                    drawRoute(routePoints)
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }.start()
    }

    private fun drawRoute(routePoints: List<GeoPoint>) {
        Log.i(TAG, "Drawing Route with ${routePoints.size} points")

        if (routePoints.isEmpty()) {
            Log.w(TAG, "No route points to draw")
            return
        }

        // Create a new Polyline instance with the mapView
        val polyline = Polyline(mMap) // Ensure you pass the MapView

        // Clear any existing points (if needed) and add new route points
        polyline.points.clear()
        polyline.points.addAll(routePoints)

        // Set the outline paint color and width
        polyline.outlinePaint.color = Color.BLUE // Set color
        polyline.outlinePaint.strokeWidth = 10f // Set width

        // Add the polyline to the map overlays
        mMap.overlays.add(polyline)
        Log.i(TAG, "Overlay Count: ${mMap.overlays.size}")

        // Refresh the map view
        mMap.invalidate() // Refresh map to show new overlays
        mMap.refreshDrawableState()
        val bounds = BoundingBox.fromGeoPoints(routePoints)
        mMap.zoomToBoundingBox(bounds, true) // Zoom to the bounding box of the route
    }

    private fun parseRoute(result: String): List<GeoPoint> {
        val routePoints = mutableListOf<GeoPoint>()

        // Parse the JSON response
        val jsonObject = JSONObject(result)
        val paths = jsonObject.getJSONArray("paths")

        if (paths.length() > 0) {
            val path = paths.getJSONObject(0)
            val encodedPoints = path.getString("points") // Get the encoded polyline

            // Decode the encoded points
            routePoints.addAll(decodePolyline(encodedPoints))
        }

        return routePoints
    }

    private fun decodePolyline(encoded: String): List<GeoPoint> {
        val poly = mutableListOf<GeoPoint>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0

            // Decode latitude
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlat = if (result and 1 != 0) {
                // Use proper syntax for the bitwise NOT operation
                -(result shr 1)
            } else {
                result shr 1
            }
            lat += dlat

            shift = 0
            result = 0

            // Decode longitude
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlng = if (result and 1 != 0) {
                // Use proper syntax for the bitwise NOT operation
                -(result shr 1)
            } else {
                result shr 1
            }
            lng += dlng

            // Add the GeoPoint to the list
            poly.add(GeoPoint((lat.toDouble() / 1E5), (lng.toDouble() / 1E5)))
        }
        return poly
    }


    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                setupMap()  // Set up the map if permission is granted
            } else {
                Toast.makeText(this, "Location permission is required to show your location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
//        Log.e("TAG", "onScroll: Latitude: ${event?.source?.getMapCenter()?.latitude}, Longitude: ${event?.source?.getMapCenter()?.longitude}")
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
//        Log.e("TAG", "onZoom: zoom level: ${event?.zoomLevel}, source: ${event?.source}")
        return false
    }

    override fun onGpsStatusChanged(event: Int) {
        // Handle GPS status changes if necessary
    }
}
