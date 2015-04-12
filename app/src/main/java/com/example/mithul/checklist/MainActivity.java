package com.example.mithul.checklist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    DataHolder data = DataHolder.getInstance();

    DBHelper help;
    SQLiteDatabase db;

    private View mProgressView;

    private ScheduleClient scheduleClient;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private ArrayList<CheckBox> checklist = new ArrayList<>();
    private ArrayList<DateCheckbox> datelist = new ArrayList<>();
    LinearLayout checklist_table;
    LayoutInflater checklist_inflater;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mProgressView = findViewById(R.id.login_progress_main);

        help = new DBHelper(this);
        db = openOrCreateDatabase(help.DATABASE_NAME, MODE_PRIVATE, null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        checklist_table = (LinearLayout) findViewById(R.id.checklist_layout);
        checklist_inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        for (int i = 0; i < data.sections.size(); ) {
            data.sections.remove(i);
        }
        i = this.getIntent();

        final DatePicker d = (DatePicker) findViewById(R.id.datePicker);
        final TimePicker p = (TimePicker) findViewById(R.id.timePicker);
        Button set_date = (Button) findViewById(R.id.set_date);
        d.setVisibility(View.GONE);
        p.setVisibility(View.GONE);
        d.setCalendarViewShown(false);
        set_date.setVisibility(View.GONE);

        scheduleClient = new ScheduleClient(this);
        scheduleClient.doBindService();

        try {
            Log.w("token", i.getStringExtra("token"));
            if (i.getStringExtra("token").trim().equals("reminder"))
                i.putExtra("token", "phone");

        } catch (NullPointerException e) {
            i.putExtra("token", "phone");
            Log.e("Intent", e.toString());
        }
        db.execSQL("CREATE TABLE IF NOT EXISTS checklistsAdmin(name VARCHAR(30) primary key);");
        Log.w("sql", "Opening");

        try {
            if (i.getStringExtra("token").trim().equals("phone")) {
                showProgress(false);
                Log.w("debug", "Detected phone");
                Cursor rs = db.rawQuery("select * from checklistsAdmin;", null);
                Log.w("sql", "select * from " + mTitle + ";");
                rs.moveToFirst();
                do {
                    data.sections.add(rs.getString(0));
                } while (rs.moveToNext());
            } else if (i.getStringExtra("token").trim().equals("online")) {
                Log.w("debug", "Detected online");
                new RetrieveChecklist().execute();

            }
        } catch (CursorIndexOutOfBoundsException e) {
            db.execSQL("insert into checklistsAdmin values('Starter')");
            data.sections.add("Starter");
            Log.e("sql", e.toString());
        }

        try {
            for (int i = 0; i < data.sections.size(); i++)
                if (this.i.getStringExtra("title").trim().equals(data.sections.get(i))) {
//                onSectionAttached(i);
                    break;
                }
        } catch (Exception e) {
            i.putExtra("token", "phone");
            Log.e("Intent", e.toString());
        }

    }

    public void showProgress(final boolean show) {
        if (mProgressView == null)
            mProgressView = findViewById(R.id.login_progress_main);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }


    public boolean isTableExists(CharSequence tableName) {

//        if(openDb) {
//            if(mDatabase == null || !mDatabase.isOpen()) {
//                mDatabase = getReadableDatabase();
//            }
//
//            if(!mDatabase.isReadOnly()) {
//                mDatabase.close();
//                mDatabase = getReadableDatabase();
//            }
//        }

        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                Log.w("sql", "exists");
                return true;
            }
            cursor.close();
        }
        Log.w("sql", "does not exist");
        return false;
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
        for (int i = 0; i < checklist.size(); i++) ;
//            CheckBox item1 = new CheckBox(this);
//        item1.setText();
//            checklist_table.addView(checklist.get(i));
    }

    public void onSectionAttached(int number) {
        if (data.sections.size() >= number) {
            mTitle = data.sections.get(number - 1);
            for (int i = 0; i < checklist.size(); ) {
                checklist_table.removeView(checklist.get(i));
                checklist.remove(i);
            }
            Log.w("menu", i.getStringExtra("token"));
            if (i.getStringExtra("token").trim().equals("phone")) {
                showProgress(false);
                Log.w("intent", "Phone table");
                db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName(mTitle) + " \n" +
                        "(name VARCHAR(32),checked BOOLEAN); ");
                db.execSQL("CREATE TABLE IF NOT EXISTS checklistsAdmin(name VARCHAR(30) primary key);");
                Log.w("sql", "Opening");
                if (isTableExists(mTitle)) {
                    Cursor rs = db.rawQuery("select * from " + mTitle + ";", null);
                    Log.w("sql", "select * from " + mTitle + ";");
                    rs.moveToFirst();
                    try {
                        do {
                            boolean check = rs.getInt(1) == 1;

                            CheckBox c = new CheckBox(this);
                            c.setText(rs.getString(0));
                            if (check)
                                c.setChecked(true);
                            Log.w("sql", rs.getString(0) + " " + rs.getString(1) + " " + check);
                            checklist.add(c);
                            checklist_table.addView(c);
                        } while (rs.moveToNext());
                    } catch (CursorIndexOutOfBoundsException e) {

//                    checklist = new ArrayList<>();
                        Log.e("sql", e.toString());
                    }
                    // Set up the drawer.

                }
            } else if (i.getStringExtra("token").trim().equals("online")) {
                int x = data.ids.get(number - 1);

                new RetrieveItemList().execute(String.valueOf(x));
            }


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

    public String tableName(CharSequence name) {
        return TextUtils.join("_", name.toString().split(" "));
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
                    Toast.makeText(MainActivity.this, "Added item", Toast.LENGTH_SHORT).show();
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
                    try {
                        db.execSQL("insert into checklistsAdmin values('" + n + "');");
                    } catch (Exception e) {
                        Log.e("sql", e.toString());
                    }
                    Toast.makeText(MainActivity.this, "Added Checklist", Toast.LENGTH_SHORT).show();
                    data.sections.add(n);
                }
            });
//            edit.setText(String.valueOf(id));

        } else if (id == R.id.save) {

            db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName(mTitle) + " \n" +
                    "(name VARCHAR(32),checked INTEGER); ");
            try {
                db.execSQL("insert into checklistsAdmin values('" + tableName(mTitle) + "');");
            } catch (Exception e) {
                Log.e("sql", e.toString());
            }
            db.execSQL("delete from " + tableName(mTitle) + ";");

            Log.w("checklist", checklist.size() + "");
            for (int i = 0; i < checklist.size(); i++) {
                boolean x1 = checklist.get(i).isChecked();
                int x = 0;
                if (x1)
                    x = 1;
//                if(checklist.get(i).isChecked())
//                    x=1;
                db.execSQL("insert into " + tableName(mTitle) + " values('" + checklist.get(i).getText() + "'," + x + ");");
                Log.w("sql", "insert into " + tableName(mTitle) + " values('" + checklist.get(i).getText() + "'," + x + ");");
            }
            Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();

//            DBHelper db= new DBHelper(this);
        } else if (id == R.id.delete_item) {
            for (int i = 0; i < checklist.size(); i++) {
                boolean x1 = checklist.get(i).isChecked() && !this.i.getStringExtra("token").trim().equals("online");
                if (x1) {
                    db.execSQL("delete from " + tableName(mTitle) + " where name='" + checklist.get(i).getText() + "';");
                    Log.w("sql", "delete from " + mTitle + " where name='" + checklist.get(i).getText() + "';");
                    checklist_table.removeView(checklist.get(i));
                    checklist.remove(i);
                    i--;
                } else if (checklist.get(i).isChecked()) {
                    checklist_table.removeView(checklist.get(i));
                    checklist.remove(i);
                    i--;
                }
                Toast.makeText(MainActivity.this, "Deleted Checked Items", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.delete_checklist) {
            for (int i = 0; i < checklist.size(); i++) {
                boolean x1 = checklist.get(i).isChecked();
                if (!this.i.getStringExtra("token").trim().equals("online")) {
                    db.execSQL("delete from " + mTitle + " where name='" + checklist.get(i).getText() + "';");
                    Log.w("sql", "delete from " + mTitle + " where name='" + checklist.get(i).getText() + "';");
                    db.execSQL("delete from checklistsAdmin where name='" + tableName(mTitle) + "';");
                    Log.w("sql", "delete from checklistsAdmin where name='" + tableName(mTitle) + "';");
                }
                checklist_table.removeView(checklist.get(i));
                checklist.remove(i);
            }
            for (int i = 0; i < data.sections.size(); i++)
                if (data.sections.get(i) == mTitle) {
                    data.sections.remove(i);
                    break;
                }
            mNavigationDrawerFragment.moveTo(0);
            if (data.sections.size() > 0)
                mTitle = data.sections.get(0);
            else {
                db.execSQL("insert into checklistsAdmin values('Starter')");
                data.sections.add("Starter");
            }
            restoreActionBar();
            Toast.makeText(MainActivity.this, "Deleted Checklist", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.sync) {
            Log.w("online", "Trying to sync");
            JSONArray send_data = new JSONArray();
            try {
                JSONArray sections = new JSONArray();
                for (int i = 0; i < checklist.size(); i++) {
                    JSONObject item1 = new JSONObject();
                    item1.accumulate("name", checklist.get(i).getText());
                    item1.accumulate("checked", checklist.get(i).isChecked());
                    send_data.put(item1);
                }
                for (int i = 0; i < data.sections.size(); i++) {
                    JSONObject item1 = new JSONObject();
                    item1.accumulate("name", data.sections.get(i));
                    sections.put(item1);
                }
                JSONObject final_send = new JSONObject();
                final_send.accumulate("name", mTitle);
                final_send.accumulate("items", send_data);
                final_send.accumulate("checklists", sections);
                Log.w("JSON", final_send.toString());
                new SyncChecklist().execute(final_send);
            } catch (JSONException e) {
                Log.e("JSON", e.toString());
                e.printStackTrace();
            }
        } else if (id == R.id.remind) {
            final DatePicker d = (DatePicker) findViewById(R.id.datePicker);
            final TimePicker p = (TimePicker) findViewById(R.id.timePicker);
            Button set_date = (Button) findViewById(R.id.set_date);
            set_date.setVisibility(View.VISIBLE);
            d.setVisibility(View.VISIBLE);
            p.setVisibility(View.VISIBLE);
            set_date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Date date = new Date(d.getYear() - 1900, d.getMonth(), d.getDayOfMonth(), p.getCurrentHour(), p.getCurrentMinute());
                    SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:MM");
                    String datetime = ft.format(date);
                    Log.w("date", datetime + " " + date.toString());
                    String reminder = "";
                    for (int i = 0; i < checklist.size(); i++) {
                        if (checklist.get(i).isChecked()) {
                            DateCheckbox x = new DateCheckbox();
                            x.id = i;
                            x.d = date;
                            datelist.add(x);
                            reminder += checklist.get(i).getText() + " ";

                            // Ask our service to set an alarm for that date, this activity talks to the client that talks to the service

                        }
                    }
                    Calendar c = Calendar.getInstance();
                    c.set(date.getYear(), date.getMonth(), date.getDay(), date.getHours(), date.getMinutes());
                    c.set(Calendar.SECOND, 0);
                    scheduleClient.setAlarmForNotification(c, reminder, mTitle, i.getStringExtra("token"));
                    Toast.makeText(MainActivity.this, "Notification set for: " + date.toString(), Toast.LENGTH_SHORT).show();
                }
            });

        }
        Log.w("Menu", id + " " + R.id.sync);
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


    class RetrieveChecklist extends AsyncTask<String, Void, JSONArray> {

        private Exception exception;

        protected JSONArray doInBackground(String... urls) {
            showProgress(true);
            JSONArray list = new JSONArray();
            try {
//                URL url= new URL(urls[0]);
                List<NameValuePair> params = new LinkedList<NameValuePair>();

                params.add(new BasicNameValuePair("user_email", data.user_email));
                params.add(new BasicNameValuePair("user_token", data.auth_token));
                String paramString = URLEncodedUtils.format(params, "utf-8");

                String URL = "http://mithul.guindytimes.com/checklists.json?";
                URL += paramString;
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet x = new HttpGet(URL);
                //sets the post request as the resulting string
                //sets a request header so the page receving the request
                //will know what to do with it
                x.setHeader("Accept", "application/json");
                x.setHeader("Content-type", "application/json");
//                x.setHeader("host","http://192.168.1.5");
                HttpResponse response = httpclient.execute(x);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    String responseString = out.toString();
                    out.close();
                    JSONArray result = new JSONArray(out.toString());
                    Log.w("JSON", result.toString());
                    Log.w("html", out.toString());
                    //..more logic
                    return result;
                } else {
                    //Closes the connection.
                    Log.w("JSON", "something wrong " + statusLine.getStatusCode());
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
//                return list;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("JSON", e.toString());
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(JSONArray list) {
            // TODO: check this.exception
            try {
                Log.w("JSON", list.length() + " length");
                for (int i = 0; i < list.length(); i++) {
                    data.sections.add(((JSONObject) list.get(i)).getString("name"));
                    data.ids.add(Integer.parseInt(((JSONObject) list.get(i)).getString("id")));
                    Log.w("JSON", ((JSONObject) list.get(i)).getString("name"));
                }
            } catch (JSONException e) {
                signout();
                Log.e("JSON", e.toString());
            }
            showProgress(false);

        }
    }


    class RetrieveItemList extends AsyncTask<String, Void, JSONArray> {

        private Exception exception;

        protected JSONArray doInBackground(String... urls) {
            showProgress(true);
            JSONArray list = new JSONArray();
            try {
//                URL url= new URL(urls[0]);
                List<NameValuePair> params = new LinkedList<NameValuePair>();

                params.add(new BasicNameValuePair("user_email", data.user_email));
                params.add(new BasicNameValuePair("user_token", data.auth_token));
                String paramString = URLEncodedUtils.format(params, "utf-8");

                String URL = "http://mithul.guindytimes.com/checklists/" + urls[0] + ".json?";
                URL += paramString;
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet x = new HttpGet(URL);
                //sets the post request as the resulting string
                //sets a request header so the page receving the request
                //will know what to do with it
                x.setHeader("Accept", "application/json");
                x.setHeader("Content-type", "application/json");
//                x.setHeader("host","http://192.168.1.5");
                HttpResponse response = httpclient.execute(x);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    String responseString = out.toString();
                    out.close();
                    JSONObject temp = new JSONObject(out.toString());
                    JSONArray result = new JSONArray(temp.getString("items"));
                    Log.w("JSON", result.toString());
                    Log.w("html", out.toString());
                    //..more logic
                    return result;
                } else {
                    //Closes the connection.
                    Log.w("JSON", "something wrong " + statusLine.getStatusCode());
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
//                return list;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("JSON", e.toString());
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(JSONArray list) {
            // TODO: check this.exception
            try {
                Log.w("JSON", list.length() + " length");
                for (int i = 0; i < list.length(); i++) {
                    CheckBox c = new CheckBox(MainActivity.this);
                    c.setText(((JSONObject) list.get(i)).getString("name"));
                    c.setChecked(((JSONObject) list.get(i)).getString("checked") == "true");
                    checklist.add(c);
                    checklist_table.addView(c);
                    Log.w("JSON", ((JSONObject) list.get(i)).getString("name"));
                }
            } catch (JSONException e) {
                signout();
                Log.e("JSON", e.toString());
            }
            showProgress(false);
        }
    }


    class SyncChecklist extends AsyncTask<JSONObject, Void, JSONArray> {

        private Exception exception;

        protected JSONArray doInBackground(JSONObject... send_data) {
            showProgress(true);
            JSONArray list = new JSONArray();
            try {
//                URL url= new URL(urls[0]);
                List<NameValuePair> params = new LinkedList<NameValuePair>();

                send_data[0].accumulate("user_email", data.user_email);
                send_data[0].accumulate("user_token", data.auth_token);
                String paramString = URLEncodedUtils.format(params, "utf-8");

                String URL = "http://mithul.guindytimes.com/sync_checklist?";
                URL += paramString;
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost x = new HttpPost(URL);
                //sets the post request as the resulting string
                //sets a request header so the page receving the request


                //will know what to do with it

                StringEntity se = new StringEntity(send_data[0].toString());

                x.setEntity(se);
                x.setHeader("Accept", "application/json");
                x.setHeader("Content-type", "application/json");
//                x.setHeader("host","http://192.168.1.5");
                HttpResponse response = httpclient.execute(x);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    String responseString = out.toString();
                    out.close();
                    JSONObject temp = new JSONObject(out.toString());
                    Log.w("JSON", temp.toString());
//                    JSONArray result = new JSONArray(temp.getString("items"));
//                    Log.w("JSON", result.toString());
                    Log.w("html", out.toString());
                    //..more logic
//                    return result;
                } else {
                    //Closes the connection.
                    signout();
                    Log.w("JSON", "something wrong " + statusLine.getStatusCode());
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
//                return list;
            } catch (Exception e) {

                e.printStackTrace();
                Log.e("JSON", e.toString());
                this.exception = e;
                return null;
            }
            return null;
        }

        protected void onPostExecute(JSONArray list) {
            // TODO: check this.exception
            Toast.makeText(MainActivity.this, "Synced !", Toast.LENGTH_SHORT).show();
            showProgress(false);
        }
    }

    private void signout() {
        db.execSQL("CREATE TABLE IF NOT EXISTS sessions_table(email VARCHAR(30),token VARCHAR(50));");
        db.execSQL("delete from sessions_table;");
        Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    @Override
    protected void onStop() {
        // When our activity is stopped ensure we also stop the connection to the service
        // this stops us leaking our activity into the system *bad*
        if (scheduleClient != null)
            scheduleClient.doUnbindService();
        super.onStop();
    }

    class DateCheckbox {
        int id;
        Date d = new Date();
    }
}





