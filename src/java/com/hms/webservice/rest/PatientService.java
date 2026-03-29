package com.hms.webservice.rest;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;

@Path("patient")
public class PatientService {

    // Inject ServletContext to locate files within the web application folder
    @Context
    private ServletContext context;

    @GET
    @Path("history/{patientId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPatientHistory(
            @PathParam("patientId") String patientId,
            @Context HttpHeaders headers) {
        
        try {
            // Check for Bearer token authorization
            String authHeader = headers.getHeaderString("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Unauthorized access\"}")
                        .build();
            }
            
            String jsonContent = readJSONFile();
            JSONObject jsonData = new JSONObject(jsonContent);
            JSONArray patients = jsonData.getJSONArray("patients");
            
            for (int i = 0; i < patients.length(); i++) {
                JSONObject patient = patients.getJSONObject(i);
                if (patient.getString("patientId").equals(patientId)) {
                    
                    JSONObject result = new JSONObject();
                    result.put("patientId", patient.getString("patientId"));
                    result.put("patientName", patient.getString("patientName"));
                    result.put("age", patient.getInt("age"));
                    result.put("bloodType", patient.getString("bloodType"));
                    result.put("phone", patient.getString("phone"));
                    result.put("medicalHistory", patient.getString("medicalHistory"));
                    
                    return Response.ok(result.toString(2)).build();
                }
            }
            
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Patient not found\"}")
                    .build();
                    
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
    
    private String readJSONFile() throws Exception {
        // Use the ServletContext to find the absolute path to patients.json
        String filePath = context.getRealPath("/WEB-INF/patients.json");
        File file = new File(filePath);
        
        if (!file.exists()) {
            throw new Exception("patients.json not found at: " + filePath);
        }
        
        StringBuilder content = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line);
        }
        reader.close();
        return content.toString();
    }
}