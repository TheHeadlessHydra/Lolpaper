package hyoma.app.lollivewallpaper;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.Toast;

/*
public class MyPreferencesActivity extends PreferenceFragment{
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}

*/

public class MyPreferencesActivity extends PreferenceActivity {
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set the preference page to load
		addPreferencesFromResource(R.xml.lollivewallpaper_settings);
	}

	Preference.OnPreferenceChangeListener numberCheckListener = new OnPreferenceChangeListener() {

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			// Check that the string is an integer
			//if (newValue != null && newValue.toString().length() > 0 && newValue.toString().matches("\\d*")) {
			//	return true;
			//}
			// now create a message to the user
			Toast.makeText(MyPreferencesActivity.this, "Invalid Input", Toast.LENGTH_SHORT).show();
			return false;
		}
	};
}
