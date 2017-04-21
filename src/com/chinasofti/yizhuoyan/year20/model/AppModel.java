package com.chinasofti.yizhuoyan.year20.model;

import java.io.File;

public abstract class AppModel {
	protected final static int CHECK_INTERVAL = 1000;
	protected final String URL_UPLOAD = "http://www.in20years.com/response.php";
	protected final String URL_CHECKRESULT = "http://www.in20years.com/response.php";
	
	public static  interface Callback{
		 void progress(int value);
		 void done(String url);
		 void error(Exception e);
	}
	private int ageingYear;
	private boolean male;
	private File photo;
	
	
	public void setAgeingYear(int ageingYear) {
		if(ageingYear==20){
			ageingYear=15;
		}
		this.ageingYear = ageingYear;
	}


	public void setMale(boolean male) {
		this.male = male;
	}


	public void setPhoto(File photo) {
		this.photo = photo;
	}


	public int getAgeingYear() {
		return ageingYear;
	}


	public boolean isMale() {
		return male;
	}


	public File getPhoto() {
		return photo;
	}


	public abstract void doAgeing(Callback callback);
}
