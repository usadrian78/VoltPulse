package ro.adrian.myapplication

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin

fun isRomanian(): Boolean = Locale.getDefault().language.equals("ro", ignoreCase = true)

data class BatteryTelemetry(
    val levelPercent: Int = 0,
    val cycleCount: Int = -1,
    val healthPercent: Float = -1f,
    val fullCapacityMah: Int = 0,
    val designCapacityMah: Int = 4400,
    val powerWatts: Float = 0f,
    val voltageVolts: Float = 0f,
    val currentMa: Int = 0,
    val temperatureCelsius: Float = 0f,
    val isCharging: Boolean = false,
    val chargingSpeedLabel: String = "STANDBY",
    val shizukuStatus: String = "",
    val isExactHardwareData: Boolean = false,
    val chargerVoltageVolts: Float = 0f,
    val peakChargeWatts: Float = 0f
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF000000),
                    surface = Color(0xFF111317),
                    onBackground = Color(0xFFF2F4F8),
                    onSurface = Color(0xFFF2F4F8)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BatteryDashboardScreen()
                }
            }
        }
    }
}

@Composable
fun BatteryDashboardScreen() {
    val context = LocalContext.current
    val ro = remember { isRomanian() }
    var telemetry by remember { mutableStateOf(BatteryTelemetry()) }

    LaunchedEffect(Unit) {
        while (true) {
            telemetry = withContext(Dispatchers.IO) {
                readBatteryTelemetry(context, ro)
            }
            delay(2000)
        }
    }

    val accentColor = when {
        telemetry.powerWatts > 25f -> Color(0xFFFF6D00)
        telemetry.isCharging -> Color(0xFF00E676)
        telemetry.levelPercent <= 20 -> Color(0xFFFF1744)
        else -> Color(0xFF00B0FF)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "VOLT // PULSE • ro.us.adrian",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = accentColor
                )

                Text(
                    text = "${android.os.Build.MODEL} • HAL Live",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
            StatusPill(text = telemetry.chargingSpeedLabel, color = accentColor)
        }

        LiquidBatteryOrbCard(telemetry = telemetry, accentColor = accentColor, ro = ro)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BentoMetricCard(
                modifier = Modifier.weight(1f),
                title = if (ro) "CICLURI ÎNCĂRCARE" else "CHARGE CYCLES",
                value = if (telemetry.cycleCount >= 0) "${telemetry.cycleCount}" else "N/A",
                subtitle = if (telemetry.cycleCount >= 0) {
                    if (telemetry.isExactHardwareData && telemetry.healthPercent > 0f) {
                        val wear = (100f - telemetry.healthPercent).coerceAtLeast(0f)
                        if (ro) String.format(Locale.US, "Uzură celulă: %.1f%%", wear)
                        else String.format(Locale.US, "Cell wear: %.1f%%", wear)
                    } else {
                        if (ro) "Prag standard: 800 cicluri" else "Standard rating: 800 cycles"
                    }
                } else {
                    if (ro) "Pornește Shizuku" else "Start Shizuku"
                },
                accentColor = accentColor,
                progress = if (telemetry.cycleCount >= 0) (telemetry.cycleCount / 800f).coerceIn(0f, 1f) else null
            )

            BentoMetricCard(
                modifier = Modifier.weight(1f),
                title = if (telemetry.isExactHardwareData) {
                    if (ro) "SĂNĂTATE REALĂ" else "REAL HEALTH"
                } else {
                    if (ro) "SĂNĂTATE (EST.)" else "HEALTH (EST.)"
                },
                value = if (telemetry.healthPercent > 0f) String.format(Locale.US, "%.1f%%", telemetry.healthPercent) else "N/A",
                subtitle = if (telemetry.fullCapacityMah > 0)
                    "${telemetry.fullCapacityMah} / ${telemetry.designCapacityMah} mAh"
                else "4400 mAh Design",
                accentColor = Color(0xFF00E676),
                progress = if (telemetry.healthPercent > 0f) (telemetry.healthPercent / 100f).coerceIn(0f, 1f) else null
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val sign = if (telemetry.powerWatts > 0.1f) "+" else ""
            val subText = if (telemetry.isCharging && telemetry.chargerVoltageVolts > 0f) {
                "USB ${String.format(Locale.US, "%.1fV", telemetry.chargerVoltageVolts)} • Peak ${String.format(Locale.US, "%.1fW", telemetry.peakChargeWatts)}"
            } else {
                "${telemetry.currentMa} mA • ${String.format(Locale.US, "%.2f V", telemetry.voltageVolts)}"
            }
            BentoMetricCard(
                modifier = Modifier.weight(1f),
                title = if (ro) "PUTERE LIVE" else "LIVE POWER",
                value = String.format(Locale.US, "%s%.1f W", sign, telemetry.powerWatts),
                subtitle = subText,
                accentColor = accentColor
            )

            val tempColor = when {
                telemetry.temperatureCelsius >= 42f -> Color(0xFFFF1744)
                telemetry.temperatureCelsius >= 37f -> Color(0xFFFFAB00)
                else -> Color(0xFF00B0FF)
            }
            BentoMetricCard(
                modifier = Modifier.weight(1f),
                title = if (ro) "TEMPERATURĂ" else "TEMPERATURE",
                value = String.format(Locale.US, "%.1f°C", telemetry.temperatureCelsius),
                subtitle = if (telemetry.temperatureCelsius < 37f) {
                    if (ro) "Termic optim" else "Optimal thermal"
                } else {
                    if (ro) "Încălzire moderată" else "Warm"
                },
                accentColor = tempColor
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF111317))
                .border(1.dp, Color(0xFF1F232B), RoundedCornerShape(24.dp))
                .clickable {
                    try {
                        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                            Shizuku.requestPermission(1001)
                        }
                    } catch (_: Exception) {}
                }
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MOTOROLA HEALTH HAL (SHIZUKU)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF8A94A6)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = telemetry.shizukuStatus,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (telemetry.isExactHardwareData) Color(0xFF00E676) else Color(0xFFFFAB00)
                    )
                }
                if (!telemetry.isExactHardwareData) {
                    Text(
                        text = if (ro) "CONECTEAZĂ" else "CONNECT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
        }

        Text(
            text = "VOLT // PULSE • Built by ro.us.adrian",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = Color(0xFF5A6270),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp)
        )
    }
}
@Composable
fun LiquidBatteryOrbCard(telemetry: BatteryTelemetry, accentColor: Color, ro: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (telemetry.isCharging) 1400 else 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF13161C), Color(0xFF0A0C0F))
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(195.dp)) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                color = accentColor.copy(alpha = 0.18f),
                radius = radius,
                style = Stroke(width = 6.dp.toPx())
            )

            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = 360f * (telemetry.levelPercent / 100f),
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )

            val innerRadius = radius - 10.dp.toPx()
            val fillRatio = (telemetry.levelPercent / 100f).coerceIn(0.05f, 0.98f)
            val waterLevelY = center.y + innerRadius - (2 * innerRadius * fillRatio)

            val wavePath = Path().apply {
                moveTo(center.x - innerRadius, center.y + innerRadius)
                var x = center.x - innerRadius
                while (x <= center.x + innerRadius) {
                    val dx = x - center.x
                    val maxChordY = kotlin.math.sqrt((innerRadius * innerRadius - dx * dx).coerceAtLeast(0f))
                    val waveOffset = sin((x / 35f) + wavePhase) * 6.dp.toPx()
                    val y = (waterLevelY + waveOffset).coerceIn(center.y - maxChordY, center.y + maxChordY)
                    lineTo(x, y)
                    x += 4f
                }
                lineTo(center.x + innerRadius, center.y + innerRadius)
                close()
            }

            drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.45f),
                        accentColor.copy(alpha = 0.12f)
                    )
                )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${telemetry.levelPercent}%",
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = if (telemetry.cycleCount >= 0) {
                    if (ro) "${telemetry.cycleCount} CICLURI" else "${telemetry.cycleCount} CYCLES"
                } else {
                    if (ro) "NIVEL ACTUAL" else "CURRENT LEVEL"
                },
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
                color = accentColor
            )
        }
    }
}

@Composable
fun BentoMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    progress: Float? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF111317))
            .border(1.dp, Color(0xFF1F232B), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 1.2.sp,
                color = Color(0xFF8A94A6)
            )
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50)),
                    color = accentColor,
                    trackColor = Color(0xFF1F232B)
                )
            }
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF9AA4B8)
            )
        }
    }
}

@Composable
fun StatusPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

var sessionPeakWatts = 0f

fun readBatteryTelemetry(context: Context, ro: Boolean): BatteryTelemetry {
    val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val levelPercent = if (level >= 0 && scale > 0) ((level * 100) / scale).coerceIn(0, 100) else 50

    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

    val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
    val voltageVolts = voltageMv / 1000f
    val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
    val tempCelsius = tempTenths / 10f

    val currentUa = try { bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) } catch (_: Exception) { 0 }
    val rawMa = currentUa / 1000
    val normalizedMa = if (isCharging) abs(rawMa) else -abs(rawMa)
    val powerWatts = (normalizedMa / 1000f) * voltageVolts

    var shizukuCycles = -1
    var shizukuFullMah = 0
    var shizukuDesignMah = 4400
    var chargerVolt = 0f
    var statusText = if (ro) "Atinge pentru conectare Shizuku" else "Tap to connect Shizuku"
    var isExact = false

    try {
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                val method = Shizuku::class.java.getDeclaredMethod(
                    "newProcess",
                    Array<String>::class.java,
                    Array<String>::class.java,
                    String::class.java
                )
                method.isAccessible = true
                val process = method.invoke(null, arrayOf("dumpsys", "battery"), null, null) as Process
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val trimmed = line?.trim() ?: continue
                    when {
                        trimmed.startsWith("cycle count:") -> {
                            shizukuCycles = trimmed.substringAfter(":").trim().toIntOrNull() ?: -1
                        }
                        trimmed.startsWith("Full capacity:") -> {
                            val uah = trimmed.substringAfter(":").trim().toIntOrNull() ?: 0
                            if (uah > 100000) shizukuFullMah = uah / 1000
                        }
                        trimmed.startsWith("Full design capacity:") -> {
                            val uah = trimmed.substringAfter(":").trim().toIntOrNull() ?: 0
                            if (uah > 100000) shizukuDesignMah = uah / 1000
                        }
                        trimmed.startsWith("Max charging voltage:") -> {
                            val uv = trimmed.substringAfter(":").trim().toLongOrNull() ?: 0L
                            if (uv > 1000000L) chargerVolt = uv / 1_000_000f
                        }
                    }
                }
                process.waitFor()
                if (shizukuCycles >= 0 || shizukuFullMah > 0) {
                    isExact = true
                    statusText = if (ro) "Activ • Date citite din Motorola HAL" else "Active • Reading Motorola HAL"
                } else {
                    statusText = if (ro) "Shizuku activ • Se citesc datele..." else "Shizuku active • Reading data..."
                }
            } else {
                statusText = if (ro) "Atinge aici pentru a permite Shizuku" else "Tap here to grant Shizuku permission"
            }
        } else {
            statusText = if (ro) "Serviciul Shizuku nu rulează pe telefon" else "Shizuku service is not running"
        }
    } catch (e: Exception) {
        statusText = if (ro) "Shizuku indisponibil (${e.javaClass.simpleName})" else "Shizuku unavailable (${e.javaClass.simpleName})"
    }

    val chargeCounterUah = try { bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) } catch (_: Exception) { 0 }
    val estimatedFullMah = if (chargeCounterUah > 0 && levelPercent > 5) {
        ((chargeCounterUah / 1000f) / (levelPercent / 100f)).toInt()
    } else 0
    val finalFullMah = if (shizukuFullMah > 0) shizukuFullMah else estimatedFullMah
    val finalHealthPercent = if (finalFullMah > 0 && shizukuDesignMah > 0) {
        ((finalFullMah.toFloat() / shizukuDesignMah) * 100f).coerceIn(1f, 100f)
    } else -1f
    if (isCharging && powerWatts > sessionPeakWatts) {
        sessionPeakWatts = powerWatts
    } else if (!isCharging) {
        sessionPeakWatts = 0f
    }
    val speedLabel = when {
        powerWatts > 35f -> "TURBOPOWER 68W"
        powerWatts > 15f -> "FAST CHARGE"
        isCharging -> "CHARGING"
        else -> "DISCHARGING"
    }
    return BatteryTelemetry(
        levelPercent = levelPercent,
        cycleCount = shizukuCycles,
        healthPercent = finalHealthPercent,
        fullCapacityMah = finalFullMah,
        designCapacityMah = shizukuDesignMah,
        powerWatts = powerWatts,
        voltageVolts = voltageVolts,
        currentMa = normalizedMa,
        temperatureCelsius = tempCelsius,
        isCharging = isCharging,
        chargingSpeedLabel = speedLabel,
        shizukuStatus = statusText,
        isExactHardwareData = isExact,
        chargerVoltageVolts = chargerVolt,
        peakChargeWatts = sessionPeakWatts
    )
}