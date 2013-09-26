package org.neo4j.bench.shortestpath;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.HdrHistogram.Histogram;
import org.neo4j.graphalgo.CommonEvaluators;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Expander;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.Traversal;
import org.neo4j.tooling.GlobalGraphOperations;

public class ShortestPathRunner
{
    public static void main( String[] args ) throws IOException
    {
        GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder( Config.DB_DIR ).setConfig(
                Config.NEO4J_CONFIG ).newGraphDatabase();

        System.out.println( nodeCount( db, 1000 ) );
        System.out.println( relationshipCount( db, 1000 ) );

        Histogram histogram = new Histogram( TimeUnit.MILLISECONDS.convert( 30, TimeUnit.SECONDS ), 5 );
        histogram.recordValue( 100 );
        histogram.recordValue( 1000 );
        System.out.println( histogramStatsString( histogram ) );

        Expander expander = Traversal.expanderForAllTypes();
        CostEvaluator<Double> evaluator = CommonEvaluators.doubleCostEvaluator( "weight" );
        PathFinder<WeightedPath> dijkstra = GraphAlgoFactory.dijkstra( expander, evaluator );
        WeightedPath path = dijkstra.findSinglePath( db.getNodeById( 1 ), db.getNodeById( 101 ) );
        System.out.println( path.length() );

        db.shutdown();

        // g = new Neo4jGraph( DATA_DIR );
        // dijkstra = GraphAlgoFactory.dijkstra(
        // Traversal.expanderForAllTypes(),
        // CommonEvaluators.doubleCostEvaluator( "weight" ) );
        // path = dijkstra.findSinglePath( g.v( START ).getRawVertex(), g.v( END
        // ).getRawVertex() );
    }

    public static String histogramStatsString( Histogram histogram )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "MIN\t\t" ).append( histogram.getHistogramData().getMinValue() ).append( "\n" );
        sb.append( "MAX\t\t" ).append( histogram.getHistogramData().getMaxValue() ).append( "\n" );
        sb.append( "50th percentile\t" ).append( histogram.getHistogramData().getValueAtPercentile( 50 ) ).append( "\n" );
        sb.append( "90th percentile\t" ).append( histogram.getHistogramData().getValueAtPercentile( 90 ) ).append( "\n" );
        sb.append( "95th percentile\t" ).append( histogram.getHistogramData().getValueAtPercentile( 95 ) ).append( "\n" );
        sb.append( "99th percentile\t" ).append( histogram.getHistogramData().getValueAtPercentile( 99 ) ).append( "\n" );
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
}
