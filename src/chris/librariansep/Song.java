package chris.librariansep;

public class Song {

	private int songID;
	private String name;
	
	public Song(int songID, String name) {
		this.songID = songID;
		this.name = name;
	}
	
	public int getID() {
		return this.songID;
	}
	
	public String getName() {
		return this.name;
	}
	
}
