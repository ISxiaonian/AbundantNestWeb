package com.example.demo.util;

public class StringUtil {

	public static boolean isEmpty(String str) {
		if(str == null) {
			return true;
		}
		if(str.trim().isEmpty()) {
			return true;
		}
		return false;
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

}
