import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class FileRead {

    // custom exceptions for different error types
    static class IllegalCommandLineInputsException extends Exception {
        public IllegalCommandLineInputsException(String message) {
            super(message);
        }
    }

    static class IllegalMapCharacterException extends Exception {
        public IllegalMapCharacterException(String message) {
            super(message);
        }
    }

    static class IncompleteMapException extends Exception {
        public IncompleteMapException(String message) {
            super(message);
        }
    }

    static class IncorrectMapFormatException extends Exception {
        public IncorrectMapFormatException(String message) {
            super(message);
        }
    }

    // holds a position in the map (row, column, and which level/floor)
    static class Position {
        int row;
        int col;
        int level;

        public Position(int row, int col, int level) {
            this.row = row;
            this.col = col;
            this.level = level;
        }
    }

    // all the settings from command line args
    static class Config {
        boolean useQueue = false;
        boolean useStack = false;
        boolean useOpt = false;
        boolean inCoordinate = false;
        boolean outCoordinate = false;
        boolean showTime = false;
        boolean help = false;
        String fileName = null;
    }

    // stores the path we found and how long it took
    static class SearchResult {
        ArrayList<Position> path;
        double runtimeSeconds;

        public SearchResult(ArrayList<Position> path, double runtimeSeconds) {
            this.path = path;
            this.runtimeSeconds = runtimeSeconds;
        }
    }

    public static void main(String[] args) {
        try {
            Config config = parseArgs(args);

            if (config.help) {
                printHelp();
                return;
            }

            // open the file
            Scanner scan = new Scanner(new File(config.fileName));
            char[][][] map;

            // read the map depending on what format the input is
            if (config.inCoordinate) {
                map = readCoordinateMap(scan);
            } else {
                map = readTextMap(scan);
            }

            scan.close();

            SearchResult result;

            // run the right search mode
            if (config.useQueue) {
                result = queueRoute(map);
            } else if (config.useStack) {
                System.out.println("Stack mode not added yet.");
                return;
            } else {
                System.out.println("Optimal mode not added yet.");
                return;
            }

            // print results
            if (result.path == null) {
                System.out.println("The Wolverine Store is closed.");
            } else {
                if (config.outCoordinate) {
                    printCoordinateRoute(result.path);
                } else {
                    printTextRoute(map, result.path);
                }
            }

            if (config.showTime) {
                System.out.println("Total Runtime: " + result.runtimeSeconds + " seconds");
            }

        } catch (IllegalCommandLineInputsException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        } catch (IllegalMapCharacterException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        } catch (IncompleteMapException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        } catch (IncorrectMapFormatException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
            System.exit(-1);
        }
    }

    // parses all the command line arguments and puts them in a Config object
    public static Config parseArgs(String[] args) throws IllegalCommandLineInputsException {
        Config config = new Config();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("--Queue")) {
                config.useQueue = true;
            } else if (arg.equals("--Stack")) {
                config.useStack = true;
            } else if (arg.equals("--Opt")) {
                config.useOpt = true;
            } else if (arg.equals("--Incoordinate")) {
                config.inCoordinate = true;
            } else if (arg.equals("--Outcoordinate")) {
                config.outCoordinate = true;
            } else if (arg.equals("--Time")) {
                config.showTime = true;
            } else if (arg.equals("--Help")) {
                config.help = true;
            } else {
                // anything thats not a flag should be the filename
                if (config.fileName == null) {
                    config.fileName = arg;
                } else {
                    throw new IllegalCommandLineInputsException("Too many file names given.");
                }
            }
        }

        // make sure exactly one mode was picked
        int count = 0;
        if (config.useQueue) count++;
        if (config.useStack) count++;
        if (config.useOpt) count++;

        if (!config.help && count != 1) {
            throw new IllegalCommandLineInputsException("Exactly one of --Queue, --Stack, or --Opt must be provided.");
        }

        if (!config.help && config.fileName == null) {
            throw new IllegalCommandLineInputsException("Missing input file name.");
        }

        return config;
    }

    public static void printHelp() {
        System.out.println("Usage: java FileRead [mode] [options] inputfile");
        System.out.println("Modes:");
        System.out.println("  --Queue           use queue-based search");
        System.out.println("  --Stack           use stack-based search");
        System.out.println("  --Opt             use shortest-path search");
        System.out.println("Options:");
        System.out.println("  --Incoordinate    input file is coordinate-based");
        System.out.println("  --Outcoordinate   output route in coordinate format");
        System.out.println("  --Time            print runtime");
        System.out.println("  --Help            show this help message");
    }

    // reads map from text format - just reads characters one by one
    public static char[][][] readTextMap(Scanner scan)
            throws IncorrectMapFormatException, IncompleteMapException, IllegalMapCharacterException {

        int rows = readPositiveInt(scan, "Missing or invalid row count.");
        int cols = readPositiveInt(scan, "Missing or invalid column count.");
        int levels = readPositiveInt(scan, "Missing or invalid maze count.");

        char[][][] map = new char[rows][cols][levels];

        for (int level = 0; level < levels; level++) {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    if (!scan.hasNext()) {
                        throw new IncompleteMapException("Not enough map entries.");
                    }

                    String token = scan.next();

                    if (token.length() != 1) {
                        throw new IncorrectMapFormatException("Each map entry must be one character.");
                    }

                    char ch = token.charAt(0);

                    if (!isValidMapChar(ch)) {
                        throw new IllegalMapCharacterException("Illegal character on map: " + ch);
                    }

                    map[row][col][level] = ch;
                }
            }
        }

        return map;
    }

    // reads map from coordinate format - reads symbol then its position
    public static char[][][] readCoordinateMap(Scanner scan)
            throws IncorrectMapFormatException, IncompleteMapException, IllegalMapCharacterException {

        int rows = readPositiveInt(scan, "Missing or invalid row count.");
        int cols = readPositiveInt(scan, "Missing or invalid column count.");
        int levels = readPositiveInt(scan, "Missing or invalid maze count.");

        // fill with walls first then overwrite the coordinates given
        char[][][] map = makeEmptyMap(rows, cols, levels);

        while (scan.hasNext()) {
            String token = scan.next();

            if (token.length() != 1) {
                throw new IllegalMapCharacterException("Map symbol is invalid.");
            }

            char ch = token.charAt(0);

            if (!isValidMapChar(ch)) {
                throw new IllegalMapCharacterException("Illegal character on map: " + ch);
            }

            if (!scan.hasNextInt()) {
                throw new IncorrectMapFormatException("Missing row value in coordinate input.");
            }
            int row = scan.nextInt();

            if (!scan.hasNextInt()) {
                throw new IncorrectMapFormatException("Missing column value in coordinate input.");
            }
            int col = scan.nextInt();

            if (!scan.hasNextInt()) {
                throw new IncorrectMapFormatException("Missing level value in coordinate input.");
            }
            int level = scan.nextInt();

            if (row < 0 || row >= rows || col < 0 || col >= cols || level < 0 || level >= levels) {
                throw new IncorrectMapFormatException("Coordinate out of bounds.");
            }

            map[row][col][level] = ch;
        }

        return map;
    }

    // helper to read a positive integer from the scanner
    public static int readPositiveInt(Scanner scan, String message)
            throws IncorrectMapFormatException {
        if (!scan.hasNextInt()) {
            throw new IncorrectMapFormatException(message);
        }

        int value = scan.nextInt();

        if (value <= 0) {
            throw new IncorrectMapFormatException(message);
        }

        return value;
    }

    // creates a map filled with walls '@' as a starting point
    public static char[][][] makeEmptyMap(int rows, int cols, int levels) {
        char[][][] map = new char[rows][cols][levels];

        for (int level = 0; level < levels; level++) {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    map[row][col][level] = '@';
                }
            }
        }

        return map;
    }

    // only these characters are allowed in the map
    public static boolean isValidMapChar(char c) {
        return c == 'W' || c == '$' || c == '.' || c == '@' || c == '|';
    }

    // does the BFS search going level by level
    public static SearchResult queueRoute(char[][][] map) {
        long startTime = System.nanoTime();

        ArrayList<Position> fullPath = new ArrayList<Position>();
        int totalLevels = map[0][0].length;

        for (int currentLevel = 0; currentLevel < totalLevels; currentLevel++) {
            // find start, goal, and walkway on this level
            Position startPos = findSymbol(map, currentLevel, 'W');
            Position goalPos = findSymbol(map, currentLevel, '$');
            Position walkwayPos = findSymbol(map, currentLevel, '|');

            // no starting point means failure
            if (startPos == null) {
                long endTime = System.nanoTime();
                return new SearchResult(null, (endTime - startTime) / 1000000000.0);
            }

            ArrayList<Position> part = null;

            // check if we can reach the $ directly on this level
            if (goalPos != null) {
                part = bfsSingleLevel(map, startPos, goalPos);
                if (part != null) {
                    addPartToFullPath(fullPath, part);
                    long endTime = System.nanoTime();
                    return new SearchResult(fullPath, (endTime - startTime) / 1000000000.0);
                }
            }

            // try to find the walkway to go to the next level
            if (walkwayPos != null) {
                part = bfsSingleLevel(map, startPos, walkwayPos);
                if (part != null) {
                    addPartToFullPath(fullPath, part);
                } else {
                    // couldnt reach walkway so no path exists
                    long endTime = System.nanoTime();
                    return new SearchResult(null, (endTime - startTime) / 1000000000.0);
                }
            } else {
                // no walkway and no goal found
                long endTime = System.nanoTime();
                return new SearchResult(null, (endTime - startTime) / 1000000000.0);
            }
        }

        long endTime = System.nanoTime();
        return new SearchResult(null, (endTime - startTime) / 1000000000.0);
    }

    // adds the next segment onto the full path (skips first node to avoid duplicates)
    public static void addPartToFullPath(ArrayList<Position> fullPath, ArrayList<Position> part) {
        if (fullPath.size() == 0) {
            fullPath.addAll(part);
        } else {
            for (int i = 1; i < part.size(); i++) {
                fullPath.add(part.get(i));
            }
        }
    }

    // BFS on a single level of the map
    public static ArrayList<Position> bfsSingleLevel(char[][][] map, Position start, Position target) {
        boolean[][][] visited = new boolean[map.length][map[0].length][map[0][0].length];
        Position[][][] parent = new Position[map.length][map[0].length][map[0][0].length];

        Queue<Position> queue = new LinkedList<Position>();
        queue.add(start);
        visited[start.row][start.col][start.level] = true;

        while (!queue.isEmpty()) {
            Position current = queue.remove();

            // done if we reached the target
            if (samePosition(current, target)) {
                return buildPath(parent, current);
            }

            // check all 4 directions (up down right left)
            tryAdd(map, current.row - 1, current.col, current.level, current, visited, parent, queue);
            tryAdd(map, current.row + 1, current.col, current.level, current, visited, parent, queue);
            tryAdd(map, current.row, current.col + 1, current.level, current, visited, parent, queue);
            tryAdd(map, current.row, current.col - 1, current.level, current, visited, parent, queue);
        }

        // no path found
        return null;
    }

    // tries to add a neighbor to the queue if its valid and not visited
    public static void tryAdd(char[][][] map, int row, int col, int level, Position current,
                              boolean[][][] visited, Position[][][] parent, Queue<Position> queue) {

        if (inBounds(map, row, col, level)
                && !visited[row][col][level]
                && isWalkable(map[row][col][level])) {

            visited[row][col][level] = true;
            parent[row][col][level] = current;
            queue.add(new Position(row, col, level));
        }
    }

    // walls are not walkable, everything else is
    public static boolean isWalkable(char c) {
        return c == '.' || c == '$' || c == 'W' || c == '|';
    }

    // prints path in coordinate format
    public static void printCoordinateRoute(ArrayList<Position> path) {
        for (int i = 1; i < path.size(); i++) {
            Position p = path.get(i);
            System.out.println("+ " + p.row + " " + p.col + " " + p.level);
        }
    }

    // prints the map with '+' showing where the path went
    public static void printTextRoute(char[][][] originalMap, ArrayList<Position> path) {
        char[][][] copy = copyMap(originalMap);

        // mark path on the copy (skip start and end)
        for (int i = 1; i < path.size() - 1; i++) {
            Position p = path.get(i);
            char current = copy[p.row][p.col][p.level];

            if (current == '.' || current == '|') {
                copy[p.row][p.col][p.level] = '+';
            }
        }

        // print each level
        for (int level = 0; level < copy[0][0].length; level++) {
            for (int row = 0; row < copy.length; row++) {
                for (int col = 0; col < copy[0].length; col++) {
                    System.out.print(copy[row][col][level]);
                    if (col < copy[0].length - 1) {
                        System.out.print(" ");
                    }
                }
                System.out.println();
            }

            // blank line between levels
            if (level < copy[0][0].length - 1) {
                System.out.println();
            }
        }
    }

    // makes a deep copy of the map so we dont mess up the original
    public static char[][][] copyMap(char[][][] map) {
        char[][][] copy = new char[map.length][map[0].length][map[0][0].length];

        for (int level = 0; level < map[0][0].length; level++) {
            for (int row = 0; row < map.length; row++) {
                for (int col = 0; col < map[0].length; col++) {
                    copy[row][col][level] = map[row][col][level];
                }
            }
        }

        return copy;
    }

    // searches a level for a specific character and returns its position
    public static Position findSymbol(char[][][] map, int level, char symbol) {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[0].length; col++) {
                if (map[row][col][level] == symbol) {
                    return new Position(row, col, level);
                }
            }
        }
        return null; // not found
    }

    public static boolean inBounds(char[][][] map, int row, int col, int level) {
        return row >= 0 && row < map.length
                && col >= 0 && col < map[0].length
                && level >= 0 && level < map[0][0].length;
    }

    public static boolean samePosition(Position a, Position b) {
        return a.row == b.row && a.col == b.col && a.level == b.level;
    }

    // builds the path by tracing back through parents from the end node
    public static ArrayList<Position> buildPath(Position[][][] parent, Position end) {
        ArrayList<Position> backwards = new ArrayList<Position>();
        Position current = end;

        // walk backwards through parent pointers
        while (current != null) {
            backwards.add(current);
            current = parent[current.row][current.col][current.level];
        }

        // reverse it so path goes start -> end
        ArrayList<Position> path = new ArrayList<Position>();

        for (int i = backwards.size() - 1; i >= 0; i--) {
            path.add(backwards.get(i));
        }

        return path;
    }
}