import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
//playing around with git. commit 2 



//import java.text.SimpleDateFormat;
//import java.util.Calendar;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableCollection;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class DynamoTester {

	static AmazonDynamoDBClient client = new AmazonDynamoDBClient(new ProfileCredentialsProvider());
	static DynamoDB dynamoDB = new DynamoDB(client);

	static String tableName = "tester2";

    public static void main(String[] args) throws Exception {
  // Region to do DyanoDB operations
    	client.setRegion(Region.getRegion(Regions.US_WEST_2)); 
        
    	createExampleTable();
        //listMyTables();
        //getandParseTableInfoForCapacity();
        loadTableInformation();
        //updateExampleTable();
        //queryTable();
        //schemaDescribe();
        //deleteExampleTable();
        
    }

    static void createExampleTable() {

        try {

            ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
            attributeDefinitions.add(new AttributeDefinition()
                .withAttributeName("id")
                .withAttributeType("N"));


            ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
            keySchema.add(new KeySchemaElement()
                .withAttributeName("id")
                .withKeyType(KeyType.HASH));

            
            CreateTableRequest request = new CreateTableRequest()
                .withTableName(tableName)
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput()
                    .withReadCapacityUnits(20L)
                    .withWriteCapacityUnits(20L));

            System.out.println("Issuing CreateTable request for " + tableName);
            Table table = dynamoDB.createTable(request);

            System.out.println("Waiting for " + tableName
                + " to be created...this may take a while...");
            table.waitForActive();

//            getandParseTableInfoForCapacity();

        } catch (Exception e) {
            System.err.println("CreateTable request failed for " + tableName);
            System.err.println(e.getMessage());
        }

    }

    static void listMyTables() {

        TableCollection<ListTablesResult> tables = dynamoDB.listTables();
        Iterator<Table> iterator = tables.iterator();

        System.out.println("Listing table names");
        System.out.println("----------------------------------------------------------------------------------");

        while (iterator.hasNext()) {
            Table table = iterator.next();
            System.out.println(table.getTableName());
        }
        System.out.println("----------------------------------------------------------------------------------");

    }

    static void getandParseTableInfoForCapacity() {

        System.out.println("Describing " + tableName);
        System.out.println("----------------------------------------------------------------------------------");
        TableDescription tableDescription = dynamoDB.getTable(tableName).describe();
        System.out.format("Name: %s:\n" + "Status: %s \n"
                + "Provisioned Throughput (read capacity units/sec): %d \n"
                + "Provisioned Throughput (write capacity units/sec): %d \n",
        tableDescription.getTableName(), 
        tableDescription.getTableStatus(), 
        tableDescription.getProvisionedThroughput().getReadCapacityUnits(),
        tableDescription.getProvisionedThroughput().getWriteCapacityUnits());
        System.out.println("----------------------------------------------------------------------------------");

    }

    static void updateExampleTable() {

        Table table = dynamoDB.getTable(tableName);
        System.out.println("Modifying provisioned throughput for " + tableName);

        try {
            table.updateTable(new ProvisionedThroughput()
                .withReadCapacityUnits(6L).withWriteCapacityUnits(7L));

            table.waitForActive();
        } catch (Exception e) {
            System.err.println("UpdateTable request failed for " + tableName);
            System.err.println(e.getMessage());
        }
    }

    static void deleteExampleTable() {

        Table table = dynamoDB.getTable(tableName);
        try {
            System.out.println("Issuing DeleteTable request for " + tableName);
            table.delete();

            System.out.println("Waiting for " + tableName
                + " to be deleted...this may take a while...");

            table.waitForDelete();
        } catch (Exception e) {
            System.err.println("DeleteTable request failed for " + tableName);
            System.err.println(e.getMessage());
        }
    }

    static void loadTableInformation(){
    
    	Table table = dynamoDB.getTable(tableName);

        try {

            System.out.println("Adding data to " + tableName);

            Item item = new Item()
                .withPrimaryKey("id",101 )
                .withString("name", "Book 101 Title")
                .withString("address", "cooler");
            table.putItem(item);


            
            System.out.println("----------------------------------------------------------------------------------");


        } catch (Exception e) {
            System.err.println("Failed to create item in " + tableName);
            System.err.println(e.getMessage());
        }

    }
    
    static void schemaDescribe(){
    	
    	Table table = dynamoDB.getTable(tableName);
        JSONObject json = new JSONObject(table.describe());
        try {
			System.out.print( json.toString(4) );
		} catch (JSONException e) {
            System.err.println("Failed to describe table: " + tableName);
			e.printStackTrace();
		}
    }
    	
    static void queryTable(){	
    	Table table = dynamoDB.getTable(tableName);

    	GetItemSpec spec = new GetItemSpec()
        .withPrimaryKey("Id", 205,"DateTime" , 007 )
        //.withProjectionExpression("Id, Title, RelatedItems[0], Reviews.FiveStar")
        .withConsistentRead(true);
    	Item item = table.getItem(spec);
    	System.out.println(item.toJSONPretty());

       	GetItemSpec spec2 = new GetItemSpec()
        .withPrimaryKey("Id", 205,"DateTime" , 007 )
        //.withProjectionExpression("Id, Title, RelatedItems[0], Reviews.FiveStar")
        .withConsistentRead(false); //With Eventual consistency
       	
       	System.out.println("Brand is : ");
    	spec2.withProjectionExpression("Brand");
    	System.out.println(table.getItem(spec2).toJSON());
    	
       	System.out.println("First color is: ");
    	spec2.withProjectionExpression("Color[0]");
    	System.out.println(table.getItem(spec2).toJSON());

   //add more queries here 	
    	
    }
    
    }
