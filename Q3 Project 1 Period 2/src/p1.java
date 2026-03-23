import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Scanner;

public class p1 {

    private static final char START = 'W';
    private static final char GOAL = '$';
    private static final char WALL = '@';
    private static final char OPEN = '.';
    private static final char WALKWAY = '|';
    private static final char PATH = '+';

    private static class Pos {
        int level;
        int row;
        int col;

        Pos(int level, int row, int col) {
            this.level = level;
            this.row = row;
            this.col = col;
        }
    }

    private static class Config {
        boolean useQueue = false;
        boolean useStack = false;
        boolean useOpt = false;
        boolean printTime = false;
        boolean inCoordinate = false;
        boolean outCoordinate = false;
        String fileName = null;
    }

    public static void main(String[] args) {
        Config config;
        try {
            config = parseArgs(args);
        } catch (IllegalCommandLineInputsException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
            return;
        }

        char[][][] map;
        try {
            map = readMap(config.fileName, config.inCoordinate);
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found.");
            System.exit(-1);
            return;
        } catch (IllegalMapCharacterException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
            return;
        } catch (IncompleteMapException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
            return;
        } catch (IncorrectMapFormatException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
            return;
        }

        Pos start = findFirst(map, START);
        Pos goal = findFirst(map, GOAL);

        if (start == null || goal == null) {
            System.out.println("Map must contain both W and $.");
            System.exit(-1);
            return;
        }

        // time only the search, not reading input or printing output
        long startTime = System.nanoTime();

        List<Pos> path = search(map, start, goal, config.useQueue, config.useOpt);

        long endTime = System.nanoTime();
        double seconds = (endTime - startTime) / 1_000_000_000.0;

        if (path == null) {
            System.out.println("The Wolverine Store is closed.");
        } else if (config.outCoordinate) {
            printCoordinates(path);
        } else {
            drawPath(map, path);
            printMap(map);
        }

        if (config.printTime) {
            System.out.println("Total Runtime: " + seconds + " seconds");
        }
    }

    private static Config parseArgs(String[] args) throws IllegalCommandLineInputsException {
        Config config = new Config();

        for (String arg : args) {
            if ("--Queue".equals(arg)) {
                config.useQueue = true;
            } else if ("--Stack".equals(arg)) {
                config.useStack = true;
            } else if ("--Opt".equals(arg)) {
                config.useOpt = true;
            } else if ("--Time".equals(arg)) {
                config.printTime = true;
            } else if ("--Incoordinate".equals(arg)) {
                config.inCoordinate = true;
            } else if ("--Outcoordinate".equals(arg)) {
                config.outCoordinate = true;
            } else if ("--Outmap".equals(arg)) {
                config.outCoordinate = false;
            } else if ("--Help".equals(arg)) {
                printHelp();
                System.exit(0);
            } else if (arg.startsWith("--")) {
                throw new IllegalCommandLineInputsException("Invalid command line option.");
            } else {
                if (config.fileName != null) {
                    throw new IllegalCommandLineInputsException("Too many input file names.");
                }
                config.fileName = arg;
            }
        }

        // exactly one of --Queue, --Stack, --Opt must be set
        int modeCount = (config.useQueue ? 1 : 0) + (config.useStack ? 1 : 0) + (config.useOpt ? 1 : 0);
        if (modeCount != 1) {
            throw new IllegalCommandLineInputsException(
                    "Exactly one of --Queue, --Stack, or --Opt must be provided.");
        }

        return config;
    }

    private static void printHelp() {
        System.out.println("Wolverine's Quest - Maze Solver");
        System.out.println("Usage: java p1 [options] [inputfile]");
        System.out.println("  --Queue          Use queue-based search");
        System.out.println("  --Stack          Use stack-based search");
        System.out.println("  --Opt            Find the shortest path");
        System.out.println("  --Time           Print total search runtime");
        System.out.println("  --Incoordinate   Input is coordinate-based format");
        System.out.println("  --Outcoordinate  Output path as coordinates");
        System.out.println("  --Help           Show this message and exit");
    }

    private static char[][][] readMap(String fileName, boolean inCoordinate)
            throws FileNotFoundException, IllegalMapCharacterException,
            IncompleteMapException, IncorrectMapFormatException {
        Scanner sc;

        if (fileName == null) {
            sc = new Scanner(System.in);
        } else {
            sc = new Scanner(new File(fileName));
        }

        if (!sc.hasNextInt()) {
            sc.close();
            throw new IncorrectMapFormatException("Invalid map header.");
        }
        int rows = sc.nextInt();

        if (!sc.hasNextInt()) {
            sc.close();
            throw new IncorrectMapFormatException("Invalid map header.");
        }
        int cols = sc.nextInt();

        if (!sc.hasNextInt()) {
            sc.close();
            throw new IncorrectMapFormatException("Invalid map header.");
        }
        int levels = sc.nextInt();

        if (rows <= 0 || cols <= 0 || levels <= 0) {
            sc.close();
            throw new IncorrectMapFormatException("Map dimensions must be positive.");
        }

        List<String> tokens = new ArrayList<>();
        while (sc.hasNext()) {
            tokens.add(sc.next());
        }
        sc.close();

        if (inCoordinate) {
            return readCoordinateFormat(tokens, rows, cols, levels);
        } else {
            return readGridFormat(tokens, rows, cols, levels);
        }
    }

    private static char[][][] readGridFormat(List<String> tokens, int rows, int cols, int levels)
            throws IllegalMapCharacterException, IncompleteMapException {
        int expected = rows * cols * levels;
        if (tokens.size() < expected) {
            throw new IncompleteMapException("Incomplete map: expected " + expected
                    + " cells, found " + tokens.size() + ".");
        }

        char[][][] map = new char[levels][rows][cols];
        int index = 0;

        for (int l = 0; l < levels; l++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    map[l][r][c] = parseCell(tokens.get(index++));
                }
            }
        }

        return map;
    }

    private static char[][][] readCoordinateFormat(List<String> tokens, int rows, int cols, int levels)
            throws IllegalMapCharacterException, IncompleteMapException, IncorrectMapFormatException {
        char[][][] map = new char[levels][rows][cols];

        for (int l = 0; l < levels; l++) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    map[l][r][c] = OPEN;
                }
            }
        }

        int entries = rows * cols * levels;
        int expectedTokens = entries * 4;

        if (tokens.size() < expectedTokens) {
            throw new IncompleteMapException("Incomplete coordinate map: expected "
                    + expectedTokens + " tokens, found " + tokens.size() + ".");
        }

        int index = 0;
        for (int i = 0; i < entries; i++) {
            char value = parseCell(tokens.get(index++));
            int row = parseInt(tokens.get(index++), "Invalid coordinate format.");
            int col = parseInt(tokens.get(index++), "Invalid coordinate format.");
            int level = parseInt(tokens.get(index++), "Invalid coordinate format.");

            if (level < 0 || level >= levels || row < 0 || row >= rows || col < 0 || col >= cols) {
                throw new IncorrectMapFormatException("Coordinate out of bounds.");
            }

            map[level][row][col] = value;
        }

        return map;
    }

    private static int parseInt(String s, String errorMessage) throws IncorrectMapFormatException {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IncorrectMapFormatException(errorMessage);
        }
    }

    private static char parseCell(String token) throws IllegalMapCharacterException {
        if (token.length() != 1) {
            throw new IllegalMapCharacterException("Illegal map character.");
        }

        char ch = token.charAt(0);
        if (ch != START && ch != GOAL && ch != WALL && ch != OPEN && ch != WALKWAY) {
            throw new IllegalMapCharacterException("Illegal map character.");
        }

        return ch;
    }

    private static Pos findFirst(char[][][] map, char target) {
        for (int l = 0; l < map.length; l++) {
            for (int r = 0; r < map[l].length; r++) {
                for (int c = 0; c < map[l][r].length; c++) {
                    if (map[l][r][c] == target) {
                        return new Pos(l, r, c);
                    }
                }
            }
        }
        return null;
    }

    private static List<Pos> search(char[][][] map, Pos start, Pos goal,
            boolean useQueue, boolean useOpt) {
        int levels = map.length;
        int rows = map[0].length;
        int cols = map[0][0].length;

        boolean[][][] visited = new boolean[levels][rows][cols];
        Pos[][][] parent = new Pos[levels][rows][cols];
        Deque<Pos> frontier = new ArrayDeque<>();

        frontier.addLast(start);
        visited[start.level][start.row][start.col] = true;

        while (!frontier.isEmpty()) {
            // queue uses removeFirst (BFS), stack uses removeLast (DFS)
            Pos cur = (useQueue || useOpt) ? frontier.removeFirst() : frontier.removeLast();

            if (samePos(cur, goal)) {
                return buildPath(parent, cur);
            }

            tryAdd(map, visited, parent, frontier, cur, cur.level, cur.row - 1, cur.col);
            tryAdd(map, visited, parent, frontier, cur, cur.level, cur.row + 1, cur.col);
            tryAdd(map, visited, parent, frontier, cur, cur.level, cur.row, cur.col - 1);
            tryAdd(map, visited, parent, frontier, cur, cur.level, cur.row, cur.col + 1);

            // walkway connects to the same row/col on every other level
            if (map[cur.level][cur.row][cur.col] == WALKWAY) {
                addWalkwayTransitions(map, visited, parent, frontier, cur);
            }
        }

        return null;
    }

    private static void addWalkwayTransitions(char[][][] map, boolean[][][] visited,
            Pos[][][] parent, Deque<Pos> frontier, Pos cur) {
        for (int otherLevel = 0; otherLevel < map.length; otherLevel++) {
            if (otherLevel == cur.level) {
                continue;
            }

            if (map[otherLevel][cur.row][cur.col] == WALL) {
                continue;
            }

            if (!visited[otherLevel][cur.row][cur.col]) {
                visited[otherLevel][cur.row][cur.col] = true;
                parent[otherLevel][cur.row][cur.col] = cur;
                frontier.addLast(new Pos(otherLevel, cur.row, cur.col));
            }
        }
    }

    private static void tryAdd(char[][][] map, boolean[][][] visited, Pos[][][] parent,
            Deque<Pos> frontier, Pos cur, int level, int row, int col) {
        if (level < 0 || level >= map.length) {
            return;
        }
        if (row < 0 || row >= map[0].length) {
            return;
        }
        if (col < 0 || col >= map[0][0].length) {
            return;
        }

        if (map[level][row][col] == WALL) {
            return;
        }

        if (!visited[level][row][col]) {
            visited[level][row][col] = true;
            parent[level][row][col] = cur;
            frontier.addLast(new Pos(level, row, col));
        }
    }

    private static boolean samePos(Pos a, Pos b) {
        return a.level == b.level && a.row == b.row && a.col == b.col;
    }

    private static List<Pos> buildPath(Pos[][][] parent, Pos end) {
        List<Pos> path = new ArrayList<>();
        Pos cur = end;

        while (cur != null) {
            path.add(cur);
            cur = parent[cur.level][cur.row][cur.col];
        }

        Collections.reverse(path);
        return path;
    }

    private static void drawPath(char[][][] map, List<Pos> path) {
        for (int i = 1; i < path.size() - 1; i++) {
            Pos p = path.get(i);
            char ch = map[p.level][p.row][p.col];
            if (ch == OPEN || ch == WALKWAY) {
                map[p.level][p.row][p.col] = PATH;
            }
        }
    }

    private static void printMap(char[][][] map) {
        for (int l = 0; l < map.length; l++) {
            for (int r = 0; r < map[l].length; r++) {
                for (int c = 0; c < map[l][r].length; c++) {
                    if (c > 0) {
                        System.out.print(" ");
                    }
                    System.out.print(map[l][r][c]);
                }
                System.out.println();
            }

            if (l < map.length - 1) {
                System.out.println();
            }
        }
    }

    // prints the path as coordinates: + row col level
    private static void printCoordinates(List<Pos> path) {
        for (Pos p : path) {
            System.out.println("+ " + p.row + " " + p.col + " " + p.level);
        }
    }
}
