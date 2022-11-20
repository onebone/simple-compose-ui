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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.onebone.animated_line_graph.AnimatedLineGraph
import me.onebone.animated_line_graph.GraphEntry
import me.onebone.animated_line_graph.LineGraphData
import me.onebone.simpleui.ui.theme.SimpleuiTheme

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
	val data = remember {
		LineGraphData(listOf(
			GraphEntry(4.3f),
			GraphEntry(2.5f),
			GraphEntry(4.6f),
			GraphEntry(1.2f),
			GraphEntry(5.4f),
			GraphEntry(1.2f),
			GraphEntry(1.6f),
		))
	}

	AnimatedLineGraph(
		modifier = Modifier.fillMaxWidth().aspectRatio(2f).background(Color.Green),
		data = data
	)
}
