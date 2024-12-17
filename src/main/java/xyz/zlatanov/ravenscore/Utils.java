package xyz.zlatanov.ravenscore;

import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

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

	public static UUID[] addToArray(UUID id, UUID[] array) {
		val arrayList = new ArrayList<>(Arrays.asList(array));
		arrayList.add(id);
		return arrayList.toArray(UUID[]::new);
	}

	public static UUID[] replaceInArray(UUID idToReplace, UUID replaceWith, UUID[] array) {
		return Arrays.stream(array)
				.map(id -> Objects.equals(id, idToReplace) ? replaceWith : id)
				.toArray(UUID[]::new);
	}

	public static String[] trimEmpty(String[] items) {
		val trimmed = Arrays.stream(items)
				.filter(StringUtils::isNotEmpty)
				.distinct()
				.toList();
		return trimmed.toArray(new String[] {});
	}
}
