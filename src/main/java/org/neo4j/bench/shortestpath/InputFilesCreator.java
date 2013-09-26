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
    public static void main( String[] args ) throws IOException
    {
        Random random = new Random( 42 );
        DecimalFormat doubleFormat = new DecimalFormat( "#.##" );
        int pathCount = 10000;

        File relationshipsCsvFile = new File( Config.RELATIONSHIP_ID_FILE );
        relationshipsCsvFile.delete();
        relationshipsCsvFile.createNewFile();
        CsvFileWriter relationshipsCsvWriter = new CsvFileWriter( relationshipsCsvFile );
        relationshipsCsvWriter.writeLine( "from", "to", "type", "weight@double" );

        File nodesCsvFile = new File( Config.NODE_ID_FILE );
        nodesCsvFile.delete();
        nodesCsvFile.createNewFile();
        CsvFileWriter nodesCsvWriter = new CsvFileWriter( nodesCsvFile );
        nodesCsvWriter.writeLine( "id", "weight@double" );

        File pathStartAndEndNodesCsvFile = new File( Config.PATH_START_END_ID_FILE );
        pathStartAndEndNodesCsvFile.delete();
        pathStartAndEndNodesCsvFile.createNewFile();
        CsvFileWriter pathStartAndEndNodesCsvWriter = new CsvFileWriter( pathStartAndEndNodesCsvFile );
        pathStartAndEndNodesCsvWriter.writeLine( "start", "end" );

        // Make Weighted Relationship File
        Set<Long> nodeIds = new HashSet<Long>();
        CsvFileReader reader = new CsvFileReader( new File( Config.RAW_RELATIONSHIP_FILE ) );
        while ( reader.hasNext() )
        {
            String[] relationshipNodes = reader.next();
            for ( String node : relationshipNodes )
            {
                nodeIds.add( incByOne( node ) );
            }
            double weight = random.nextDouble();
            relationshipsCsvWriter.writeLine( incByOne( relationshipNodes[0] ).toString(),
                    incByOne( relationshipNodes[1] ).toString(), "LINK", doubleFormat.format( weight ) );
        }
        relationshipsCsvWriter.close();

        // Make Weighted Node File
        for ( Long nodeId : nodeIds )
        {
            double weight = random.nextDouble();
            nodesCsvWriter.writeLine( nodeId.toString(), doubleFormat.format( weight ) );
        }
        nodesCsvWriter.close();

        // Make Path Start And End Nodes File
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
            pathStartAndEndNodesCsvWriter.writeLine( startNodeId.toString(), endNodeId.toString() );
        }
        pathStartAndEndNodesCsvWriter.close();
    }

    // To avoid Reference Node in Neo4j, make sure no Nodes have ID==0
    private static Long incByOne( String longString )
    {
        return Long.parseLong( longString ) + 1;
    }
}
