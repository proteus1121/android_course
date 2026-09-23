package ua.edu.mobile.smartlife.ui.map

import android.Manifest
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import ua.edu.mobile.smartlife.R
import ua.edu.mobile.smartlife.data.model.HealthRecord
import ua.edu.mobile.smartlife.ui.AppViewModelProvider
import ua.edu.mobile.smartlife.ui.components.formatDate
import ua.edu.mobile.smartlife.ui.components.formattedValue
import ua.edu.mobile.smartlife.ui.components.rememberPermissionsState
import java.io.File

private val KYIV = GeoPoint(50.4501, 30.5234)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val records by viewModel.recordsWithLocation.collectAsStateWithLifecycle()
    val myLocation by viewModel.myLocation.collectAsStateWithLifecycle()
    val selectedPoint by viewModel.selectedPoint.collectAsStateWithLifecycle()
    val permissions = rememberPermissionsState(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        requireAll = false
    )
    val mapView = rememberMapViewWithLifecycle()
    var centeredOnMe by remember { mutableStateOf(false) }

    // Як тільки з'явився дозвіл — визначаємо місцезнаходження
    LaunchedEffect(permissions.granted) {
        if (permissions.granted) viewModel.refreshMyLocation()
    }
    // Перший раз плавно переміщуємо карту до користувача
    LaunchedEffect(myLocation) {
        val point = myLocation ?: return@LaunchedEffect
        if (!centeredOnMe) {
            mapView.controller.animateTo(GeoPoint(point.latitude, point.longitude), 15.0, 1_000L)
            centeredOnMe = true
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Карта записів (${records.size})") }) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(onClick = { mapView.controller.zoomIn() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Наблизити")
                }
                SmallFloatingActionButton(onClick = { mapView.controller.zoomOut() }) {
                    Icon(Icons.Filled.Remove, contentDescription = "Віддалити")
                }
                SmallFloatingActionButton(onClick = { mapView.showAll(records) }) {
                    Icon(Icons.Filled.FitScreen, contentDescription = "Показати всі записи")
                }
                FloatingActionButton(onClick = {
                    if (permissions.granted) {
                        viewModel.refreshMyLocation()
                        myLocation?.let {
                            mapView.controller.animateTo(GeoPoint(it.latitude, it.longitude), 16.0, 800L)
                        }
                    } else {
                        permissions.request()
                    }
                }) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "Моє місцезнаходження")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // AndroidView вбудовує класичний Android View (MapView) у Compose
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize(),
                update = { map ->
                    map.showOverlays(
                        records = records,
                        myLocation = myLocation,
                        selectedPoint = selectedPoint,
                        onMapTap = viewModel::selectPoint
                    )
                }
            )

            // Обов'язковий підпис ліцензії даних OpenStreetMap
            Text(
                "© OpenStreetMap contributors",
                style = MaterialTheme.typography.labelSmall,
                color = Color.DarkGray,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(Color.White.copy(alpha = 0.7f))
                    .padding(horizontal = 4.dp)
            )

            selectedPoint?.let { point ->
                SelectedPointCard(
                    point = point,
                    myLocation = myLocation,
                    onClose = { viewModel.selectPoint(null) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                )
            }

            if (!permissions.granted) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Дозвольте геолокацію, щоб бачити себе на карті") },
                        trailingContent = { TextButton(onClick = permissions.request) { Text("Дозволити") } }
                    )
                }
            }
        }
    }
}

/** Картка з координатами обраної точки та відстанню до неї. */
@Composable
private fun SelectedPointCard(
    point: MapPoint,
    myLocation: MapPoint?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text("Обрана точка") },
            supportingContent = {
                val coordinates = "%.5f, %.5f".format(point.latitude, point.longitude)
                val distance = myLocation?.let {
                    // distanceToAsDouble повертає відстань по поверхні Землі в метрах
                    val meters = GeoPoint(it.latitude, it.longitude)
                        .distanceToAsDouble(GeoPoint(point.latitude, point.longitude))
                    if (meters < 1000) "\nВідстань від вас: %.0f м".format(meters)
                    else "\nВідстань від вас: %.2f км".format(meters / 1000)
                } ?: ""
                Text(coordinates + distance)
            },
            trailingContent = {
                IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Закрити") }
            }
        )
    }
}

/** Створює MapView один раз і передає йому події життєвого циклу екрана. */
@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        configureOsmdroid(context)
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)           // стандартні плитки OpenStreetMap
            setMultiTouchControls(true)                        // масштабування двома пальцями
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(12.0)
            controller.setCenter(KYIV)
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }
    return mapView
}

/** osmdroid вимагає User-Agent і місце для кешу плиток. */
private fun configureOsmdroid(context: Context) {
    Configuration.getInstance().apply {
        userAgentValue = context.packageName
        osmdroidBasePath = File(context.cacheDir, "osmdroid")
        osmdroidTileCache = File(osmdroidBasePath, "tiles")
    }
}

/** Перемальовує всі шари карти: обробник дотиків, маркери записів, "я тут", обрану точку. */
private fun MapView.showOverlays(
    records: List<HealthRecord>,
    myLocation: MapPoint?,
    selectedPoint: MapPoint?,
    onMapTap: (MapPoint) -> Unit
) {
    overlays.clear()
    overlays.add(MapEventsOverlay(object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
            onMapTap(MapPoint(p.latitude, p.longitude))
            return true
        }

        override fun longPressHelper(p: GeoPoint): Boolean = false
    }))

    records.forEach { record ->
        overlays.add(Marker(this).apply {
            position = GeoPoint(record.latitude!!, record.longitude!!)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(context, R.drawable.ic_map_record)
            title = "${record.type.title}: ${record.formattedValue()}"
            snippet = formatDate(record.timestamp)
        })
    }

    myLocation?.let {
        overlays.add(Marker(this).apply {
            position = GeoPoint(it.latitude, it.longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            icon = ContextCompat.getDrawable(context, R.drawable.ic_map_me)
            title = "Ви тут"
        })
    }

    selectedPoint?.let {
        overlays.add(Marker(this).apply {
            position = GeoPoint(it.latitude, it.longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(context, R.drawable.ic_map_pin)
            title = "Обрана точка"
        })
    }
    invalidate()
}

/** Масштабує карту так, щоб було видно всі записи. */
private fun MapView.showAll(records: List<HealthRecord>) {
    val points = records.map { GeoPoint(it.latitude!!, it.longitude!!) }
    when {
        points.isEmpty() -> return
        points.size == 1 -> controller.animateTo(points.first(), 16.0, 800L)
        else -> zoomToBoundingBox(BoundingBox.fromGeoPoints(points).increaseByScale(1.3f), true)
    }
}
