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

public class DocumentAPITableExample {

	static AmazonDynamoDBClient client = new AmazonDynamoDBClient(new ProfileCredentialsProvider());
	static DynamoDB dynamoDB = new DynamoDB(client);

	static String tableName = "letter";

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
                .withAttributeName("Id")
                .withAttributeType("N"));
            attributeDefinitions.add(new AttributeDefinition()
            	.withAttributeName("DateTime")
            	.withAttributeType("N"));

            ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
            keySchema.add(new KeySchemaElement()
                .withAttributeName("Id")
                .withKeyType(KeyType.HASH));
            keySchema.add(new KeySchemaElement()
            	.withAttributeName("DateTime")
            	.withKeyType(KeyType.RANGE));
            
            CreateTableRequest request = new CreateTableRequest()
                .withTableName(tableName)
                .withKeySchema(keySchema)
                .withAttributeDefinitions(attributeDefinitions)
                .withProvisionedThroughput(new ProvisionedThroughput()
                    .withReadCapacityUnits(1L)
                    .withWriteCapacityUnits(1L));

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
                .withPrimaryKey("Id", 101, "DateTime" , System.currentTimeMillis() )
                .withString("Title", "Book 101 Title")
                .withString("ISBN", "111-1111111111")
                .withStringSet("Authors",
                    new HashSet<String>(Arrays.asList("Author1")))
                .withNumber("Price", 2)
                .withString("Dimensions", "8.5 x 11.0 x 0.5")
                .withNumber("PageCount", 500)
                .withBoolean("InPublication", true)
                .withString("ProductCategory", "Book");
            table.putItem(item);
            table.describe();
            
            item = new Item()
                .withPrimaryKey("Id", 102, "DateTime" , System.currentTimeMillis())
                .withString("Title", "Book 102 Title")
                .withString("ISBN", "222-2222222222")
                .withStringSet("Authors", new HashSet<String>(
                    Arrays.asList("Author1", "Author2")))
                .withNumber("Price", 20)
                .withString("Dimensions", "8.5 x 11.0 x 0.8")
                .withNumber("PageCount", 600)
                .withBoolean("InPublication", true)
                .withString("ProductCategory", "Book");
            table.putItem(item);
           


            item = new Item()
                .withPrimaryKey("Id", 103, "DateTime" , System.currentTimeMillis())
                .withString("Title", "Book 103 Title")
                .withString("ISBN", "333-3333333333")
                .withStringSet( "Authors", new HashSet<String>(
                    Arrays.asList("Author1", "Author2")))
                // Intentional. Later we'll run Scan to find price error. Find
                // items > 1000 in price.
                .withNumber("Price", 2000)
                .withString("Dimensions", "8.5 x 11.0 x 1.5")
                .withNumber("PageCount", 600)
                .withBoolean("InPublication", false)
                .withString("ProductCategory", "Book");
            table.putItem(item);


            // Add bikes.

            item = new Item()
                .withPrimaryKey("Id", 201, "DateTime" , System.currentTimeMillis())
                .withString("Title", "18-Bike-201")
                // Size, followed by some title.
                .withString("Description", "201 Description")
                .withString("BicycleType", "Road")
                .withString("Brand", "Mountain A")
                // Trek, Specialized.
                .withNumber("Price", 100)
                .withString("Gender", "M")
                // Men's
                .withStringSet("Color", new HashSet<String>(
                    Arrays.asList("Red", "Black")))
                .withString("ProductCategory", "Bicycle");
            table.putItem(item);
            table.describe();

            item = new Item()
                .withPrimaryKey("Id", 202, "DateTime" , System.currentTimeMillis())
                .withString("Title", "21-Bike-202")
                .withString("Description", "202 Description")
                .withString("BicycleType", "Road")
                .withString("Brand", "Brand-Company A")
                .withNumber("Price", 200)
                .withString("Gender", "M")
                .withStringSet("Color", new HashSet<String>(
                    Arrays.asList("Green", "Black")))
                .withString("ProductCategory", "Bicycle");
            table.putItem(item);


            item = new Item()
                .withPrimaryKey("Id", 203, "DateTime" , System.currentTimeMillis())
                .withString("Title", "19-Bike-203")
                .withString("Description", "203 Description")
                .withString("BicycleType", "Road")
                .withString("Brand", "Brand-Company B")
                .withNumber("Price", 300)
                .withString("Gender", "W")
                // Women's
                .withStringSet( "Color", new HashSet<String>(
                    Arrays.asList("Red", "Green", "Black")))
                .withString("ProductCategory", "Bicycle");
            table.putItem(item);


            item = new Item()
                .withPrimaryKey("Id", 204, "DateTime" , System.currentTimeMillis())
                .withString("Title", "18-Bike-204")
                .withString("Description", "204 Description")
                .withString("BicycleType", "Mountain")
                .withString("Brand", "Brand-Company B")
                .withNumber("Price", 400)
                .withString("Gender", "W")
                .withStringSet("Color", new HashSet<String>(
                    Arrays.asList("Red")))
                .withString("ProductCategory", "Bicycle");
            table.putItem(item);


            item = new Item()
                .withPrimaryKey("Id", 205,"DateTime" , 007 )
                .withString("Title", "20-Bike-205")
                .withString("Description", "205 Description")
                .withString("BicycleType", "Hybrid")
                .withString("Brand", "Brand-Company C")
                .withNumber("Price", 500)
                .withString("Gender", "B")
                // Boy's
                .withStringSet("Color", new HashSet<String>(
                    Arrays.asList("Red", "Black")))
                .withString("ProductCategory", "Bicycle");
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
