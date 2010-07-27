package uk.co.brotherlogic.jarpur;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class LinkTable {

	Map<String, String> links = new TreeMap<String, String>();

	private static LinkTable singleton;

	public static LinkTable getLinkTable() {
		if (singleton == null)
			singleton = new LinkTable("");
		return singleton;
	}

	public static LinkTable getLinkTable(String base) {
		if (singleton == null)
			singleton = new LinkTable(base);
		return singleton;
	}

	private LinkTable(String base) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(base + "props/"
					+ new File("mapping.properties")));
			for (Entry<Object, Object> entry : properties.entrySet()) {
				String ref = entry.getValue().toString();
				links.put(entry.getKey().toString(), ref);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String resolveLink(Object o) {
		System.err.println("RESOLVING LINK: " + o);
		String classname = o.getClass().getCanonicalName();
		System.err.println("CLASSHNAME: " + classname);
		if (links.containsKey(classname)) {
			System.err.println("LINK FOUND:" + links.get(classname));
			return resolveLink(o, links.get(classname));
		} else
			System.err.println("NOT FOUND: " + links.keySet());
		return null;
	}

	Pattern objPattern = Pattern.compile("\\%\\%(.*?)\\%\\%");

	private String resolveLink(Object o, String ref) {
		try {
			Class cls = Class.forName(ref);
			Page pg = (Page) cls.getConstructor(new Class[0]).newInstance(
					new Object[0]);
			String params = pg.linkParams(o);

			String nref = ref
					.substring(JarpurProperties.get("base").length() + 1);

			// Convert to a link
			nref.replace(".", "/");
			System.err.println("NREF: " + nref);

			// Remove any Default
			if (nref.endsWith("Default"))
				nref = nref
						.substring(0, nref.length() - "Default".length() - 1);
			else if (ref.endsWith("Default/"))
				nref = nref.substring(0, nref.length() - "Default/".length()
						- 1);

			return JarpurProperties.get("web") + "/" + nref + "/" + params;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	protected String resolveMethod(Object obj, String methodName) {

		try {
			Class cls = obj.getClass();
			Method m = cls.getMethod(methodName, new Class[0]);
			Object ret = m.invoke(obj, new Object[0]);
			return ret.toString();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}
}
