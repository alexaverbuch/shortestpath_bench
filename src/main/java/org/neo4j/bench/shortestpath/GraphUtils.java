package org.neo4j.bench.shortestpath;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

public class GraphUtils
{
    public static long nodeCount( GraphDatabaseService db, long transactionSize )
    {
        GlobalGraphOperations globalOperations = GlobalGraphOperations.at( db );
        long nodeCount = 0;
        Transaction tx = db.beginTx();
        try
        {
            for ( Node node : globalOperations.getAllNodes() )
            {
                if ( node.getId() == 0 ) continue;
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

    public static long nodePropertyCount( GraphDatabaseService db, long transactionSize )
    {
        GlobalGraphOperations globalOperations = GlobalGraphOperations.at( db );
        long nodePropertyCount = 0;
        Transaction tx = db.beginTx();
        try
        {
            for ( Node node : globalOperations.getAllNodes() )
            {
                for ( String key : node.getPropertyKeys() )
                {
                    node.getProperty( key );
                    nodePropertyCount++;
                    if ( nodePropertyCount % transactionSize == 0 )
                    {
                        tx.success();
                        tx = db.beginTx();
                    }
                }
            }
            tx.success();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getCause() );
        }
        return nodePropertyCount;
    }

    public static long relationshipPropertyCount( GraphDatabaseService db, long transactionSize )
    {
        GlobalGraphOperations globalOperations = GlobalGraphOperations.at( db );
        long relationshipPropertyCount = 0;
        Transaction tx = db.beginTx();
        try
        {
            for ( Relationship relationship : globalOperations.getAllRelationships() )
            {
                for ( String key : relationship.getPropertyKeys() )
                {
                    relationship.getProperty( key );
                    relationshipPropertyCount++;
                    if ( relationshipPropertyCount % transactionSize == 0 )
                    {
                        tx.success();
                        tx = db.beginTx();
                    }
                }
            }
            tx.success();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getCause() );
        }
        return relationshipPropertyCount;
    }
}
