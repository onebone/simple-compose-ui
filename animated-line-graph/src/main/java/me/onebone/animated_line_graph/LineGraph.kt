package me.onebone.animated_line_graph

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

val GraphVerticalPadding = 32.dp

@OptIn(ExperimentalTextApi::class)
@Composable
fun AnimatedLineGraph(
	modifier: Modifier = Modifier,
	negate: Boolean = false,
    data: LineGraphData
) {
	if (data.list.isEmpty()) return

	var alpha by remember { mutableStateOf(0f) }
	var factor by remember { mutableStateOf(0f) }
	LaunchedEffect(Unit) {
		animate(0f, 1f, animationSpec = tween(800)) { value, _ ->
			alpha = value
		}

		delay(200)

		animate(0f, 1f, animationSpec = tween(800)) { value, _ ->
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

	val textMeasurer = rememberTextMeasurer()
	val fontSize = with(LocalDensity.current) { (GraphVerticalPadding - 12.dp).toSp() }

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
			val delta =
				if (negate) value - midValue
				else midValue - value

			return size.height / 2f + delta * factor * scale
		}

		data.list.forEachIndexed { index, entry ->
			if (index > 0) {
				drawLine(
					color = Color.LightGray,
					start = Offset(getXByIndex(index - 1), getYByValue(data.list[index - 1].value)),
					end = Offset(getXByIndex(index), getYByValue(entry.value)),
					strokeWidth = 10f,
					alpha = alpha
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

			val result = textMeasurer.measure(
				buildAnnotatedString { append("${entry.value}") },
				style = TextStyle(fontSize = fontSize)
			)

			val anchor = getAnchorByEntry(data.list.getOrNull(index - 1), entry, data.list.getOrNull(index + 1))
			val adjustedAnchor = if (negate) anchor.negate() else anchor

			val topLeft = Offset(
				x = getXByIndex(index) + when (adjustedAnchor) {
					ValueTextAnchor.LeftBottom -> -result.size.width
					ValueTextAnchor.Top,
					ValueTextAnchor.Bottom -> -result.size.width / 2
					ValueTextAnchor.RightBottom -> 0
				},
				y = getYByValue(entry.value) + when (adjustedAnchor) {
					ValueTextAnchor.LeftBottom,
					ValueTextAnchor.RightBottom,
					ValueTextAnchor.Bottom -> 0f

					ValueTextAnchor.Top -> -result.size.height * 1.2f
				}
			)

			drawText(
				textLayoutResult = result,
				topLeft = topLeft,
				color = Color.Black,
				alpha = factor
			)
		}
	}
}

private enum class ValueTextAnchor {
	LeftBottom, Top, RightBottom, Bottom;

	fun negate(): ValueTextAnchor = when (this) {
		Top -> Bottom
		Bottom -> Top
        LeftBottom -> RightBottom
		RightBottom -> LeftBottom
	}
}

private fun getAnchorByEntry(prev: GraphEntry?, current: GraphEntry, next: GraphEntry?): ValueTextAnchor {
	val wasIncreasing = if (prev != null) prev.value < current.value else false
	val wasDecreasing = if (prev != null) prev.value > current.value else false
	val isIncreasing = if (next != null) current.value < next.value else false
	val isDecreasing = if (next != null) current.value > next.value else false

	return when {
		wasIncreasing && isIncreasing -> ValueTextAnchor.RightBottom
		wasIncreasing && isDecreasing -> ValueTextAnchor.Top
		wasDecreasing && isIncreasing -> ValueTextAnchor.Bottom
		wasDecreasing && isDecreasing -> ValueTextAnchor.LeftBottom
		prev == null && isIncreasing -> ValueTextAnchor.Bottom
		prev == null && isDecreasing -> ValueTextAnchor.Top
		wasIncreasing && next == null -> ValueTextAnchor.Top
		wasDecreasing && next == null -> ValueTextAnchor.Bottom
		else -> ValueTextAnchor.Bottom
	}
}
