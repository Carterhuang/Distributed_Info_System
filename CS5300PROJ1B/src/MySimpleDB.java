
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;

/**
 * 
 * @author xiaoanyan
 * Implement SimpleDB client side
 * Initialize Bootstrap view every time creating a table
 *
 */
public class MySimpleDB implements SimpleDB{
	private String AWS_ACCESS_KEY;
	private String AWS_SECRET_KEY;
	private BasicAWSCredentials basicAWSCredentials;
	private AmazonSimpleDBClient client;
	private Properties prop = new Properties();

	
	//constructor
	public MySimpleDB() {
		try {
			
			prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("AwsCredentials.properties"));} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.AWS_ACCESS_KEY = prop.getProperty("accessKey");
		this.AWS_SECRET_KEY = prop.getProperty("secretKey");
		this.basicAWSCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);
		this.client = new AmazonSimpleDBClient(basicAWSCredentials);
	}

	@Override
	public void create(String domainName) {
		boolean found = false;
		String tableName = domainName;
		
		// check existing tables, to see if tableName already exists
		ListDomainsResult response = client.listDomains();
		for (String domain:response.getDomainNames()) {
			if (domain.equals(tableName)) {
				found = true;
			}
		}
		
		//if not found, create domain
		if (!found) {
			client.createDomain(new CreateDomainRequest(tableName));
		} else {
			System.out.println(domainName + " already exit");
		}
	}

	@Override
	public void putData(String domainName, String attr, String itemName,
			String value) {
		ReplaceableAttribute replaceAttribute1 = 
				new ReplaceableAttribute(attr, value, true);
		List<ReplaceableAttribute> listReplaceAttribute = new ArrayList<ReplaceableAttribute> ();
		listReplaceAttribute.add(replaceAttribute1);
		client.putAttributes(new PutAttributesRequest(domainName, itemName, listReplaceAttribute));
	}

	@Override
	public  List<String>getValues(String domainName, String itemName) {
		List<String> values = new ArrayList<String>();
		GetAttributesResult result = client.getAttributes(new GetAttributesRequest(domainName, itemName));
		for (Attribute attribute:result.getAttributes()) {
			values.add(attribute.getValue());
		}
		return values;
	}
	
	//TOBE TESTED
	@Override
	public String getValue(String domainName, String itemName, String ColName) {
		String value = "";
		GetAttributesResult result = client.getAttributes(new GetAttributesRequest(domainName, itemName));
		for (Attribute attribute:result.getAttributes()) {
			if (ColName.equals(attribute.getName())) {
				value = attribute.getValue();
				return value;
			}
		}
		return value;
	}
	

	@Override
	public void delete(String domainName) {
		System.out.println("Deleting " + domainName + " domain.\n");
		client.deleteDomain(new DeleteDomainRequest(domainName));
	}

	@Override
	public void delete(String domainName, String itemName) {
		System.out.println("delete item " + itemName);
		client.deleteAttributes(new DeleteAttributesRequest(domainName, itemName));		
	}

	@Override
	public void printDomains() {
		System.out.println("List all domains in your account:");
		for (String domainName:client.listDomains().getDomainNames()){
			System.out.println(" " + domainName);
		}		
	}

	@Override
	public void deleteAll() {
		System.out.println("Deleting all Domains");
		for (String domainName:client.listDomains().getDomainNames()){
			client.deleteDomain(new DeleteDomainRequest(domainName));
		}
	}



}