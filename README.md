## Twitter-communities

First step is to provide your credentials in a file `access.txt` (4 lines) at the root of the project.

Then you can run the main part of the code using

```
make
```

 It will fetche all the users followed by the root users defined in the main. It goes on and on so stop when you desire.

You can play with the graph with gremlin:

```
conf = new BaseConfiguration()
conf.setProperty('storage.backend', 'persistit')
conf.setProperty('storage.directory', '/tmp/twitter_graph')
g=TitanFactory.open(conf)
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

