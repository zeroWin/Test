package msServer;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class LearnGoogleJson {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		builder.setPrettyPrinting().serializeNulls();
		Gson gson = builder.create();
		
		Albums albums = new Albums();
		albums.title = "Free Music Archive - Albums";
		albums.message = "";
		albums.total = "11259";
		albums.total_pages = 2252;
		albums.page = 1;
		albums.limit = "5";
		
		
		DataSet dataSet = new DataSet();
		dataSet.album_id = "7596";
		dataSet.album_title = "Album 1";
		

		AlbumImages images = new AlbumImages();
		images.image_id = "1";
		
		
		
		dataSet.images.add(images);
		
		albums.dataset.add(dataSet);
		dataSet.images.add(images);
		
		System.out.println(gson.toJson(albums));
		//System.out.println(gson.toJson(dataSet));
		//System.out.println(gson.toJson(images));
		
		String result = gson.toJson(albums);
		Albums albums1 = gson.fromJson(result, Albums.class);

		System.out.println(albums.dataset.size());
		System.out.println(albums1.dataset.get(0).album_id);
	}

}


class Albums{
	public String title;
	public String message;
	public List<String> errors = new ArrayList<String>();
	public String total;
	public int total_pages;
	public int page;
	public String limit;
	public List<DataSet> dataset  = new ArrayList<DataSet>();
}


class DataSet{
	public String album_id;
	public String album_title;
	@SerializedName("album_images")
	public List<AlbumImages> images = new ArrayList<AlbumImages>();
}

class AlbumImages{
	public String image_id;
	public String user_id;
}