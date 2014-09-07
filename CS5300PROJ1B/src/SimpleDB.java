import java.util.List;

public interface SimpleDB {
	//set client name
	public void create(String domainName);
	//put value 
	public void putData(String domainName, String attr, String ItemName, String value);
	//get values by itemname
	public List<String> getValues(String domainName, String itemName);
	// get value by query
	public String getValue(String domainName, String itemName, String ColName);
	//delete domain
	public void delete(String domainName);
	//delete an item and all of its attributes on choosen domain
	public void delete(String domainName, String itemName);
	//delete all domain associated with this client
	public void deleteAll();
	//list all domains on this client
	public void printDomains();
}