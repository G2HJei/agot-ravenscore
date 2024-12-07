package xyz.zlatanov.ravenscore;

import java.time.format.DateTimeFormatter;

import lombok.val;

public class Utils {

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static String capitalizeFirstLetter(Object o) {
		val enumName = o.toString().toLowerCase();
		val firstLetter = enumName.substring(0, 1).toUpperCase();
		val restOfString = enumName.substring(1);
		return firstLetter + restOfString;
	}
}
