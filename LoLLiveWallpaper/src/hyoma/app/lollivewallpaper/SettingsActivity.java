// *****************************************************************************
//***************************
/*
This is what happens when the settings button is pressed. 
*/
//***************************
// *****************************************************************************

package hyoma.app.lollivewallpaper;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
}