package src.util;

import src.controllers.admin.Export;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;

public class Backup extends Thread {
	private static final String TAG = "Backup";
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd.HH");
	private static final long BACKUP_INTERVAL = 24 * 60 * 60 * 1000;

	private AtomicBoolean kill = new AtomicBoolean(false);

	@Override
	public void run() {
		Logger.info(TAG, "Thread starting");
		long time = System.currentTimeMillis();
		while (!kill.get()) {
			try {
				time += BACKUP_INTERVAL;
				long diff = time - System.currentTimeMillis();
				Thread.sleep(diff);
				backup();
			}
			catch (InterruptedException e) {
				break;
			}
		}
		Logger.info(TAG, "Thread exiting");
	}

	public void kill() {
		kill.set(true);
		this.interrupt();
	}

	public void backup() {
		Logger.info(TAG, "Performing backup");
		Calendar calendar = GregorianCalendar.getInstance();
		Date date = calendar.getTime();
		//TODO: Wipe old backups as necessary

		//Perform new backup
		try {
			Export.exportDB("backup/" + format.format(date) + ".json.txt");
		}
		catch (Exception e) {
			Logger.error(TAG, e);
		}

		System.gc();
		Logger.info(TAG, "Backup complete");
	}
}
