import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Client {
	
	public String ipAddress;
	
	public ArrayList<ClientRequests> requests = new ArrayList<ClientRequests>();
	
	public Client(String ip) {
		ipAddress = ip;	
	}
	
	public void addClientRequest(String date,String requestedDomain,String resourcePath,String method,String statusCode) {
		requests.add(new ClientRequests(date,ipAddress,requestedDomain,resourcePath,method,statusCode));
	}
	
	public String getRequests() {
		String	str="";
		for(ClientRequests request:requests) {
			str = str + request.returnRequest()+"\n\r";			
		}
		return str;
	}
	
	public void writeToTxt(){		
		try {
        	File file = new File(ipAddress+".txt");
    		if(file.exists()==true)
    			file.delete();
        	FileWriter writer = new FileWriter(file.getName(),true);
        	writer.write(getRequests());      	
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
	}
}
