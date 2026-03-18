import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

// repush
public class FileRead {

    static class Position {
        int row;
        int col;
        int level;

        public Position(int row, int col, int level) {
            this.row = row;
            this.col = col;
            this.level = level;
        }

        public String toString() {
            return "(" + row + ", " + col + ", " + level + ")";
        }
    }

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

            Position start = findSymbol(map, 'W');
            Position goal = findSymbol(map, '$');

            if (start != null) {
                System.out.println("\nStart found at: row=" + start.row + " col=" + start.col + " level=" + start.level);
            } else {
                System.out.println("\nStart position W not found.");
            }

            if (goal != null) {
                System.out.println("Goal found at: row=" + goal.row + " col=" + goal.col + " level=" + goal.level);
            } else {
                System.out.println("Goal position $ not found.");
            }

            System.out.println();
            queueRouteSetup(map);
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
        ArrayList<String> coordinates = new ArrayList<String>();

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

    public static Position findSymbol(char[][][] map, char target) {
        for (int level = 0; level < map[0][0].length; level++) {
            for (int row = 0; row < map.length; row++) {
                for (int col = 0; col < map[0].length; col++) {
                    if (map[row][col][level] == target) {
                        return new Position(row, col, level);
                    }
                }
            }
        }
        return null;
    }

    public static void queueRouteSetup(char[][][] map) {
        Position start = findSymbol(map, 'W');

        if (start == null) {
            System.out.println("Cannot start route search because W was not found.");
            return;
        }

        boolean[][][] visited = new boolean[map.length][map[0].length][map[0][0].length];
        Queue<Position> queue = new LinkedList<Position>();

        queue.add(start);
        visited[start.row][start.col][start.level] = true;

        System.out.println("Queue route setup started.");
        System.out.println("Starting position: " + start);

        if (!queue.isEmpty()) {
            Position current = queue.remove();
            System.out.println("Removed from queue: " + current);

            ArrayList<Position> neighbors = getNeighbors(map, current, visited);

            System.out.println("Valid neighbors:");
            for (int i = 0; i < neighbors.size(); i++) {
                Position next = neighbors.get(i);
                queue.add(next);
                visited[next.row][next.col][next.level] = true;
                System.out.println(next);
            }

            System.out.println("Queue size after adding neighbors: " + queue.size());
        }
    }

    public static ArrayList<Position> getNeighbors(char[][][] map, Position current, boolean[][][] visited) {
        ArrayList<Position> neighbors = new ArrayList<Position>();

        // up
        addNeighbor(map, current.row - 1, current.col, current.level, visited, neighbors);
        // down
        addNeighbor(map, current.row + 1, current.col, current.level, visited, neighbors);
        // left
        addNeighbor(map, current.row, current.col - 1, current.level, visited, neighbors);
        // right
        addNeighbor(map, current.row, current.col + 1, current.level, visited, neighbors);

        return neighbors;
    }

    public static void addNeighbor(char[][][] map, int row, int col, int level,
                                   boolean[][][] visited, ArrayList<Position> neighbors) {
        if (inBounds(map, row, col, level)
                && !visited[row][col][level]
                && isWalkable(map[row][col][level])) {
            neighbors.add(new Position(row, col, level));
        }
    }

    public static boolean inBounds(char[][][] map, int row, int col, int level) {
        return row >= 0 && row < map.length
                && col >= 0 && col < map[0].length
                && level >= 0 && level < map[0][0].length;
    }

    public static boolean isWalkable(char cell) {
        return cell == '.' || cell == '$' || cell == 'W';
    }
}