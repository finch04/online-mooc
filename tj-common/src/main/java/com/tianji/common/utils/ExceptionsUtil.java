package com.tianji.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @Description 异常转换工具
 */
public class ExceptionsUtil {

	/**
	 *
	 * <b>方法名：</b>：unchecked<br>
	 * <b>功能说明：</b>：将CheckedException转换为UncheckedException.<br>
	 */
	public static RuntimeException unchecked(Exception e) {
		if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}

	/**
	 *
	 * <b>方法名：</b>：getStackTraceAsString<br>
	 * <b>功能说明：</b>：将ErrorStack转化为String<br>
	 */
	public static String getStackTraceAsString(Exception e) {
		StringWriter stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}

	/**
	 *
	 * <b>方法名：</b>：getErrorMessageWithNestedException<br>
	 * <b>功能说明：</b>：获取组合本异常信息与底层异常信息的异常描述, 适用于本异常为统一包装异常类，底层异常才是根本原因的情况<br>
	 */
	public static String getErrorMessageWithNestedException(Exception e) {
		Throwable nestedException = e.getCause();
		return new StringBuilder().append(e.getMessage()).append(" nested exception is ")
				.append(nestedException.getClass().getName()).append(":").append(nestedException.getMessage())
				.toString();
	}

	/**
	 *
	 * <b>方法名：</b>：isCausedBy<br>
	 * <b>功能说明：</b>：判断异常是否由某些底层的异常引起<br>
	 */
	public static boolean isCausedBy(Exception ex, @SuppressWarnings("unchecked") Class<? extends Exception>... causeExceptionClasses) {
		Throwable cause = ex;
		while (cause != null) {
			for (Class<? extends Exception> causeClass : causeExceptionClasses) {
				if (causeClass.isInstance(cause)) {
					return true;
				}
			}
			cause = cause.getCause();
		}
		return false;
	}

	/**
	 *
	 * <b>方法名：</b>：convertReflectionExceptionToUnchecked<br>
	 * <b>功能说明：</b>：将反射时的checked exception转换为unchecked exception.<br>
	 */
	public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
		if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException || e instanceof NoSuchMethodException) {
			return new IllegalArgumentException("Reflection Exception.", e);
		} else if (e instanceof InvocationTargetException) {
			return new RuntimeException("Reflection Exception.", ((InvocationTargetException) e).getTargetException());
		} else if (e instanceof RuntimeException) {
			return (RuntimeException) e;
		}
		return new RuntimeException("Unexpected Checked Exception.", e);
	}
}
