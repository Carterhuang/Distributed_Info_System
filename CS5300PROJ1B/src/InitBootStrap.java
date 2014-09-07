/***
 * 
 * @author xiaoanyan
 * initialize bootstrap
 */
public class InitBootStrap {

	public static void main (String[] args) {
		
		MySimpleDB DB = new MySimpleDB();
		//System.out.println(1 + " " + DB.getValue("bootStrap", "basicView", "Col"));

		//reset
		DB.delete("bootStrap");
		//create
		DB.create("bootStrap");
		System.out.println("Creating Domain... ");
		DB.printDomains();
		//add data
		DB.putData("bootStrap", "Col", "basicView", "0.0.0.0");
		System.out.println("Adding Dummy IP: ");	
		System.out.println(2 + " " + DB.getValue("bootStrap", "basicView", "Col"));

	}
}