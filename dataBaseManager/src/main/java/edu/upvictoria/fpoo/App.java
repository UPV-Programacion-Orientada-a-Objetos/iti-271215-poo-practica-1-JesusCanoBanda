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
        Pattern selectPattern = Pattern.compile("^SELECT\\s+(\\*|\\w+)\\s+FROM\\s+(\\w+)$", Pattern.CASE_INSENSITIVE);
        Pattern insertPattern = Pattern.compile("^INSERT\\s+INTO\\s+(\\w+)\\s*\\(([^)]+)\\)\\s*VALUES\\s*\\(([^)]+)\\);$", Pattern.CASE_INSENSITIVE);
        Pattern deletePattern = Pattern.compile("^DELETE\\s+FROM\\s+(\\w+)\\s+WHERE\\s+(.+)$", Pattern.CASE_INSENSITIVE);

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
            System.out.println("Tabla " + tableName + " eliminada exitosamente.");
        } else if ((matcher = selectPattern.matcher(c)).matches()) {
            // Procesar comando SELECT
            String columnName = matcher.group(1);
            String tableName = matcher.group(2);
            selectColumnFromTable(columnName, tableName);
        } else if ((matcher = insertPattern.matcher(c)).matches()) {
            // Procesar comando INSERT INTO
            insertTable(matcher);
        } else if ((matcher = deletePattern.matcher(c)).matches()) {
            // Procesar comando DELETE
            String tableName = matcher.group(1);
            String condition = matcher.group(2);
            deleteFromTable(tableName, condition);
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

            StringBuilder headerBuilder = new StringBuilder();

            while (matcher.find()) {
                String columnName = matcher.group(1);
                headerBuilder.append(columnName).append(",");
            }

            // Escribir encabezado en el archivo
            String headerLine = headerBuilder.toString().trim();
            writer.write(headerLine.substring(0, headerLine.length() - 1) + "\n"); // Eliminar la última coma y agregar nueva línea

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
        BufferedReader rea = new BufferedReader(new InputStreamReader(System.in));
        // Obtener el archivo correspondiente a la tabla
        String filePath = Path + File.separator + tableName + ".csv";
        File tableFile = new File(filePath);

        // Verificar si el archivo existe y eliminarlo si es así
        if (tableFile.exists() && tableFile.isFile()) {
            try {
                System.out.print("¿Estas seguro de que deseas eliminar la tabla " + tableName + "? (s/n): ");
                String r = rea.readLine().trim().toLowerCase();
                if (r.equals("s")) {
                    if (tableFile.delete()) {
                        System.out.println("Tabla " + tableName + " eliminada correctamente.");
                    } else {
                        System.out.println("Error al eliminar la tabla " + tableName);
                    }
                } else {
                    System.out.println("La eliminación de la tabla " + tableName + " ha sido cancelada.");
                }
            } catch (IOException e) {
                System.out.println("Error al leer la entrada del usuario.");
            }
        } else {
            System.out.println("La tabla " + tableName + " no existe.");
        }
    }

    private static void selectColumnFromTable(String columnName, String tableName) {
        // Obtener la ruta del archivo CSV de la tabla especificada
        String filePath = Path + File.separator + tableName + ".csv";
        File tableFile = new File(filePath);

        // Verificar si el archivo existe y es un archivo CSV
        if (tableFile.exists() && tableFile.isFile() && filePath.endsWith(".csv")) {
            try {
                // Crear un lector para leer el archivo CSV
                BufferedReader reader = new BufferedReader(new FileReader(tableFile));

                // Leer la primera línea del archivo para obtener los nombres de las columnas
                String headerLine = reader.readLine();
                if (headerLine != null) {
                    // Separar los nombres de las columnas
                    String[] columnNames = headerLine.split(",");

                    // Si la columna es "*", mostrar todas las columnas
                    if (columnName.equals("*")) {
                        // Mostrar el nombre de la tabla
                        System.out.println("Tabla: " + tableName);

                        // Mostrar los nombres de todas las columnas
                        for (String col : columnNames) {
                            System.out.println("Columna: " + col.trim());
                        }

                        // Leer y mostrar todos los valores de todas las columnas
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] values = line.split(",");
                            for (String value : values) {
                                System.out.print(value.trim() + "\t");
                            }
                            System.out.println(); // Nueva línea para cada fila
                        }
                    } else {
                        // Encontrar el índice de la columna especificada
                        int columnIndex = -1;
                        for (int i = 0; i < columnNames.length; i++) {
                            if (columnNames[i].trim().equalsIgnoreCase(columnName)) {
                                columnIndex = i;
                                break;
                            }
                        }

                        if (columnIndex != -1) {
                            // La columna existe, mostrar el nombre de la columna encontrada
                            System.out.println("Columna encontrada: " + columnNames[columnIndex]);

                            // Mostrar todos los valores de esa columna
                            String line;
                            while ((line = reader.readLine()) != null) {
                                String[] values = line.split(",");
                                if (values.length > columnIndex) {
                                    // Verificar si la fila tiene un valor en la columna especificada
                                    System.out.println(values[columnIndex].trim());
                                }
                            }
                        } else {
                            System.out.println("La columna " + columnName + " no existe en la tabla " + tableName);
                        }
                    }
                } else {
                    System.out.println("El archivo de la tabla " + tableName + " está vacío.");
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Error al leer el archivo de la tabla " + tableName + ": ");
            }
        } else {
            System.out.println("La tabla " + tableName + " no existe o no es un archivo CSV.");
        }
    }

    private static void insertTable(Matcher matcher) {
        // Extraer el nombre de la tabla y los valores a insertar
        String tableName = matcher.group(1);
        String[] columnNames = matcher.group(2).split("\\s*,\\s*");
        String[] values = matcher.group(3).split("\\s*,\\s*");

        // Verificar si el número de columnas coincide con el número de valores
        if (columnNames.length != values.length) {
            System.out.println("Error: El número de columnas y valores no coincide en el comando INSERT INTO.");
            return;
        }

        // Construir la fila a insertar
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < columnNames.length; i++) {
            row.append(values[i]);
            if (i != columnNames.length - 1) {
                row.append(", ");
            }
        }

        // Obtener la ruta del archivo CSV de la tabla especificada
        String filePath = Path + File.separator + tableName + ".csv";
        File tableFile = new File(filePath);

        // Verificar si el archivo existe y es un archivo CSV
        if (tableFile.exists() && tableFile.isFile() && filePath.endsWith(".csv")) {
            try {
                // Crear un escritor para agregar la nueva fila al archivo CSV
                BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile, true));

                // Escribir la nueva fila en el archivo CSV
                writer.write(row.toString());
                writer.newLine();

                // Cerrar el escritor
                writer.close();

                // Mostrar mensaje de éxito
                System.out.println("Fila insertada en la tabla " + tableName + ".");
            } catch (IOException e) {
                System.out.println("Error al insertar fila en la tabla " + tableName + ": " + e.getMessage());
            }
        } else {
            System.out.println("La tabla " + tableName + " no existe o no es un archivo CSV.");
        }
    }

    private static void deleteFromTable(String tableName, String condition) {
        // Obtener la ruta del archivo CSV de la tabla especificada
        String filePath = Path + File.separator + tableName + ".csv";
        File tableFile = new File(filePath);

        // Verificar si el archivo existe y es un archivo CSV
        if (tableFile.exists() && tableFile.isFile() && filePath.endsWith(".csv")) {
            try {
                // Crear un lector para leer el archivo CSV
                BufferedReader reader = new BufferedReader(new FileReader(tableFile));

                // Crear un archivo temporal para escribir las filas que no coinciden con la condición
                File tempFile = new File(Path + File.separator + "temp.csv");
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                // Leer la primera línea del archivo (encabezado)
                String headerLine = reader.readLine();
                writer.write(headerLine);
                writer.newLine();

                // Obtener los índices de las columnas mencionadas en la condición
                String[] columns = headerLine.split(",");
                int[] conditionIndices = new int[2];
                for (int i = 0; i < columns.length; i++) {
                    if (condition.contains(columns[i].trim())) {
                        conditionIndices[0] = i;
                        break;
                    }
                }

                // Número de columnas en el archivo CSV
                int numColumns = columns.length;

                // Leer y procesar las filas del archivo CSV
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");

                    // Evaluar la condición para cada fila
                    if (!evaluateCondition(values[conditionIndices[0]], condition)) {
                        // Escribir la fila original si no coincide con la condición
                        writer.write(line);
                        writer.newLine();
                        System.out.println("hola");
                    } else {
                        // Escribir una fila vacía con el mismo número de columnas que el encabezado
                        for (int i = 0; i < numColumns; i++) {
                            // Escribir una coma si no es la última columna
                            if (i < numColumns - 1)
                                writer.write(",");
                        }
                        writer.newLine();
                    }
                }

                // Cerrar los lectores y escritores
                reader.close();
                writer.close();

                // Reemplazar el archivo original con el archivo temporal
                if (tableFile.delete()) {
                    if (tempFile.renameTo(tableFile)) {
                        System.out.println("Filas modificadas en la tabla " + tableName + " según la condición.");
                    } else {
                        System.out.println("Error al renombrar la tabla.");
                    }
                } else {
                    System.out.println("Error al eliminar la tabla " + tableName);
                }
            } catch (IOException e) {
                System.out.println("Error al modificar filas de la tabla " + tableName + ": " + e.getMessage());
            }
        } else {
            System.out.println("La tabla " + tableName + " no existe o no es un archivo CSV.");
        }
    }












    private static boolean evaluateCondition(String value, String condition) {
        // Dividir la condición en el operador y el valor
        String[] parts = condition.split("!=");
        if (parts.length == 2) {
            String conditionValue = parts[1].trim();
            return !value.equals(conditionValue);
        } else {
            parts = condition.split("=");
            if (parts.length == 2) {
                String conditionValue = parts[1].trim();
                return value.equals(conditionValue);
            } else {
                System.out.println("Error: Condición no válida.");
                return false;
            }
        }
    }




//USE /home/cano/Documentos/BASEDEDATOS
    //CREATE TABLE LUU(NAME VARCHAR(20) NOT NULL, APP VARCHAR(20) NOT NULL PRIMARY KEY, EDAD INT NULL);

}
