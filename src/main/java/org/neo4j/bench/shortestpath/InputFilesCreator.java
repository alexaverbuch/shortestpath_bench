package org.neo4j.bench.shortestpath;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class InputFilesCreator
{
    private static String DIRECTED_FALSE = "unidirectional";
    private static String DIRECTED_TRUE = "bidirectional";

    public static void main( String[] args ) throws IOException
    {
        String errMsg = String.format( "Expected 1 parameter, found %s. Parameters should be <%s|%s>\n", args.length,
                DIRECTED_FALSE, DIRECTED_TRUE );

        if ( args.length != 1 )
        {
            System.out.println( errMsg );
            return;
        }

        if ( args[0].equals( DIRECTED_FALSE ) == false && args[0].equals( DIRECTED_TRUE ) == false )
        {
            System.out.println( String.format( "Unexpected value for parameter 0: %s\n%s", args[0], errMsg ) );
            return;
        }

        Random random = new Random( 42 );
        DecimalFormat doubleFormat = new DecimalFormat( "#.##" );
        int pathCount = 10000;
        boolean directed = ( args[0].equals( DIRECTED_TRUE ) );

        File relationshipsCsvFile = new File( Config.RELATIONSHIP_ID_FILE );
        relationshipsCsvFile.delete();
        relationshipsCsvFile.createNewFile();
        CsvFileWriter relationshipsWriter = new CsvFileWriter( relationshipsCsvFile );
        CsvFileReader relationshipsReader = new CsvFileReader( new File( Config.RAW_RELATIONSHIP_FILE ) );

        File nodesCsvFile = new File( Config.NODE_ID_FILE );
        nodesCsvFile.delete();
        nodesCsvFile.createNewFile();
        CsvFileWriter nodesWriter = new CsvFileWriter( nodesCsvFile );

        File pathStartAndEndNodesCsvFile = new File( Config.PATH_START_END_ID_FILE );
        pathStartAndEndNodesCsvFile.delete();
        pathStartAndEndNodesCsvFile.createNewFile();
        CsvFileWriter pathStartAndEndNodesWriter = new CsvFileWriter( pathStartAndEndNodesCsvFile );

        Set<Long> nodeIds = createRelationshipFileAndReturnUniqueNodeIds( relationshipsReader, relationshipsWriter,
                random, doubleFormat, directed );

        createNodeFile( nodeIds, nodesWriter, random, doubleFormat );

        // Make Path Start And End Nodes File
        createPathStartAndEndNodeFile( pathStartAndEndNodesWriter, nodeIds, pathCount, random );

        relationshipsWriter.close();
        nodesWriter.close();
        pathStartAndEndNodesWriter.close();
    }

    // Make Weighted Relationship File
    private static Set<Long> createRelationshipFileAndReturnUniqueNodeIds( CsvFileReader relationshipsReader,
            CsvFileWriter relationshipsWriter, Random random, DecimalFormat doubleFormat, boolean bidirectional )
            throws IOException
    {
        relationshipsWriter.writeLine( "from", "to", "type", "weight@double" );
        Set<Long> nodeIds = new HashSet<Long>();
        while ( relationshipsReader.hasNext() )
        {
            String[] relationshipNodes = relationshipsReader.next();
            for ( String node : relationshipNodes )
            {
                nodeIds.add( incByOne( node ) );
            }
            double weight = random.nextDouble();
            String correctedStartNodeId = incByOne( relationshipNodes[0] ).toString();
            String correctedEndNodeId = incByOne( relationshipNodes[1] ).toString();
            String formattedWeight = doubleFormat.format( weight );
            relationshipsWriter.writeLine( correctedStartNodeId, correctedEndNodeId, "LINK", formattedWeight );
            if ( bidirectional )
            {
                relationshipsWriter.writeLine( correctedEndNodeId, correctedStartNodeId, "LINK", formattedWeight );
            }
        }
        return nodeIds;
    }

    // Make Weighted Node File
    private static void createNodeFile( Set<Long> nodeIds, CsvFileWriter nodesWriter, Random random,
            DecimalFormat doubleFormat ) throws IOException
    {
        nodesWriter.writeLine( "id", "weight@double" );
        for ( Long nodeId : nodeIds )
        {
            double weight = random.nextDouble();
            nodesWriter.writeLine( nodeId.toString(), doubleFormat.format( weight ) );
        }
    }

    // Make Path Start And End Nodes File
    private static void createPathStartAndEndNodeFile( CsvFileWriter pathStartAndEndNodesWriter, Set<Long> nodeIds,
            int pathCount, Random random ) throws IOException
    {
        pathStartAndEndNodesWriter.writeLine( "start", "end" );
        List<Long> allNodeIds = Arrays.asList( nodeIds.toArray( new Long[nodeIds.size()] ) );
        int allNodeIdsCount = allNodeIds.size();
        for ( int i = 0; i < pathCount; i++ )
        {
            Long startNodeId = allNodeIds.get( (int) ( random.nextDouble() * allNodeIdsCount ) );
            Long endNodeId = -1L;
            while ( endNodeId == -1 )
            {
                long val = allNodeIds.get( (int) ( random.nextDouble() * allNodeIdsCount ) );
                endNodeId = ( val == startNodeId ) ? -1 : val;
            }
            pathStartAndEndNodesWriter.writeLine( startNodeId.toString(), endNodeId.toString() );
        }
    }

    // To avoid Reference Node in Neo4j, make sure no Nodes have ID==0
    private static Long incByOne( String longString )
    {
        return Long.parseLong( longString ) + 1;
    }
}
