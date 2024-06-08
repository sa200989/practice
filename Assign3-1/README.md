##### Author: Instructor team SE, ASU Polytechnic, CIDSE, SE


##### Purpose
This program shows a very simple client server implementation. The server
has 3 services, echo, add, addmany. Basic error handling on the server side
is implemented. Client does not have error handling and only has hard coded
calls to the server.

* Please run `gradle Server` and `gradle Client` together.
* Program runs on localhost
* Port is hard coded

## Protocol: ##

### Echo: ###

Request: 

    {
        "type" : "echo", -- type of request
        "data" : <String>  -- String to be echoed 
    }

General response:

    {
        "type" : "echo", -- echoes the initial response
        "ok" : <bool>, -- true or false depending on request
        "echo" : <String>,  -- echoed String if ok true
        "message" : <String>,  -- error message if ok false
    }

Success response:

    {
        "type" : "echo",
        "ok" : true,
        "echo" : <String> -- the echoed string
    }

Error response:

    {
        "type" : "echo",
        "ok" : false,
        "message" : <String> -- what went wrong
    }

### Add: ### 
Request:

    {
        "type" : "add",
        "num1" : <String>, -- first number -- String needs to be an int number e.g. "3"
        "num2" : <String> -- second number -- String needs to be an int number e.g. "4" 
    }

General response

    {
        "type" : "add", -- echoes the initial request
        "ok" : <bool>, -- true or false depending on request
        "result" : <int>,  -- result if ok true
        "message" : <String>,  -- error message if ok false
    }

Success response:

    {
        "type" : "add",
        "ok" : true,
        "result" : <int> -- the result of add
    }

Error response:

    {
        "type" : "add",
        "ok" : false,
        "message" : <String> - error message about what went wrong
    }

    See general error responses at the end of the document for the error messages. 

### AddMany: ###
Another request, this one does not just get two numbers but gets an array of numbers.

Request:

    {
        "type" : "addmany",
        "nums" : [<String>], -- json array of ints but given as Strings, e.g. ["1", "2"]
    }

General response

    {
        "type" : "addmany", -- echoes the initial request
        "ok" : <bool>, -- true or false depending on request
        "result" : <int>,  -- result if ok true
        "message" : <String>,  -- error message if ok false
    }

Success response:

    {
        "type" : "addmany",
        "ok" : true,
        "result" : <int> -- the result of adding
    }

Error response:

    {
        "type" : "addmany",
        "ok" : false,
        "message" : <String> - error message about what went wrong
    }

    Specific error messages:
        - "message" : "Only one value given"

    See general error responses at the end of the document for the error messages. 

## Movie Ratings:
This simple service allows a user to add a new movie to the server with a rating, view the ratings of movies or to add a rating to an existing movie.

Every time a movie is rated, its rating is updated with the overall average rating.

You can either store the movies and ratings on the server as variable (thus not persistant) or you can make it persistent storing to file. Both will be accepted, the persistent one is of course the better option.


Request to add a new movie:

    {
        "type" : "rating",
        "task" : "add",
        "movie" : <String> // The name of the movie
        "rating" : <int>,  // A rating 1-5
        "username" : <String>  // Who is adding the movie and rating
    }


Request to view all movie ratings:

    {
        "type" : "rating",
        "task" : "view"
    }

Request to view ratings for one movie:

    {
        "type" : "rating",
        "task" : "view",
        "movie" : <String>
    }


Request to rate a movie:

    {
        "type" : "rating",
        "task" : "rate",
        "movie" : <String>,  // The movie they want to rate
        "rating" : <int>,  // A rating 1-5
        "username" : <String> // Who is rating the movie
    }


General response:

    {
        "type" : "rating",
        "ok" : <bool>,
        "movies" : [{"movie" : <String>, "rating" : <int>, "raters" : [<String>]}]  // The list of movies
        "message" : <String>  // For ok message, if error use error response
    }

    ///////////////////////////////////
    // Example of "movies" output:
    // The value for rating is the mean value of all ratings the movie received
    // "movies" : [{"movie" : "movie1", "rating" : 5, "raters" : ["user1", "user2"]}, {"movie" : "movie2", "rating" : 4, "raters" : ["user3", "user4"]}]

Error Response:

    {
    "type" : "rating",
    "ok" : false,
    "message" : <String>  // Error message
    }

    Specific error messages: 
        Error Response for movie already added (for "add"):
            - "<Movie Title> has already been added.""
        Error Response for user already gave a rating (for "rate"):
            - "You already provided a rating for this movie."
        Error Response for rating or trying to view a movie that doesn't exist:
            - "<Movie Title> has not yet been added."
        Error Response if there are no ratings yet: (for "view" for specific movie)
            - "<Movie Title> has no ratings yet.""
        Error Response if there are no ratings yet at all yet ("view"):
            - "There are no ratings yet.""
        Error Response for invalid rating:
           - "<Rating> is not a valid rating. Please enter an integer 1-5."


### General error responses: ###
These are used for all requests.

Error response: When a required field "key" is not in request

    {
        "ok" : false
        "message" : "Field <key> does not exist in request" 
    }

Error response: When a required field "key" is not of correct "type"

    {
        "ok" : false
        "message" : "Field <key> needs to be of type: <type>"
    }

Error response: When the "type" is not supported, so an unsupported request

    {
        "ok" : false
        "message" : "Type <type> is not supported."
    }


Error response: When the request is not a JSON

    {
        "ok" : false
        "message" : "req not JSON"
    }
