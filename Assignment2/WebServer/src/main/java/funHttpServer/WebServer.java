/*
Simple Web Server in Java which allows you to call 
localhost:9000/ and show you the root.html webpage from the www/root.html folder
You can also do some other simple GET requests:
1) /random shows you a random picture (well random from the set defined)
2) json shows you the response as JSON for /random instead the html page
3) /file/filename shows you the raw file (not as HTML)
4) /multiply?num1=3&num2=4 multiplies the two inputs and responses with the result
5) /github?query=users/amehlhase316/repos (or other GitHub repo owners) will lead to receiving
   JSON which will for now only be printed in the console. See the todo below

The reading of the request is done "manually", meaning no library that helps making things a 
little easier is used. This is done so you see exactly how to pars the request and 
write a response back
*/

package funHttpServer;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Map;
import java.util.LinkedHashMap;
import java.nio.charset.Charset;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

class WebServer {
    public static void main(String args[]) {
        WebServer server = new WebServer(9000);
    }

    /**
     * Main thread
     * @param port to listen on
     */
    public WebServer(int port) {
        ServerSocket server = null;
        Socket sock = null;
        InputStream in = null;
        OutputStream out = null;

        try {
            server = new ServerSocket(port);
            while (true) {
                sock = server.accept();
                out = sock.getOutputStream();
                in = sock.getInputStream();
                byte[] response = createResponse(in);
                out.write(response);
                out.flush();
                in.close();
                out.close();
                sock.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sock != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Used in the "/random" endpoint
     */
    private final static HashMap<String, String> _images = new HashMap<>() {
        {
            put("streets", "https://iili.io/JV1pSV.jpg");
            put("bread", "https://iili.io/Jj9MWG.jpg");
        }
    };

    private Random random = new Random();

    /**
     * Reads in socket stream and generates a response
     * @param inStream HTTP input stream from socket
     * @return the byte encoded HTTP response
     */
    public byte[] createResponse(InputStream inStream) {

        byte[] response = null;
        String request = null;
        BufferedReader in = null;
        StringBuilder builder = new StringBuilder();

        try {
            in = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            boolean done = false;
            while (!done) {
                String line = in.readLine();
                if (line == null || line.equals("")) done = true;
                else if (line.startsWith("GET")) {
                    int firstSpace = line.indexOf(" ");
                    int secondSpace = line.indexOf(" ", firstSpace + 1);
                    request = line.substring(firstSpace + 2, secondSpace);
                }
            }
            if (request == null) {
                response = "<html>Illegal request: no GET</html>".getBytes();
            } else {
                if (request.length() == 0) {
                    String page = new String(readFileInBytes(new File("www/root.html")));
                    page = page.replace("${links}", buildFileList());

                    builder.append("HTTP/1.1 200 OK\n");
                    builder.append("Content-Type: text/html; charset=utf-8\n");
                    builder.append("\n");
                    builder.append(page);
                }
                else if (request.equalsIgnoreCase("json")) {
                    int index = random.nextInt(_images.size());
                    String header = (String) _images.keySet().toArray()[index];
                    String url = _images.get(header);
                    builder.append("HTTP/1.1 200 OK\n");
                    builder.append("Content-Type: application/json; charset=utf-8\n");
                    builder.append("\n");
                    builder.append("{");
                    builder.append("\"header\":\"").append(header).append("\",");
                    builder.append("\"image\":\"").append(url).append("\"}");

                } else if (request.equalsIgnoreCase("random")) {
                    File file = new File("www/index.html");
                    builder.append("HTTP/1.1 200 OK\n");
                    builder.append("Content-Type: text/html; charset=utf-8\n");
                    builder.append("\n");
                    builder.append(new String(readFileInBytes(file)));
                } else if (request.contains("file/")) {
                    File file = new File(request.replace("file/", ""));
                    if (file.exists()) {
                        builder.append("HTTP/1.1 200 OK\n");
                        builder.append("Content-Type: text/html; charset=utf-8\n");
                        builder.append("\n");
                        builder.append("Would theoretically be a file but removed this part, you do not have to do anything with it for the assignment");
                    } else {
                        builder.append("HTTP/1.1 404 Not Found\n");
                        builder.append("Content-Type: text/html; charset=utf-8\n");
                        builder.append("\n");
                        builder.append("File not found: " + file);
                    }
                } else if (request.contains("multiply?")) {
                    try {
                        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
                        query_pairs = splitQuery(request.replace("multiply?", ""));

                        if (query_pairs.containsKey("num1") && query_pairs.containsKey("num2")) {

                            if (query_pairs.get("num1").matches("\\d+") && query_pairs.get("num2").matches("\\d+")) {

                                Integer num1 = Integer.parseInt(query_pairs.get("num1"));
                                Integer num2 = Integer.parseInt(query_pairs.get("num2"));

                                Integer result = num1 * num2;

                                builder.append("HTTP/1.1 200 OK\n");
                                builder.append("Content-Type: text/html; charset=utf-8\n");
                                builder.append("\n");
                                builder.append("Result is: " + result);
                            } else {
                                builder.append("HTTP/1.1 400 Bad Request\n");
                                builder.append("Content-Type: text/html; charset=utf-8\n");
                                builder.append("\n");
                                builder.append("Error: One or both of the inputs provided are not numbers");
                            }
                        } else {
                            builder.append("HTTP/1.1 400 Bad Request\n");
                            builder.append("Content-Type: text/html; charset=utf-8\n");
                            builder.append("\n");
                            builder.append("Error: Two numbers needed for multiplication. Make sure both 'num1' and 'num2' parameters are provided");
                        }
                    } catch (Exception e) {
                        builder.append("HTTP/1.1 500 Internal Server Error\n");
                        builder.append("Content-Type: text/html; charset=utf-8\n");
                        builder.append("\n");
                        builder.append("Error: " + e.getMessage());
                    }
                } else if (request.contains("github?")) {
                    Map<String, String> query_pairs = new LinkedHashMap<String, String>();
                    query_pairs = splitQuery(request.replace("github?", ""));
                    String json = fetchURL("https://api.github.com/" + query_pairs.get("query"));

                    builder.append("HTTP/1.1 200 OK\n");
                    builder.append("Content-Type: text/html; charset=utf-8\n");
                    builder.append("\n");
                    builder.append("Check the todos mentioned in the Java source file");

                    JSONArray jsonArray = new JSONArray(json);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String fullName = obj.getString("full_name");
                        int id = obj.getInt("id");
                        JSONObject ownerObj = obj.getJSONObject("owner");
                        String ownerLogin = ownerObj.getString("login");

                        builder.append("<div>Full Name: ").append(fullName).append("<br>ID: ").append(id).append("<br>Owner: ").append(ownerLogin).append("</div><hr>");
                    }
                }
                else if (request.contains("add?")) {
                    try {
                        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
                        query_pairs = splitQuery(request.replace("add?", ""));
                        if (query_pairs.containsKey("num1") && query_pairs.containsKey("num2")) {
                            if (query_pairs.get("num1").matches("\\d+") && query_pairs.get("num2").matches("\\d+")) {
                                Integer num1 = Integer.parseInt(query_pairs.get("num1"));
                                Integer num2 = Integer.parseInt(query_pairs.get("num2"));
                                Integer result = num1 + num2;

                                builder.append("HTTP/1.1 200 OK\n");
                                builder.append("Content-Type: text/html; charset=utf-8\n");
                                builder.append("\n");
                                builder.append("Result is: " + result);
                            } else {
                                builder.append("HTTP/1.1 400 Bad Request\n");
                                builder.append("Content-Type: text/html; charset=utf-8\n");
                                builder.append("\n");
                                builder.append("Error: One or both of the inputs provided are not numbers");
                            }
                        } else {
                            builder.append("HTTP/1.1 400 Bad Request\n");
                            builder.append("Content-Type: text/html; charset=utf-8\n");
                            builder.append("\n");
                            builder.append("Error: Two numbers needed for addition. Make sure both 'num1' and 'num2' parameters are provided");
                        }
                    } catch (Exception e) {
                        builder.append("HTTP/1.1 500 Internal Server Error\n");
                        builder.append("Content-Type: text/html; charset=utf-8\n");
                        builder.append("\n");
                        builder.append("Error: " + e.getMessage());
                    }
                } else if (request.contains("reverse?")) {
                    try {
                        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
                        query_pairs = splitQuery(request.replace("reverse?", ""));
                        if (query_pairs.containsKey("string")) {
                            String reverseString = new StringBuilder(query_pairs.get("string")).reverse().toString();
                            builder.append("HTTP/1.1 200 OK\n");
                            builder.append("Content-Type: text/html; charset=utf-8\n");
                            builder.append("\n");
                            builder.append("Reversed String is: " + reverseString);
                        } else {
                            builder.append("HTTP/1.1 400 Bad Request\n");
                            builder.append("Content-Type: text/html; charset=utf-8\n");
                            builder.append("\n");
                            builder.append("Error: A string is needed for reversing. Make sure 'string' parameter is provided");
                        }
                    } catch (Exception e) {
                        builder.append("HTTP/1.1 500 Internal Server Error\n");
                        builder.append("Content-Type: text/html; charset=utf-8\n");
                        builder.append("\n");
                        builder.append("Error: " + e.getMessage());
                    }
                } else {
                    builder.append("HTTP/1.1 400 Bad Request\n");
                    builder.append("Content-Type: text/html; charset=utf-8\n");
                    builder.append("\n");
                    builder.append("I am not sure what you want me to do...");
                }
                response = builder.toString().getBytes();
            }
        } catch (IOException e) {
            response = ("<html>ERROR: " + e.getMessage() + "</html>").getBytes();
        } catch (JSONException e) {
            builder.append("HTTP/1.1 400 Bad Request\n");
            builder.append("Content-Type: text/html; charset=utf-8\n");
            builder.append("\n");
            builder.append("Malformed request or data: ").append(request);
        }
        return response;
    }
    /**
     * Method to read in a query and split it up correctly
     * @param query parameters on path
     * @return Map of all parameters and their specific values
     * @throws UnsupportedEncodingException If the URLs aren't encoded with UTF-8
     */
    public static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        // "q=hello+world%2Fme&bob=5"
        String[] pairs = query.split("&");
        // ["q=hello+world%2Fme", "bob=5"]
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                    URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        // {{"q", "hello world/me"}, {"bob","5"}}
        return query_pairs;
    }

    /**
     * Builds an HTML file list from the www directory
     * @return HTML string output of file list
     */
    public static String buildFileList() {
        ArrayList<String> filenames = new ArrayList<>();

        // Creating a File object for directory
        File directoryPath = new File("www/");
        filenames.addAll(Arrays.asList(directoryPath.list()));

        if (filenames.size() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("<ul>\n");
            for (var filename : filenames) {
                builder.append("<li>" + filename + "</li>");
            }
            builder.append("</ul>\n");
            return builder.toString();
        } else {
            return "No files in directory";
        }
    }

    /**
     * Read bytes from a file and return them in the byte array. We read in blocks
     * of 512 bytes for efficiency.
     */
    public static byte[] readFileInBytes(File f) throws IOException {

        FileInputStream file = new FileInputStream(f);
        ByteArrayOutputStream data = new ByteArrayOutputStream(file.available());

        byte buffer[] = new byte[512];
        int numRead = file.read(buffer);
        while (numRead > 0) {
            data.write(buffer, 0, numRead);
            numRead = file.read(buffer);
        }
        file.close();

        byte[] result = data.toByteArray();
        data.close();

        return result;
    }

    /**
     *
     * a method to make a web request. Note that this method will block execution
     * for up to 20 seconds while the request is being satisfied. Better to use a
     * non-blocking request.
     *
     * @param aUrl the String indicating the query url for the OMDb api search
     * @return the String result of the http request.
     *
     **/
    public String fetchURL(String aUrl) {
        StringBuilder sb = new StringBuilder();
        URLConnection conn = null;
        InputStreamReader in = null;
        try {
            URL url = new URL(aUrl);
            conn = url.openConnection();
            if (conn != null)
                conn.setReadTimeout(20 * 1000); // timeout in 20 seconds
            if (conn != null && conn.getInputStream() != null) {
                in = new InputStreamReader(conn.getInputStream(), Charset.defaultCharset());
                BufferedReader br = new BufferedReader(in);
                if (br != null) {
                    int ch;
                    // read the next character until end of reader
                    while ((ch = br.read()) != -1) {
                        sb.append((char) ch);
                    }
                    br.close();
                }
            }
            in.close();
        } catch (Exception ex) {
            System.out.println("Exception in url request:" + ex.getMessage());
        }
        return sb.toString();
    }
}