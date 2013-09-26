Shortest Path in Neo4j
---------------------

### Environment

	Processor			: 4x Intel i3-2330M CPU @ 2.20GHz (2 cores, with HyperThreading)
	Memory				: 6GB
	Storage				: INTEL SSDSA2BW16 160GB SSD drive
	Operating System	: Ubuntu 12.04.3 LTS (Linux 3.2.0-52-generic (x86_64))
	Java				: Java HotSpot 64-Bit VM
	Neo4j				: Neo4j Enterprise 1.9.4

### Graph
	Source			: http://snap.stanford.edu/data/egonets-Facebook.html
	Nodes			: 4,040
	Relationships	: 88,234

### Experiment
Compare performance of different path finding algorithms.
Start and end nodes are selected uniformly at random from across the entire node space (~4000 nodes).
Each algorithm is run 1000 times, using different (well, randomly selected) start and end nodes each time.

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

 1. **Compile:** `mvn clean compile -Dmaven.compiler.source=1.6 -Dmaven.compiler.target=1.6`
 2. **Generate Graph .csv Files:** `mvn exec:java -Dexec.mainClass=org.neo4j.bench.shortestpath.InputFilesCreator`
 3. **Build neo4j-importer (see README):** `https://github.com/dmontag/neo4j-import`
 4. **Load Generated .csv Files into Neo4j:** `./run.sh /path/to/shortestpath_bench/data/generated/nodes.csv ../shortestpath_bench/data/generated/relationships.csv`
 5. **Run Benchmark:** `mvn exec:java -Dexec.mainClass=org.neo4j.bench.shortestpath.ShortestPathBench`
