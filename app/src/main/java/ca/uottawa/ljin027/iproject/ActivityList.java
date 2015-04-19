package ca.uottawa.ljin027.iproject;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityList extends ActionBarActivity {

    private ProjectManager mProjectManager = null;
    private ListView mView_ProjectList = null;
    private TextView mView_ProjectHint = null;
    private boolean mInSwitching;

    private final String TAG = "<<<< Activity List >>>>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        // Set the action bar style
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setIcon(R.drawable.ic_icon);

        // Store the handle
        mView_ProjectList = (ListView)findViewById(R.id.list_project);
        mView_ProjectHint = (TextView)findViewById(R.id.text_hint);
        mView_ProjectList.setOnItemClickListener(new ItemClickListener());

        Log.d(TAG, "Activity created");

        mInSwitching = false;
        startService(new Intent(this, ServiceMusic.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        mProjectManager = new ProjectManager(this);
        fillList();
    }

    @Override
    public void onStop() {
        if(!mInSwitching)
            stopService(new Intent(this, ServiceMusic.class));
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void fillList() {
        int projectNumber = mProjectManager.getProjectNumber();
        if(projectNumber != 0) {
            mView_ProjectHint.setVisibility(View.GONE);
            mView_ProjectList.setVisibility(View.VISIBLE);

            String[] from = new String[]{"name"};
            int[] to = new int[]{R.id.text_list_name};

            List<HashMap<String, String>> nodeList = new ArrayList<HashMap<String, String>>();
            for (int i = 0; i < projectNumber; i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                String[] projectContent = new String[ProjectManager.POS_MAX];
                mProjectManager.getProjectByIndex(i, projectContent);
                map.put("name", projectContent[ProjectManager.POS_NAME]);
                nodeList.add(map);
            }

            SimpleAdapter adapter = new SimpleAdapter(this, nodeList, R.layout.project_listitem, from, to);
            mView_ProjectList.setAdapter(adapter);
        } else {
            mView_ProjectList.setVisibility(View.GONE);
            mView_ProjectHint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_project) {
            mInSwitching = true;
            Intent intent = new Intent(getApplicationContext(), ActivityEdit.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position, long rowId) {
            mInSwitching = true;
            Intent intent = new Intent(getApplicationContext(), ActivityShow.class);
            intent.putExtra(ProjectManager.PROJECT_ID, mProjectManager.getIdByIndex(position));
            startActivity(intent);
        }
    }
}