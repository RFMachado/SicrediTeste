package br.com.teste.sicredi.feature.detail.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import br.com.teste.sicredi.R
import br.com.teste.sicredi.feature.common.ViewState
import br.com.teste.sicredi.feature.detail.domain.entity.EventDetailData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class EventDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val viewModel: EventDetailViewModel by viewModel()
    private val eventId by lazy { intent.getIntExtra(EXTRA_EVENT_ID, 0) }

    companion object {
        const val EXTRA_EVENT_ID = "eventId"

        fun launchIntent(context: Context, eventId: Int): Intent {
            val intent = Intent(context, EventDetailActivity::class.java)
            intent.putExtra(EXTRA_EVENT_ID, eventId)

            return intent
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.setAllGesturesEnabled(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        viewModel.fetchEventDetail(eventId)

        bindObservers()
        bindListeners()
        setupMapView()
    }

    private fun moveToEventPositionMark(lat: Double, lng: Double, title: String) {
        val eventPosition = LatLng(lat, lng)
        mMap.addMarker(
            MarkerOptions()
                .position(eventPosition)
                .title(title)
        )

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventPosition, 15f))
    }

    private fun setupMapView() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    private fun bindListeners() {
        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun bindObservers() {
        viewModel.getState().observe(this, Observer { state ->
            when (state) {
                is ViewState.Loading -> {
                    showLoading()
                }
                is ViewState.Success -> {
                    hideLoading()
                    showData(state.data)
                }
                is ViewState.Failed -> {
                    hideLoading()
                    showError(state.throwable)
                }
            }
        })
    }

    private fun showData(eventData: EventDetailData) {
        eventData.run {
            txtTitle.text = title
            txtDescription.text = description
            txtPrice.text = getString(R.string.price, price)
            txtDate.text = getString(R.string.date_and_time, dateString)

            moveToEventPositionMark(lat = lat, lng = lng, title = title)
        }

        Glide.with(this)
            .load(eventData.image)
            .error(R.drawable.placeholder)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imgEvent)

    }

    private fun showError(throwable: Throwable) {
        Timber.e(throwable)
    }

    private fun showLoading() {
        progressBar.isVisible = true
    }

    private fun hideLoading() {
        progressBar.isVisible = false
    }
}