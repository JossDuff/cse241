import java.util.*; 
import java.io.*;
import java.sql.*;  //bridges java and oracle using ojdbc8.jar

public class alset {

  public static void main(String args[]) {
    Scanner in = new Scanner(System.in);  // Create a Scanner object

    // for holding username and password
    String user_name = "";
    String pwd = "";
    boolean validLogin = false;
    do {
    
      // getting username
      System.out.println("Please input your Oracle username on Edgar1:");
      user_name = in.nextLine();
    
      // getting password
      System.out.println("Please input your Oracle password on Edgar1:");
      // designed for inputting password without displaying the password:
      Console console = System.console();
      char [] pwdC = console.readPassword();
      pwd = new String(pwdC);

      try (                         
        Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", user_name, pwd);
      ){
        System.out.println("Connection successfully made.");
        System.out.println("Username and password accepted.");
        validLogin = true;
      }
      catch(SQLException e){
        System.out.println("Invalid username or password.\n");
      }
 
    } while(!validLogin);
   

    // Enter program
    
    boolean keepRunning = true;
    boolean interfaceSelect = true;
    String interfaceMenuChoice = "0";
    String custName = "";
    do {

      if(interfaceSelect){
        String interfaceMenu = "What interface would you like to access?\n[1] Customer\n[2] Service center manager\n[e] Exit program";
        String[] interfaceMenuOptions = {"1","2","e"};
        interfaceMenuChoice = handleChoice(interfaceMenu, interfaceMenuOptions);
        interfaceSelect = false;
      }

      // User is in the Customer interface
      if(interfaceMenuChoice.equals("1")){
        System.out.println("\nCUSTOMER INTERFACE");
         
        
        // QUERIES
        try (
          // insures that if an error happens in the {} portion, these resources are released automatically  
          Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", user_name, pwd);
        ) {
          
          // GETTING CUSTOMER NAME
          if(custName.equals("")){
            // QUERY
            String[] custNames = qListCust(con);         
            System.out.println("\nExisting customers:");
            for(int i = 0; i<custNames.length; i++){System.out.println(custNames[i]);}
            custName = handleChoice("Enter your name: (case sensitive)", custNames);
            System.out.println("Welcome, " + custName);
          }

          //initial options menu
          String custMenu = "What would you like to do?\n[1] View my car(s)\n[2] Buy a car\n[c] Cancel";
          String[] custMenuOptions = {"1","2","c"};
          String custMenuChoice = handleChoice(custMenu, custMenuOptions); 

          // VIEW CARS CHOICE
          if(custMenuChoice.equals("1")){
            // QUERY
            String[] custCars = qViewCars(con, custName);
            System.out.println("ID | Model | Year | Price");
            for(int i = 0; i<custCars.length; i++){System.out.println(custCars[i]);}
          }
          // BUY CAR CHOICE
          if(custMenuChoice.equals("2")){
            String[] carsForSale = qViewCarsForSale(con);
            System.out.println("Cars for sale:");
            System.out.println("ID | Model | Year | Price");
            for(int i = 0; i<carsForSale.length; i++){System.out.println(carsForSale[i]);}
            String [] carsForSaleID = qViewCarsForSaleID(con);
            String carPurchChoice = handleChoice("Enter the ID of the car you want to purchase or [c] to cancel", carsForSaleID);
            if(!carPurchChoice.equals("c")){
              qExecuteCarPurch(con, carPurchChoice, custName);
            }
          }
          if(custMenuChoice.equals("c")){
            custName = "";
            interfaceSelect = true;  
          }
        }
        catch(SQLException e){
          e.printStackTrace();
        }
      }

      //User is in the service center manager interface
      else if(interfaceMenuChoice.equals("2")){
        System.out.println("\nSERVICE CENTER MANAGER INTERFACE");

        try (
          // insures that if an error happens in the {} portion, these resources are released automatically  
          Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241", user_name, pwd);
        ) {
          String mangMenu = "What would you like to do?\n[1] View service centers\n[c] Cancel";
          String[] mangMenuOptions = {"1","c"};
          String mangMenuChoice = handleChoice(mangMenu, mangMenuOptions);
          
          if(mangMenuChoice.equals("c")){
            interfaceSelect = true;  
          }
          if(mangMenuChoice.equals("1")){
            String[] serviceCenters = qViewServiceCenters(con);
            System.out.println("ID | Models served | location");
            for(int i = 0; i<serviceCenters.length; i++){System.out.println(serviceCenters[i]);}
          }
          
        } catch(SQLException e){
          e.printStackTrace();
        }
      }

      else if(interfaceMenuChoice.equals("e")){
        keepRunning = false;
      }

      // base case
      else{
        System.out.println("\nSomething went wrong when choosing interface menu\n");
      }


  } while(keepRunning); // end of program
  
  System.out.println("\nThank you for using Alset.  Goodbye.\n");
  
  }

  
  public static String[] qViewServiceCenters(Connection con){
      ArrayList<String> scL = new ArrayList<String>();  
      try(
        Statement s=con.createStatement();
      ){
        String q;
        ResultSet result;
        q = "SELECT * FROM SERVICE_CENTER NATURAL JOIN LOCATION";
        result = s.executeQuery(q);
        if (!result.next()){
          System.out.println ("Empty result.");  // Tests if result set is empty
        }
        else {
          // Iterates through all records in the result set
          do {
            // Turns the contents into Java strings
             String M = " ";
             String U = " ";
             String S = " ";
             String K = " ";
             if((result.getString("serves_M")).equals("1")){
              M = "M";
             }

             if((result.getString("serves_U")).equals("1")){
              U = "U";
             }

             if((result.getString("serves_S")).equals("1")){
              S = "S";
             }

             if((result.getString("serves_K")).equals("1")){
              K = "K";
             }
             String modelsServed = M + U + S + K;
             scL.add(result.getString("s_ID") + "    " + modelsServed + "            Planet: " + result.getString("planet") +" Country: "+ result.getString("country") + " City: " + result.getString("city") + " Address: " + result.getString("house_number") + " " +result.getString("street"));
          } while (result.next()); // While result.next() is true there is a next record
        }
      } catch(Exception e){
        e.printStackTrace();
      }

      String[] sc = new String[scL.size()];
      sc = scL.toArray(sc);
      return sc;
  }

  
  public static boolean qExecuteCarPurch(Connection con, String carID, String custName){
   
      try(
        Statement s=con.createStatement();
      ){
        String q;
        int result;
        q = "UPDATE VEHICLE SET OWN_ID = (SELECT OWN_ID FROM OWNER WHERE NAME = '" + custName + "'), IN_SHOWROOM = NULL, IN_USED_FLEET = NULL WHERE V_ID = " + carID;
        result = s.executeUpdate(q);
      } catch(Exception e){
        e.printStackTrace();
        return false;
      }
      return true;
  }

  
  
  public static String[] qListCust(Connection con){
      ArrayList<String> namesL = new ArrayList<String>();  
      try(
        Statement s=con.createStatement();
      ){
        String q;
        ResultSet result;
        q = "SELECT NAME FROM OWNER";
        result = s.executeQuery(q);
        if (!result.next()){
          System.out.println ("Empty result.");  // Tests if result set is empty
          String[] empty = {"Empty"};
          return empty;
        }
        else {
          // Iterates through all records in the result set
          do {
            // Turns the contents into Java strings
             namesL.add(result.getString("name"));
          } while (result.next()); // While result.next() is true there is a next record
        }
      } catch(Exception e){
        e.printStackTrace();
      }

      String[] names = new String[namesL.size()];
      names = namesL.toArray(names);
      return names;
  }
  
  
  public static String[] qViewCarsForSaleID (Connection con){
      ArrayList<String> carsL = new ArrayList<String>();
      try (
        Statement s=con.createStatement();
      ){
         String q;  // Holds prepared statement
         ResultSet result;  // receives returned relationship from SQL server
         q = "SELECT V_ID FROM VEHICLE WHERE OWN_ID IS NULL";
         result = s.executeQuery(q); // Sends query string to server
      
         if (!result.next()){
           System.out.println ("There are currently no cars for sale.");  // Tests if result set is empty
         }
         else {

           // Iterates through all records in the result set
           do {
             // Turns the contents into Java strings
             carsL.add(result.getString("v_ID"));
           } while (result.next()); // While result.next() is true there is a next record
         }
     } catch(Exception e){
       e.printStackTrace();
     }
     carsL.add("c");

     // Turns into array
     String[] cars = new String[carsL.size()];
     cars = carsL.toArray(cars);
     //Prints cars

     return cars;
  }
  
  public static String[] qViewCarsForSale (Connection con){
      ArrayList<String> carsL = new ArrayList<String>();
      try (
        Statement s=con.createStatement();
      ){
         String q;  // Holds prepared statement
         ResultSet result;  // receives returned relationship from SQL server
         q = "SELECT V_ID, MODEL, YEAR, PRICE FROM VEHICLE WHERE OWN_ID IS NULL";
         result = s.executeQuery(q); // Sends query string to server
      
         if (!result.next()){
           System.out.println ("There are currently no cars for sale.");  // Tests if result set is empty
         }
         else {

           // Iterates through all records in the result set
           do {
             // Turns the contents into Java strings
             carsL.add(result.getString("v_ID")+ "    " + result.getString("model") + "       " + result.getString("year") + "   " + result.getString("price"));
           } while (result.next()); // While result.next() is true there is a next record
         }
     } catch(Exception e){
       e.printStackTrace();
     }
     // Turns into array
     String[] cars = new String[carsL.size()];
     cars = carsL.toArray(cars);
     //Prints cars

     return cars;
  }
  
  public static String[] qViewCars(Connection con, String custName){
      ArrayList<String> carsL = new ArrayList<String>();
      try (
        Statement s=con.createStatement();
      ){
         String q;  // Holds prepared statement
         ResultSet result;  // receives returned relationship from SQL server
         q = "SELECT V_ID, MODEL, YEAR, PRICE FROM VEHICLE WHERE OWN_ID = (SELECT OWN_ID FROM OWNER WHERE NAME = '" + custName + "')";
         result = s.executeQuery(q); // Sends query string to server
      
         if (!result.next()){
           System.out.println ("Empty result.");  // Tests if result set is empty
         }
         else {

           // Iterates through all records in the result set
           do {
             // Turns the contents into Java strings
             carsL.add(result.getString("v_ID")+ "    " + result.getString("model") + "       " + result.getString("year") + "   " + result.getString("price"));
           } while (result.next()); // While result.next() is true there is a next record
         }
     } catch(Exception e){
       e.printStackTrace();
     }
     // Turns into array
     String[] cars = new String[carsL.size()];
     cars = carsL.toArray(cars);
     //Prints cars

     return cars;
  }

  // This program is going to handle a lot of user input so this is a generic function to help with 
  // presenting the user their options and waiting for valid input
  public static String handleChoice(String choiceMsg, String[] options){
    Scanner in = new Scanner(System.in);  // Create a Scanner object
    String userSelection; 
    do{
      // Start by printing the message offering the user a choice
      System.out.println("\n" + choiceMsg + "\n");
      userSelection = in.nextLine();
      // Iterates through options array
      for(int i = 0; i < options.length; i++){
        // return if the user's input is the same as one of the options.  Ignores case, whitespace, and tabs.
        if((userSelection.replaceAll("\\s+","").equals(options[i].replaceAll("\\s+","")))){
          return userSelection;
        }
      }
      System.out.println("\nInvalid input.\n");
    }while(true);
  }

}

