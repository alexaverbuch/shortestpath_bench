package org.neo4j.bench.shortestpath;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class InputFilesCreator
{
    public static void main( String[] args ) throws IOException
    {
        File relationshipsCsvFile = new File( "data-files/relationships.csv" );
        relationshipsCsvFile.delete();
        relationshipsCsvFile.createNewFile();
        CsvFileWriter relationshipsCsvWriter = new CsvFileWriter( relationshipsCsvFile );
        relationshipsCsvWriter.writeLine( "from", "to", "type", "weight@double" );

        File nodesCsvFile = new File( "data-files/nodes.csv" );
        nodesCsvFile.delete();
        nodesCsvFile.createNewFile();
        CsvFileWriter nodesCsvWriter = new CsvFileWriter( nodesCsvFile );
        nodesCsvWriter.writeLine( "id", "weight@double" );

        Random random = new Random( 42 );

        DecimalFormat doubleFormat = new DecimalFormat( "#.##" );

        Set<Long> nodeIds = new HashSet<Long>();
        CsvFileReader reader = new CsvFileReader( new File( "data-files/facebook_combined.txt" ) );
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

        for ( Long nodeId : nodeIds )
        {
            double weight = random.nextDouble();
            nodesCsvWriter.writeLine( nodeId.toString(), doubleFormat.format( weight ) );
        }
        nodesCsvWriter.close();
    }

    // To avoid problem with Reference Node in Neo4j
    private static Long incByOne( String longString )
    {
        return Long.parseLong( longString ) + 1;
    }
}
