import org.json.JSONArray;
import org.json.JSONObject;
import java.net.*;
import java.io.*;
import java.util.Scanner;

/**
 */
class SockClient {
  static Socket sock = null;
  static String host = "3.134.104.34";
  static int port = 8888;
  static OutputStream out;
  // Using and Object Stream here and a Data Stream as return. Could both be the same type I just wanted
  // to show the difference. Do not change these types.
  static ObjectOutputStream os;
  static DataInputStream in;
  public static void main (String args[]) {

    if (args.length != 2) {
      System.out.println("Expected arguments: <host(String)> <port(int)>");
      System.exit(1);
    }

    try {
      host = args[0];
      port = Integer.parseInt(args[1]);
    } catch (NumberFormatException nfe) {
      System.out.println("[Port|sleepDelay] must be an integer");
      System.exit(2);
    }


    try {
      connect(host, port); // connecting to server
      System.out.println("Client connected to server.");
      boolean requesting = true;
      while (requesting) {
        System.out.println("What would you like to do: 1 - echo, 2 - add, 3 - addmany, 4 - movies, (0 to quit)");
        Scanner scanner = new Scanner(System.in);
        int choice = Integer.parseInt(scanner.nextLine());
        JSONObject json = new JSONObject();
        switch (choice) {
          case 0:
            System.out.println("Choose quit. Thank you for using our services. Goodbye!");
            requesting = false;
            break;
          case 1:
            System.out.println("Choose echo, which String do you want to send?");
            String message = scanner.nextLine();
            json.put("type", "echo");
            json.put("data", message);
            break;
          case 2:
            System.out.println("Choose add, enter first number:");
            String num1 = scanner.nextLine();
            json.put("type", "add");
            json.put("num1", num1);
            System.out.println("Enter second number:");
            String num2 = scanner.nextLine();
            json.put("num2", num2);
            break;
          case 3:
            System.out.println("Choose addmany, enter as many numbers as you like, when done choose 0:");
            JSONArray array = new JSONArray();
            String num = "1";
            while (!num.equals("0")) {
              num = scanner.nextLine();
              array.put(num);
              System.out.println("Got your " + num);
            }
            json.put("type", "addmany");
            json.put("nums", array);
            break;
          case 4:
            System.out.println("Choose movie rating task, 1 - add a movie, 2 - view movie ratings, 3 - rate a movie");
            int movieTaskChoice = Integer.parseInt(scanner.nextLine());
            switch (movieTaskChoice) {
              case 1:
                System.out.println("Enter movie name:");
                String movieName = scanner.nextLine();
                System.out.println("Enter your rating (1-5):");
                int rating = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter your username:");
                String username = scanner.nextLine();
                json.put("type", "rating");
                json.put("task", "add");
                json.put("movie", movieName);
                json.put("rating", rating);
                json.put("username", username);
                break;
              case 2:
                System.out.println("Enter movie name (leave blank for all movies):");
                String movieToView = scanner.nextLine();
                json.put("type", "rating");
                json.put("task", "view");
                if (!movieToView.isBlank()) {
                  json.put("movie", movieToView);
                }
                break;
              case 3:
                System.out.println("Enter movie name:");
                String movieToRate = scanner.nextLine();
                System.out.println("Enter your rating (1-5):");
                int ratingToGive = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter your username:");
                String rater = scanner.nextLine();
                json.put("type", "rating");
                json.put("task", "rate");
                json.put("movie", movieToRate);
                json.put("rating", ratingToGive);
                json.put("username", rater);
                break;
              default:
                System.out.println("Invalid command! Please try again.");
                break;
            }
            break;
        }
        if (!requesting) {
          continue;
        }

        // write the whole message
        os.writeObject(json.toString());
        // make sure it wrote and doesn't get cached in a buffer
        os.flush();

        // TODO: handle the response
        // - not doing anything other than printing payload
        // !! you will most likely need to parse the response for the other services so this works.
        // handle the response
        String i = (String) in.readUTF();
        JSONObject res = new JSONObject(i);
        System.out.println("Got response: " + res);
        if (res.getBoolean("ok")) {
          if (res.has("type")){
          String type = res.getString("type");
          switch (type) {
            case "echo":
              System.out.println(res.getString("echo"));
              break;
            case "add":
            case "addmany":
              System.out.println("The result is " + res.getInt("result"));
              break;
            case "rating":
              String task = res.getString("task");
              if (task.equals("add") || task.equals("rate")) {
                System.out.println(res.getString("message"));
              } else if (res.has("movies")) {
                JSONArray movies = res.getJSONArray("movies");
                for (int index = 0; index < movies.length(); index++) {
                  JSONObject movie = movies.getJSONObject(index);
                  System.out.println("Movie: " + movie.getString("movie"));
                  System.out.println("Rating: " + movie.getInt("rating"));
                  System.out.println("Raters: " + movie.getJSONArray("raters").toString());
                }
              }
              break;
            default:
              System.out.println("Unknown type '" + type + "'.");
              break;
          }
          } else {
            System.out.println("Operation successful.");
          }
        } else {
          System.out.println("Error: " + res.getString("message"));
        }
      }
      // want to keep requesting services so don't close connection
      //overandout();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void overandout() throws IOException {
    //closing things, could
    in.close();
    os.close();
    sock.close(); // close socked after sending
  }

  public static void connect(String host, int port) throws IOException {
    // open the connection
    sock = new Socket(host, port); // connect to host and socket on port 8888

    // get output channel
    out = sock.getOutputStream();

    // create an object output writer (Java only)
    os = new ObjectOutputStream(out);

    in = new DataInputStream(sock.getInputStream());
  }
}