package com.cantor.ipplan.shared;

public class Utils {

	
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
}
