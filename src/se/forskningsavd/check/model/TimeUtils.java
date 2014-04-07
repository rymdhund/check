package se.forskningsavd.check.model;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
	public static final long LENGTH_OF_A_DAY = 24L*3600L*1000L;

	public static long startOfDayTimestamp(){
		return startOfDayTimestamp(new Date());
	}

	public static long startOfDayTimestamp(Date d){
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.setTimeInMillis(System.currentTimeMillis());
		cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
		cal.set(Calendar.MINUTE, 0); // set minutes to zero
		cal.set(Calendar.SECOND, 0); //set seconds to zero
		return cal.getTimeInMillis();
	}
	
	public static int daysFrom(Date d){
		long t1 = startOfDayTimestamp(d);
		long t2 = startOfDayTimestamp();
		return (int)((t2-t1)/LENGTH_OF_A_DAY);
	}
}
