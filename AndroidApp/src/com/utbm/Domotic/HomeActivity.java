package com.utbm.Domotic;

import com.utbm.pandaboarddomotic.R;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class HomeActivity extends Activity{

    // Use the Fragment Manager and a Fragment Transaction to handle each fragment
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // show the main fragment when application starts
        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(android.R.id.content, new HomeFragment());
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.context_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle click on setting option
        switch(item.getItemId()){
            case(R.id.settings):
                return startSettings(); 
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean startSettings(){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack("settings")
            .replace(android.R.id.content, new SettingsFragment())
            .commit();
        return true;
    }
}
