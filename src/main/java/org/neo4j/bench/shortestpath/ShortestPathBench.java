package org.neo4j.bench.shortestpath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.HdrHistogram.Histogram;
import org.neo4j.graphalgo.CommonEvaluators;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Expander;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.Traversal;

// mvn exec:java -Dexec.mainClass=org.neo4j.bench.shortestpath.ShortestPathBench
public class ShortestPathBench
{
    public static void main( String[] args ) throws IOException
    {
        if ( args.length == 0 || ( args[0].equals( "all" ) == false && args[0].equals( "single" ) == false ) )
        {
            System.out.println( "\nERROR\nExpected parameter: 'all' OR 'single'\n" );
            return;
        }

        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder( Config.DB_DIR ).setConfig(
                Config.NEO4J_CONFIG ).newGraphDatabase();

        System.out.println( "Node Count = " + GraphUtils.nodeCount( db, 1000 ) );
        System.out.println( "Node Property Count = " + GraphUtils.nodePropertyCount( db, 1000 ) );
        System.out.println( "Relationship Count = " + GraphUtils.relationshipCount( db, 1000 ) );
        System.out.println( "Relationship Property Count = " + GraphUtils.relationshipPropertyCount( db, 1000 ) );

        int runCount = 1000;
        List<Pair<Node>> startAndEndNodes = loadStartAndEndNodes( db, runCount );

        Expander expander = Traversal.expanderForAllTypes();

        CostEvaluator<Double> evaluator = CommonEvaluators.doubleCostEvaluator( "weight" );
        int maxDepth = Integer.MAX_VALUE;
        PathFinder<?> shortestPath = GraphAlgoFactory.shortestPath( expander, maxDepth );

        CostEvaluator<Double> constantEvaluator = new CostEvaluator<Double>()
        {
            @Override
            public Double getCost( Relationship relationship, Direction direction )
            {
                return 1D;
            }
        };
        PathFinder<? extends Path> unweightedDijkstra = GraphAlgoFactory.dijkstra( expander, constantEvaluator );

        PathFinder<? extends Path> weightedDijkstra = GraphAlgoFactory.dijkstra( expander, evaluator );

        if ( args[0].equals( "single" ) )
        {
            System.out.println( "- Shortest Path -\n" + runFindSinglePath( shortestPath, startAndEndNodes ) );
            System.out.println( "- Unweighted Dijkstra -\n" + runFindSinglePath( unweightedDijkstra, startAndEndNodes ) );
            System.out.println( "- Weighted Dijkstra -\n" + runFindSinglePath( weightedDijkstra, startAndEndNodes ) );
        }
        else
        {
            System.out.println( "- Shortest Path -\n" + runFindAllPaths( shortestPath, startAndEndNodes ) );
            System.out.println( "- Unweighted Dijkstra -\n" + runFindAllPaths( unweightedDijkstra, startAndEndNodes ) );
            System.out.println( "- Weighted Dijkstra -\n" + runFindAllPaths( weightedDijkstra, startAndEndNodes ) );
        }

        db.shutdown();
    }

    public static String runFindSinglePath( PathFinder<? extends Path> pathFinder, List<Pair<Node>> startAndEndNodes )
    {
        Histogram timeHistogram = new Histogram( TimeUnit.MILLISECONDS.convert( 30, TimeUnit.SECONDS ), 5 );
        Histogram pathLengthHistogram = new Histogram( 10000, 5 );
        Path path = null;

        for ( Pair<Node> startAndEndNode : startAndEndNodes )
        {
            long startTime = System.currentTimeMillis();
            path = pathFinder.findSinglePath( startAndEndNode.getFirst(), startAndEndNode.getSecond() );
            long runTime = System.currentTimeMillis() - startTime;
            timeHistogram.recordValue( runTime );
            pathLengthHistogram.recordValue( path.length() );
        }
        return histogramString( timeHistogram, "Run Time (ms)" ) + histogramString( pathLengthHistogram, "Path Length" );
    }

    public static String runFindAllPaths( PathFinder<? extends Path> pathFinder, List<Pair<Node>> startAndEndNodes )
    {
        Histogram timeHistogram = new Histogram( TimeUnit.MILLISECONDS.convert( 30, TimeUnit.SECONDS ), 5 );
        Histogram pathLengthHistogram = new Histogram( 10000, 5 );
        Histogram pathCountHistogram = new Histogram( 10000, 5 );

        for ( Pair<Node> startAndEndNode : startAndEndNodes )
        {
            long startTime = System.currentTimeMillis();
            Iterable<? extends Path> paths = pathFinder.findAllPaths( startAndEndNode.getFirst(),
                    startAndEndNode.getSecond() );
            pathLengthHistogram.recordValue( paths.iterator().next().length() );
            long runTime = System.currentTimeMillis() - startTime;
            pathCountHistogram.recordValue( IteratorUtil.count( paths ) );
            timeHistogram.recordValue( runTime );
        }
        return histogramString( timeHistogram, "Run Time (ms)" ) + histogramString( pathLengthHistogram, "Path Length" )
               + histogramString( pathCountHistogram, "Discovered Path Count" );
    }

    public static String histogramString( Histogram histogram, String name )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "\t" ).append( name ).append( "\n" );
        sb.append( "\t\tCOUNT\t\t : " ).append( histogram.getHistogramData().getTotalCount() ).append( "\n" );
        sb.append( "\t\tMIN\t\t : " ).append( histogram.getHistogramData().getMinValue() ).append( "\n" );
        sb.append( "\t\tMAX\t\t : " ).append( histogram.getHistogramData().getMaxValue() ).append( "\n" );
        sb.append( "\t\t50th PERCENTILE\t : " ).append( histogram.getHistogramData().getValueAtPercentile( 50 ) ).append(
                "\n" );
        sb.append( "\t\t90th PERCENTILE\t : " ).append( histogram.getHistogramData().getValueAtPercentile( 90 ) ).append(
                "\n" );
        sb.append( "\t\t95th PERCENTILE\t : " ).append( histogram.getHistogramData().getValueAtPercentile( 95 ) ).append(
                "\n" );
        sb.append( "\t\t99th PERCENTILE\t : " ).append( histogram.getHistogramData().getValueAtPercentile( 99 ) ).append(
                "\n" );
        sb.append( "\t\tMEAN\t\t : " ).append( histogram.getHistogramData().getMean() ).append( "\n" );
        return sb.toString();
    }

    public static List<Pair<Node>> loadStartAndEndNodes( GraphDatabaseService db, int maxCount )
    {
        int count = 0;
        List<Pair<Node>> startAndEndNodes = new ArrayList<Pair<Node>>();
        Transaction tx = db.beginTx();
        try
        {
            CsvFileReader reader = new CsvFileReader( new File( Config.PATH_START_END_ID_FILE ), "," );
            // Skip Files Headers
            if ( reader.hasNext() )
            {
                reader.next();
            }

            while ( reader.hasNext() )
            {
                String[] startAndEndNode = reader.next();
                long startNodeId = Long.parseLong( startAndEndNode[0] );
                long endNodeId = Long.parseLong( startAndEndNode[1] );
                Node startNode = db.getNodeById( startNodeId );
                Node endNode = db.getNodeById( endNodeId );
                startAndEndNodes.add( new Pair( startNode, endNode ) );
                count++;
                if ( count >= maxCount ) break;
            }
            tx.success();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new RuntimeException( e.getCause() );
        }
        finally
        {
            tx.finish();
        }

        return startAndEndNodes;
    }

    static class Pair<T>
    {
        private final T first;
        private final T second;

        private Pair( T first, T second )
        {
            this.first = first;
            this.second = second;
        }

        public T getFirst()
        {
            return first;
        }

        public T getSecond()
        {
            return second;
        }

        @Override
        public String toString()
        {
            return "Pair[" + first + "," + second + "]";
        }
    }
}
