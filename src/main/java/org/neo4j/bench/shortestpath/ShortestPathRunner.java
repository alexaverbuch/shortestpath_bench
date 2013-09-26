package org.neo4j.bench.shortestpath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.HdrHistogram.Histogram;
import org.neo4j.graphalgo.CommonEvaluators;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Expander;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.kernel.Traversal;
import org.neo4j.tooling.GlobalGraphOperations;

public class ShortestPathRunner
{
    public static void main( String[] args ) throws IOException
    {
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder( Config.DB_DIR ).setConfig(
                Config.NEO4J_CONFIG ).newGraphDatabase();

        System.out.println( "Node Count = " + nodeCount( db, 1000 ) );
        System.out.println( "Relationship Count = " + relationshipCount( db, 1000 ) );

        Random random = new Random( 42 );
        int runCount = 1000;
        long minNodeId = 1;
        long maxNodeId = 3900;
        List<Pair<Node>> startAndEndNodes = generateStartAndEndNodes( random, db, runCount, minNodeId, maxNodeId );

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
        PathFinder<?> unweightedDijkstra = GraphAlgoFactory.dijkstra( expander, constantEvaluator );

        PathFinder<?> weightedDijkstra = GraphAlgoFactory.dijkstra( expander, evaluator );

        runPathFinder( shortestPath, startAndEndNodes );
        runPathFinder( unweightedDijkstra, startAndEndNodes );
        runPathFinder( weightedDijkstra, startAndEndNodes );

        System.out.println( "- Shortest Path -\n" + runPathFinder( shortestPath, startAndEndNodes ) );
        System.out.println( "- Unweighted Dijkstra -\n" + runPathFinder( unweightedDijkstra, startAndEndNodes ) );
        System.out.println( "- Weighted Dijkstra -\n" + runPathFinder( weightedDijkstra, startAndEndNodes ) );

        db.shutdown();
    }

    public static String runPathFinder( PathFinder<?> pathFinder, List<Pair<Node>> startAndEndNodes )
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
        return timeHistogramString( timeHistogram ) + pathLengthHistogramString( pathLengthHistogram );
    }

    public static String timeHistogramString( Histogram histogram )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "\tRun Time\n" );
        sb.append( "\t\tCOUNT\t\t" ).append( histogram.getHistogramData().getTotalCount() ).append( "\n" );
        sb.append( "\t\tMIN\t\t" ).append( histogram.getHistogramData().getMinValue() ).append( "\n" );
        sb.append( "\t\tMAX\t\t" ).append( histogram.getHistogramData().getMaxValue() ).append( "\n" );
        sb.append( "\t\t50th percentile\t" ).append( histogram.getHistogramData().getValueAtPercentile( 50 ) ).append(
                "\n" );
        sb.append( "\t\t90th percentile\t" ).append( histogram.getHistogramData().getValueAtPercentile( 90 ) ).append(
                "\n" );
        sb.append( "\t\t95th percentile\t" ).append( histogram.getHistogramData().getValueAtPercentile( 95 ) ).append(
                "\n" );
        sb.append( "\t\t99th percentile\t" ).append( histogram.getHistogramData().getValueAtPercentile( 99 ) ).append(
                "\n" );
        return sb.toString();
    }

    public static String pathLengthHistogramString( Histogram histogram )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "\tPath Length\n" );
        sb.append( "\t\tMIN\t\t" ).append( histogram.getHistogramData().getMinValue() ).append( "\n" );
        sb.append( "\t\tMAX\t\t" ).append( histogram.getHistogramData().getMaxValue() ).append( "\n" );
        sb.append( "\t\t50th percentile\t" ).append( histogram.getHistogramData().getValueAtPercentile( 50 ) ).append(
                "\n" );
        sb.append( "\t\t90th percentile\t" ).append( histogram.getHistogramData().getValueAtPercentile( 90 ) ).append(
                "\n" );
        sb.append( "\t\t95th percentile\t" ).append( histogram.getHistogramData().getValueAtPercentile( 95 ) ).append(
                "\n" );
        sb.append( "\t\t99th percentile\t" ).append( histogram.getHistogramData().getValueAtPercentile( 99 ) ).append(
                "\n" );
        return sb.toString();
    }

    public static long nodeCount( GraphDatabaseService db, long transactionSize )
    {
        GlobalGraphOperations globalOperations = GlobalGraphOperations.at( db );
        long nodeCount = 0;
        Transaction tx = db.beginTx();
        try
        {
            for ( Node node : globalOperations.getAllNodes() )
            {
                nodeCount++;
                if ( nodeCount % transactionSize == 0 )
                {
                    tx.success();
                    tx = db.beginTx();
                }
            }
            tx.success();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getCause() );
        }
        return nodeCount;
    }

    public static long relationshipCount( GraphDatabaseService db, long transactionSize )
    {
        GlobalGraphOperations globalOperations = GlobalGraphOperations.at( db );
        long relationshipCount = 0;
        Transaction tx = db.beginTx();
        try
        {
            for ( Relationship relationship : globalOperations.getAllRelationships() )
            {
                relationshipCount++;
                if ( relationshipCount % transactionSize == 0 )
                {
                    tx.success();
                    tx = db.beginTx();
                }
            }
            tx.success();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getCause() );
        }
        return relationshipCount;
    }

    public static List<Pair<Node>> generateStartAndEndNodes( Random random, GraphDatabaseService db, int count,
            long minId, long maxId )
    {
        List<Pair<Node>> startAndEndNodes = new ArrayList<Pair<Node>>();
        long idRange = maxId - minId;
        Transaction tx = db.beginTx();
        try
        {
            for ( int i = 0; i < count; i++ )
            {
                long startNode = (long) ( ( random.nextDouble() * idRange ) + minId );
                long endNode = -1;
                while ( endNode == -1 )
                {
                    long val = (long) ( ( random.nextDouble() * idRange ) + minId );
                    endNode = ( val == startNode ) ? -1 : val;
                }
                startAndEndNodes.add( new Pair( db.getNodeById( startNode ), db.getNodeById( endNode ) ) );
            }
            tx.success();
        }
        catch ( Exception e )
        {
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
