package me.onebone.animated_line_graph

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.delay

val GraphVerticalPadding = 32.dp
val ValueTextMargin = 6.dp

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

	val textMeasurer = rememberTextMeasurer()
	val fontSize = with(LocalDensity.current) { (GraphVerticalPadding - 12.dp).toSp() }

	var size by remember { mutableStateOf<IntSize?>(null) }
	val differ = remember { LineGraphDiffer() }
	if (size != null) {
		differ.Render(size = size!!.toSize(), factor = factor, negate = negate, data = data)
	}

	val valueTextMargin = with(LocalDensity.current) { ValueTextMargin.toPx() }

	Spacer(
		modifier = modifier
			.onSizeChanged { size = it }
			.drawBehind {
				val children = differ.root.children.map { it as LineGraphNode.Leaf }

				children.forEachIndexed { index, entry ->
					if (index > 0) {
						drawLine(
							color = Color.LightGray,
							start = Offset(
								children[index - 1].x.value,
								children[index - 1].y.value
							),
							end = Offset(entry.x.value, entry.y.value),
							strokeWidth = 10f,
							alpha = alpha
						)
					}
				}

				children.forEachIndexed { index, entry ->
					drawCircle(
						color = Color.Blue,
						radius = 20f,
						center = Offset(entry.x.value, entry.y.value),
						alpha = alpha
					)

					val result = textMeasurer.measure(
						buildAnnotatedString { append("${data.list[index].value}") },
						style = TextStyle(fontSize = fontSize)
					)

					val anchor = getAnchorByEntry(
						data.list.getOrNull(index - 1),
						data.list[index],
						data.list.getOrNull(index + 1)
					)
					val adjustedAnchor = if (negate) anchor.negate() else anchor

					val topLeft = Offset(
						x = entry.x.value + when (adjustedAnchor) {
							ValueTextAnchor.LeftBottom -> -result.size.width
							ValueTextAnchor.Top,
							ValueTextAnchor.Bottom -> -result.size.width / 2
							ValueTextAnchor.RightBottom -> 0
						},
						y = entry.y.value + when (adjustedAnchor) {
							ValueTextAnchor.LeftBottom,
							ValueTextAnchor.RightBottom,
							ValueTextAnchor.Bottom -> valueTextMargin

							ValueTextAnchor.Top -> -result.size.height - valueTextMargin
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
	)
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
