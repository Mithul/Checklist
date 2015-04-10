package com.example.mithul.checklist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    DataHolder data = DataHolder.getInstance();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private ArrayList<CheckBox> checklist = new ArrayList<>();
    LinearLayout checklist_table;
    LayoutInflater checklist_inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        checklist_table = (LinearLayout) findViewById(R.id.checklist_layout);
        checklist_inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    private void populateCheckList() {
        for (int i = 0; i < checklist.size(); i++)
//            CheckBox item1 = new CheckBox(this);
//        item1.setText();
            checklist_table.addView(checklist.get(i));
    }

    public void onSectionAttached(int number) {
        if (data.sections.size() >= number) {
            mTitle = data.sections.get(number - 1);
            populateCheckList();
        }
        else
            mTitle = "Checklist";
//        switch (number) {
//            case 1:
//                mTitle = getString(R.string.title_section1);
//                break;
//            case 2:
//                mTitle = getString(R.string.title_section2);
//                break;
//            case 3:
//                mTitle = getString(R.string.title_section3);
//                break;
//        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }


    Button b;
    EditText edit;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        LinearLayout parent = (LinearLayout) checklist_inflater.inflate(R.layout.activity_main, null);

        int id = item.getItemId();
        if (id == R.id.add_item) {
            edit = new EditText(this);
//            edit.setText(String.valueOf(id));
//        ViewGroup.LayoutParams params =new ViewGroup.LayoutParams(100,50);
//        edit.setLayoutParams(params);
//        params.width=100;
            b = new Button(this);
            b.setText("Save");
//        LinearLayout l = R.id.linearLayout2;
            checklist_table.addView(edit);
            checklist_table.addView(b);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox c = new CheckBox(MainActivity.this);
                    checklist.add(c);
                    c.setText(edit.getText());
                    checklist_table.removeView(b);
                    checklist_table.removeView(edit);
                    Toast.makeText(MainActivity.this, "Added item", Toast.LENGTH_SHORT);
                    checklist_table.addView(c);
                }
            });
        } else if (id == R.id.add_checklist) {
            edit = new EditText(this);
            b = new Button(this);
            b.setText("Save");
            checklist_table.addView(edit);
            checklist_table.addView(b);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String n = new String();
                    n = edit.getText().toString();
                    checklist_table.removeView(b);
                    checklist_table.removeView(edit);
                    Toast.makeText(MainActivity.this, "Added Checklist", Toast.LENGTH_SHORT);
                    data.sections.add(n);
                }
            });
//            edit.setText(String.valueOf(id));

        }
//        c.setText(id);
//        c.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                Toast.makeText(MainActivity.this,"Long press",Toast.LENGTH_SHORT);
//                return false;
//            }
//        });
//        checklist_table.addView(c);
//        populateCheckList();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
