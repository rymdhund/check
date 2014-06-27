package se.forskningsavd.check.model;

import java.util.AbstractList;
import java.util.ArrayList;

import android.util.Log;

public class ReminderList extends AbstractList<Reminder>{
	private static final String TAG = "ReminderList";
	private ArrayList<Reminder> doneReminders;
	private ArrayList<Reminder> activeReminders;

	public ReminderList(){
		doneReminders = new ArrayList<Reminder>();
		activeReminders = new ArrayList<Reminder>();
	}
	
	@Override
	public boolean add(Reminder r){
		if(r.isDone()){
			doneReminders.add(r);
		}else{
			activeReminders.add(r);
		}
		return true;
	}

	@Override
	public Reminder get(int idx) {
		Log.d(TAG, "Get "+idx);
		update();
		if(idx < activeReminders.size()){
			return activeReminders.get(idx);
		}else{
			return doneReminders.get(idx-activeReminders.size());
		}
	}
	
	@Override
	public Reminder remove(int idx){
		Log.d(TAG, "Remove "+idx);
		if(idx < activeReminders.size()){
			return activeReminders.remove(idx);
		}else{
			return doneReminders.remove(idx-activeReminders.size());
		}
	}

	@Override
	public int size() {
		update();
		return activeReminders.size()+doneReminders.size();
	}
	
	private void update(){
		for(int i = 0; i < doneReminders.size(); i++){
			if(!doneReminders.get(i).isDone()){
				Reminder r = doneReminders.remove(i);
				activeReminders.add(r);
			}
		}
		for(int i = 0; i < activeReminders.size(); i++){
			if(activeReminders.get(i).isDone()){
				Reminder r = activeReminders.remove(i);
				doneReminders.add(r);
			}
		}
	}
}