public class HttpResponse {
    int statusCode;
    String reasonPhrase;
    String date;
    MimeHeader mh;   
    
   

    public HttpResponse(String response) {
    	
    	int first_space = response.indexOf(" ");
        int second_space = response.indexOf(" ",first_space+1);
        System.out.println("First: "+first_space);
        System.out.println("Second: "+second_space);
        statusCode = Integer.parseInt(response.substring(first_space+1, second_space));
        first_space = second_space+1;
        second_space = response.indexOf("\r");
        System.out.println("First: "+first_space);
        System.out.println("Second: "+second_space);
        reasonPhrase = response.substring(first_space,second_space);      
        
        String raw_mime_header = response.substring(second_space+1);
        mh = new MimeHeader(raw_mime_header);
    }

    public HttpResponse(int code, String reason, MimeHeader m) {
        statusCode = code;
        reasonPhrase = reason;
        mh = m;
        mh.put("Connection", "close");
    }

    public String toString() {
        return "HTTP/1.1 " + statusCode + " " + reasonPhrase + "\r\n" + mh + "\r\n";
    }
}