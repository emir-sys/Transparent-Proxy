
public class ClientRequests {
	
	public String date;
	public String ipAddress;
	public String requestedDomain;
	public String resourcePath;
	public String method;
	public String statusCode;
	
	public ClientRequests(String date,String ipAddress,String requestedDomain,String resourcePath,String method,String statusCode) {
		this.date=date;
		this.ipAddress=ipAddress;
		this.requestedDomain=requestedDomain;
		this.resourcePath=resourcePath;
		this.method=method;
		this.statusCode=statusCode;
	}
	
	public String returnRequest() {
		String request="";
		request += "Date: "+date + "\n" + "ipAddress: "+ipAddress + "\n" + "requestedDomain: "+requestedDomain+
				"\n" + "resourcePath: "+ resourcePath + "\n" + "method: "+method + "\n" + "statusCode: "+statusCode;
		return request;
		
	}
	
	public void print() {
		System.out.println("Date: "+date);
		System.out.println("ipAddress: "+ipAddress);
		System.out.println("requestedDomain: "+requestedDomain);
		System.out.println("resourcePath: "+resourcePath);
		System.out.println("method: "+method);
		System.out.println("statusCode: "+statusCode);
	}
}
