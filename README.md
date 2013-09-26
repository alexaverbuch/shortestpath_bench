Shortest Path in Neo4j
---------------------

### Experiment
Originally created as a response to the data presented in [this blog post](http://istc-bigdata.org/index.php/benchmarking-graph-databases/),
this experiment compares the performance of different path finding algorithm implementations in Neo4j.
The start and end nodes are selected uniformly at random from across the entire node space (~4000 nodes).
Each algorithm is run 1000 times, using different (well, randomly selected) start and end nodes each time.

### Dataset
	Source			: http://snap.stanford.edu/data/egonets-Facebook.html
	Nodes			: 4,040
	Relationships	: 88,234

### Environment

	Processor			: 4x Intel i3-2330M CPU @ 2.20GHz (2 cores, with HyperThreading)
	Memory				: 6GB
	Storage				: INTEL SSDSA2BW16 160GB SSD drive
	Operating System	: Ubuntu 12.04.3 LTS (Linux 3.2.0-52-generic (x86_64))
	Java				: Java HotSpot 64-Bit VM
	Neo4j				: Neo4j Enterprise 1.9.4

## Results

### Single Shorest Path

**- Shortest Path -**

	Run Time (ms)
		COUNT			 : 1000
		MIN				 : 0
		MIN				 : 222
		50th PERCENTILE	 : 1
		90th PERCENTILE	 : 6
		95th PERCENTILE	 : 10
		99th PERCENTILE	 : 18
		MEAN			 : 2.769
	Path Length
		COUNT			 : 1000
		MIN				 : 1
		MIN				 : 7
		50th PERCENTILE	 : 4
		90th PERCENTILE	 : 5
		95th PERCENTILE	 : 6
		99th PERCENTILE	 : 7
		MEAN			 : 3.613

**- Unweighted Dijkstra -**

	Run Time (ms)
		COUNT			 : 1000
		MIN				 : 0
		MIN				 : 12365
		50th PERCENTILE	 : 143
		90th PERCENTILE	 : 651
		95th PERCENTILE	 : 1093
		99th PERCENTILE	 : 2150
		MEAN			 : 294.47
	Path Length
		COUNT			 : 1000
		MIN				 : 1
		MIN				 : 7
		50th PERCENTILE	 : 4
		90th PERCENTILE	 : 5
		95th PERCENTILE	 : 6
		99th PERCENTILE	 : 7
		MEAN			 : 3.613

**- Weighted Dijkstra -**

	Run Time (ms)
		COUNT			 : 1000
		MIN				 : 0
		MIN				 : 589
		50th PERCENTILE	 : 134
		90th PERCENTILE	 : 209
		95th PERCENTILE	 : 241
		99th PERCENTILE	 : 363
		MEAN			 : 126.423
	Path Length
		COUNT			 : 1000
		MIN				 : 1
		MIN				 : 32
		50th PERCENTILE	 : 13
		90th PERCENTILE	 : 21
		95th PERCENTILE	 : 23
		99th PERCENTILE	 : 28
		MEAN			 : 13.146

### All Shortest Paths

**- Shortest Path -**

	Run Time (ms)
		COUNT			 : 1000
		MIN				 : 0
		MIN				 : 159
		50th PERCENTILE	 : 2
		90th PERCENTILE	 : 7
		95th PERCENTILE	 : 10
		99th PERCENTILE	 : 17
		MEAN			 : 3.059
	Path Length
		COUNT			 : 1000
		MIN				 : 1
		MIN				 : 7
		50th PERCENTILE	 : 4
		90th PERCENTILE	 : 5
		95th PERCENTILE	 : 6
		99th PERCENTILE	 : 7
		MEAN			 : 3.613
	Discovered Path Count
		COUNT			 : 1000
		MIN				 : 1
		MIN				 : 515
		50th PERCENTILE	 : 2
		90th PERCENTILE	 : 15
		95th PERCENTILE	 : 25
		99th PERCENTILE	 : 68
		MEAN			 : 6.975

**- Unweighted Dijkstra -**

	Run Time (ms)
		COUNT			 : 1000
		MIN				 : 0
		MIN				 : 12580
		50th PERCENTILE	 : 132
		90th PERCENTILE	 : 652
		95th PERCENTILE	 : 1064
		99th PERCENTILE	 : 2203
		MEAN			 : 293.552
	Path Length
		COUNT			 : 1000
		MIN				 : 1
		MIN				 : 7
		50th PERCENTILE	 : 4
		90th PERCENTILE	 : 5
		95th PERCENTILE	 : 6
		99th PERCENTILE	 : 7
		MEAN			 : 3.613
	Discovered Path Count
		COUNT			 : 1000
		MIN				 : 1
		MIN				 : 515
		50th PERCENTILE	 : 2
		90th PERCENTILE	 : 15
		95th PERCENTILE	 : 25
		99th PERCENTILE	 : 68
		MEAN			 : 6.975

**- Weighted Dijkstra -**

	Run Time (ms)
		COUNT			 : 1000
		MIN				 : 0
		MIN				 : 609
		50th PERCENTILE	 : 129
		90th PERCENTILE	 : 209
		95th PERCENTILE	 : 241
		99th PERCENTILE	 : 362
		MEAN			 : 124.228
	Path Length
		COUNT			 : 1000
		MIN				 : 1
		MIN				 : 32
		50th PERCENTILE	 : 13
		90th PERCENTILE	 : 21
		95th PERCENTILE	 : 23
		99th PERCENTILE	 : 28
		MEAN			 : 13.146
	Discovered Path Count
		COUNT			 : 1000
		MIN				 : 1
		MIN				 : 12
		50th PERCENTILE	 : 1
		90th PERCENTILE	 : 3
		95th PERCENTILE	 : 4
		99th PERCENTILE	 : 8
		MEAN			 : 1.665


### Recreate at home

Follow these instructions if you wish to recreate the experiment at home.

After step 4. below, the project directory should look as follows:

	shortestpath_bench/
		data/
			raw/
			generated/
				nodes.csv
				relationships.csv
				path-start-and-end-nodes.csv
		db/
			...
		lib/
			...
		pom.xml  
		README.md  
		src/
			...
		target/
			...

 **(1) Compile:** 
 
	mvn clean compile -Dmaven.compiler.source=1.6 -Dmaven.compiler.target=1.6

**(2) Generate Graph .csv Files:** 

	mvn exec:java -Dexec.mainClass=org.neo4j.bench.shortestpath.InputFilesCreator

 **(3) Download and build neo4j-import (see [README](https://github.com/dmontag/neo4j-import/blob/master/README.textile)):** 

	https://github.com/dmontag/neo4j-import

 **(4) Load Generated .csv Files into Neo4j using neo4j-import:** 

	./run.sh /path/to/shortestpath_bench/db /path/to/shortestpath_bench/data/generated/nodes.csv ../shortestpath_bench/data/generated/relationships.csv

 **(5) Run Benchmark (select 'single' to find one shortest path OR 'all' to find all shortest paths):**

	MAVEN_OPTS="-server -XX:+UseConcMarkSweepGC -Xmx512m" mvn exec:java -Dexec.mainClass=org.neo4j.bench.shortestpath.ShortestPathBench -Dexec.arguments="single"
