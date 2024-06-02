package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.Properties

class Map : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 초기화
        Places.initialize(requireContext(), BuildConfig.GOOGLE_MAPS_API_KEY)
        placesClient = Places.createClient(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (!success) {
                Log.i("도로스타일 적용실패", "ㅇㅇ")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 좌표 = 삼육대 후문
        val defaultLocation = LatLng(37.643781, 127.109162)
        googleMap.addMarker(MarkerOptions().position(defaultLocation).title("삼육대학교 후문"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 19f))

        // 등록된 장소(poi) 클릭시
        googleMap.setOnPoiClickListener { poi ->
            fetchPlaceDetails(poi.placeId)
        }
    }

    @SuppressLint("ResourceType")
    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(
            Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
            Place.Field.PHONE_NUMBER, Place.Field.RATING, Place.Field.USER_RATINGS_TOTAL,
            Place.Field.OPENING_HOURS, Place.Field.REVIEWS
        )

        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            var details = ""
            details += "이름: ${place.name?:"등록되지 않음"}\n"
            details += "주소: ${place.address?:"등록되지 않음"}\n\n"
            details += "전화번호: ${place.phoneNumber?:"등록되지 않음"}\n"
            details += "별점: ${place.rating?:0} / 5 (${place.userRatingsTotal?:0}개의 별점 평가)\n\n"
            details += "영업 시간:\n${place.openingHours?.weekdayText?.joinToString("\n")?:"등록되지 않음"}\n"
            details.trimIndent()

            AlertDialog.Builder(requireContext())
                .setTitle(place.name)
                .setMessage(details)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            val toast = Toast.makeText(requireContext(), "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT)
            toast.show()
        }
    }
}