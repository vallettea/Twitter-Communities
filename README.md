## Twitter-communities

First step is to provide your credentials in a file `access.txt` at the root of the project. The format of the file is:

```
id1:key1
id2:key2
...
```

yes, you can provide many handles to speed up the retrieval (yes you can create many applications on your tweeter account).

Then you can run the main part of the code using

```
make
```

 It will fetche all the users followed by the root users defined in the main. It goes on and on so stop when you desire.

You can play with the graph with gremlin: (here printing the page rank)

```
conf = new BaseConfiguration()
conf.setProperty('storage.backend', 'persistit')
conf.setProperty('storage.directory', '/tmp/twitter_graph')
g=TitanFactory.open(conf)
c = 0
m = [:]
g.V.outE.inV.uid.groupCount(m).back(2).loop(3){c++ < 1000}.map
```

To empty the db use 

```
make clean-graph
```

To launch the api use 
```
make api
```

it will serve all the graph on (if not too big i guess):

```
http://localhost:5678/v1/allgraph
```

