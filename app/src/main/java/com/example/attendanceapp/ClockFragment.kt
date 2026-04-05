package com.example.attendanceapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 打刻画面：現在日時と位置情報を表示し、出勤/退勤を記録する
 */
class ClockFragment : Fragment() {

    private val viewModel: AttendanceViewModel by viewModels()

    private lateinit var textCurrentDatetime: TextView
    private lateinit var textCurrentLocation: TextView
    private lateinit var buttonClockIn: Button
    private lateinit var buttonClockOut: com.google.android.material.button.MaterialButton

    /** 現在日時を1秒ごとに更新するためのハンドラ */
    private val handler = Handler(Looper.getMainLooper())
    private val dateTimeFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.JAPAN)

    /** 取得済みの住所文字列 */
    private var currentAddress: String = ""

    /** 位置情報クライアント */
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    /** パーミッションリクエスト */
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            // パーミッション許可後に位置情報を取得
            fetchCurrentLocation()
        } else {
            textCurrentLocation.text = getString(R.string.location_permission_denied)
        }
    }

    /** 現在日時を1秒ごとに更新する Runnable */
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            textCurrentDatetime.text = dateTimeFormat.format(Date())
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_clock, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ビューの取得
        textCurrentDatetime = view.findViewById(R.id.text_current_datetime)
        textCurrentLocation = view.findViewById(R.id.text_current_location)
        buttonClockIn = view.findViewById(R.id.button_clock_in)
        buttonClockOut = view.findViewById(R.id.button_clock_out)

        // 位置情報の取得を開始
        checkLocationPermissionAndFetch()

        // 出勤ボタン
        buttonClockIn.setOnClickListener {
            val now = Date()
            val date = dateFormat.format(now)
            val time = timeFormat.format(now)
            val location = currentAddress.ifEmpty { getString(R.string.location_unknown) }
            viewModel.clockIn(date, time, location)
            Toast.makeText(requireContext(), getString(R.string.clock_in_done), Toast.LENGTH_SHORT).show()
        }

        // 退勤ボタン
        buttonClockOut.setOnClickListener {
            val now = Date()
            val time = timeFormat.format(now)
            val location = currentAddress.ifEmpty { getString(R.string.location_unknown) }
            viewModel.clockOut(time, location)
            Toast.makeText(requireContext(), getString(R.string.clock_out_done), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // 日時の自動更新を開始
        handler.post(updateTimeRunnable)
    }

    override fun onPause() {
        super.onPause()
        // 日時の自動更新を停止
        handler.removeCallbacks(updateTimeRunnable)
    }

    /** 位置情報パーミッションを確認し、未許可ならリクエストする */
    private fun checkLocationPermissionAndFetch() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            fetchCurrentLocation()
        } else {
            // パーミッションをリクエスト
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /** FusedLocationProviderClient で現在地を取得し、Geocoder で住所に変換する */
    @Suppress("MissingPermission")
    private fun fetchCurrentLocation() {
        textCurrentLocation.text = getString(R.string.location_loading)

        val cancellationToken = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                // Geocoder で緯度経度を住所に変換
                try {
                    val geocoder = Geocoder(requireContext(), Locale.JAPAN)
                    val addresses = geocoder.getFromLocation(
                        location.latitude, location.longitude, 1
                    )
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        // 都道府県 + 市区町村 + 町名
                        val adminArea = addr.adminArea ?: ""
                        val locality = addr.locality ?: ""
                        val thoroughfare = addr.thoroughfare ?: ""
                        currentAddress = "$adminArea$locality$thoroughfare"
                    } else {
                        currentAddress = getString(R.string.location_unknown)
                    }
                } catch (e: Exception) {
                    currentAddress = getString(R.string.location_unknown)
                }
            } else {
                currentAddress = getString(R.string.location_unknown)
            }
            textCurrentLocation.text = currentAddress
        }.addOnFailureListener {
            currentAddress = getString(R.string.location_unknown)
            textCurrentLocation.text = currentAddress
        }
    }
}
