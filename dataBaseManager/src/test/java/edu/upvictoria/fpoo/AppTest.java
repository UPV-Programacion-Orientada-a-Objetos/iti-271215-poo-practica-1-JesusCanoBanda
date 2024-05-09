package edu.upvictoria.fpoo;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;


/**
 * Unit test for simple App.
 */
public class AppTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    public void testPath() {
        // Prueba el caso en que la ruta especificada existe
        String existingPath = "/home/cano/Documentos/BASEDEDATOS";
        App.Path(existingPath);
        assertEquals(existingPath, App.Path);

    }

    public void testCreateTable() {
        String nombreTabla = "tabla1";
        String columns = "id INT, name VARCHAR(50), age INT";
        App.createTable(nombreTabla, columns);
        String filePath = App.Path + File.separator + nombreTabla + ".csv";
        File tableFile = new File(filePath);
        assertTrue(tableFile.exists());
    }

    public void testShowTables() {
        // Simular un directorio con archivos CSV y verificar si se muestran correctamente
        String testDirPath = "/home/cano/Documentos/BASEDEDATOS";
        File testDir = new File(testDirPath);
        testDir.mkdir();
        File testFile1 = new File(testDir, "tabla1.csv");
        File testFile2 = new File(testDir, "tabla2.csv");
        try {
            testFile1.createNewFile();
            testFile2.createNewFile();
        } catch (IOException e) {
            System.out.println("Error al crear el archivo");
        }
        App.Path(testDirPath);
        App.showTables();
    }

    public void testDrop() {
        // Simular la eliminación de un archivo CSV y verificar si se elimina correctamente
        String tableName = "tabla2";
        String filePath = App.Path + File.separator + tableName + ".csv";
        File tableFile = new File(filePath);
        try {
            tableFile.createNewFile();
        } catch (IOException e) {
            System.out.println("error al crear el archivo");
        }
        App.Drop(tableName);
        assertFalse(tableFile.exists());
    }

    public void testInsert() {
        // Simular la inserción de datos en un archivo CSV y verificar si se insertan correctamente
        String tableName = "tabla3";
        String[] columnNames = {"id", "name", "age"};
        String[] values = {"1", "John", "30"};
        //App.Insert(values);
        String filePath = App.Path + File.separator + tableName + ".csv";
        File tableFile = new File(filePath);
        assertTrue(tableFile.exists());
    }

}

