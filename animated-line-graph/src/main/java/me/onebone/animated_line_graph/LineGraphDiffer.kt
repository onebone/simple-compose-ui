package me.onebone.animated_line_graph

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.launch

internal class LineGraphDiffer {
	private var composition: Composition? = null
	val root = LineGraphNode.Parent()

	@Composable
	fun Render(
		size: Size,
		factor: Float,
		negate: Boolean,
		data: LineGraphData
	) {
		val maxValue = remember(data.list) {
			data.list.maxOf { it.value }
		}
		val minValue = remember(data.list) {
			data.list.minOf { it.value }
		}
		val midValue = (maxValue + minValue) / 2

		val graphVerticalPadding = with(LocalDensity.current) { GraphVerticalPadding.toPx() * 2 }

		val scale = if (maxValue == minValue)
			1f
		else
			(size.height - graphVerticalPadding) / (maxValue - minValue)

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

		val composition = getComposition(rememberCompositionContext())
		composition.setContent {
			data.list.forEachIndexed { index, entry ->
				key(entry.key) {
					LineGraphLeaf(x = getXByIndex(index), y = getYByValue(entry.value))
				}
			}
        }
	}

	private fun getComposition(compositionContext: CompositionContext): Composition {
		val existing = composition
		return if (existing == null || existing.isDisposed) {
			Composition(
				LineGraphApplier(root),
				compositionContext
			).also { composition = it }
        } else {
			existing
		}
	}
}

internal sealed class LineGraphNode {
	class Parent : LineGraphNode() {
		var children = mutableStateListOf<LineGraphNode>()
	}

	class Leaf(x: Float, y: Float) : LineGraphNode() {
		val x = Animatable(x)
		val y = Animatable(y)
	}
}

internal class LineGraphApplier(root: LineGraphNode.Parent) : AbstractApplier<LineGraphNode>(root) {
	private val parent get() = root as LineGraphNode.Parent

	override fun insertBottomUp(index: Int, instance: LineGraphNode) {
		// intentionally empty
	}

	override fun insertTopDown(index: Int, instance: LineGraphNode) {
		parent.children.add(index, instance as LineGraphNode.Leaf)
	}

	override fun move(from: Int, to: Int, count: Int) {
		parent.children.move(from, to, count)
	}

	override fun remove(index: Int, count: Int) {
		parent.children.remove(index, count)
	}

	override fun onClear() {
		parent.children.clear()
	}
}


@Retention(AnnotationRetention.BINARY)
@ComposableTargetMarker(description = "Line Graph")
@Target(
	AnnotationTarget.FILE,
	AnnotationTarget.FUNCTION,
	AnnotationTarget.PROPERTY_GETTER,
	AnnotationTarget.TYPE,
	AnnotationTarget.TYPE_PARAMETER
)
internal annotation class LineGraphComposable

@LineGraphComposable
@Composable
internal fun LineGraphLeaf(
	x: Float,
	y: Float
) {
	val scope = rememberCoroutineScope()

	ComposeNode<LineGraphNode.Leaf, LineGraphApplier>(
		factory = { LineGraphNode.Leaf(x, y) },
        update = {
			set(x) { scope.launch { this@set.x.animateTo(it) } }
	        set(y) { scope.launch { this@set.y.animateTo(it) } }
        }
	)
}
