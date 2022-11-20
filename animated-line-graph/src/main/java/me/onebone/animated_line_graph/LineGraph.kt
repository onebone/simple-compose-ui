package me.onebone.animated_line_graph

import androidx.compose.animation.core.animate
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

val GraphVerticalPadding = 24.dp

@Composable
fun AnimatedLineGraph(
	modifier: Modifier = Modifier,
    data: LineGraphData
) {
	if (data.list.isEmpty()) return

	var alpha by remember { mutableStateOf(0f) }
	var factor by remember { mutableStateOf(0f) }
	LaunchedEffect(Unit) {
		animate(0f, 1f) { value, _ ->
			alpha = value
		}

		delay(200)

		animate(0f, 1f) { value, _ ->
			factor = value
		}
	}

	val maxValue = remember(data.list) {
		data.list.maxOf { it.value }
	}
	val minValue = remember(data.list) {
		data.list.minOf { it.value }
	}
	val midValue = (maxValue + minValue) / 2

	val graphVerticalPadding = with(LocalDensity.current) { GraphVerticalPadding.toPx() * 2 }

	Canvas(modifier = modifier) {
		val scale = if (maxValue == minValue)
			1f
		else
			size.height / (maxValue - minValue) * (size.height - graphVerticalPadding) / size.height

		fun getXByIndex(index: Int): Float {
			val widthPerIndex = size.width / data.list.size

			return widthPerIndex * index + widthPerIndex / 2
		}

		fun getYByValue(value: Float): Float {
			return size.height / 2f + (value - midValue) * factor * scale
		}

		data.list.forEachIndexed { index, entry ->
			if (index > 0) {
				drawLine(
					color = Color.LightGray,
					start = Offset(getXByIndex(index - 1), getYByValue(data.list[index - 1].value)),
					end = Offset(getXByIndex(index), getYByValue(entry.value)),
					strokeWidth = 10f
				)
			}
		}

		data.list.forEachIndexed { index, entry ->
			drawCircle(
				color = Color.Blue,
				radius = 20f,
				center = Offset(getXByIndex(index), getYByValue(entry.value)),
				alpha = alpha
			)
		}
	}
}
