package com.example.mithul.checklist;

import java.util.ArrayList;

/**
 * Created by mithul on 9/4/15.
 */
public class DataHolder {
    ArrayList<String> sections = new ArrayList<String>();
    private static final DataHolder holder = new DataHolder();

    public static DataHolder getInstance() {
        return holder;
    }
}
