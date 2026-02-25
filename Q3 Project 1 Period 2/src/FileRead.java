import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
//repush
public class FileRead {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length > 0) {
			File map = new File(args[0]); 
			String[][][] coordinates = file(map);
			
			if (coordinates != null) {
				coordinates(coordinates);
			}
		} else {
			System.out.println("No file name");
		}
	}
	public static String[][][] file(File map){
		try {
			Scanner scan = new Scanner(map);
			int w = scan.nextInt();
			int h = scan.nextInt();
			int l = scan.nextInt();
			String[][][] coordinates = new String[h][w][l];
			for(int i = 0; i < l; i++) {
				for(int j = 0; j < h; j++) {
					String a = "";
					for(int k = 0; k < w; k++) {
						coordinates[j][k][i] = scan.next();
						a += coordinates[j][k][i] + " ";
						
					}
					System.out.println(a);
				}
			}
			scan.close();
			return coordinates;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
				
	}
	
	
	
	public static ArrayList<String> coordinates(String[][][] a) {
		System.out.println();
		ArrayList<String> b = new ArrayList<String>();
		
		for(int i = 0; i < a[0][0].length; i++) {
			for(int j = 0; j < a.length; j++) {
				for(int k = 0; k < a[0].length; k++) {
					String c = a[j][k][i] + " " + j + " " + k + " " +  i;
					b.add(c);
					System.out.println(c);
				}
				
		
			}

		}
		return b;
	}
}
