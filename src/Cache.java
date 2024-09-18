import java.util.ArrayList;

public class Cache {
	public byte[] response;
	public String responseHeader;
	public String lastModified;
	public MimeHeader request;
	public String path;
	
	
	public ArrayList<Cache> cache = new ArrayList<Cache>();
	
	public Cache() {
		
	}
	
	public Cache(byte[] response,String responseHeader,String lastModified,MimeHeader request,String path) {
		this.response=response;
		this.responseHeader=responseHeader;
		this.lastModified=lastModified;
		this.request=request;
		this.path=path;
	}
	
	public void addToCache(byte[] response,String responseHeader,String lastModified,MimeHeader request,String path) {
		cache.add(new Cache(response,responseHeader,lastModified,request,path));
	}
	
	public int search(String path){
		for(int i=0;i<cache.size();i++) {
			if((cache.get(i).path).equals(path)){
				return i;
			}
		}
		
		return -1;
	}
	
	public void update(int index,byte[] response,String responseHeader,String lastModified,MimeHeader request,String path) {
		cache.get(index).response=response;
		cache.get(index).responseHeader=responseHeader;
		cache.get(index).lastModified=lastModified;
		cache.get(index).request=request;
		cache.get(index).path=path;
	}
}
