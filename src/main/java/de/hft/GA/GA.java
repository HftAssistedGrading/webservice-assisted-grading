
package de.hft.GA;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hft.gradingassistant.GradingAssistantForFreeTextAnswers;
import de.hft.gradingassistant.record.*;


/**
 * This file defines the grading assistant resource class (to be hosted at the URI path "/gradingassistant")
 * 
 * 
 * @copyright 2014 HFT Stuttgart
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @author	Cornelia Kiefer
 */

@Path("/gradingassistant")
public class GA {  
   
    private int index = 0;
	
	@Context
	UriInfo uriInfo;

  
	@GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String testIfWebserviceIsRunning() {     	   	    	
    	return "true";		    	
    }    
   
    @POST
    @Path("/post")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postAPostedRecord(String json) {    	
    		    	
    	//transform the json formatted String entry to a ListOfPostedRecords    	
    	de.hft.gradingassistant.record.ListOfPostedRecords listOfPR = new de.hft.gradingassistant.record.ListOfPostedRecords();
		  
    		//create a JSONObject from argument String
			JSONObject obj = null;
			try {
				obj = new JSONObject(json);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//create a JSONArray from the JSONObject
			JSONArray arr = null;
			try {
				arr = obj.getJSONArray("records");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//for each element in this array, collect field infos
			for (int i = 0; i < arr.length(); i++)
			{
			    try {
			    	int id = arr.getJSONObject(i).getInt("id");					
					String question = arr.getJSONObject(i).getString("question");
					String referenceanswer = arr.getJSONObject(i).getString("referenceanswer");
					String answer = arr.getJSONObject(i).getString("answer");
					int max = arr.getJSONObject(i).getInt("max");			
					int min = arr.getJSONObject(i).getInt("min");
					int sec = arr.getJSONObject(i).getInt("sec");
					int numAttempts = arr.getJSONObject(i).getInt("numAttempts");
										
					PostedRecord pr = new PostedRecord();
					
					pr.studentId = "s" + id;
					pr.id = id; 
					pr.question = question;
					pr.referenceanswer0 = referenceanswer;
					pr.answer = answer;
					pr.max = max;
					pr.min = min;
					pr.sec = sec;
					pr.numAttempts = numAttempts;
					
					listOfPR.getEntry().add(pr);
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
			URI location = addEntry(listOfPR); 
			String result = "";
			GradingAssistantForFreeTextAnswers ga = new GradingAssistantForFreeTextAnswers();
			ga.readPostedRecords(listOfPR);
			
			if(arr.length()>1){	//more than one student answer: sort	
									
			   	ga.doAnalysis();		   	
			   	result = ga.getAnalyzedRecordsAsJSONString();
			   	
			} else { //less than one student answer: no sorting
				result =ga.getListOfPRAsJSONString();
			}
		
	        // return HTTP status 201 "created" with location of new resource
	 		return Response.created(location).entity(result).build();
 		 		
	}
    
    
    private URI addEntry(ListOfPostedRecords l) {
		
		// create location of new resource
		URI location = uriInfo.getAbsolutePathBuilder()
				.path("" + index).build();
		
		index++;
		// set location as href
		l.setHref(location.toString());		
		
		// return new location
		return location;
	}

}
