package edu.upvictoria.fpoo;
import java.io.*;
import java.util.regex.*;

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
                    System.out.println("Programa terminado");
                    break;
                }

                Comandos(c);
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

    private static void Comandos(String c) {
        // Expresiones regulares para cada comando
        Pattern usePattern = Pattern.compile("^USE\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        Pattern createTablePattern = Pattern.compile("^CREATE\\s+TABLE\\s+(\\w+)\\s*\\((.*)\\);$", Pattern.CASE_INSENSITIVE);
        Pattern showTablesPattern = Pattern.compile("^SHOW\\s+TABLES$", Pattern.CASE_INSENSITIVE);
        Pattern dropTablePattern = Pattern.compile("^DROP\\s+TABLE\\s+(\\w+)$", Pattern.CASE_INSENSITIVE);
        Pattern selectPattern = Pattern.compile("^SELECT\\s+(\\*|\\w+)\\s+FROM\\s+(\\w+)$", Pattern.CASE_INSENSITIVE);
        Pattern insertPattern = Pattern.compile("^INSERT\\s+INTO\\s+(\\w+)\\s*\\(([^)]+)\\)\\s*VALUES\\s*\\(([^)]+)\\);$", Pattern.CASE_INSENSITIVE);
        Pattern deletePattern = Pattern.compile("^DELETE\\s+FROM\\s+(\\w+)\\s+WHERE\\s+(.+)$", Pattern.CASE_INSENSITIVE);

        Matcher matcher;

        if ((matcher = usePattern.matcher(c)).matches()) {
            // Comando USE
            String path = matcher.group(1);
            Path(path);
            System.out.println("Ruta de trabajo establecida en: " + Path);
        } else if ((matcher = createTablePattern.matcher(c)).matches()) {
            // Comando CREATE TABLE
            String tableName = matcher.group(1);
            String columns = matcher.group(2);
            createTable(tableName, columns);
            System.out.println("Tabla " + tableName + " creada exitosamente.");
        } else if ((showTablesPattern.matcher(c)).matches()) {
            // Comando SHOW TABLES
            showTables();
        } else if ((matcher = dropTablePattern.matcher(c)).matches()) {
            // Comando DROP TABLE
            String tableName = matcher.group(1);
            Drop(tableName);
            System.out.println("Tabla " + tableName + " eliminada exitosamente.");
        } else if ((matcher = selectPattern.matcher(c)).matches()) {
            // Comando SELECT
            String columnName = matcher.group(1);
            String tableName = matcher.group(2);
            Select(columnName, tableName);
        } else if ((matcher = insertPattern.matcher(c)).matches()) {
            // Comando INSERT INTO
            Insert(matcher);
        } else if ((matcher = deletePattern.matcher(c)).matches()) {
            // Comando DELETE
            String tableName = matcher.group(1);
            String condition = matcher.group(2);
            Delete(tableName, condition);
        } else {

            System.out.println("Comando no reconocido: " + c);
        }
    }

    private static void Path(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            System.out.println("La ruta especificada no existe.");
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                path = reader.readLine().trim();
                reader.close();
                Path(path);
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
            writer.write(headerLine.substring(0, headerLine.length() - 1) + "\n");
            // Eliminar la última coma y agregar nueva línea
            
            writer.close();
        } catch (IOException e) {
            System.out.println("Error al ejecutar un comando");
        }
    }

    private static void showTables() {

        File directory = new File(Path);
        File[] files = directory.listFiles();


        if (files != null && files.length > 0) {
            System.out.println("Tablas disponibles:");


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

    private static void Drop(String tableName) {
        BufferedReader rea = new BufferedReader(new InputStreamReader(System.in));

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

    private static void Select(String columnName, String tableName) {
        // Obtener la ruta del archivo CSV de la tabla especificada
        String filePath = Path + File.separator + tableName + ".csv";
        File tableFile = new File(filePath);

        // Verificar si el archivo existe y es un archivo CSV
        if (tableFile.exists() && tableFile.isFile() && filePath.endsWith(".csv")) {
            try {

                BufferedReader reader = new BufferedReader(new FileReader(tableFile));

                // Leer la primera línea del archivo para obtener los nombres de las columnas
                String headerLine = reader.readLine();
                if (headerLine != null) {

                    String[] columnNames = headerLine.split(",");


                    if (columnName.equals("*")) {
                        // Mostrar el nombre de la tabla
                        System.out.println("Tabla: " + tableName);


                        for (String col : columnNames) {
                            System.out.println("Columna: " + col.trim());
                        }


                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] values = line.split(",");
                            for (String value : values) {
                                System.out.print(value.trim() + "\t");
                            }
                            System.out.println();
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

    private static void Insert(Matcher matcher) {
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

                writer.close();


                System.out.println("Fila insertada en la tabla " + tableName + ".");
            } catch (IOException e) {
                System.out.println("Error al insertar fila en la tabla " + tableName + ": " + e.getMessage());
            }
        } else {
            System.out.println("La tabla " + tableName + " no existe o no es un archivo CSV.");
        }
    }

    private static void Delete(String tableName, String condition) {
        // Obtener la ruta del archivo CSV de la tabla especificada
        String filePath = Path + File.separator + tableName + ".csv";
        File tableFile = new File(filePath);

        // Verificar si el archivo existe y es un archivo CSV
        if (tableFile.exists() && tableFile.isFile() && filePath.endsWith(".csv")) {
            try {
                // Crear un lector para el archivo CSV
                BufferedReader reader = new BufferedReader(new FileReader(tableFile));

                // Crear un archivo temporal
                File tempFile = new File(Path + File.separator + "temp.csv");
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                // Leer el encabezado
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

                int numColumns = columns.length;

                // Leer y procesar las filas del archivo CSV
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    System.out.println(" " + line);
                    // Evaluar la condición para cada fila
                    if (!Where(values[conditionIndices[0]], condition)) {
                        // Escribir la fila original si no coincide con la condición
                        writer.write(" ");
                        writer.newLine();
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

    private static boolean Where (String value, String condition) {
        // Dividir la condición para ver la condicion
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
