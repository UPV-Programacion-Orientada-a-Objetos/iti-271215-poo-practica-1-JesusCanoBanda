package edu.upvictoria.fpoo;
import java.io.*;
import java.util.regex.*;

public class App 
{
    public static String Path = ""; // Ruta de trabajo
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

        Matcher mat;

        if ((mat = usePattern.matcher(c)).matches()) {
            // Comando USE
            String path = mat.group(1);
            Path(path);
            System.out.println("Ruta de trabajo establecida en: " + Path);
        } else if ((mat = createTablePattern.matcher(c)).matches()) {
            // Comando CREATE TABLE
            String nombreTabla = mat.group(1);
            String columnas = mat.group(2);
            createTable(nombreTabla, columnas);
            System.out.println("Tabla " + nombreTabla + " creada exitosamente.");
        } else if ((showTablesPattern.matcher(c)).matches()) {
            // Comando SHOW TABLES
            showTables();
        } else if ((mat = dropTablePattern.matcher(c)).matches()) {
            // Comando DROP TABLE
            String nombreTabla = mat.group(1);
            Drop(nombreTabla);
            System.out.println("Tabla " + nombreTabla + " eliminada exitosamente.");
        } else if ((mat = selectPattern.matcher(c)).matches()) {
            // Comando SELECT
            String nombreColumna = mat.group(1);
            String nombreTabla = mat.group(2);
            Select(nombreColumna, nombreTabla);
        } else if ((mat = insertPattern.matcher(c)).matches()) {
            // Comando INSERT INTO
            Insert(mat);
        } else if ((mat = deletePattern.matcher(c)).matches()) {
            // Comando DELETE
            String nombretabla = mat.group(1);
            String condicion = mat.group(2);
            Delete(nombretabla, condicion);
        } else {

            System.out.println("Comando no reconocido: " + c);
        }
    }

    public static void Path(String path) {
        File directorio = new File(path);
        if (!directorio.exists()) {
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

    public static void createTable(String tableName, String sql) {
        // Crear archivo CSV con las columnas especificadas
        String filePath = Path + File.separator + tableName + ".csv";
        try {
            FileWriter writer = new FileWriter(filePath);
            Pattern columnPattern = Pattern.compile("(\\w+)\\s+(\\w+)(?:\\((\\d+)\\))?\\s*(?:NOT\\s+NULL)?\\s*(?:NULL)?\\s*(?:PRIMARY\\s+KEY)?\\s*(?:,|$)", Pattern.CASE_INSENSITIVE);
            Matcher mat = columnPattern.matcher(sql);

            StringBuilder cabecera = new StringBuilder();

            while (mat.find()) {
                String nombreColumna = mat.group(1);
                cabecera.append(nombreColumna).append(",");
            }

            // Escribir encabezado en el archivo
            String headerLine = cabecera.toString().trim();
            writer.write(headerLine.substring(0, headerLine.length() - 1) + "\n");
            // Eliminar la última coma y agregar nueva línea
            
            writer.close();
        } catch (IOException e) {
            System.out.println("Error al ejecutar un comando");
        }
    }

    public static void showTables() {

        File directorio = new File(Path);
        File[] files = directorio.listFiles();


        if (files != null && files.length > 0) {
            System.out.println("Tablas disponibles:");


            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    // Verificar si el archivo es un archivo CSV
                    if (fileName.endsWith(".csv")) {
                        // Eliminar la extensión .csv del nombre del archivo
                        String nombreTabla = fileName.substring(0, fileName.lastIndexOf('.'));
                        System.out.println(nombreTabla);
                    }
                }
            }
        } else {
            System.out.println("No hay tablas disponibles en la dirección especificada.");
        }
    }

    public static void Drop(String nombreTabla) {
        BufferedReader rea = new BufferedReader(new InputStreamReader(System.in));

        String filePath = Path + File.separator + nombreTabla + ".csv";
        File tableFile = new File(filePath);

        // Verificar si el archivo existe y eliminarlo si es así
        if (tableFile.exists() && tableFile.isFile()) {
            try {
                System.out.print("¿Estas seguro de que deseas eliminar la tabla " + nombreTabla + "? (s/n): ");
                String r = rea.readLine().trim().toLowerCase();
                if (r.equals("s")) {
                    if (tableFile.delete()) {
                        System.out.println("Tabla " + nombreTabla + " eliminada correctamente.");
                    } else {
                        System.out.println("Error al eliminar la tabla " + nombreTabla);
                    }
                } else {
                    System.out.println("La eliminación de la tabla " + nombreTabla + " ha sido cancelada.");
                }
            } catch (IOException e) {
                System.out.println("Error al leer la entrada del usuario.");
            }
        } else {
            System.out.println("La tabla " + nombreTabla + " no existe.");
        }
    }

    private static void Select(String nombreColumna, String nombreTabla) {
        // Obtener la ruta del archivo CSV de la tabla especificada
        String filePath = Path + File.separator + nombreTabla + ".csv";
        File tableFile = new File(filePath);

        // Verificar si el archivo existe y es un archivo CSV
        if (tableFile.exists() && tableFile.isFile() && filePath.endsWith(".csv")) {
            try {

                BufferedReader reader = new BufferedReader(new FileReader(tableFile));

                // Leer la primera línea del archivo para obtener los nombres de las columnas
                String lineaCabecera = reader.readLine();
                if (lineaCabecera != null) {

                    String[] columnNames = lineaCabecera.split(",");


                    if (nombreColumna.equals("*")) {
                        // Mostrar el nombre de la tabla
                        System.out.println("Tabla: " + nombreTabla);


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
                            if (columnNames[i].trim().equalsIgnoreCase(nombreColumna)) {
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
                            System.out.println("La columna " + nombreColumna + " no existe en la tabla " + nombreTabla);
                        }
                    }
                } else {
                    System.out.println("El archivo de la tabla " + nombreTabla + " está vacío.");
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Error al leer el archivo de la tabla " + nombreTabla + ": ");
            }
        } else {
            System.out.println("La tabla " + nombreTabla + " no existe o no es un archivo CSV.");
        }
    }

    public static void Insert(Matcher matcher) {
        // Extraer el nombre de la tabla y los valores a insertar
        String nombreTabla = matcher.group(1);
        String[] nombreColumna = matcher.group(2).split("\\s*,\\s*");
        String[] values = matcher.group(3).split("\\s*,\\s*");

        // Verificar si el número de columnas coincide con el número de valores
        if (nombreColumna.length != values.length) {
            System.out.println("Error: El número de columnas y valores no coincide en el comando INSERT INTO.");
            return;
        }

        // Construir la fila a insertar
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < nombreColumna.length; i++) {
            row.append(values[i]);
            if (i != nombreColumna.length - 1) {
                row.append(", ");
            }
        }

        // Obtener la ruta del archivo CSV de la tabla especificada
        String filePath = Path + File.separator + nombreTabla + ".csv";
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


                System.out.println("Fila insertada en la tabla " + nombreTabla + ".");
            } catch (IOException e) {
                System.out.println("Error al insertar fila en la tabla " + nombreTabla + ": " + e.getMessage());
            }
        } else {
            System.out.println("La tabla " + nombreTabla + " no existe o no es un archivo CSV.");
        }
    }

    private static void Delete(String nombreTabla, String condicion) {
        // Obtener la ruta del archivo CSV de la tabla especificada
        String filePath = Path + File.separator + nombreTabla + ".csv";
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
                String lineaCabecera = reader.readLine();
                writer.write(lineaCabecera);
                writer.newLine();

                // Obtener los índices de las columnas mencionadas en la condición
                String[] columns = lineaCabecera.split(",");
                int[] conditionIndices = new int[2];
                for (int i = 0; i < columns.length; i++) {
                    if (condicion.contains(columns[i].trim())) {
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
                    if (!Where(values[conditionIndices[0]], condicion)) {
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
                        System.out.println("Filas modificadas en la tabla " + nombreTabla + " según la condición.");
                    } else {
                        System.out.println("Error al renombrar la tabla.");
                    }
                } else {
                    System.out.println("Error al eliminar la tabla " + nombreTabla);
                }
            } catch (IOException e) {
                System.out.println("Error al modificar filas de la tabla " + nombreTabla + ": " + e.getMessage());
            }
        } else {
            System.out.println("La tabla " + nombreTabla + " no existe o no es un archivo CSV.");
        }
    }

    private static boolean Where (String value, String condicion) {
        // Dividir la condición para ver la condicion
        String[] partes = condicion.split("!=");
        if (partes.length == 2) {
            String conditionValue = partes[1].trim();
            return !value.equals(conditionValue);
        } else {
            partes = condicion.split("=");
            if (partes.length == 2) {
                String conditionValue = partes[1].trim();
                return value.equals(conditionValue);
            } else {
                System.out.println("Error: Condición no válida.");
                return false;
            }
        }
    }
//USE /home/cano/Documentos/BASEDEDATOS
}