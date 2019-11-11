package com.project.routeapidemo

import android.graphics.Color
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONObject
import android.os.AsyncTask
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    internal var markerPoints = ArrayList<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 16f))

        mMap!!.setOnMapClickListener { latLng ->
            if (markerPoints.size > 1) {
                markerPoints.clear()
                mMap!!.clear()
            }

            // Adding new item to the ArrayList
            markerPoints.add(latLng)

            // Creating MarkerOptions
            val options = MarkerOptions()

            // Setting the position of the marker
            options.position(latLng)

            if (markerPoints.size === 1) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            } else if (markerPoints.size === 2) {
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            }

            // Add new marker to the Google Map Android API V2
            mMap!!.addMarker(options)

            // Checks, whether start and end locations are captured
            if (markerPoints.size >= 2) {
                val origin = markerPoints.get(0) as LatLng
                val dest = markerPoints.get(1) as LatLng

                // Getting URL to the Google Directions API
                val url = getDirectionsUrl(origin, dest)

                val downloadTask = DownloadTask()

                // Start downloading json data from Google Directions API
                downloadTask.execute(url)
            }
        }

    }

    private inner class DownloadTask : AsyncTask<String, Unit, String>() {


        override protected fun doInBackground(vararg url: String): String {

            var data = ""

            try {
                data = downloadUrl(url[0])
            } catch (e: Exception) {
                Log.d("Background Task", e.toString())
            }

            return data
        }

        override protected fun onPostExecute(result: String) {
            super.onPostExecute(result)

            val parserTask = ParserTask()


            parserTask.execute(result)

        }
    }

    private inner class ParserTask : AsyncTask<String, Int, List<List<HashMap<String,String>>>>() {

        // Parsing the data in non-ui thread
        override fun doInBackground(vararg jsonData: String): List<List<HashMap<String,String>>>? {

            val jObject: JSONObject
            var routes: List<List<HashMap<String,String>>>? = null

            try {
                jObject = JSONObject(jsonData[0])
                val parser = DirectionsJSONParser()

                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return routes
        }

        override fun onPostExecute(result: List<List<HashMap<String,String>>>) {
            var points: ArrayList<LatLng>? = null
            var lineOptions: PolylineOptions? = null
            val markerOptions = MarkerOptions()
            lineOptions = PolylineOptions()

            for (i in result.indices) {
                points = ArrayList()

                val path = result[i]

                for (j in path.indices) {
                    val point = path[j]

                    val lat = point.get("lat")?.let { java.lang.Double.parseDouble(it) }
                    val lng = point.get("lng")?.let { java.lang.Double.parseDouble(it) }
                    val position = lat?.let { lng?.let { it1 -> LatLng(it, it1) } }

                    if (position != null) {
                        points!!.add(position)
                    }
                }

                lineOptions.addAll(points!!)
                lineOptions.width(12f)
                lineOptions.color(Color.RED)
                lineOptions.geodesic(true)

            }

            // Drawing polyline in the Google Map for the i-th route
            mMap!!.addPolyline(lineOptions)
        }
    }

    private fun getDirectionsUrl(origin: LatLng, dest: LatLng): String {

        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude

        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude

        // Sensor enabled
        val sensor = "sensor=false"
        val mode = "mode=driving"

        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor&$mode"

        // Output format
        val output = "json"

        // Building the url to the web service


        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyDSudsmknGATqrkg9IjJQOFk2QqkSII6qs"
    }

    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)

            urlConnection = url.openConnection() as HttpURLConnection

            urlConnection!!.connect()

            iStream = urlConnection!!.getInputStream()

            val br = BufferedReader(InputStreamReader(iStream))

            val sb = StringBuffer()

          /*  var line = ""
            while ((line = br.readLine()) != null) {
                sb.append(line)
            }*/
            var line : String?

            do {

                line = br.readLine()

                if (line == null)

                    break

                sb.append(line)

            } while (true)

            data = sb.toString()

            br.close()

        } catch (e: Exception) {
            Log.d("Exception", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }
}

/*
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    override fun onMarkerClick(p0: Marker?): Boolean {
return false   }

    val permission_class=Permission_Class()
    private lateinit var mMap: GoogleMap
    private  var PERMISSION_GRANTED: Boolean=false
    private val LOCATION_REQUEST_CODE = 101





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        if (permission_class.getPermission(
                this@MapsActivity,
                Manifest.permission.ACCESS_FINE_LOCATION,
                LOCATION_REQUEST_CODE,
                "This permission for Location"
            )
        ) {
            PERMISSION_GRANTED = true
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        } else {
            PERMISSION_GRANTED = false
        }


    }

        override fun onMapReady(googleMap: GoogleMap) {


        mMap=googleMap

        if (mMap != null) {



                Toast.makeText(this@MapsActivity, "Location Allow", Toast.LENGTH_SHORT).show()
                mMap?.isMyLocationEnabled = true
                mMap.getUiSettings().setZoomControlsEnabled(true)
                mMap.setOnMarkerClickListener(this)
                val myPlace = LatLng(40.73, -73.99)  // this is New York
                mMap.addMarker(MarkerOptions().position(myPlace).title("My Favorite City"))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myPlace))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 12.0f))


            val latLngOrigin = LatLng(10.3181466, 123.9029382) // Ayala
            val latLngDestination = LatLng(10.311795,123.915864) // SM City
            mMap!!.addMarker(MarkerOptions().position(latLngOrigin).title("Ayala"))
            mMap!!.addMarker(MarkerOptions().position(latLngDestination).title("SM City"))
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOrigin, 12.0f))


        }}





    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] !=
                    PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Unable to show location - permission required",Toast.LENGTH_LONG).show()


                } else {
                    val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map) as SupportMapFragment
                    mapFragment.getMapAsync(this)
                    Toast.makeText(this,"permission Granted",Toast.LENGTH_LONG).show()


                }
            }
        }
    }


}
*/
