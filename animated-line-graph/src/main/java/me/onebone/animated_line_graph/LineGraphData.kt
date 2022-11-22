package me.onebone.animated_line_graph

data class LineGraphData(
	val list: List<GraphEntry>
)

data class GraphEntry(
	val key: Any,
	val value: Float
)
