package com.cantor.ipplan.shared;

import java.util.Date;

public class Utils {

	public static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
	public static final String GOOGLE_TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
	public static final String GOOGLE_CLIENT_ID = "317070202620-jl480ige77srbimqut9l6fmklh05e90s.apps.googleusercontent.com";
	public static final String GOOGLE_SCOPE = "https://www.google.com/m8/feeds https://www.googleapis.com/auth/calendar";
	//TODO при инсталяции нужно будет поменять здесь и в Google Apis Console 
	public static final String REDIRECT_URI = "http://127.0.0.1:8889/main/auth";
	
	/**
	 * Cклонение существительных по правилам русского языка
	 *
	 * @param array forms 0 - сделка, 1 - сделки, 2 - сделок 
	 * @param int count
	 * @return string
	 * @author Василий Меленчук http://habrahabr.ru/users/basilisk/
	 */	
	static public String getNumberPadeg(String[] forms, int count) {
	    int mod100 = count%100;
	    switch (count%10) {
	        case 1:
	            if (mod100 == 11) return forms[2];
	            	else return forms[0];
	        case 2:
	        case 3:
	        case 4:
	            if ((mod100 > 10) && (mod100 < 20))
	                return forms[2];
	            else
	                return forms[1];
	        case 5:
	        case 6:
	        case 7:
	        case 8:
	        case 9:
	        case 0:
	            return forms[2];
	    }
		return forms[0];
	}

	static final long dayTime = 24 * 60 * 60 * 1000; // 24 h * 60 min * 60 s * 1000 millis
	static final long weekTime = 7 * dayTime; 
	
	static public String getDuration(Date d1, Date d2) {
		long duration = d2.getTime()-d1.getTime();
		long wcount = duration/weekTime;
		long dcount = (duration%weekTime)/dayTime;
		String s = "";
		if(wcount>0) 
			s+= wcount+" "+getNumberPadeg(new String[]{"неделя","недели","недель"},(int)wcount);
		if(dcount>0) {
			if(wcount>0) s+=", ";
			s+=dcount+" "+getNumberPadeg(new String[]{"день","дня","дней"},(int)dcount);
		}
		return s;
	}
}
