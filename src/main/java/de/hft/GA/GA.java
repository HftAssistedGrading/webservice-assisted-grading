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
 * @copyright 2014 HFT Stuttgart
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @author	Cornelia Kiefer, Verena Meyer
 */

@Path("/gradingassistant")
public class GA {  
   
	//TODO: Index for what?
    private int index = 0;
	
	@Context
	UriInfo uriInfo;

	//Test if WebService is running.
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
			
			//String[] refa = {};
			//for each element in this array, collect field infos
			for (int i = 0; i < arr.length(); i++) {

				try {
					PostedRecord pr = new PostedRecord();
					
					//refa = arr.getJSONObject(i).getString("referenceanswer").split("\\n");
					pr.getPostedRecordString().put("studentId", "s" + arr.getJSONObject(i).getInt("id"));
					pr.getPostedRecordString().put("question", arr.getJSONObject(i).getString("question"));
					pr.getPostedRecordString().put("answer", arr.getJSONObject(i).getString("answer"));
					pr.getPostedRecordString().put("refAnswer", arr.getJSONObject(i).getString("referenceanswer"));
					pr.getPostedRecordString().put("languageFlag", arr.getJSONObject(i).getString("languageoptions"));
					
					pr.getPostedRecordDouble().put("threshold", arr.getJSONObject(i).getDouble("threshold"));
					pr.getPostedRecordInt().put("id", arr.getJSONObject(i).getInt("id"));
					pr.getPostedRecordInt().put("max", arr.getJSONObject(i).getInt("max"));
					pr.getPostedRecordInt().put("min", arr.getJSONObject(i).getInt("min"));
					pr.getPostedRecordInt().put("sec", arr.getJSONObject(i).getInt("sec"));
					pr.getPostedRecordInt().put("numAttepts", arr.getJSONObject(i).getInt("numAttempts"));
					
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
			
			if (arr.length()>=1) {	//more than one student answer: sort	
									
			   	ga.doAnalysis();		   	
			   	result = ga.getAnalyzedRecordsAsJSONString();
			   	
			} else { //less than one student answer: no sorting required
				result =ga.getListOfPRAsJSONString();
			}
		
	        // return HTTP status 201 "created" with location of new resource
	 		return Response.created(location).entity(result).build();
 		 		
	}

    private URI addEntry(ListOfPostedRecords listOfPR) {
		
		// create location of new resource
		URI location = uriInfo.getAbsolutePathBuilder().path("" + index).build();
		
		index++;
		// set location as href
		listOfPR.setHref(location.toString());		
		
		// return new location
		return location;
	}
}