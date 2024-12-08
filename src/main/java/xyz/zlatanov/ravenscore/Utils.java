package xyz.zlatanov.ravenscore;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

import lombok.val;

public class Utils {

	public static final DecimalFormat		DECIMAL_FORMATTER	= new DecimalFormat("#,##0.00");
	public static final DateTimeFormatter	DATE_FORMATTER		= DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static String capitalizeFirstLetter(Object o) {
		val enumName = o.toString().toLowerCase();
		val firstLetter = enumName.substring(0, 1).toUpperCase();
		val restOfString = enumName.substring(1);
		return firstLetter + restOfString;
	}
}
