package com.example.demo.util;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Caches {

	public static ConcurrentMap<String, Object> map = new ConcurrentHashMap<String, Object>();

	public static Map<String, String> environment = System.getenv();

	public static List<String> projectsList;

	public static List<String> typesList;

	public static Hashtable<String, String> typeClass = new Hashtable<String, String>();

	public static Hashtable<String, String> dateFields = new Hashtable<String, String>();

	public static Set<String> attachmentFields = new HashSet<String>();

	public static Set<String> relationshipFields = new HashSet<String>();

	public static Set<String> userFields = new HashSet<String>();

	public static Set<String> textFields = new HashSet<String>();

	public static Set<String> pickFields = new HashSet<String>();

	public static Set<String> richContentFields = new HashSet<String>();

}