package com.gabai.presentation.ui.clinics

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.gabai.domain.model.Clinic
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

// Free OSM raster tiles -- no API key/billing (per user's open-source map choice).
// tile.openstreetmap.org's usage policy caps this to light/low-volume use; swap
// for a dedicated tile provider before any real production traffic.
private const val OSM_RASTER_STYLE = """
{
  "version": 8,
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
      "tileSize": 256,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [
    { "id": "osm", "type": "raster", "source": "osm" }
  ]
}
"""

@Composable
fun ClinicMap(
    clinics: List<Clinic>,
    userLocation: UserLocation?,
    modifier: Modifier = Modifier,
    onClinicMarkerClick: (Clinic) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember {
        MapLibre.getInstance(context)
        MapView(context)
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView },
        update = { view ->
            view.getMapAsync { map ->
                map.setStyle(Style.Builder().fromJson(OSM_RASTER_STYLE)) {
                    map.clear()
                    val points = mutableListOf<LatLng>()

                    userLocation?.let {
                        val userLatLng = LatLng(it.latitude, it.longitude)
                        points.add(userLatLng)
                        map.addMarker(MarkerOptions().position(userLatLng).title("You"))
                    }

                    val clinicMarkers = mutableMapOf<Long, Clinic>()
                    clinics.forEach { clinic ->
                        val latLng = LatLng(clinic.latitude, clinic.longitude)
                        points.add(latLng)
                        val marker = map.addMarker(
                            MarkerOptions().position(latLng).title(clinic.name).snippet(clinic.address)
                        )
                        clinicMarkers[marker.id] = clinic
                    }

                    map.setOnMarkerClickListener { marker ->
                        clinicMarkers[marker.id]?.let(onClinicMarkerClick)
                        false
                    }

                    if (points.size == 1) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(points.first(), 14.0))
                    } else if (points.size > 1) {
                        val bounds = LatLngBounds.Builder().apply { points.forEach(::include) }.build()
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80))
                    }
                }
            }
        }
    )
}

fun clinicDirectionsIntent(clinic: Clinic): Intent {
    val label = Uri.encode(clinic.name)
    val uri = Uri.parse("geo:${clinic.latitude},${clinic.longitude}?q=${clinic.latitude},${clinic.longitude}($label)")
    return Intent(Intent.ACTION_VIEW, uri)
}
