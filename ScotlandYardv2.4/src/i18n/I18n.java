package i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 
 * @author Johannes Jowereit
 * @version 2.4 (19-APR-2010)
 */
public class I18n {

	public static String tr(String sourceText) {
		try {
			ResourceBundle rb = ResourceBundle.getBundle("Translation", Locale.getDefault());
			return rb.getString(sourceText);
		} catch (Exception e) {
			e.printStackTrace();
			return "_" + sourceText + "_";
		}
	}
	
	public static String tr(String key, Object... arguments) {
		return MessageFormat.format(tr(key), arguments);
	}
	
}
