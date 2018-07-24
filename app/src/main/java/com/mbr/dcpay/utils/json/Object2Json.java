package com.mbr.dcpay.utils.json;

import android.util.Log;
import android.util.SparseArray;

import com.mbr.dcpay.demo.MainActivity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author 作者 E-mail: panhang@ehoo.cn
 * @version 创建时间：2014-8-22 下午2:20:25
 * @desc 把Object转为Json String的工具类
 *       <p>
 *       默认支持String、boolean、Number、Map<String, Object>、[]、List
 *       <p>
 *       如果需要支持自定义的数据类型，那么自定义的数据类型必须实现{@link ToJson}接口
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Object2Json {
	private static final String	TAG	= "Object2Json";

	/**
	 * 用于支持自定义的数据类型的接口
	 * <p>
	 * <font color=red>
	 * 
	 * 注意：本接口内{@link #toJson()}方法的返回值不是限制为只能返回String。这是为了方便实现。
	 * <p>
	 * 实现时只需要把自定义的数据结构转换成本工具类支持的类型即可，而不需要实现完全的到Json String的转换。
	 * <p>
	 * 当然，完全的到Json String的转换也是支持的。 </font>
	 */
	public static interface ToJson {
		public Object toJson();
	}
	public static interface JsonCell extends ToJson {
		public Object toObject(String jstr);
	}

	/**
	 * Object 2 Json String
	 */
	public static String toJson(Object o) {
		return toJson(o, false);
	}
	
	public static String toJson(Object o, boolean silentfail) {
		try {
			try {
				if (o == null) { return "null"; }
				if (o instanceof String) { return string2Json((String) o); }
				if (o instanceof Boolean) { return boolean2Json((Boolean) o); }
				if (o instanceof Number) { return number2Json((Number) o); }
				if (o instanceof Integer) { return int2Json((Integer) o); }
				if (o instanceof Long) { return long2Json((Long) o); }
				if (o instanceof Character) { return char2Json((Character) o); }
				if (o instanceof Double) { return double2Json((Double) o); }
				if (o instanceof Float) { return float2Json((Float) o); }
				if (o instanceof Short) { return short2Json((Short) o); }
			}
			catch (Exception e) {
				if (MainActivity.isLogOn) {
					Log.v(TAG, "toJson(o): Exception: " + e);
				}
			}
			try {
				if (o instanceof Map) { return map2Json((Map) o, silentfail); }
			}
			catch (Exception e) {
				if (MainActivity.isLogOn) {
					Log.v(TAG, "toJson(o):map2Json Exception: " + e);
				}
			}
			if (o instanceof Object[]) { return array2Json((Object[])o, silentfail); }
			if (o instanceof int[]) { return array2Json((int[])o, silentfail); }
			if (o instanceof Integer[]) { return array2Json((Integer[])o, silentfail); }
			if (o instanceof boolean[]) { return array2Json((boolean[])o, silentfail); }
			if (o instanceof Boolean[]) { return array2Json((Boolean[])o, silentfail); }
			if (o instanceof long[]) { return array2Json((long[])o, silentfail); }
			if (o instanceof Long[]) { return array2Json((Long[])o, silentfail); }
			if (o instanceof double[]) { return array2Json((double[])o, silentfail); }
			if (o instanceof Double[]) { return array2Json((Double[])o, silentfail); }
			if (o instanceof float[]) { return array2Json((float[])o, silentfail); }
			if (o instanceof Float[]) { return array2Json((Float[])o, silentfail); }
			if (o instanceof short[]) { return array2Json((short[])o, silentfail); }
			if (o instanceof Short[]) { return array2Json((Short[])o, silentfail); }
			if (o instanceof List) { return list2Json((List<?>) o, silentfail); }
			if (o instanceof SparseArray)
				return sparseArray2Json((SparseArray) o, silentfail);
			try {
				if (o instanceof ToJson) { 
					Object rt = ((ToJson) o).toJson();
					if (rt instanceof String) {
						return (String) rt;
					}
					else {
						return toJson(rt); 
					}
				}
			}
			catch (Exception e) {
				if (MainActivity.isLogOn) {
					Log.v(TAG, "toJson(o):ToJson Exception: " + e);
				}
			}
			try {
				if (o instanceof HashSet) { return hashset2Json((HashSet) o, silentfail); }
			}
			catch (Exception e) {
				if (MainActivity.isLogOn) {
					Log.v(TAG, "toJson(o):hashset2Json Exception: " + e);
				}
			}
			if (silentfail) {
				return o + "";
			}
		}
		catch (Exception e) {
			if (MainActivity.isLogOn) {
				Log.v(TAG, "toJson(o) Exception: " + e);
			}
		}
		if (MainActivity.isLogOn) {
			Log.e(TAG, "TAG Unsupported type: "
					+ o.getClass().getName());
		}
		throw new RuntimeException("TAG Unsupported type: "
				+ o.getClass().getName());
	}

	/**
	 * String 2 Json String
	 */
	public static String string2Json(String s) {
		if (s == null) return "";
		StringBuilder sb = new StringBuilder(s.length() + 20);
		sb.append('\"');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '/':
				sb.append("\\/");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				sb.append(c);
			}
		}
		sb.append('\"');
		return sb.toString();
	}

	/**
	 * Map<String, Object> 2 Json String
	 */
	public static String map2Json(Map map) {
		return map2Json(map, false);
	}
	public static String map2Json(Map map, boolean silentfail) {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("{");

		if (map != null) {
			synchronized (map) {
				Iterator<Entry<?, ?>> iterator = map.entrySet().iterator();
				Entry<?, ?> entry;
				while (iterator.hasNext()) {
					try {
						entry = iterator.next();
						sBuilder.append('\"').append(entry.getKey())
								.append('\"').append(':')
								.append(toJson(entry.getValue(), silentfail)).append(',');
					}
					catch (Exception e) {
						if (MainActivity.isLogOn) {
							Log.v(TAG, "map2Json(o) Exception: " + e);
						}
					}
				}
			}
		}
		if (sBuilder.length() == 1)
			sBuilder.append("}");
		else
			sBuilder.setCharAt(sBuilder.length() - 1, '}');
		return sBuilder.toString();
	}

	/**
	 * HashSet<Object> 2 Json String
	 */
	public static String hashset2Json(HashSet<Object> hashSet) {
		return hashset2Json(hashSet, false);
	}
	public static String hashset2Json(HashSet<Object> hashSet, boolean silentfail) {
		StringBuilder sBuilder = new StringBuilder();
		sBuilder.append("[");

		if (hashSet != null) {
			synchronized (hashSet) {
				Iterator<Object> iterator = hashSet.iterator();
				Object entry;
				while (iterator.hasNext()) {
					try {
						entry = iterator.next();
						sBuilder.append(toJson(entry, silentfail)).append(',');
					}
					catch (Exception e) {
						if (MainActivity.isLogOn) {
							Log.v(TAG, "hashset2Json(o) Exception: " + e);
						}
					}
				}
			}
		}
		if (sBuilder.length() == 1)
			sBuilder.append("]");
		else
			sBuilder.setCharAt(sBuilder.length() - 1, ']');
		return sBuilder.toString();
	}

	static String sparseArray2Json(SparseArray<?> sparseArray) {
		return sparseArray2Json(sparseArray, false);
	}
	static String sparseArray2Json(SparseArray<?> sparseArray, boolean silentfail) {
		if (sparseArray == null) return "[]";
		int size = sparseArray.size();
		if (size == 0) return "[]";
		StringBuilder sb = new StringBuilder((size * 2) << 4);
		sb.append('[');
		for (int i = 0; i < size; i++) {
			sb.append("{\"");
			sb.append(sparseArray.keyAt(i));
			sb.append("\":");
			sb.append(toJson(sparseArray.valueAt(i), silentfail));
			sb.append("},");
		}
		// 将最后添加的 ',' 变为 ']':
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

	/**
	 * Boolean 2 Json String
	 */
	public static String boolean2Json(Boolean value) {
		if (value == null) return "";
		return value.toString();
	}
	public static String int2Json(Integer value) {
		if (value == null) return "";
		return value.toString();
	}
	public static String long2Json(Long value) {
		if (value == null) return "";
		return value.toString();
	}
	public static String double2Json(Double value) {
		if (value == null) return "";
		return value.toString();
	}
	public static String char2Json(Character value) {
		if (value == null) return "";
		return value.toString();
	}
	public static String float2Json(Float value) {
		if (value == null) return "";
		return value.toString();
	}
	public static String short2Json(Short value) {
		if (value == null) return "";
		return value.toString();
	}
	
	/**
	 * Number 2 Json String
	 * 
	 * @see Number
	 */
	public static String number2Json(Number number) {
		if (number == null) return "";
		return number.toString();
	}

	/**
	 * Array 2 Json String
	 */
	public static String array2Json(Object[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(Object[] array, boolean silentfail) {
		return arrayT2Json(array, silentfail);
	}
	public static String array2Json(int[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(int[] array, boolean silentfail) {
		return arrayT2Json(toObject(array), silentfail);
	}
	public static String array2Json(Integer[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(Integer[] array, boolean silentfail) {
		return arrayT2Json(array, silentfail);
	}
	public static String array2Json(boolean[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(boolean[] array, boolean silentfail) {
		return arrayT2Json(toObject(array), silentfail);
	}
	public static String array2Json(Boolean[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(Boolean[] array, boolean silentfail) {
		return arrayT2Json(array, silentfail);
	}
	public static String array2Json(short[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(short[] array, boolean silentfail) {
		return arrayT2Json(toObject(array), silentfail);
	}
	public static String array2Json(Short[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(Short[] array, boolean silentfail) {
		return arrayT2Json(array, silentfail);
	}
	public static String array2Json(long[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(long[] array, boolean silentfail) {
		return arrayT2Json(toObject(array), silentfail);
	}
	public static String array2Json(Long[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(Long[] array, boolean silentfail) {
		return arrayT2Json(array, silentfail);
	}
	public static String array2Json(float[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(float[] array, boolean silentfail) {
		return arrayT2Json(toObject(array), silentfail);
	}
	public static String array2Json(Float[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(Float[] array, boolean silentfail) {
		return arrayT2Json(array, silentfail);
	}
	public static String array2Json(double[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(double[] array, boolean silentfail) {
		return arrayT2Json(toObject(array), silentfail);
	}
	public static String array2Json(Double[] array) {
		return array2Json(array, false);
	}
	public static String array2Json(Double[] array, boolean silentfail) {
		return arrayT2Json(array, silentfail);
	}
	private static <T> String arrayT2Json(T[] array, boolean silentfail) {
		if (array == null) return "[]";
		if (array.length == 0) return "[]";
		StringBuilder sb = new StringBuilder(array.length << 4);
		sb.append('[');
		for (Object o : array) {
			sb.append(toJson(o, silentfail));
			sb.append(',');
		}
		// 将最后添加的 ',' 变为 ']':
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

	/**
	 * List 2 Json String
	 */
	public static String list2Json(List<?> list) {
		return list2Json(list, false);
	}
	public static String list2Json(List<?> list, boolean silentfail) {
		if (list == null) return "[]";
		if (list.size() == 0) return "[]";
		StringBuilder sb = new StringBuilder(list.size() << 4);
		sb.append('[');
		for (Object o : list) {
			sb.append(toJson(o, silentfail));
			sb.append(',');
		}
		// 将最后添加的 ',' 变为 ']':
		sb.setCharAt(sb.length() - 1, ']');
		return sb.toString();
	}

    public static Character[] toObject(final char[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new Character[0];
        }
        final Character[] result = new Character[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Character.valueOf(array[i]);
        }
        return result;
     }

    public static Long[] toObject(final long[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new Long[0];
        }
        final Long[] result = new Long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Long.valueOf(array[i]);
        }
        return result;
    }

    public static Integer[] toObject(final int[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new Integer[0];
        }
        final Integer[] result = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Integer.valueOf(array[i]);
        }
        return result;
    }

    public static Short[] toObject(final short[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new Short[0];
        }
        final Short[] result = new Short[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Short.valueOf(array[i]);
        }
        return result;
    }

    public static Byte[] toObject(final byte[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new Byte[0];
        }
        final Byte[] result = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Byte.valueOf(array[i]);
        }
        return result;
    }

    public static Double[] toObject(final double[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new Double[0];
        }
        final Double[] result = new Double[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Double.valueOf(array[i]);
        }
        return result;
    }

    public static Float[] toObject(final float[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new Float[0];
        }
        final Float[] result = new Float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Float.valueOf(array[i]);
        }
        return result;
    }

    public static Boolean[] toObject(final boolean[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new Boolean[0];
        }
        final Boolean[] result = new Boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = (array[i] ? Boolean.TRUE : Boolean.FALSE);
        }
        return result;
    }
}
