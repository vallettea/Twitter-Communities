
GRAPH_FILE="toto"


all: start
.PHONY: start

start:
	sbt "project graph" "run-main net.babel.graph.SampleClient"

graph:
	sbt "project graph" "run-main net.babel.graph.GraphApp $(GRAPH_FILE)"