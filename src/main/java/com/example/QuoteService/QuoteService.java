package com.example.QuoteService;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.DefaultValue;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

//Sets the path of the main page to /quotes as per requirement #2 in instructions.
@Path("/quotes")
//Sets the entire class default to JSON as per requirement #4 in instructions.
@Produces(MediaType.APPLICATION_JSON)
public class QuoteService {

/*******************************
 * Quote Object & List
 *******************************/

    //Quote Object class
    //With wrapping enabled, specifies the name of the root wrapper QuoteObject
    @JsonRootName("QuoteObject")
    public static class QuoteObject {
        //Specifies the properties in the JSON format
        @JsonProperty int id;
        @JsonProperty String quote;
}
    //Quote List collection, initialized with all quotes ready
    public static List<QuoteObject> listOfQuotes = new ArrayList<QuoteObject>() {
        {
            QuoteObject qo1 = new QuoteObject();
            qo1.id = 1;
            qo1.quote = "The greatest glory in living lies not in never falling, but in rising every time we fall. -Nelson Mandela";

            QuoteObject qo2 = new QuoteObject();
            qo2.id = 2;
            qo2.quote = "If life were predictable it would cease to be life, and be without flavor. -Eleanor Roosevelt";

            QuoteObject qo3 = new QuoteObject();
            qo3.id = 3;
            qo3.quote = "The best and most beautiful things in the world cannot be seen or even touched - they must be felt with the heart. -Helen Keller";

            QuoteObject qo4 = new QuoteObject();
            qo4.id = 4;
            qo4.quote = "Tell me and I forget. Teach me and I remember. Involve me and I learn. -Benjamin Franklin";

            QuoteObject qo5 = new QuoteObject();
            qo5.id = 5;
            qo5.quote = "You will face many defeats in life, but never let yourself be defeated. -Maya Angelou";

            add(qo1);
            add(qo2);
            add(qo3);
            add(qo4);
            add(qo5);
        }
    };

    //Id positioning for creating and deleting new quotes so that no two ID's are the same.
    static int lastIDValue = listOfQuotes.size() + 1;

/*******************************
 * Local Functions
 *******************************/

    public QuoteObject find(int id) {
        QuoteObject toReturn = null;

        for (QuoteObject x : listOfQuotes) {
            if(x.id == id) toReturn = x;
        }
        return toReturn;
    }

/*******************************
 * Quote Object & List
 *******************************/

    //@GET annotation to show this function just does a READ
    @GET
    @Path("/getAllQuotes")
    @Produces(MediaType.APPLICATION_JSON)
    //@QueryParam lets us bind the variables so we can specify the pagination, @DefaultValue allows the freedom to not have to specify ?page=
    public Response getAllQuotes(@DefaultValue("1")@QueryParam("page") int page, @DefaultValue("5")@QueryParam("per_page") int per_page) throws IOException {

        //In the example, we say that if there are 10 quotes, and we want page 2 and 5 per page of quotes, we get quotes 6, 7, 8, 9, and 10.
        //Thus, the math for finding the starting quote would be page * per_page - per_page
        int startingPage = page * per_page - per_page;

        //This makes the ending page the last index plus 1
        int endingPage = startingPage + per_page;

        //ObjectMapper is part of jackson dependency that allows response in JSON format to be delivered to client
        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder entity = new StringBuilder();

        for (int i = startingPage; i < endingPage; i++) {
            entity.append(objectMapper.writeValueAsString(listOfQuotes.get(i))).append("\n");
        }
        return Response.status(200).entity(entity.toString()).build();
    }

    //@GET annotation to show that this function just does a READ
    @GET
    //@Path annotation shows how we specify the id to get the desired quote in proper URL format
    @Path("/getQuote/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    //@PathParam links "id" to the above @Path annotation's "id"
    public Response getQuoteById(@PathParam("id") int id) throws IOException {

        //Using find() function defined above, we find the quote object needed from the list of quotes.
        QuoteObject q = find(id);

        if (q == null) {
            //Case in which quote ID does not exist
            return Response.status(400).entity("Quote Not Found").build();
        }

        //ObjectMapper is part of jackson dependency that allows response in JSON format to be delivered to client
        ObjectMapper objectMapper = new ObjectMapper();
        String entity = objectMapper.writeValueAsString(q);

        return Response.status(200).entity(entity).build();
    }

    //@POST annotation to show that this function does a CREATE
    @POST
    //@Path annotation shows how we specify the quote to add the desired quote in proper URL format
    @Path("/addQuote/{quote}")
    @Consumes(MediaType.APPLICATION_JSON)
    //@Produces(MediaType.APPLICATION_JSON)
    public Response addQuote(@PathParam("quote") String quote) throws IOException {

        //Create new Quote Object so that we can manipulate and add to the list of quotes.
        QuoteObject q = new QuoteObject();
        q.id = lastIDValue;
        q.quote = quote;
        listOfQuotes.add(q);

        //Set for next time it needs to be used.
        lastIDValue++;

        ObjectMapper objectMapper = new ObjectMapper();
        String entity = objectMapper.writeValueAsString(q);

        return Response.status(200).entity(entity).build();
    }

    //@PUT annotation because we are doing an UPDATE/REPLACE, except we want an idempotent update where the resource
    //stays is still there and has the same state as it did with the first call.
    @PUT
    @Path("/updateQuote/{id}/{newQuote}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateQuote(@PathParam("id") int id, @PathParam("newQuote") String newQuote) throws IOException{

        //Retrieve quote to be updated
        if (find(id) == null) {

            //Case where quote to replace was not found in list, error message 400 = Bad Request
            return Response.status(400).entity("Quote Not Found").build();
        }

        //Update quote by finding it again in list since we know it has to be there. Then, pass that new updated quote inside the list to the Response Build
        find(id).quote = newQuote;

        ObjectMapper objectMapper = new ObjectMapper();
        String entity = objectMapper.writeValueAsString(find(id));

        return Response.status(200).entity(entity).build();
    }

    //@DELETE annotation because we are doing a DELETE (duh)
    @DELETE
    @Path("/deleteQuote/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuote(@PathParam("id") int id) {

        //Retrieve quote to be removed
        if (find(id) == null) {

            //Case where quote to delete was not found in list, error message 400 = Bad Request
            return Response.status(400).entity("Quote Not Found").build();
        }

        listOfQuotes.removeIf(q -> q.id == id);
        return Response.status(200).entity("Quote " + id + "successfully removed!").build();
    }


    //Landing page for assignment, was created in initial setup of program
    //I want to keep this "landing page" for organizational purposes and as a 'little sugar on top'
    @GET
    @Produces("text/plain")
    public String hello() {
        return "Welcome to Willy Moore's RESTful Web Service Landing Page!";
    }


}