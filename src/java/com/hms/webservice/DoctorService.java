package com.hms.webservice;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import org.json.JSONArray;
import org.json.JSONObject;

@WebService(serviceName = "DoctorService")
public class DoctorService {

    // Inject context to find the web application folder
    @Resource
    private WebServiceContext context;

    private String getJsonFilePath() {
        ServletContext servletContext = (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
        return servletContext.getRealPath("/WEB-INF/patients.json");
    }

    // ... [Keep viewAllPatients and searchPatient the same, but use readJSONFile() updated below] ...

    @WebMethod(operationName = "updateMedicalHistory")
    public String updateMedicalHistory(
            @WebParam(name = "patientId") String patientId,
            @WebParam(name = "newHistory") String newHistory) {
        try {
            String jsonContent = readJSONFile();
            JSONObject jsonData = new JSONObject(jsonContent);
            JSONArray patients = jsonData.getJSONArray("patients");
            
            boolean found = false;
            for (int i = 0; i < patients.length(); i++) {
                JSONObject patient = patients.getJSONObject(i);
                if (patient.getString("patientId").equals(patientId)) {
                    patient.put("medicalHistory", newHistory);
                    found = true;
                    break;
                }
            }
            
            if (found) {
                // Write the updated JSON back to the file
                FileWriter fileWriter = new FileWriter(getJsonFilePath());
                fileWriter.write(jsonData.toString(2));
                fileWriter.close();
                return "Medical history updated successfully for patient: " + patientId;
            } else {
                return "Patient not found!";
            }
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private String readJSONFile() throws Exception {
        File file = new File(getJsonFilePath());
        if (!file.exists()) {
            throw new Exception("patients.json not found at " + file.getAbsolutePath());
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