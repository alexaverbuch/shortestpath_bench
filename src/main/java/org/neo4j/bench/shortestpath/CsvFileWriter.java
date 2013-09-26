package org.neo4j.bench.shortestpath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CsvFileWriter
{
    private final String COLUMN_SEPARATOR = " ";
    private BufferedWriter bufferedWriter = null;

    public CsvFileWriter( File file ) throws IOException
    {
        bufferedWriter = new BufferedWriter( new FileWriter( file ) );
    }

    public void writeLine( String... columns ) throws IOException
    {
        for ( String column : columns )
        {
            bufferedWriter.write( column );
            bufferedWriter.write( COLUMN_SEPARATOR );
        }
        bufferedWriter.newLine();
    }

    public void close() throws IOException
    {
        bufferedWriter.flush();
        bufferedWriter.close();
    }
}
