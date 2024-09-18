import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

class ProxyServer implements Runnable {

	public Socket clientSocket;
	public ArrayList<String> forbiddenAddresses = new ArrayList<>();
	public ArrayList<String> supportedMethods = new ArrayList<>();
	public ArrayList<Client> clients = new ArrayList<Client>();
	public DataInputStream inFromClient;
	public DataOutputStream outToClient;
	public int clientIndex;
	public Cache cachedRequests;
	public byte[] body;
	public int b;
	
	
	
	
	String host;
	String path;
	String hd;

	PrinterClass pC;
	
	public ProxyServer() {
		supportedMethods.add("GET");
		supportedMethods.add("HEAD");
		supportedMethods.add("POST");
		supportedMethods.add("CONNECT");
		cachedRequests = new Cache();
		
	}

	public void acceptClient(Socket s) {
		try {
			clientSocket = s;
			InetSocketAddress socketAddress = (InetSocketAddress) clientSocket.getRemoteSocketAddress();
			String clientIpAddress = socketAddress.getAddress().getHostAddress();
			
			if(!searchClient(clientIpAddress)){				
				clients.add(new Client(clientIpAddress));
				clientIndex=clients.size()-1;
			}
			
			pC = new PrinterClass();
			
			pC.add("A connection from a client is initiated...");
			inFromClient = new DataInputStream(s.getInputStream());
			outToClient = new DataOutputStream(s.getOutputStream());			
			new Thread(this).start();		
						
								
		}			
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	@Override
	public void run() {
		try {
			
			hd = getHeader(inFromClient);			
			System.out.println(hd);						
			int sp1 = hd.indexOf(' ');
			int sp2 = hd.indexOf(' ', sp1 + 1);
			int eol = hd.indexOf('\r');
			String reqHeaderRemainingLines;
			String url="";
			MimeHeader reqMH = null;
			String method="";
			if(hd.length()>0){
				reqHeaderRemainingLines = hd.substring(eol + 2);				
				if(hd.contains("CONNECT ")){
					url = hd.substring(sp1 + 1, sp2-4);
					if(!url.startsWith("https:")){
						url = "https://" + url;	
					}	
				}
				else {
					url = hd.substring(sp1 + 1, sp2);
					if(!url.startsWith("http")) {
						url = "http://" + url;
					}
				}				
				reqMH = new MimeHeader(reqHeaderRemainingLines);			
				method = hd.substring(0, sp1);
				host = reqMH.get("Host");

				reqMH.put("Connection", "close");
				System.out.println(url);
				
				URL u = new URL(url);

				String tmpPath = u.getPath();
				
				String tmpHost = u.getHost();			
				
				
				path = ((tmpPath == "") ? "/" : tmpPath);
				
				if (forbiddenAddresses.contains(host)) {
					pC.add("Connection blocked to the host due to the proxy policy");
					outToClient.writeBytes(createErrorPage(401, "Not Authorized", url));
					if(reqMH.get("Date")!=null) {
						clients.get(clientIndex).addClientRequest(reqMH.get("Date"),host,path,method,"401");
					}
					else {
						clients.get(clientIndex).addClientRequest("",host,path,method,"401");
					}
					
	               
				} else if (tmpHost != null) {
					if (supportedMethods.contains(method.toUpperCase())){
						if(method.equals("CONNECT")) {
							if(clientSocket.isConnected()) {					
								clients.get(clientIndex).addClientRequest("",host,path,method,"");
								handleConnect();	
							}
												
						}
						else {
							int index=cachedRequests.search(tmpPath);
							if(index!=-1) {						
								handleCache(index,reqMH);							
							}
							else{
								pC.add("Client requests...\r\nHost: " + host + "\r\nPath: " + path);
								handleProxy(url, reqMH, method);
							}
						}					
						
					} else {
						pC.add("Requested method " + method + " is not allowed on proxy server");
						outToClient.writeBytes(createErrorPage(405, "Method Not Allowed", method));
						if(reqMH.get("Date")!=null) {
							clients.get(clientIndex).addClientRequest(reqMH.get("Date"),host,path,method,"405");
						}
						else {
							clients.get(clientIndex).addClientRequest("",host,path,method,"405");
						}
					}
				} else {					
					pC.add("Error for request: " + url);
				}
			}			
			
			pC.removeThread();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void handleConnect(){
		
		try {
			Socket sSocket = new Socket(host, 443);
			DataInputStream inFromServer = new DataInputStream(sSocket.getInputStream());
			DataOutputStream outToServer = new DataOutputStream(sSocket.getOutputStream());
			DataInputStream inFromClient =  new DataInputStream(clientSocket.getInputStream());
			DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
			
		
			 Thread readFromServer = new Thread() {
				public void run(){
					try {
						int a;

						byte[] buffer = new byte[1024];
						while ((a = inFromServer.read(buffer)) != -1) {
							outToClient.write(buffer,0,a);
							outToClient.flush();
						}
												
					}
							
					catch(Exception e){						
						e.printStackTrace();						
					}
					finally {
						try {
							outToClient.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				}
			};
			
			Thread writeToServer = new Thread() {
				public void run(){
					try {
						int a;
							
						byte[] buffer = new byte[1024];

						while ((a = inFromClient.read(buffer)) != -1) {
							outToServer.write(buffer, 0, a);
							outToServer.flush();
						}
																
					}					
					catch(Exception e) {
						e.printStackTrace();						
					}
					finally {
						try {
							outToServer.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
			};
					
			
			
			readFromServer.start();
			writeToServer.start();
			outToClient.writeBytes("HTTP/1.1 200 OK"+"\r\n"+"\r\n");
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
	}
	private void handleCache(int index,MimeHeader reqMH) {
		
		try {
			
			Socket sSocket = new Socket(host, 80);
			DataInputStream inFromServer = new DataInputStream(sSocket.getInputStream());
			DataOutputStream outToServer = new DataOutputStream(sSocket.getOutputStream());
			reqMH.put("If-Modified-Since",cachedRequests.cache.get(index).lastModified);
			
			String ifModified = "GET " + path +" HTTP/1.1\r\n"+reqMH+"\r\n";
			
			outToServer.writeBytes(ifModified);
			
			ByteArrayOutputStream bAOS = new ByteArrayOutputStream(10000);

			int a;

			byte[] buffer = new byte[1024];

			while ((a = inFromServer.read(buffer)) != -1) {
				bAOS.write(buffer, 0, a);
			}

			byte[] response = bAOS.toByteArray();

			String rawResponse = new String(response);
						
			String responseHeader = rawResponse.substring(0, rawResponse.indexOf("\r\n\r\n"));
			int first_space = responseHeader.indexOf(" ");
	        int second_space = responseHeader.indexOf(" ",first_space+1);	        
	        String statusCode = responseHeader.substring(first_space+1, second_space);
	        
	        if(statusCode.equals("304")){
	        	pC.add("Request handled from cache");
	        	pC.add("\r\nResponse Header\r\n" + cachedRequests.cache.get(index).responseHeader);
				outToClient.write(cachedRequests.cache.get(index).response);			
				outToClient.close();
	        	
	        }
	        else {
	        	pC.add("Cache is updated");
	        	int eol = responseHeader.indexOf('\r');
				String reqHeaderRemainingLines = responseHeader.substring(eol + 2);
				MimeHeader responseMH = new MimeHeader(reqHeaderRemainingLines);
				
				clients.get(clientIndex).addClientRequest(responseMH.get("Date"),host,path,"GET",statusCode);
				cachedRequests.update(index,response, responseHeader, responseMH.get("Last-Modified"), reqMH, reqMH.get("Host")+path);
				pC.add("\r\nResponse Header\r\n" + responseHeader);
				
				pC.add("\r\n\r\nGot " + response.length + " bytes of response data...\r\n"
						+ "Sending it back to the client...\r\n");

				outToClient.write(response);

				outToClient.close();

				
	        }
			
			
			pC.add("\r\nServed http://" + host + path + "\r\nExiting ServerHelper thread...\r\n"
					+ "\r\n----------------------------------------------------" + "\r\n");
			sSocket.close();
			
		} 
		catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
	}
	private void handleProxy(String url, MimeHeader reqMH, String method) {
		try {
			pC.add("\r\nInitiating the server connection");
			Socket sSocket = new Socket(host, 80);
			DataInputStream inFromServer = new DataInputStream(sSocket.getInputStream());
			DataOutputStream outToServer = new DataOutputStream(sSocket.getOutputStream());

			reqMH.put("User-Agent", reqMH.get("User-Agent") + " via CSE471 Proxy");

			pC.add("\r\nSending to server...\r\n" + method +" " + path + " HTTP/1.1\r\n" + reqMH + "\r\n");
			
			if(method.equals("POST")) {
				String body_s = new String(body,0,b);				
				outToServer.writeBytes(method+" " + path + " HTTP/1.1\r\n" + reqMH + "\r\n"+body_s+"\r\n");
			}
			else {
				outToServer.writeBytes(method+" " + path + " HTTP/1.1\r\n" + reqMH + "\r\n");
			}

			

			pC.add("HTTP request sent to: " + host);

			ByteArrayOutputStream bAOS = new ByteArrayOutputStream(10000);

			int a;

			byte[] buffer = new byte[1024];

			while ((a = inFromServer.read(buffer)) != -1) {
				bAOS.write(buffer, 0, a);
			}

			byte[] response = bAOS.toByteArray();

			String rawResponse = new String(response);
			
			
			
			
			String responseHeader = rawResponse.substring(0, rawResponse.indexOf("\r\n\r\n"));
			
			int first_space = responseHeader.indexOf(" ");
	        int second_space = responseHeader.indexOf(" ",first_space+1);	        
	        String statusCode = responseHeader.substring(first_space+1, second_space);
	        
			int eol = responseHeader.indexOf('\r');
			String reqHeaderRemainingLines = responseHeader.substring(eol + 2);
			MimeHeader responseMH = new MimeHeader(reqHeaderRemainingLines);
			
			
			
			if(responseMH.get("Last-Modified")!=null){
				
				cachedRequests.addToCache(response, responseHeader, responseMH.get("Last-Modified"), reqMH, path);
			}
			
			clients.get(clientIndex).addClientRequest(responseMH.get("Date"),host,path,method,statusCode);
			
			pC.add("\r\nResponse Header\r\n" + responseHeader);
			
			pC.add("\r\n\r\nGot " + response.length + " bytes of response data...\r\n"
					+ "Sending it back to the client...\r\n");
			
			
			outToClient.write(response);
			
			outToClient.close();
			

			sSocket.close();

			pC.add("Served http://" + host + path + "\r\nExiting ServerHelper thread...\r\n"
					+ "\r\n----------------------------------------------------" + "\r\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean searchClient(String ipAddress) {
		for(int i=0;i<clients.size();i++) {
			if(clients.get(i).ipAddress.equals(ipAddress)) {
				clientIndex=i;
				return true;
			}
		}		
		return false;
	}
	private String createErrorPage(int code, String msg, String address) {		
		
		String html_page = "<!DOCTYPE html>\r\n"
        		+ "<body>\r\n"
        		+ "<h1>\r\n"
        		+ code+" "+msg+"\r\n"
        		+ "</h1>\r\n"
        		+ "Error when fetching URL: "+address+"/\r\n"
        		+ "</body>\r\n"
        		+ "</html>";   
        
               
		MimeHeader mh = makeMimeHeader("text/html", html_page.length());
		HttpResponse hr = new HttpResponse(code, msg, mh);
		return hr + html_page;
		
	}

	private MimeHeader makeMimeHeader(String type, int length) {
		MimeHeader mh = new MimeHeader();
		Date d = new Date();
		TimeZone gmt = TimeZone.getTimeZone("GMT");
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss zzz");
		sdf.setTimeZone(gmt);
		String sdf_date = sdf.format(d);
		mh.put("Date",sdf_date);
		mh.put("Server","TermProject");
		mh.put("Content-Type", type);	
		if (length >= 0)
			mh.put("Content-Length", String.valueOf(length));
		return mh;
	}

	public String getHeader(DataInputStream in) throws Exception {
		byte[] header = new byte[1024];
	
		int data;
		int h = 0;

		while ((data = in.read()) != -1) {
			header[h++] = (byte) data;
			if (header[h - 1] == '\n' && header[h - 2] == '\r' && header[h - 3] == '\n' && header[h - 4] == '\r') {
				break;
			}
		}	
		
		String isPost = new String(header);
		if(isPost.contains("POST ")){			
			int first = isPost.indexOf("Content-Length:");			
			int second = isPost.indexOf('\r',first);
			int length=Integer.parseInt(isPost.substring(first+16,second));				
			body=new byte[1024];
			b=0;
			while(true) {
				data = in.read();
				body[b++]=(byte) data;				
				if(b==length){
					break;
				}				
			}
		}
		
		return new String(header, 0, h);
	}
	
	public void addHostToFilter(String host) {
		forbiddenAddresses.add(host);
	}

	public ArrayList<String> getForbiddenAddresses() {
		return forbiddenAddresses;
	}

	
	

}