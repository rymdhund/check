package se.forskningsavd.check.model;

import java.util.Date;

public class Reminder {
	private String name;
	private int dayInterval;
	private int count;
	private int maxCount;
	private int color;
	private Date lastChecked;
	private long dbId;

	public Reminder(String name, int dayInterval, int checkCount, int maxCheckCount, int color, long dbId){
		this.name = name;
		this.dayInterval = dayInterval;
		this.count = checkCount;
		this.maxCount = maxCheckCount;
		this.color = color;
		lastChecked = null;
		this.dbId = dbId;
	}
	
	public Reminder(String name, int dayInterval, int count, int maxCount, int color){
		this(name, dayInterval, count, maxCount, color, 0);
	}

	
	public Reminder(String name, int dayInterval, int color){
		this(name, dayInterval, 0, 1, color);
	}
	
	public boolean isDone(){
		return count >= maxCount;
	}
	
	public void check(){
		if(count < maxCount) count++;
	}

	public String getName() {
		return name;
	}

	public void uncheck() {
		if(count > 0) count--;
	}

	public int getCheckCount() {
		return count;
	}
	
	public void setCheckCount(int checkCount){
		count = checkCount;
	}
	
	public int getMaxCheckCount() {
		return maxCount;
	}

	public long getDbId() {
		return dbId;
	}

	public int getColor() {
		return color;
	}

	public void setDbId(long id) {
		dbId = id;
	}

	public void setLastChecked(Date date) {
		lastChecked = date;
	}
	
	public boolean isDue(){
		if(lastChecked == null) return true;
		return TimeUtils.daysFrom(lastChecked) >= dayInterval;
	}
}
