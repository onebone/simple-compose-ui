package me.onebone.simpleui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import me.onebone.animated_line_graph.AnimatedLineGraph
import me.onebone.animated_line_graph.GraphEntry
import me.onebone.animated_line_graph.LineGraphData
import me.onebone.simpleui.ui.theme.SimpleuiTheme
import kotlin.math.floor
import kotlin.random.Random

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			SimpleuiTheme {
				// A surface container using the 'background' color from the theme
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colors.background
				) {
					SampleGraph()
				}
			}
		}
	}
}

@Composable
fun SampleGraph() {
	var data by remember {
		mutableStateOf(
			LineGraphData(listOf(
				GraphEntry(0, 4.3f),
				GraphEntry(1, 2.5f),
				GraphEntry(2, 4.6f),
				GraphEntry(3, 1.2f),
				GraphEntry(4, 5.4f),
				GraphEntry(5, 1.2f),
				GraphEntry(6, 1.7f)
			))
		)
	}

	LaunchedEffect(Unit) {
		while (true) {
			delay(3000)

			data = LineGraphData(List(Random.nextInt(5, 10)) {
				GraphEntry(it, floor((Random.nextFloat() * 8 + 1) * 10) / 10)
			})
		}
	}

	AnimatedLineGraph(
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(2f)
			.background(Color.Green),
		negate = true,
		data = data
	)
}
