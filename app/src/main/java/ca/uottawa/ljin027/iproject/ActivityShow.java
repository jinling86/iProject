package ca.uottawa.ljin027.iproject;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class is implemented for CSI5175 Bonus Assignment.
 * This class displays the detail information of a project in the UI. It also computes the remaining
 * time and tasks of the project.
 * The project and sub-tasks can be marked as done in this activity. After marking as done, the
 * project/sub-task cannot be further edit. But the completed project can be deleted.
 * This activity supports the project sharing functions. It can generate an ipj(iProject) file and
 * invoke mail apps to send it out. It can also read and import the ipj file from external storage.
 * The newly imported file has a newly assigned internal name, so a file can be imported for
 * multiple times.
 * The app should go back to the list activity when finish.
 *
 * @author Ling Jin
 * @version 1.0
 * @since 15/04/2015
 */
public class ActivityShow extends ActionBarActivity {
    private static final String PROJECT_ID = "ID";
    private static final String PROJECT_CONTENT = "CONTENT";
    private static final String PROJECT_COMPLETION = "COMPLETION";
    private static final String PROJECT_IMPORTANCE = "IMPORTANCE";

    private ProjectManager mProjectManager;
    private String mProjectID;
    private String[] mProjectContents;
    // In switching indicates another activity of the app is started when this activity is stopped
    private boolean mInSwitching;
    private boolean mCurrentImportance;
    private boolean mCurrentCompletion;
    private int mCurrentCompletedTasks;
    private int mCurrentTaskNumber;

    private TextView mText_ProjectName;
    private TextView mText_CourseName;
    private TextView mText_CourseInstructor;
    private TextView mText_ProjectStartTime;
    private TextView mText_ProjectDueDate;
    private TextView mText_ProjectDescription;

    private ProgressBar mBar_TimeProgress;
    private ProgressBar mBar_TaskProgress;

    private ImageButton mButton_ImportanceMarker;
    private Button mButton_Done;
    private Button mButton_Delete;
    private ListView mList;

    private static final String TAG = "<<<< Activity Show >>>>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Process file import
        readExternalFile();

        // Use different layout configurations
        int rotation = getWindow().getWindowManager().getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            setContentView(R.layout.activity_show_landscape);
        } else {
            setContentView(R.layout.activity_show);
        }

        // Set the action bar style
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setTitle(R.string.show_title);
        actionBar.setDisplayHomeAsUpEnabled(true);

        linkViews();
        mProjectManager = new ProjectManager(this);
        if (savedInstanceState != null) {
            mProjectID = (String) savedInstanceState.getSerializable(PROJECT_ID);
            mProjectContents = (String[]) savedInstanceState.getSerializable(PROJECT_CONTENT);
            mCurrentCompletion = (Boolean) savedInstanceState.getSerializable(PROJECT_COMPLETION);
            mCurrentImportance = (Boolean) savedInstanceState.getSerializable(PROJECT_IMPORTANCE);

            startService(new Intent(this, ServiceMusic.class));
        } else {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mProjectID = extras.getString(ProjectManager.PROJECT_ID);
            } else {
                Log.d(TAG, "Activity receives no extra!");
            }
            mProjectContents = new String[ProjectManager.POS_MAX];
            mProjectManager.getProjectByID(mProjectID, mProjectContents);
        }

        if (mProjectContents[ProjectManager.POS_IMPORTANCE].length() == 0)
            mCurrentImportance = false;
        else
            mCurrentImportance = true;
        if (mProjectContents[ProjectManager.POS_COMPLETION].length() == 0)
            mCurrentCompletion = false;
        else
            mCurrentCompletion = true;

        populateFields();
        mInSwitching = false;

        // A hint
        Toast.makeText(this, "Sharing Needs Valid Mail Account!", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Activity created.");
    }

    void readExternalFile() {
        Uri data = getIntent().getData();
        if (data != null) {
            Log.d(TAG, "Parsing external file.");
            if (ContentResolver.SCHEME_CONTENT.equals(data.getScheme())) {
                try {
                    // Read project and add it into system
                    ContentResolver resolver = getContentResolver();
                    InputStream is = resolver.openInputStream(data);
                    if (is != null) {
                        ObjectInputStream ois = new ObjectInputStream(is);
                        Project project = (Project) ois.readObject();
                        if (project != null) {
                            mProjectManager = new ProjectManager(this);
                            mProjectID = mProjectManager.addProjectFromExternal(project);
                            Log.d(TAG, "Parsing external successfully.");
                            Toast.makeText(this, "File Imported!", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                } catch (ClassNotFoundException | IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
            // Finish current activity if file phasing failure
            Log.d(TAG, "Parsing external failed.");
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Fill views when rotate screen
        int rotation = getWindow().getWindowManager().getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            setContentView(R.layout.activity_show_landscape);
        } else {
            setContentView(R.layout.activity_show);
        }
        linkViews();
        populateFields();
    }

    private void linkViews() {
        mText_ProjectName = (TextView) findViewById(R.id.show_text_name);
        mText_CourseName = (TextView) findViewById(R.id.show_text_courseName);
        mText_CourseInstructor = (TextView) findViewById(R.id.show_text_instructor);
        mText_ProjectStartTime = (TextView) findViewById(R.id.show_text_projectStartTime);
        mText_ProjectDueDate = (TextView) findViewById(R.id.show_text_projectDueDate);
        mText_ProjectDescription = (TextView) findViewById(R.id.show_text_description);

        mBar_TimeProgress = (ProgressBar) findViewById(R.id.show_progress_timeProgress);
        mBar_TaskProgress = (ProgressBar) findViewById(R.id.show_progress_taskProgress);

        mList = (ListView) findViewById(R.id.show_list_task);
        mButton_ImportanceMarker = (ImageButton) findViewById(R.id.show_button_importanceMarker);
        mButton_Done = (Button) findViewById(R.id.show_button_done);
        mButton_Delete = (Button) findViewById(R.id.show_button_delete);

        mButton_ImportanceMarker.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update project importance and change the background icon
                Log.d(TAG, "User changed project importance.");
                if (mCurrentCompletion)
                    return;
                mCurrentImportance = !mCurrentImportance;

                if (mCurrentImportance)
                    mProjectManager.setProjectState(mProjectID, Project.IMPORTANT, mCurrentCompletion);
                else
                    mProjectManager.setProjectState(mProjectID, Project.UNIMPORTANT, mCurrentCompletion);

                if (mCurrentImportance)
                    mButton_ImportanceMarker.setBackgroundResource(R.drawable.ic_important);
                else
                    mButton_ImportanceMarker.setBackgroundResource(R.drawable.ic_unimportant);
            }
        });

        mButton_Done.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Finish a project, after this, the project can only be deleted
                Log.d(TAG, "User clicked done button.");
                if (mCurrentCompletion)
                    Log.d(TAG, "Completion state error!");

                if (mCurrentCompletedTasks != mCurrentTaskNumber) {
                    for (int i = 0; i < mCurrentTaskNumber; i++) {
                        mProjectManager.setTaskState(mProjectID, i, true);
                    }
                }

                mCurrentCompletion = true;
                populateFields();
                mProjectManager.setProjectState(
                        mProjectID,
                        mCurrentImportance ? Project.IMPORTANT : Project.UNIMPORTANT,
                        mCurrentCompletion);
            }
        });

        mButton_Delete.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "User clicked delete button.");
                mProjectManager.deleteProject(mProjectID);
                startActivity(new Intent(getApplicationContext(), ActivityList.class));
                mInSwitching = true;
                finish();
            }
        });
    }

    private void populateFields() {
        mText_ProjectName.setText(mProjectContents[ProjectManager.POS_NAME]);
        mText_CourseName.setText(mProjectContents[ProjectManager.POS_COURSE_NAME]);
        mText_CourseInstructor.setText(mProjectContents[ProjectManager.POS_INSTRUCTOR]);
        mText_ProjectStartTime.setText(
                mProjectContents[ProjectManager.POS_START_DATE] + mProjectContents[ProjectManager.POS_START_TIME]);
        mText_ProjectDueDate.setText(
                mProjectContents[ProjectManager.POS_DUE_DATE] + mProjectContents[ProjectManager.POS_DUE_TIME]);
        mText_ProjectDescription.setText(mProjectContents[ProjectManager.POS_DESCRIPTION]);

        if (mCurrentImportance)
            mButton_ImportanceMarker.setBackgroundResource(R.drawable.ic_important);
        else
            mButton_ImportanceMarker.setBackgroundResource(R.drawable.ic_unimportant);
        if (mCurrentCompletion) {
            mButton_Done.setVisibility(View.INVISIBLE);
            mButton_Delete.setVisibility(View.VISIBLE);
        } else {
            mButton_Done.setVisibility(View.VISIBLE);
            mButton_Delete.setVisibility(View.INVISIBLE);
        }

        mCurrentCompletedTasks = 0;
        mCurrentTaskNumber = mProjectManager.getTaskNumber(mProjectID);
        ArrayList<ShowList> taskList = new ArrayList<ShowList>();
        for (int i = 0; i < mCurrentTaskNumber; i++) {
            String[] contents = mProjectManager.getTask(mProjectID, i);
            taskList.add(new ShowList(
                    contents[ProjectManager.POS_NAME],
                    contents[ProjectManager.POS_MEMBERS],
                    contents[ProjectManager.POS_DESCRIPTION],
                    contents[ProjectManager.POS_START_DATE] + contents[ProjectManager.POS_START_TIME],
                    contents[ProjectManager.POS_DUE_DATE] + contents[ProjectManager.POS_DUE_TIME],
                    contents[ProjectManager.POS_COMPLETION].length() != 0,
                    mCurrentCompletion
            ));
            if (contents[ProjectManager.POS_COMPLETION].length() != 0)
                mCurrentCompletedTasks++;
        }
        mList.setAdapter(new AdapterShow(taskList, this));

        TextView view_TaskHint = (TextView) findViewById(R.id.show_text_taskHint);
        if (mCurrentTaskNumber == 0)
            view_TaskHint.setText("No Tasks");
        else if (mCurrentTaskNumber == 1)
            view_TaskHint.setText("Task");
        else
            view_TaskHint.setText("Tasks");

        mBar_TimeProgress.setProgress(getProjectProgress());
        mBar_TaskProgress.setProgress(getTaskProgress());
        Log.d(TAG, "UI updated.");
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "On pressed back button.");
        startActivity(getSupportParentActivityIntent());
        finish();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "On stop.");
        if (!mInSwitching)
            stopService(new Intent(this, ServiceMusic.class));
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "On restart.");
        startService(new Intent(this, ServiceMusic.class));
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "On save instance state.");
        super.onSaveInstanceState(outState);
        outState.putSerializable(PROJECT_ID, mProjectID);
        outState.putSerializable(PROJECT_CONTENT, mProjectContents);
        outState.putSerializable(PROJECT_COMPLETION, mCurrentCompletion);
        outState.putSerializable(PROJECT_IMPORTANCE, mCurrentImportance);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mProjectContents[ProjectManager.POS_COMPLETION].length() != 0)
            getMenuInflater().inflate(R.menu.menu_show_completed, menu);
        else
            getMenuInflater().inflate(R.menu.menu_show, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit_project) {
            Log.d(TAG, "User clicked edit menu.");
            if (mCurrentCompletion) {
                Toast.makeText(this, "Cannot Edit A Completed Project!", Toast.LENGTH_SHORT).show();
            } else {
                mInSwitching = true;
                Intent intent = new Intent(getApplicationContext(), ActivityEdit.class);
                intent.putExtra(ProjectManager.PROJECT_ID, mProjectID);
                startActivity(intent);
            }
            return false;
        }
        if (id == R.id.action_share_project) {
            // Create a file contains current project, and send it via Email
            // This can be done only when a email account has been set up
            String fileName = mProjectManager.saveOneProject(mProjectID);
            if (fileName.length() != 0) {
                Log.d(TAG, fileName + " has been exported and is being sent!");
                Toast.makeText(this, fileName + " has been exported", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ljin027@uottawa.ca"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Sharing an iProject");
                intent.putExtra(Intent.EXTRA_TEXT,
                        "This is my project \""
                                + mProjectContents[ProjectManager.POS_NAME]
                                + "\":\n"
                                + mProjectContents[ProjectManager.POS_DESCRIPTION]);

                File file = new File(fileName);
                if (!file.exists() || !file.canRead()) {
                    Toast.makeText(this, "Attachment Error!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                Uri uri = Uri.parse("file://" + file.getAbsolutePath());
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, "Send Email"));
            }
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        mInSwitching = true;
        return new Intent(this, ActivityList.class);
    }

    public void onCheckTaskComplete(View view) {
        // User finish a task, it cannot be edited/deleted any more
        CheckBox checkBox = (CheckBox) view;
        int position = (Integer) checkBox.getTag();
        Log.d(TAG, "User clicked check button at row " + position + ".");
        if (checkBox.isChecked()) {
            mCurrentCompletedTasks++;
            mProjectManager.setTaskState(mProjectID, position, true);
            populateFields();
        } else {
            mCurrentCompletedTasks--;
            mProjectManager.setTaskState(mProjectID, position, false);
            populateFields();
        }
    }

    private int getProjectProgress() {
        long currentTime = System.currentTimeMillis();
        Date startDate = Project.getDate(mProjectContents[ProjectManager.POS_START_DATE] + mProjectContents[ProjectManager.POS_START_TIME]);
        Date dueDate = Project.getDate(mProjectContents[ProjectManager.POS_DUE_DATE] + mProjectContents[ProjectManager.POS_DUE_TIME]);
        long startTime = startDate.getTime();
        long dueTime = dueDate.getTime();
        if (dueTime > currentTime && startTime < currentTime)
            return (int) (100 * (currentTime - startTime) / (dueTime - startTime));
        else if (currentTime > dueTime)
            return 100;
        else
            return 0;
    }

    private int getTaskProgress() {
        if (mCurrentTaskNumber == 0)
            return 100;
        else
            return 100 * mCurrentCompletedTasks / mCurrentTaskNumber;
    }
}
