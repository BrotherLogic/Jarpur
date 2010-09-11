package uk.co.brotherlogic.jarpur.replacers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class Replacer {

	private final List<Replacer> replacers = new LinkedList<Replacer>();
	private Object refObj;

	public List<Replacer> getReplacers() {
		return replacers;
	}

	public void addReplacer(Replacer add) {
		replacers.add(add);
	}

	public String process(Object ref, Map<String, Object> objectMap) {
		System.err.println("REPLACING: " + this.getClass() + " => " + ref);
		refObj = ref;
		StringBuffer buffer = new StringBuffer();
		for (Replacer repl : replacers) {
			buffer.append(repl.process(ref, objectMap));
		}
		return buffer.toString();
	}

	@Override
	public String toString() {
		return "FullPage: " + this.getClass();
	}

	protected void setRefObj(Object obj) {
		refObj = obj;
	}

	protected Object resolveMethodWithParameter(Object obj, String methodName,
			Map<String, Object> paramMap) {

		int firstBracket = methodName.indexOf('(');
		String method = methodName.substring(0, firstBracket);
		String[] parameters = methodName.substring(firstBracket + 1,
				methodName.length() - 1).split(",");

		Method[] methodArr = obj.getClass().getMethods();
		for (Method method2 : methodArr) {
			if (method2.getName().equals(method)) {
				try {
					Object[] objArr = new Object[parameters.length];
					for (int i = 0; i < objArr.length; i++) {
						objArr[i] = resolve(parameters[i], paramMap);
					}

					return method2.invoke(obj, objArr);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	protected Object resolveMethod(Object obj, String methodName,
			Map<String, Object> paramMap) {

		if (obj != null)
			System.err.println("Resolving " + obj.getClass() + " with method "
					+ methodName);
		else
			System.err.println("Resolving " + obj + " with method "
					+ methodName);

		if (methodName.contains("("))
			return resolveMethodWithParameter(obj, methodName, paramMap);
		try {
			Class cls = obj.getClass();
			Method m = cls.getMethod(methodName, new Class[0]);
			Object ret = m.invoke(obj, new Object[0]);
			return ret;
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

	protected Object resolve(String replace, Map<String, Object> paramMap) {

		String[] elems = replace.split("\\.");
		if (paramMap.containsKey(elems[0])) {
			Object obj = paramMap.get(elems[0]);
			for (int i = 1; i < elems.length; i++) {
				obj = resolveMethod(obj, elems[i], paramMap);
			}
			return obj;
		} else {
			// Try to resolve using the base
			Object obj = resolveMethod(refObj, replace, paramMap);
			if (obj != null)
				return obj;
		}

		return "UNABLE TO REPLACE";
	}
}
