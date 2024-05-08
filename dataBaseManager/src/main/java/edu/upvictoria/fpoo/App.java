package edu.upvictoria.fpoo;
import java.io.*;
import java.util.regex.*;
//import java.util.*;

public class App 
{
    private static String Path = ""; // Ruta de trabajo
    public static void main( String[] args )
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                String c = reader.readLine().trim();

                if (c.equalsIgnoreCase("exit")) {
                    System.out.println("Saliendo del programa...");
                    break;
                }

                comandos(c);
            } catch (IOException e) {
                System.out.println("Error al salir del programa.");
            }
        }

        try {
            reader.close();
        } catch (IOException e) {
            System.out.println("Error al cerrar el reader");
        }
    }

    private static void comandos(String c) {
        // Expresiones regulares para cada comando SQL
        Pattern usePattern = Pattern.compile("^USE\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        Pattern createTablePattern = Pattern.compile("^CREATE\\s+TABLE\\s+(\\w+)\\s*\\((.*)\\);$", Pattern.CASE_INSENSITIVE);
        Pattern showTablesPattern = Pattern.compile("^SHOW\\s+TABLES$", Pattern.CASE_INSENSITIVE);
        Pattern dropTablePattern = Pattern.compile("^DROP\\s+TABLE\\s+(\\w+)$", Pattern.CASE_INSENSITIVE);

        Matcher matcher;

        if ((matcher = usePattern.matcher(c)).matches()) {
            // Procesar comando USE
            String path = matcher.group(1);
            setPath(path);
            System.out.println("Ruta de trabajo establecida en: " + Path);
        } else if ((matcher = createTablePattern.matcher(c)).matches()) {
            // Procesar comando CREATE TABLE
            String tableName = matcher.group(1);
            String columns = matcher.group(2);
            createTable(tableName, columns);
            System.out.println("Tabla " + tableName + " creada exitosamente.");
        } else if ((showTablesPattern.matcher(c)).matches()) {
            // Procesar comando SHOW TABLES
            showTables();
        } else if ((matcher = dropTablePattern.matcher(c)).matches()) {
            // Procesar comando DROP TABLE
            String tableName = matcher.group(1);
            dropTable(tableName);

        } else {
            // Otros comandos SQL
            System.out.println("Comando no reconocido: " + c);
        }
    }


    private static void setPath(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            System.out.println("La ruta especificada no existe.");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                path = reader.readLine().trim();
                reader.close();
                setPath(path);
            } catch (IOException e) {
                System.out.println("Error al abrir el archivo.");
            }
        } else {
            Path = path;
        }
    }

    private static void createTable(String tableName, String sql) {
        // Crear archivo CSV con las columnas especificadas
        String filePath = Path + File.separator + tableName + ".csv";
        try {
            FileWriter writer = new FileWriter(filePath);
            Pattern columnPattern = Pattern.compile("(\\w+)\\s+(\\w+)(?:\\((\\d+)\\))?\\s*(?:NOT\\s+NULL)?\\s*(?:NULL)?\\s*(?:PRIMARY\\s+KEY)?\\s*(?:,|$)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = columnPattern.matcher(sql);

            while (matcher.find()) {
                String columnName = matcher.group(1);
                String columnType = matcher.group(2);
                String columnSize = matcher.group(3);
                StringBuilder columnDefinition = new StringBuilder();
                columnDefinition.append(columnName).append(", ").append(columnType);
                if (columnSize != null) {
                    columnDefinition.append("(").append(columnSize).append(")");
                }
                // Validar si es NOT NULL o NULL
                if (matcher.group().toUpperCase().contains("NOT NULL")) {
                    columnDefinition.append(", NOT NULL");
                } else if (matcher.group().toUpperCase().contains("NULL")) {
                    columnDefinition.append(", NULL");
                }
                // Validar si es PRIMARY KEY
                if (matcher.group().toUpperCase().contains("PRIMARY KEY")) {
                    columnDefinition.append(", PRIMARY KEY");
                }
                columnDefinition.append("\n");
                writer.write(columnDefinition.toString());
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("Error al ejecutar un comando");
        }
    }

    private static void showTables() {
        // Obtener la lista de archivos en la dirección establecida
        File directory = new File(Path);
        File[] files = directory.listFiles();

        // Verificar si la carpeta existe y contiene archivos
        if (files != null && files.length > 0) {
            System.out.println("Tablas disponibles:");

            // Iterar sobre los archivos y mostrar los nombres de las tablas
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    // Verificar si el archivo es un archivo CSV
                    if (fileName.endsWith(".csv")) {
                        // Eliminar la extensión .csv del nombre del archivo
                        String tableName = fileName.substring(0, fileName.lastIndexOf('.'));
                        System.out.println(tableName);
                    }
                }
            }
        } else {
            System.out.println("No hay tablas disponibles en la dirección especificada.");
        }
    }

    private static void dropTable(String tableName) {
        // Obtener el archivo correspondiente a la tabla
        String filePath = Path + File.separator + tableName + ".csv";
        File tableFile = new File(filePath);

        // Verificar si el archivo existe y eliminarlo si es así
        if (tableFile.exists() && tableFile.isFile()) {
            if (tableFile.delete()) {
                System.out.println("Tabla " + tableName + " eliminada correctamente.");
            } else {
                System.out.println("Error al eliminar la tabla " + tableName);
            }
        } else {
            System.out.println("La tabla " + tableName + " no existe.");
        }
    }




//USE /home/cano/Documentos/BASEDEDATOS
    //CREATE TABLE LUU(NAME VARCHAR(20) NOT NULL, APP VARCHAR(20) NOT NULL PRIMARY KEY, EDAD INT NULL);

}
