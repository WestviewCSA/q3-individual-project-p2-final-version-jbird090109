import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

import javax.sound.sampled.Line;

public class p1{
	
	private char[][][] map;
	
	public static void main(String[] arg) {
		boolean useQueue = false;
		boolean useStack = false;
		boolean useOpt = false;
		boolean useTime = false;
		boolean inCoord = false;
		boolean outCoord = false;
		
		
		for(int i =0;  i < arg.length -1; i++) {
			if(arg[i].equals("--Queue")) {
				useQueue = true;
			} else if(arg[i].equals("--Stack")) {
				useStack = true;
			} else if(arg[i].equals("--Opt")) {
				useOpt = true;
			} else if(arg[i].equals("--Time")) {
				useTime = true;
			} else if(arg[i].equals("--Incoordinate")) {
				inCoord = true;
			} else if(arg[i].equals("--Outcoordinate")) {
				outCoord = true;
			} else if(arg[i].equals("--Help")) {
				System.out.println("This program finds a path through a maze");
	            System.out.println("--Stack 		: use stack-based approach");
	            System.out.println("--Queue 		: use queue-based approach");
	            System.out.println("--Opt   		: use optimal path approach");
	            System.out.println("--Time  		: print runtime");
	            System.out.println("--Incoordinate  : input is coordinate format");
	            System.out.println("--Outcoordinate : output is coordinate format");
	            System.exit(0);
			}
		}
		
		int c = 0;
		if(useQueue) {
			c++;
		}
		if(useStack) {
			c++;
		}
		if(useOpt) {
			c++;
		}
		
		if(c != 1) {
			System.out.println("Error: must use exactly one of --Stack, --Queue, or --Opt");
			System.exit(-1);
		}
		
		String filename = arg[arg.length - 1];
		p1 program = new p1();

		boolean loadedMap = false;
		if(inCoord) {
		    loadedMap = program.readCord(filename);
		} else {
		    loadedMap = program.readMap(filename);
		}

		if(loadedMap) {
		    long startTime = System.nanoTime();

		    if(useQueue)      program.solveQueue(outCoord);
		    else if(useStack) program.solveStack(outCoord);
		    else if(useOpt)   program.solveOptimal(outCoord);

		    if(useTime) {
		        long endTime = System.nanoTime();
		        double seconds = (endTime - startTime) / 1000000000.0;
		        System.out.printf("Total Runtime: %.6f seconds%n", seconds);
		    }
		} else {
		    System.out.println("Failed to load map: " + filename);
		    System.exit(-1);
		}
	}
	
	public p1() {}

	public void printMap() {
		
		int levels = map[0][0].length;
		int rows = map.length;
		int cols = map[0].length;
		
		for (int level = 0; level < levels; level++) {
			for(int row = 0; row < rows; row++) {
				for(int col = 0; col < cols; col++) {	
					System.out.print(map[row][col][level]);
				}
				System.out.println();
			}
		}
	}
	
	public boolean readMap(String filename) {
		File file = new File(filename);
		try {
			Scanner s = new Scanner(file);

			int rows = s.nextInt();
			int cols = s.nextInt();
			int levels = s.nextInt();
			
			if(rows < 1 || cols < 1 || levels < 1) {
				System.out.println("File does not start with 3 positive non-zero numbers!");
				return false;
			}
			
			s.nextLine();
			map = new char[rows][cols][levels];
			for(int level = 0; level<levels; level++) {
				for(int row = 0; row<rows; row++) {
					
					String line = s.nextLine();
					
					if(line.length() < cols) {
						System.out.println("Line too short \nRow: " + row + "\nLevel: "+level);
						return false;
					}
					
					
					for(int col = 0; col<cols; col++) {
						//checking for invalid characters
						char c = line.charAt(col);
						if(c != 'W' && c != '|' && c != '$' && c != '@' && c != '.') {
							System.out.println("Invalid character: '" + c +"' \nPoint: ("+row+","+col+")\nLevel: "+level); 
							return false;
						}
						map[row][col][level]= c;
					}
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean readCord(String filename) {
		File file = new File(filename);
		try {
			Scanner s = new Scanner(file);
			int rows = s.nextInt();
			int cols = s.nextInt();
			int levels = s.nextInt();
			
			if(rows < 1 || cols < 1 || levels < 1) {
				System.out.println("Filem does not start with 3 positive non-zero numbers");;
				return false;
			}
			
			s.nextLine();
			map = new char[rows][cols][levels];
			
			// filling everything with open pathway
			for (int row = 0; row < rows; row++) {
			    for (int col = 0; col < cols; col++) {
			    	for(int level = 0; level<levels; level++) {
			    		map[row][col][level] = '.';
			    	}
			    }
			}
			while(s.hasNext()) {
				char type = s.next().charAt(0);
				int row = s.nextInt();
				int col = s.nextInt();
				int level = s.nextInt();
				
				if(type != 'W' && type != '|' && type != '$' && type != '@' && type != '.') {
					System.out.println("Invalid character: '" + type + "'\nPoint: (" + row + "," + col + ")\nLevel: " + level);
					return false;
				}
				
				if(row < 0 || row >= rows || col < 0 || col >= cols || level < 0 || level >= levels) {
					System.out.println("Coordinates out of bounds!\nPoint: (" + row + "," + col + ")\nLevel: " + level);
					return false;
				}
				map[row][col][level] = type;
			}
						
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public void solveQueue(boolean outCoord) {
		int rows   = map.length;
	    int cols   = map[0].length;
	    int levels = map[0][0].length;
	    
	    int[] dRow = {-1, 1, 0, 0};
	    int[] dCol = { 0, 0, 1,-1};
	    
	    int startRow = -1;
	    int startCol = -1;
	    int startLevel = 0;
	    boolean found = false;
	    
	    
	    for(int level = 0; level < levels; level++) {
	    	for(int row = 0; row < rows; row++) {
	    		for(int col = 0; col < cols; col++) {
	    			if(!found && map[row][col][level] == 'W') {
	    				startRow = row;
	    				startCol = col;
	    				startLevel = level;
	    				found = true;
	    			}
	    			
	    		}
	    	}
	    }
	    
	    int[][][][] sol = new int[rows][cols][levels][3];
	    
	    // use -1 as symbol for not visited yet
	    for(int r = 0; r < rows; r++) {
	    	for(int c = 0; c < cols; c++) {
	    		for(int l = 0; l < levels; l++) {
	    			sol[r][c][l][0] = -1;
	    			sol[r][c][l][1] = -1;
	    			sol[r][c][l][2] = -1;
	    		}
	    	}
	    }
	    
	    boolean[][][] visited = new boolean[rows][cols][levels];
	    
	    Queue<int[]> queue = new LinkedList<int[]>();
	    
	    visited[startRow][startCol][startLevel] = true;
	    
	    int[] start = {startRow, startCol, startLevel};
	    
	    queue.add(start);
	    
	    
	    int[] goal = null;
	    
	    while(!queue.isEmpty()) {
	    	int[] current = queue.remove();
	    	int row = current[0];
	    	int col = current[1];
	    	int level = current[2];
	    	
	    	// checking north, south, east, west
	    	
	    	for(int d = 0; d < 4; d++) {
	    		int nRow = row + dRow[d];
	    		int nCol = col + dCol[d];
	    		int nLevel = level;
	    		
	    		// checks if out of bounds
	    		if (nRow < 0 || nRow >= rows || nCol < 0 || nCol >= cols) {
	                continue;
	            }
	    		
	    		
	    		// checks visited
	    		if (visited[nRow][nCol][nLevel]) {
	                continue;
	            }
	    		
	    		
	    		if (map[nRow][nCol][nLevel] == '@') {
	                continue;
	            }
	    		
	    		if (map[nRow][nCol][nLevel] == '|') {
	    			int nextLevel = nLevel + 1;
	    		    if (nextLevel >= levels) continue;
	    		    
	    		    boolean foundW = false;
	    		    for (int r = 0; r < rows && !foundW; r++) {
	    		    	for (int c = 0; c < cols && !foundW; c++) {
	    		    		if (map[r][c][nextLevel] == 'W') {
	    		    			if (!visited[r][c][nextLevel]) {
	    		    				visited[r][c][nextLevel] = true;
	    		    				sol[r][c][nextLevel][0] = row;
	    		    				sol[r][c][nextLevel][1] = col;
	    		    				sol[r][c][nextLevel][2] = level;
	    		    				int[] v = {r, c, nextLevel};
	    		    				queue.add(v);
	    		    				}
	    		    			foundW = true;
	    		    			}
	    		    		}
	    		    	}
	    		    continue; 
	            }
	    		
	    		visited[nRow][nCol][nLevel] = true;
	    		sol[nRow][nCol][nLevel][0] = row;
	    		sol[nRow][nCol][nLevel][1] = col;
	    		sol[nRow][nCol][nLevel][2] = level;
	    		
	    		int[] v = {nRow, nCol, nLevel};
	    		queue.add(v);
	    		
	    		if (map[nRow][nCol][nLevel] == '$') {
	                goal = new int[]{nRow, nCol, nLevel};
	                break;
	            }

	    	}
	    	
	    	if (goal != null) {
	            break;
	        }
	    }
	    
	    if (goal == null) {
	    	System.out.println("The Wolverine Store is closed.");
	        return;
	    }
	    
	    //going backwards and getting path
	    int pathLength = 0;
	    int[] curr = goal;

	    while (true) {
	    	int[] p = sol[curr[0]][curr[1]][curr[2]];
	    	
	    	if (p[0] == -1) {
	            break;
	        }
	    	pathLength++;
	    	
	    	curr= p;
	    }
	    
	    //storing path 
	    int[][] path = new int[pathLength][3];
	    int index = pathLength-1;
	    
	    curr = goal;
	    
	    while(index >=0) {
	    	int[] p = sol[curr[0]][curr[1]][curr[2]];
	    	
	    	if (p[0] == -1) {
	            break;
	        }
	    	
	    	if(map[p[0]][p[1]][p[2]] != 'W') {
	    		map[p[0]][p[1]][p[2]] = '+';
	    	}
	    	
	    	path[index][0] = p[0];
	    	path[index][1] = p[1];
	    	path[index][2] = p[2];
	    	
	    	index --; 
	    	curr = p;
	    }
	    
	    if(outCoord) {
	    	printCoord(path);
	    } else {
	    	printMap();
	    }
	}
	
	
	private void printCoord(int[][] path) {
		for (int i = 0; i < path.length; i++) {
			
			if (map[path[i][0]][path[i][1]][path[i][2]] == 'W') {
	            continue;
	        }
			System.out.println("+ " + path[i][0] + " " + path[i][1] + " " + path[i][2]);
		}
		
	}

	// same code as solveQueue() but just using stack and pop/push
	public void solveStack(boolean outCoord) {
		
		
		int rows   = map.length;
	    int cols   = map[0].length;
	    int levels = map[0][0].length;
	    
	    int[] dRow = {-1, 1, 0, 0};
	    int[] dCol = { 0, 0, 1,-1};
	    
	    int startRow = -1;
	    int startCol = -1;
	    int startLevel = 0;
	    boolean found = false;
	    
	    
	    for(int level = 0; level < levels; level++) {
	    	for(int row = 0; row < rows; row++) {
	    		for(int col = 0; col < cols; col++) {
	    			if(!found && map[row][col][level] == 'W') {
	    				startRow = row;
	    				startCol = col;
	    				startLevel = level;
	    				found = true;
	    			}
	    			
	    		}
	    	}
	    }
	    
	    int[][][][] sol = new int[rows][cols][levels][3];
	    
	    // use -1 as symbol for not visited yet
	    for(int r = 0; r < rows; r++) {
	    	for(int c = 0; c < cols; c++) {
	    		for(int l = 0; l < levels; l++) {
	    			sol[r][c][l][0] = -1;
	    			sol[r][c][l][1] = -1;
	    			sol[r][c][l][2] = -1;
	    		}
	    	}
	    }
	    
	    boolean[][][] visited = new boolean[rows][cols][levels];
	    
	    Stack<int[]> stack = new Stack<int[]>();
	    
	    visited[startRow][startCol][startLevel] = true;
	    
	    int[] start = {startRow, startCol, startLevel};
	    
	    stack.push(start);
	    
	    
	    int[] goal = null;
	    
	    while(!stack.isEmpty()) {
	    	int[] current = stack.pop();
	    	int row = current[0];
	    	int col = current[1];
	    	int level = current[2];
	    	
	    	// checking north, south, east, west
	    	
	    	for(int d = 0; d < 4; d++) {
	    		int nRow = row + dRow[d];
	    		int nCol = col + dCol[d];
	    		int nLevel = level;
	    		
	    		// checks if out of bounds
	    		if (nRow < 0 || nRow >= rows || nCol < 0 || nCol >= cols) {
	                continue;
	            }
	    		
	    		
	    		// checks visited
	    		if (visited[nRow][nCol][nLevel]) {
	                continue;
	            }
	    		
	    		
	    		if (map[nRow][nCol][nLevel] == '@') {
	                continue;
	            }
	    		
	    		if (map[nRow][nCol][nLevel] == '|') {
	    			int nextLevel = nLevel + 1;
	    		    if (nextLevel >= levels) continue;
	    		    
	    		    boolean foundW = false;
	    		    for (int r = 0; r < rows && !foundW; r++) {
	    		    	for (int c = 0; c < cols && !foundW; c++) {
	    		    		if (map[r][c][nextLevel] == 'W') {
	    		    			if (!visited[r][c][nextLevel]) {
	    		    				visited[r][c][nextLevel] = true;
	    		    				sol[r][c][nextLevel][0] = row;
	    		    				sol[r][c][nextLevel][1] = col;
	    		    				sol[r][c][nextLevel][2] = level;
	    		    				int[] v = {r, c, nextLevel};
	    		    				stack.push(v);
	    		    				}
	    		    			foundW = true;
	    		    			}
	    		    		}
	    		    	}
	    		    continue; 
	            }
	    		
	    		visited[nRow][nCol][nLevel] = true;
	    		sol[nRow][nCol][nLevel][0] = row;
	    		sol[nRow][nCol][nLevel][1] = col;
	    		sol[nRow][nCol][nLevel][2] = level;
	    		
	    		int[] v = {nRow, nCol, nLevel};
	    		stack.push(v);
	    		
	    		if (map[nRow][nCol][nLevel] == '$') {
	                goal = new int[]{nRow, nCol, nLevel};
	                break;
	            }

	    	}
	    	
	    	if (goal != null) {
	            break;
	        }
	    }
	    
	    if (goal == null) {
	    	System.out.println("The Wolverine Store is closed.");
	        return;
	    }
	    
	    int pathLength = 0;
	    int[] curr = goal;

	    while (true) {
	    	int[] p = sol[curr[0]][curr[1]][curr[2]];
	    	
	    	if (p[0] == -1) {
	            break;
	        }
	    	pathLength++;
	    	
	    	curr= p;
	    }
	    
	    //storing path 
	    int[][] path = new int[pathLength][3];
	    int index = pathLength-1;
	    
	    curr = goal;
	    
	    while(index >=0) {
	    	int[] p = sol[curr[0]][curr[1]][curr[2]];
	    	
	    	if (p[0] == -1) {
	            break;
	        }
	    	
	    	if(map[p[0]][p[1]][p[2]] != 'W') {
	    		map[p[0]][p[1]][p[2]] = '+';
	    	}
	    	
	    	path[index][0] = p[0];
	    	path[index][1] = p[1];
	    	path[index][2] = p[2];
	    	
	    	index --; 
	    	curr = p;
	    }
	    
	    if(outCoord) {
	    	printCoord(path);
	    } else {
	    	printMap();
	    }
	}
	
	
	// same code as solveQueue() but it solves in less time thank solveQueue() + solveStack()
	public void solveOptimal(boolean outCoord) {
		int rows   = map.length;
	    int cols   = map[0].length;
	    int levels = map[0][0].length;
	    
	    int[] dRow = {-1, 1, 0, 0};
	    int[] dCol = { 0, 0, 1,-1};
	    
	    int startRow = -1;
	    int startCol = -1;
	    int startLevel = 0;
	    boolean found = false;
	    
	    
	    for(int level = 0; level < levels; level++) {
	    	for(int row = 0; row < rows; row++) {
	    		for(int col = 0; col < cols; col++) {
	    			if(!found && map[row][col][level] == 'W') {
	    				startRow = row;
	    				startCol = col;
	    				startLevel = level;
	    				found = true;
	    			}
	    			
	    		}
	    	}
	    }
	    
	    int[][][][] sol = new int[rows][cols][levels][3];
	    
	    // use -1 as symbol for not visited yet
	    for(int r = 0; r < rows; r++) {
	    	for(int c = 0; c < cols; c++) {
	    		for(int l = 0; l < levels; l++) {
	    			sol[r][c][l][0] = -1;
	    			sol[r][c][l][1] = -1;
	    			sol[r][c][l][2] = -1;
	    		}
	    	}
	    }
	    
	    boolean[][][] visited = new boolean[rows][cols][levels];
	    
	    Queue<int[]> queue = new LinkedList<int[]>();
	    
	    visited[startRow][startCol][startLevel] = true;
	    
	    int[] start = {startRow, startCol, startLevel};
	    
	    queue.add(start);
	    
	    
	    int[] goal = null;
	    
	    while(!queue.isEmpty()) {
	    	int[] current = queue.remove();
	    	int row = current[0];
	    	int col = current[1];
	    	int level = current[2];
	    	
	    	// checking north, south, east, west
	    	
	    	for(int d = 0; d < 4; d++) {
	    		int nRow = row + dRow[d];
	    		int nCol = col + dCol[d];
	    		int nLevel = level;
	    		
	    		// checks if out of bounds
	    		if (nRow < 0 || nRow >= rows || nCol < 0 || nCol >= cols) {
	                continue;
	            }
	    		
	    		
	    		// checks visited
	    		if (visited[nRow][nCol][nLevel]) {
	                continue;
	            }
	    		
	    		
	    		if (map[nRow][nCol][nLevel] == '@') {
	                continue;
	            }
	    		
	    		if (map[nRow][nCol][nLevel] == '|') {
	    			int nextLevel = nLevel + 1;
	    		    if (nextLevel >= levels) continue;
	    		    
	    		    boolean foundW = false;
	    		    for (int r = 0; r < rows && !foundW; r++) {
	    		    	for (int c = 0; c < cols && !foundW; c++) {
	    		    		if (map[r][c][nextLevel] == 'W') {
	    		    			if (!visited[r][c][nextLevel]) {
	    		    				visited[r][c][nextLevel] = true;
	    		    				sol[r][c][nextLevel][0] = row;
	    		    				sol[r][c][nextLevel][1] = col;
	    		    				sol[r][c][nextLevel][2] = level;
	    		    				int[] v = {r, c, nextLevel};
	    		    				queue.add(v);
	    		    				}
	    		    			foundW = true;
	    		    			}
	    		    		}
	    		    	}
	    		    continue; 
	            }
	    		
	    		visited[nRow][nCol][nLevel] = true;
	    		sol[nRow][nCol][nLevel][0] = row;
	    		sol[nRow][nCol][nLevel][1] = col;
	    		sol[nRow][nCol][nLevel][2] = level;
	    		
	    		int[] v = {nRow, nCol, nLevel};
	    		queue.add(v);
	    		
	    		if (map[nRow][nCol][nLevel] == '$') {
	                goal = new int[]{nRow, nCol, nLevel};
	                break;
	            }

	    	}
	    	
	    	if (goal != null) {
	            break;
	        }
	    }
	    
	    if (goal == null) {
	    	System.out.println("The Wolverine Store is closed.");
	        return;
	    }
	    
	    int pathLength = 0;
	    int[] curr = goal;

	    while (true) {
	    	int[] p = sol[curr[0]][curr[1]][curr[2]];
	    	
	    	if (p[0] == -1) {
	            break;
	        }
	    	pathLength++;
	    	
	    	curr= p;
	    }
	    
	    //storing path 
	    int[][] path = new int[pathLength][3];
	    int index = pathLength-1;
	    
	    curr = goal;
	    
	    while(index >=0) {
	    	int[] p = sol[curr[0]][curr[1]][curr[2]];
	    	
	    	if (p[0] == -1) {
	            break;
	        }
	    	
	    	if(map[p[0]][p[1]][p[2]] != 'W') {
	    		map[p[0]][p[1]][p[2]] = '+';
	    	}
	    	
	    	path[index][0] = p[0];
	    	path[index][1] = p[1];
	    	path[index][2] = p[2];
	    	
	    	index --; 
	    	curr = p;
	    }
	    
	    if(outCoord) {
	    	printCoord(path);
	    } else {
	    	printMap();
	    }
	}
	
}

