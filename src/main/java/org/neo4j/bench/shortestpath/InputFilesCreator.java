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

        File nodesCsvFile = new File( "data-files/nodes.csv" );
        nodesCsvFile.delete();
        nodesCsvFile.createNewFile();
        CsvFileWriter nodesCsvWriter = new CsvFileWriter( nodesCsvFile );

        Random random = new Random( 42 );

        Set<Long> nodeIds = new HashSet<Long>();
        CsvFileReader reader = new CsvFileReader( new File( "data-files/facebook_combined.txt" ) );
        while ( reader.hasNext() )
        {
            String[] relationshipNodes = reader.next();
            for ( String node : relationshipNodes )
            {
                nodeIds.add( Long.parseLong( node ) );
            }
            DecimalFormat df = new DecimalFormat( "#.##" );
            double weight = random.nextDouble();
            relationshipsCsvWriter.writeLine( relationshipNodes[0], relationshipNodes[1], df.format( weight ) );
        }
        relationshipsCsvWriter.close();

        for ( Long nodeId : nodeIds )
        {
            DecimalFormat df = new DecimalFormat( "#.##" );
            double weight = random.nextDouble();
            nodesCsvWriter.writeLine( nodeId.toString(), df.format( weight ) );
        }
        nodesCsvWriter.close();
    }
}
