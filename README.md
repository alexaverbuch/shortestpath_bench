Shortest Path in Neo4j
---------------------

### Experiment
Originally created as a response to the incorrect data presented in [this blog post](http://istc-bigdata.org/index.php/benchmarking-graph-databases/),
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

### Results
**- Shortest Path -**

		Run Time (ms)
			COUNT			 : 1000
			MIN				 : 0
			MAX				 : 25
			50th percentile	 : 1
			90th percentile	 : 5
			95th percentile	 : 9
			99th percentile	 : 12
		Path Length
			MIN			 	 : 1
			MAX				 : 7
			50th percentile	 : 4
			90th percentile	 : 5
			95th percentile	 : 6
			99th percentile	 : 7

**- Unweighted Dijkstra -**

		Run Time (ms)
			COUNT			 : 1000
			MIN				 : 0
			MAX				 : 11495
			50th percentile	 : 130
			90th percentile	 : 630
			95th percentile	 : 989
			99th percentile	 : 2380
		Path Length
			MIN				 : 1
			MAX				 : 7
			50th percentile	 : 4
			90th percentile	 : 5
			95th percentile	 : 6
			99th percentile	 : 7

**- Weighted Dijkstra -**

		Run Time (ms)
			COUNT			 : 1000
			MIN				 : 0
			MAX				 : 565
			50th percentile	 : 118
			90th percentile	 : 195
			95th percentile	 : 214
			99th percentile	 : 332
		Path Length
			MIN				 : 1
			MAX				 : 36
			50th percentile	 : 13
			90th percentile	 : 20
			95th percentile	 : 22
			99th percentile	 : 28


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

 1. **Compile:** 
 
`mvn clean compile -Dmaven.compiler.source=1.6 -Dmaven.compiler.target=1.6`

 2. **Generate Graph .csv Files:** 

`mvn exec:java -Dexec.mainClass=org.neo4j.bench.shortestpath.InputFilesCreator`

 3. **Build neo4j-importer (see README):** 

`https://github.com/dmontag/neo4j-import`

 4. **Load Generated .csv Files into Neo4j:** 

`./run.sh /path/to/shortestpath_bench/db /path/to/shortestpath_bench/data/generated/nodes.csv ../shortestpath_bench/data/generated/relationships.csv`

 5. **Run Benchmark (select 'single' to find one shortest path OR 'all' to find all shortest paths):**

`MAVEN_OPTS="-server -XX:+UseConcMarkSweepGC -Xmx512m" mvn exec:java -Dexec.mainClass=org.neo4j.bench.shortestpath.ShortestPathBench -Dexec.arguments="single"`
