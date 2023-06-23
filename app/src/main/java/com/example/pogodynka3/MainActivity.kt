package com.example.pogodynka3

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.pogodynka.POJO.ModelClass
import com.example.pogodynka.Utilities.ApiUtilities
import com.example.pogodynka3.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var activityMainBinding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        supportActionBar?.hide()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        activityMainBinding.switchView.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1F)
                val dim = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    40F, applicationContext.resources.displayMetrics)
                val ivParams = LinearLayout.LayoutParams(dim.toInt() , dim.toInt())
                ivParams.gravity = Gravity.CENTER
                activityMainBinding.rlSunriseWrapper.layoutParams = params
                activityMainBinding.rlPressureWrapper.layoutParams = params
                activityMainBinding.rlSunsetWrapper.layoutParams = params
                activityMainBinding.llMainBgAbove.orientation = LinearLayout.VERTICAL
                activityMainBinding.ivPressure.layoutParams = ivParams
                activityMainBinding.ivSunrise.layoutParams = ivParams
                activityMainBinding.ivSunset.layoutParams = ivParams

                activityMainBinding.tvD1.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_large))
                activityMainBinding.tvD2.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_large))
                 activityMainBinding.tvD3.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_large))
                activityMainBinding.tvPressure.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_xx_large))
                activityMainBinding.tvSunrise.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_xx_large))
                activityMainBinding.tvSunset.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_xx_large))

                activityMainBinding.tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_x_large))
                activityMainBinding.tvTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_x_large))
                activityMainBinding.tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_x_large))
            }else{
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1F)
                val dim = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    25F, applicationContext.resources.displayMetrics)
                val ivParams = LinearLayout.LayoutParams(dim.toInt() , dim.toInt())
                ivParams.gravity = Gravity.CENTER
                activityMainBinding.rlSunriseWrapper.layoutParams = params
                activityMainBinding.rlPressureWrapper.layoutParams = params
                activityMainBinding.rlSunsetWrapper.layoutParams = params
                activityMainBinding.llMainBgAbove.orientation = LinearLayout.HORIZONTAL
                activityMainBinding.ivPressure.layoutParams = ivParams
                activityMainBinding.ivSunrise.layoutParams = ivParams
                activityMainBinding.ivSunset.layoutParams = ivParams

                activityMainBinding.tvD1.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_med))
                activityMainBinding.tvD2.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_med))
                activityMainBinding.tvD3.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_med))
                activityMainBinding.tvPressure.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_x_large))
                activityMainBinding.tvSunrise.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_x_large))
                activityMainBinding.tvSunset.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_x_large))

                activityMainBinding.tvDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_large))
                activityMainBinding.tvTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_large))
                activityMainBinding.tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_large))
            }
        }

        activityMainBinding.etGetCityName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                getCityWeather (activityMainBinding.etGetCityName.text.toString())
                val view = this.currentFocus
                if (view != null){
                    val imm:InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    activityMainBinding.etGetCityName.clearFocus()
                }
                true
            } else {
                false
            }
        }
        getCurrentLocation()
    }


    private fun getCityWeather(cityName: String) {
        activityMainBinding.pbLoading.visibility = View.VISIBLE
        ApiUtilities.getApiInterface()?.getCityWeatherData(cityName, api_key.key)?.enqueue(object : Callback<ModelClass> {
               @RequiresApi(Build.VERSION_CODES.O)
               override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                   setDataOnViews(response.body())
               }

               override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                   Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
               }
        })
    }

    private fun getCurrentLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }
                fusedLocationProviderClient!!.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result

                    if (location != null) {
                        fetchCurrentLocationWeather(
                            location.latitude.toString(),
                            location.longitude.toString()
                        )
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }

    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {
        activityMainBinding.pbLoading.visibility = View.VISIBLE
        ApiUtilities.getApiInterface()?.getCurrentWeatherData(latitude, longitude, api_key.key)
            ?.enqueue(object : Callback<ModelClass> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if (response.isSuccessful) {
                        setDataOnViews(response.body())
                    }
                }

                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                    Toast.makeText(applicationContext, "ERROR", Toast.LENGTH_SHORT).show()
                }

            })


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataOnViews(body: ModelClass?) {


        val sdf = SimpleDateFormat("dd/MM/yyyy")
        val sdf2 = SimpleDateFormat("HH:mm")
        val currentDate = sdf.format(Date())
        val currentTime = sdf2.format(Date())
        activityMainBinding.tvDate.text = currentDate
        activityMainBinding.tvTime.text = currentTime
        try {
            activityMainBinding.tvTemp.text = Math.round(body!!.main.temp).toString() + "°C"
            activityMainBinding.tvDesc.text = body.weather[0].description
            activityMainBinding.tvPressure.text = body.main.pressure.toString() + " hpa"
            activityMainBinding.tvSunset.text = timeStampToLocal(body.sys.sunset.toLong())


            activityMainBinding.tvSunrise.text = timeStampToLocal(body.sys.sunrise.toLong())
            activityMainBinding.etGetCityName.setText(body.name)

            val icon = IMG_URL + body.weather[0].icon + "@4x.png"
            Picasso.with(this).load(icon).into(activityMainBinding.ivWeatherIcon)
        }catch (e: java.lang.Exception){
            Toast.makeText(applicationContext,"Niepoprawne miasto", Toast.LENGTH_SHORT).show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeStampToLocal(timeStamp: Long): String {
        val localTime = timeStamp.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }
        return localTime.toString()
    }


    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        private const val IMG_URL = "https://openweathermap.org/img/wn/"
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
