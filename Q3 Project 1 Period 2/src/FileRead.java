import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
//repush
public class FileRead {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No file name");
            return;
        }

        File mapFile = new File(args[0]);
        char[][][] map = readMap(mapFile);

        if (map != null) {
            printTextMap(map);
            System.out.println();
            ArrayList<String> coords = toCoordinateList(map);
            printCoordinateList(coords);

            int[] start = findSymbol(map, 'W');
            int[] goal = findSymbol(map, '$');

            if (start != null) {
                System.out.println("\nStart found at: row=" + start[0] + " col=" + start[1] + " level=" + start[2]);
            } else {
                System.out.println("\nStart position W not found.");
            }

            if (goal != null) {
                System.out.println("Goal found at: row=" + goal[0] + " col=" + goal[1] + " level=" + goal[2]);
            } else {
                System.out.println("Goal position $ not found.");
            }
        }
    }

    public static char[][][] readMap(File mapFile) {
        try {
            Scanner scan = new Scanner(mapFile);

            if (!scan.hasNextInt()) {
                System.out.println("Invalid file: missing width");
                scan.close();
                return null;
            }
            int width = scan.nextInt();

            if (!scan.hasNextInt()) {
                System.out.println("Invalid file: missing height");
                scan.close();
                return null;
            }
            int height = scan.nextInt();

            if (!scan.hasNextInt()) {
                System.out.println("Invalid file: missing level count");
                scan.close();
                return null;
            }
            int levels = scan.nextInt();

            if (width <= 0 || height <= 0 || levels <= 0) {
                System.out.println("Invalid file: dimensions must be positive");
                scan.close();
                return null;
            }

            // [row][col][level]
            char[][][] map = new char[height][width][levels];

            int startCount = 0;
            int goalCount = 0;

            for (int level = 0; level < levels; level++) {
                for (int row = 0; row < height; row++) {
                    for (int col = 0; col < width; col++) {
                        if (!scan.hasNext()) {
                            System.out.println("Invalid file: not enough map entries");
                            scan.close();
                            return null;
                        }

                        String token = scan.next();

                        if (token.length() != 1) {
                            System.out.println("Invalid file: each map entry must be one character");
                            scan.close();
                            return null;
                        }

                        char cell = token.charAt(0);

                        if (!isValidMapChar(cell)) {
                            System.out.println("Invalid file: illegal character '" + cell + "'");
                            scan.close();
                            return null;
                        }

                        if (cell == 'W') {
                            startCount++;
                        } else if (cell == '$') {
                            goalCount++;
                        }

                        map[row][col][level] = cell;
                    }
                }
            }

            scan.close();

            if (startCount != 1) {
                System.out.println("Invalid file: there must be exactly one W");
                return null;
            }

            if (goalCount < 1) {
                System.out.println("Invalid file: there must be at least one $");
                return null;
            }

            return map;

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + mapFile.getName());
            return null;
        }
    }

    public static boolean isValidMapChar(char c) {
        return c == 'W' || c == '$' || c == '@' || c == '.';
    }

    public static void printTextMap(char[][][] map) {
        for (int level = 0; level < map[0][0].length; level++) {
            for (int row = 0; row < map.length; row++) {
                StringBuilder line = new StringBuilder();
                for (int col = 0; col < map[0].length; col++) {
                    line.append(map[row][col][level]);
                    if (col < map[0].length - 1) {
                        line.append(' ');
                    }
                }
                System.out.println(line);
            }

            if (level < map[0][0].length - 1) {
                System.out.println();
            }
        }
    }

    public static ArrayList<String> toCoordinateList(char[][][] map) {
        ArrayList<String> coordinates = new ArrayList<>();

        for (int level = 0; level < map[0][0].length; level++) {
            for (int row = 0; row < map.length; row++) {
                for (int col = 0; col < map[0].length; col++) {
                    coordinates.add(map[row][col][level] + " " + row + " " + col + " " + level);
                }
            }
        }

        return coordinates;
    }

    public static void printCoordinateList(ArrayList<String> coordinates) {
        for (String entry : coordinates) {
            System.out.println(entry);
        }
    }

    public static int[] findSymbol(char[][][] map, char target) {
        for (int level = 0; level < map[0][0].length; level++) {
            for (int row = 0; row < map.length; row++) {
                for (int col = 0; col < map[0].length; col++) {
                    if (map[row][col][level] == target) {
                        return new int[]{row, col, level};
                    }
                }
            }
        }
        return null;
    }
}