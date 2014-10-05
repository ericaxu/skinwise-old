import play.Application;
import play.GlobalSettings;
import util.dbimport.INCI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Global extends GlobalSettings {
	@Override
	public void onStart(Application app) {
		try {
			byte[] data = Files.readAllBytes(Paths.get("data/inci.json.txt"));
			String inci = new String(data);
			INCI.importDB(inci);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStop(Application app) {
	}
}