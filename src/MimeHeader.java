import java.util.*;

public class MimeHeader extends HashMap<String, String> {

	public MimeHeader() {

	}

	public MimeHeader(String s) {
		parse(s);
	}

	private void parse(String data) {
		StringTokenizer st = new StringTokenizer(data, "\r\n");

		while (st.hasMoreTokens()) {
			String s = st.nextToken();			
			int first_space = s.indexOf(":");
			if(first_space != -1){
				if(s.contains("Host:") && s.contains(":443")) {
					int second_space = s.indexOf(":443");					
				    put(s.substring(0, first_space), s.substring(first_space+2,second_space));
				}
				else {
					int second_space = s.indexOf(" ",first_space+1);	        
				    put(s.substring(0, first_space), s.substring(second_space+1));
				}
			}			
		
	        
		}
	}

	@Override
	public String toString() {
		String str = "";
		Iterator<String> e = keySet().iterator();
		while (e.hasNext()) {
			String key = e.next();
			String val = get(key);
			str += key + ": " + val + "\r\n";
		}
		return str;
	}

}
