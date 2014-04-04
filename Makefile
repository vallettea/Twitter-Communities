HANDLE=randhindi
GRAPH_FILE="toto"


all: start
.PHONY: start

start:
	sbt "project graph" "run-main net.babel.graph.SampleClient $(HANDLE)"

graph:
	sbt "project graph" "run-main net.babel.graph.GraphApp $(GRAPH_FILE)"

clean-graph:
	rm -fr /tmp/twitter_graph/

api:
	sbt "project graph" "run-main net.babel.api.Api"
