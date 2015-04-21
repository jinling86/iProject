package ca.uottawa.ljin027.iproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class is implemented for CSI5175 Bonus Assignment.
 * This class shows project list to users. It also provides adding, editing, sorting and reporting
 * functions. It can start project edit Activity and project show Activity.
 * It starts the background music when startup.
 *
 * @author Ling Jin
 * @version 1.0
 * @since 15/04/2015
 */
public class ActivityList extends ActionBarActivity {

    private ProjectManager mProjectManager = null;
    private ListView mView_ProjectList = null;
    private TextView mView_ProjectHint = null;
    private boolean mInSwitching;
    Timer mTimer = null;
    AdapterList mListAdapter;
    ArrayList<ListList> mListContent;

    private final String TAG = "<<<< Activity List >>>>";
    private final String SORT_PREFERENCE = "SortBy";
    private final String EXAMPLE_PREFERENCE = "Example";
    private final int SORT_BY_CREATED_TIME = 0;
    private final int SORT_BY_DUE_TIME = 1;
    private final int SORT_BY_IMPORTANCE = 2;
    private static boolean mFirstLaunch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Set the action bar style
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setIcon(R.drawable.ic_icon);

        if (mTimer == null) {
            mTimer = new Timer();
        }

        linkViews();

        Log.d(TAG, "Activity created");
    }

    private void linkViews() {
        mView_ProjectList = (ListView) findViewById(R.id.list_project);
        mView_ProjectHint = (TextView) findViewById(R.id.text_hint);
        mView_ProjectList.setOnItemClickListener(new ItemClickListener());
    }

    @Override
    public void onStart() {
        super.onStart();
        mInSwitching = false;
        mProjectManager = new ProjectManager(this);
        mProjectManager.deleteDuplicatedProjects();
        makeAnExample();
        sortProjects();
        // Use a timer to update remaining time
        if (mTimer == null) {
            mTimer = new Timer();
        }
        populateFields();
        startService(new Intent(this, ServiceMusic.class));
        // Use a static variable to indicate the first time launch of the activity
        // Prompt the warning dialog when the activity starts for the first time
        if (mFirstLaunch) {
            mFirstLaunch = false;
            String report = mProjectManager.getWarning();
            if (report.compareTo(ProjectManager.NO_WARNING) != 0) {
                DialogReport reportDialog = new DialogReport();
                reportDialog.setContent(report, R.color.dialog_warn_text);
                reportDialog.show(getFragmentManager(), null);
            }
        }
    }

    @Override
    public void onStop() {
        if (!mInSwitching)
            stopService(new Intent(this, ServiceMusic.class));
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        super.onStop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        // Make the app disappear from the UI, avoid activity stack operation
        moveTaskToBack(true);
        finish();
    }

    private void populateFields() {
        // Give a hint when there is no project in the system
        int projectNumber = mProjectManager.getProjectNumber();
        if (projectNumber != 0) {
            mView_ProjectHint.setVisibility(View.GONE);
            mView_ProjectList.setVisibility(View.VISIBLE);
            fillList();
        } else {
            mView_ProjectList.setVisibility(View.GONE);
            mView_ProjectHint.setVisibility(View.VISIBLE);
        }
    }

    private void sortProjects() {
        // Read the sort method in the system preference file, and sort
        int sortMethod = getPreferences(MODE_PRIVATE).getInt(SORT_PREFERENCE, SORT_BY_CREATED_TIME);
        if (sortMethod == SORT_BY_DUE_TIME)
            mProjectManager.setDateDescendingMap();
        else if (sortMethod == SORT_BY_IMPORTANCE)
            mProjectManager.setPriorityMap();
        else
            mProjectManager.setDefaultMap();
    }

    private void makeAnExample() {
        // Add an example project into system
        boolean exampleAdded = getPreferences(MODE_PRIVATE).getBoolean(EXAMPLE_PREFERENCE, false);
        if(exampleAdded == false) {
            SharedPreferences.Editor savor = getPreferences(MODE_PRIVATE).edit();
            savor.putBoolean(EXAMPLE_PREFERENCE, true);
            savor.commit();
            if(mProjectManager.getProjectNumber() == 0) {
                String id = mProjectManager.addProject();
                long currentTime = System.currentTimeMillis();
                Date dayBefore = new Date(currentTime - 24*60*60*1000);
                Date dayAfter = new Date(currentTime + 24*60*60*1000);
                String startTime = Project.getDateString(dayBefore) + Project.getTimeString(dayBefore);
                String dueDate = Project.getDateString(dayAfter) + Project.getTimeString(dayAfter);
                mProjectManager.setProject(
                        id,
                        "An Example Project",
                        "It is an example project!",
                        "Nancy ",
                        "CSI 5175",
                        startTime,
                        dueDate
                );
                mProjectManager.addTask(id, startTime, dueDate);
                mProjectManager.setTask(id, 0, "Task 1", "Example task 1", "Ling", startTime, dueDate );
                mProjectManager.addTask(id, startTime, dueDate);
                mProjectManager.setTask(id, 1, "Task 2", "Example task 2", "Ling", startTime, dueDate );
            }
        }
    }

    private void fillList() {
        mTimer.cancel();

        int projectNumber = mProjectManager.getProjectNumber();
        mListContent = new ArrayList<ListList>();
        for (int i = 0; i < projectNumber; i++) {
            String[] projectContent = new String[ProjectManager.POS_MAX];
            mProjectManager.getProjectByIndex(i, projectContent);
            String projectID = mProjectManager.getIdByIndex(i);

            mListContent.add(new ListList(
                    projectContent[ProjectManager.POS_NAME],
                    getCourseDigest(projectContent),
                    getTaskDigest(projectID),
                    getTimeDigest(projectID, projectContent),
                    projectContent[ProjectManager.POS_COMPLETION].length() != 0,
                    projectContent[ProjectManager.POS_IMPORTANCE].length() != 0,
                    getTimeProgress(projectContent),
                    getTaskProgress(projectID)
            ));
        }
        mListAdapter = new AdapterList(mListContent, this);
        mView_ProjectList.setAdapter(mListAdapter);

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new UpdateTimeTask(), 0, 1000);
    }

    private String getTaskDigest(String projectID) {
        // Composite strings to be displayed
        StringBuilder builder = new StringBuilder();
        int taskNumber = mProjectManager.getTaskNumber(projectID);
        if (taskNumber != 0) {
            int taskCompletion = 0;
            for (int i = 0; i < taskNumber; i++) {
                String[] contents = mProjectManager.getTask(projectID, i);
                if (contents[ProjectManager.POS_COMPLETION].length() != 0)
                    taskCompletion++;
            }
            if (taskCompletion <= 1) {
                builder.append(taskCompletion);
                builder.append(" task has been completed, ");
            } else {
                builder.append(taskCompletion);
                builder.append(" tasks has been completed, ");
            }

            builder.append(taskNumber - taskCompletion);
            builder.append(" left");
        } else {
            builder.append("Project does not contain sub-tasks");
        }
        return builder.toString();
    }

    private String getTimeDigest(String projectID, String[] projectContents) {
        // Composite strings to be displayed
        if (projectContents[ProjectManager.POS_COMPLETION].length() != 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("Project has been completed");
            return builder.toString();
        } else {
            StringBuilder builder = new StringBuilder();
            long currentTime = System.currentTimeMillis();
            Date dueDate = Project.getDate(projectContents[ProjectManager.POS_DUE_DATE] + projectContents[ProjectManager.POS_DUE_TIME]);
            long dueTime = dueDate.getTime();
            if (dueTime <= currentTime) {
                builder.append("Time exceeded");
            } else {
                long offset = (dueTime - currentTime) / 1000;
                long offsetSecond = offset % 60;
                long offsetMinute = (offset / 60) % 60;
                long offsetHour = (offset / (60 * 60)) % 24;
                long offsetDay = offset / (60 * 60 * 24);
                if (offsetDay <= 1) {
                    builder.append(offsetDay);
                    builder.append(" day, ");
                } else {
                    builder.append(offsetDay);
                    builder.append(" days, ");
                }
                if (offsetHour <= 1) {
                    builder.append(offsetHour);
                    builder.append(" hour, ");
                } else {
                    builder.append(offsetHour);
                    builder.append(" hours, ");
                }
                if (offsetMinute <= 1) {
                    builder.append(offsetMinute);
                    builder.append(" minute, ");
                } else {
                    builder.append(offsetMinute);
                    builder.append(" minutes, ");
                }
                if (offsetSecond <= 1) {
                    builder.append(offsetSecond);
                    builder.append(" second, left");
                } else {
                    builder.append(offsetSecond);
                    builder.append(" seconds left");
                }

            }

            return builder.toString();
        }
    }

    private String getCourseDigest(String[] projectContents) {
        // Composite strings to be displayed
        StringBuilder builder = new StringBuilder();
        builder.append(projectContents[ProjectManager.POS_COURSE_NAME]);
        builder.append(", instructed by ");
        builder.append(projectContents[ProjectManager.POS_INSTRUCTOR]);
        return builder.toString();
    }

    private int getTimeProgress(String[] projectContents) {
        // Compute remaining time
        long currentTime = System.currentTimeMillis();
        Date startDate = Project.getDate(projectContents[ProjectManager.POS_START_DATE] + projectContents[ProjectManager.POS_START_TIME]);
        Date dueDate = Project.getDate(projectContents[ProjectManager.POS_DUE_DATE] + projectContents[ProjectManager.POS_DUE_TIME]);
        long startTime = startDate.getTime();
        long dueTime = dueDate.getTime();
        if (dueTime > currentTime && startTime < currentTime)
            return (int) (100 * (currentTime - startTime) / (dueTime - startTime));
        else if (currentTime > dueTime)
            return 100;
        else
            return 0;
    }

    private int getTaskProgress(String projectID) {
        // Compute remaining tasks
        int taskNumber = mProjectManager.getTaskNumber(projectID);
        int taskCompletion = 0;
        for (int i = 0; i < taskNumber; i++) {
            String[] contents = mProjectManager.getTask(projectID, i);
            if (contents[ProjectManager.POS_COMPLETION].length() != 0)
                taskCompletion++;
        }
        if (taskNumber == 0)
            return 100;
        else
            return 100 * taskCompletion / taskNumber;
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

        // Store sorting method to system preference
        if (id == R.id.action_sort_by_creation) {
            mProjectManager.setDefaultMap();
            populateFields();
            SharedPreferences.Editor savor = getPreferences(MODE_PRIVATE).edit();
            savor.putInt(SORT_PREFERENCE, SORT_BY_CREATED_TIME);
            savor.commit();
            Toast.makeText(this, "List Sorted by Creation Time!", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_sort_by_due) {
            mProjectManager.setDateDescendingMap();
            populateFields();
            SharedPreferences.Editor savor = getPreferences(MODE_PRIVATE).edit();
            savor.putInt(SORT_PREFERENCE, SORT_BY_DUE_TIME);
            savor.commit();
            Toast.makeText(this, "List Sorted by Due Date!", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_sort_by_importance) {
            mProjectManager.setPriorityMap();
            populateFields();
            SharedPreferences.Editor savor = getPreferences(MODE_PRIVATE).edit();
            savor.putInt(SORT_PREFERENCE, SORT_BY_IMPORTANCE);
            savor.commit();
            Toast.makeText(this, "List Sorted by Importance!", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.action_show_doing) {
            String report = mProjectManager.getOngoingReport();
            DialogReport reportDialog = new DialogReport();
            reportDialog.setContent(report, R.color.dialog_report_text);
            reportDialog.show(getFragmentManager(), null);
            Log.d(TAG, report);
            return true;
        }
        if (id == R.id.action_show_warn) {
            String report = mProjectManager.getWarning();
            DialogReport reportDialog = new DialogReport();
            reportDialog.setContent(report, R.color.dialog_warn_text);
            reportDialog.show(getFragmentManager(), null);
            Log.d(TAG, report);
            return true;
        }
        if (id == R.id.action_show_about) {
            StringBuilder builder = new StringBuilder();
            builder.append("Thank you for using iProject!\n\n");
            builder.append("Author  : Ling Jin\n");
            builder.append("Email   : ljin027@uottawa.ca\n");
            builder.append("Address : uOttawa, ON, Canada\n");

            DialogReport reportDialog = new DialogReport();
            reportDialog.setContent(builder.toString(), R.color.dialog_about_text);
            reportDialog.show(getFragmentManager(), null);
            Log.d(TAG, builder.toString());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position, long rowId) {
            // Show details of a project
            mInSwitching = true;
            Intent intent = new Intent(getApplicationContext(), ActivityShow.class);
            intent.putExtra(ProjectManager.PROJECT_ID, mProjectManager.getIdByIndex(position));
            startActivity(intent);
        }
    }

    class UpdateTimeTask extends TimerTask {
        public void run() {
            // Do processing in activity thread, views cannot be modified in the timer thread!
            runOnUiThread(InternalTimeTask);
        }
    }

    private Runnable InternalTimeTask = new Runnable() {
        public void run() {
            // Update the remaining time
            int projectNumber = mProjectManager.getProjectNumber();
            if (mListContent == null || mListAdapter == null)
                return;
            if (mListContent.size() != projectNumber) {
                Log.d(TAG, "Timer state error!");
                return;
            }
            for (int i = 0; i < projectNumber; i++) {
                String[] projectContent = new String[ProjectManager.POS_MAX];
                mProjectManager.getProjectByIndex(i, projectContent);
                String projectID = mProjectManager.getIdByIndex(i);
                mListContent.get(i).setTimeDigest(getTimeDigest(projectID, projectContent));
            }
            mListAdapter.notifyDataSetChanged();
            Log.d(TAG, "Time updated!");
        }
    };
}