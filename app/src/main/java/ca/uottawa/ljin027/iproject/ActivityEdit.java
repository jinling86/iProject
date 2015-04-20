package ca.uottawa.ljin027.iproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ActivityEdit extends ActionBarActivity {
    private static final String PROJECT_ID = "ID";
    private static final String PROJECT_TMP_ID = "TMP_ID";
    private static final String PROJECT_INDICATOR = "INDICATOR";
    private static final String TASK_STATE = "TASK_EDITING";
    private static final String TASK_CONTENT = "TASK_CONTENT";

    private static final String DEFAULT_STRING = "Not Provided";
    private static final String NULL_STRING = "";
    private static final int DEFAULT_TASK_INDEX = -1;

    private EditText mView_ProjectName;
    private EditText mView_ProjectDescription;
    private EditText mView_CourseName;
    private EditText mView_CourseInstructor;
    private EditText mView_ProjectStartTime;
    private EditText mView_ProjectStartDate;
    private EditText mView_ProjectDueTime;
    private EditText mView_ProjectDueDate;
    private EditText mView_TaskName;
    private EditText mView_TaskDescription;
    private EditText mView_TaskMembers;
    private EditText mView_TaskStartTime;
    private EditText mView_TaskStartDate;
    private EditText mView_TaskDueTime;
    private EditText mView_TaskDueDate;

    private ViewGroup mLayout_EditTask;
    private Button mButton_AddTask;

    private ListView mList_UpperPart;
    private ListView mList_LowerPart;
    private ScrollView mView_Framework;

    private String mProjectID;
    private String mTmpID;
    private boolean mNewProject;
    private ProjectManager mProjectManager;
    private boolean mInSwitching = false;

    private int mEditingTaskIndex = DEFAULT_TASK_INDEX;
    private String [] mEditingTaskContents;

    private static final String TAG = "<<<< Activity Edit >>>>";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);

        // Set the action bar style
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setTitle(R.string.edit_title);
        actionBar.setDisplayHomeAsUpEnabled(true);

        linkViews();

        mNewProject = false;
        mTmpID = null;
        mProjectID = "";
        if(savedInstanceState != null) {
            mProjectID = (String) savedInstanceState.getSerializable(PROJECT_ID);
            mTmpID = (String) savedInstanceState.getSerializable(PROJECT_TMP_ID);
            mNewProject = (Boolean) savedInstanceState.getSerializable(PROJECT_INDICATOR);
            mEditingTaskIndex = (Integer) savedInstanceState.getSerializable(TASK_STATE);
            if(mEditingTaskIndex != DEFAULT_TASK_INDEX)
                mEditingTaskContents = (String []) savedInstanceState.getSerializable(TASK_CONTENT);

            startService(new Intent(this, ServiceMusic.class));
        }
        else {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                mProjectID = extras.getString(ProjectManager.PROJECT_ID);
            }
            else {
                mNewProject = true;
            }
        }

        mProjectManager = new ProjectManager(this);
        if(mTmpID == null) {
            mTmpID = mProjectManager.addProject();
            if(!mNewProject) {
                mProjectManager.copyProject(mProjectID, mTmpID);
            }
        }

        populateFields();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Log.d(TAG, "Created successfully, ID = " + mTmpID + (mNewProject ? " (new)" : ""));
    }

    private void populateFields() {
        int prevScrollPosition = mView_Framework.getScrollY();
        String[] projectContent = new String[ProjectManager.POS_MAX];
        mProjectManager.getProjectByID(mTmpID, projectContent);
        mView_ProjectName.setText(
                projectContent[ProjectManager.POS_NAME].compareTo(DEFAULT_STRING) == 0 ?
                NULL_STRING: projectContent[ProjectManager.POS_NAME]);
        mView_ProjectDescription.setText(
                projectContent[ProjectManager.POS_DESCRIPTION].compareTo(DEFAULT_STRING) == 0 ?
                NULL_STRING: projectContent[ProjectManager.POS_DESCRIPTION]);
        mView_CourseName.setText(
                projectContent[ProjectManager.POS_COURSE_NAME].compareTo(DEFAULT_STRING) == 0 ?
                NULL_STRING: projectContent[ProjectManager.POS_COURSE_NAME]);
        mView_CourseInstructor.setText(
                projectContent[ProjectManager.POS_INSTRUCTOR].compareTo(DEFAULT_STRING) == 0 ?
                NULL_STRING: projectContent[ProjectManager.POS_INSTRUCTOR]);
        mView_ProjectStartDate.setText(projectContent[ProjectManager.POS_START_DATE]);
        mView_ProjectStartTime.setText(projectContent[ProjectManager.POS_START_TIME]);
        mView_ProjectDueDate.setText(projectContent[ProjectManager.POS_DUE_DATE]);
        mView_ProjectDueTime.setText(projectContent[ProjectManager.POS_DUE_TIME]);

        if(mEditingTaskIndex == DEFAULT_TASK_INDEX) {
            mButton_AddTask.setVisibility(View.VISIBLE);
            mLayout_EditTask.setVisibility(View.GONE);
            int taskNumber = mProjectManager.getTaskNumber(mTmpID);
            if(taskNumber != 0) {
                fillList(mList_UpperPart, 0, taskNumber);
                fillList(mList_LowerPart, taskNumber, taskNumber);
            }
        }
        else {
            mButton_AddTask.setVisibility(View.GONE);
            String[] taskContent = mProjectManager.getTask(mTmpID, mEditingTaskIndex);
            mView_TaskName.setText(
                    taskContent[ProjectManager.POS_NAME].compareTo(DEFAULT_STRING) == 0 ?
                    NULL_STRING: taskContent[ProjectManager.POS_NAME]);
            mView_TaskDescription.setText(
                    taskContent[ProjectManager.POS_DESCRIPTION].compareTo(DEFAULT_STRING) == 0 ?
                    NULL_STRING: taskContent[ProjectManager.POS_DESCRIPTION]);
            mView_TaskMembers.setText(
                    taskContent[ProjectManager.POS_MEMBERS].compareTo(DEFAULT_STRING) == 0 ?
                    NULL_STRING: taskContent[ProjectManager.POS_MEMBERS]);
            mView_TaskStartDate.setText(taskContent[ProjectManager.POS_START_DATE]);
            mView_TaskStartTime.setText(taskContent[ProjectManager.POS_START_TIME]);
            mView_TaskDueDate.setText(taskContent[ProjectManager.POS_DUE_DATE]);
            mView_TaskDueTime.setText(taskContent[ProjectManager.POS_DUE_TIME]);

            mLayout_EditTask.setVisibility(View.VISIBLE);
            int taskNumber = mProjectManager.getTaskNumber(mTmpID);
            fillList(mList_UpperPart, 0, mEditingTaskIndex);
            fillList(mList_LowerPart, mEditingTaskIndex + 1, taskNumber);
        }

        mView_Framework.setScrollY(prevScrollPosition);
    }

    private void fillList(ListView listView, int taskStart, int taskEnd) {
        String[] from = new String[]{"name", "description", "members", "start", "end"};
        int[] to = new int[]{
                R.id.list_text_taskName,
                R.id.list_text_taskDescription,
                R.id.list_text_taskMembers,
                R.id.list_text_taskStartTime,
                R.id.list_text_taskDueDate
        };

        List<HashMap<String, String>> taskList = new ArrayList<HashMap<String, String>>();
        for (int i = taskStart; i < taskEnd; i++) {
            String [] taskContent = mProjectManager.getTask(mTmpID, i);
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("name", taskContent[ProjectManager.POS_NAME]);
            map.put("description", taskContent[ProjectManager.POS_DESCRIPTION]);
            map.put("members", taskContent[ProjectManager.POS_MEMBERS]);
            map.put("start", taskContent[ProjectManager.POS_START_DATE]+taskContent[ProjectManager.POS_START_TIME]);
            map.put("end", taskContent[ProjectManager.POS_DUE_DATE]+taskContent[ProjectManager.POS_DUE_TIME]);
            taskList.add(map);
        }

        SimpleAdapter adapter = null;
        if(mEditingTaskIndex == DEFAULT_TASK_INDEX)
            adapter = new SimpleAdapter(this, taskList, R.layout.list_task_edit, from, to);
        else
            adapter = new SimpleAdapter(this, taskList, R.layout.list_task_show, from, to);
        listView.setAdapter(adapter);

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void update() {
        String projectName = mView_ProjectName.getText().toString();
        String projectDescription = mView_ProjectDescription.getText().toString();
        String courseName = mView_CourseName.getText().toString();
        String instructor = mView_CourseInstructor.getText().toString();
        String startTime = mView_ProjectStartDate.getText().toString() + mView_ProjectStartTime.getText().toString();
        String dueDate = mView_ProjectDueDate.getText().toString() + mView_ProjectDueTime.getText().toString();
        mProjectManager.setProject(
                mTmpID,
                projectName.length() == 0 ? DEFAULT_STRING: projectName,
                projectDescription.length() == 0 ? DEFAULT_STRING: projectDescription,
                instructor.length() == 0 ? DEFAULT_STRING: instructor,
                courseName.length() == 0 ? DEFAULT_STRING: courseName,
                startTime,
                dueDate);

        if(mEditingTaskIndex != DEFAULT_TASK_INDEX) {
            String taskName = mView_TaskName.getText().toString();
            String taskDescription = mView_TaskDescription.getText().toString();
            String taskMembers = mView_TaskMembers.getText().toString();
            String taskStartTime = mView_TaskStartDate.getText().toString() + mView_TaskStartTime.getText().toString();
            String taskDueDate = mView_TaskDueDate.getText().toString() + mView_TaskDueTime.getText().toString();
            mProjectManager.setTask(
                    mTmpID,
                    mEditingTaskIndex,
                    taskName.length() == 0 ? DEFAULT_STRING: taskName,
                    taskDescription.length() == 0 ? DEFAULT_STRING: taskDescription,
                    taskMembers.length() == 0 ? DEFAULT_STRING: taskMembers,
                    taskStartTime,
                    taskDueDate);
        }
    }

    private void linkViews() {
        mView_ProjectName = (EditText) findViewById(R.id.edit_edit_name);
        mView_ProjectDescription = (EditText) findViewById(R.id.edit_edit_description);
        mView_CourseName = (EditText) findViewById(R.id.edit_edit_courseName);
        mView_CourseInstructor = (EditText) findViewById(R.id.edit_edit_courseInstructor);
        mView_ProjectStartTime = (EditText) findViewById(R.id.edit_edit_projectStartTime);
        mView_ProjectStartDate = (EditText) findViewById(R.id.edit_edit_projectStartDate);
        mView_ProjectDueTime = (EditText) findViewById(R.id.edit_edit_projectDueTime);
        mView_ProjectDueDate = (EditText) findViewById(R.id.edit_edit_projectDueDate);

        mView_TaskName = (EditText) findViewById(R.id.edit_edit_taskName);
        mView_TaskDescription = (EditText) findViewById(R.id.edit_edit_taskDescription);
        mView_TaskMembers = (EditText) findViewById(R.id.edit_edit_taskMembers);
        mView_TaskStartTime = (EditText) findViewById(R.id.edit_edit_taskStartTime);
        mView_TaskStartDate = (EditText) findViewById(R.id.edit_edit_taskStartDate);
        mView_TaskDueTime = (EditText) findViewById(R.id.edit_edit_taskDueTime);
        mView_TaskDueDate = (EditText) findViewById(R.id.edit_edit_taskDueDate);

        mLayout_EditTask = (ViewGroup) findViewById(R.id.edit_layout_editTask);

        mList_UpperPart = (ListView) findViewById(R.id.edit_list_upperList);
        mList_LowerPart = (ListView) findViewById(R.id.edit_list_lowerList);
        mView_Framework = (ScrollView) findViewById(R.id.edit_scrollView);

        mView_ProjectStartTime.setOnClickListener(new TimeSetter(mView_ProjectStartTime));
        mView_ProjectDueTime.setOnClickListener(new TimeSetter(mView_ProjectDueTime));
        mView_TaskStartTime.setOnClickListener(new TimeSetter(mView_TaskStartTime));
        mView_TaskDueTime.setOnClickListener(new TimeSetter(mView_TaskDueTime));
        mView_ProjectStartDate.setOnClickListener(new DateSetter(mView_ProjectStartDate));
        mView_ProjectDueDate.setOnClickListener(new DateSetter(mView_ProjectDueDate));
        mView_TaskStartDate.setOnClickListener(new DateSetter(mView_TaskStartDate));
        mView_TaskDueDate.setOnClickListener(new DateSetter(mView_TaskDueDate));

        mButton_AddTask = (Button) findViewById(R.id.edit_button_add);
        mButton_AddTask.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEditingTaskIndex == DEFAULT_TASK_INDEX) {
                    update();
                    String startTime = mView_ProjectStartDate.getText().toString() + mView_ProjectStartTime.getText().toString();
                    String dueDate = mView_ProjectDueDate.getText().toString() + mView_ProjectDueTime.getText().toString();
                    mProjectManager.addTask(mTmpID, startTime, dueDate);
                    mEditingTaskIndex = mProjectManager.getTaskNumber(mTmpID) - 1;
                    populateFields();
                    closeKeyboard();
                }
            }
        });

        ImageButton button_DeleteTask = (ImageButton) findViewById(R.id.edit_button_delete);
        button_DeleteTask.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEditingTaskIndex != DEFAULT_TASK_INDEX) {
                    mProjectManager.deleteTask(mTmpID, mEditingTaskIndex);
                    mEditingTaskIndex = DEFAULT_TASK_INDEX;
                    populateFields();
                    closeKeyboard();
                }
                else {
                    Log.d(TAG, "Internal state error!");
                }

            }
        });

        ImageButton button_SaveTask = (ImageButton) findViewById(R.id.edit_button_save);
        button_SaveTask.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEditingTaskIndex != DEFAULT_TASK_INDEX) {
                    update();
                    mEditingTaskIndex = DEFAULT_TASK_INDEX;
                    populateFields();
                    closeKeyboard();
                }
                else {
                    Log.d(TAG, "Internal state error!");
                }
            }
        });

        ImageButton button_CancelTask = (ImageButton) findViewById(R.id.edit_button_cancel);
        button_CancelTask.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEditingTaskIndex != DEFAULT_TASK_INDEX) {
                    mProjectManager.setTask(
                            mTmpID,
                            mEditingTaskIndex,
                            mEditingTaskContents[ProjectManager.POS_NAME],
                            mEditingTaskContents[ProjectManager.POS_DESCRIPTION],
                            mEditingTaskContents[ProjectManager.POS_MEMBERS],
                            mEditingTaskContents[ProjectManager.POS_START_DATE] + mEditingTaskContents[ProjectManager.POS_START_TIME],
                            mEditingTaskContents[ProjectManager.POS_DUE_DATE] + mEditingTaskContents[ProjectManager.POS_DUE_TIME]);
                    populateFields();
                }
                else {
                    Log.d(TAG, "Internal state error!");
                }
            }
        });

        Button button_DeleteProject = (Button) findViewById(R.id.edit_button_deleteProject);
        button_DeleteProject.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProjectManager.deleteProject(mTmpID);
                mTmpID = null;
                mInSwitching = true;
                if(!mNewProject) {
                    mProjectManager.deleteProject(mProjectID);
                }
                startActivity(new Intent(getApplicationContext(), ActivityList.class));
            }
        });
    }

    public void onButtonEditTaskClick(View view) {
        if(mEditingTaskIndex == DEFAULT_TASK_INDEX) {
            mEditingTaskIndex = mList_UpperPart.getPositionForView((View) view.getParent());
            mEditingTaskContents = mProjectManager.getTask(mTmpID, mEditingTaskIndex);
            if(mEditingTaskContents[ProjectManager.POS_COMPLETION].length() == 0)
                populateFields();
            else {
                mEditingTaskIndex = DEFAULT_TASK_INDEX;
                Toast.makeText(this, "Task Has Completed!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mTmpID != null) {
            update();
            outState.putSerializable(PROJECT_ID, mProjectID);
            outState.putSerializable(PROJECT_TMP_ID, mTmpID);
            outState.putSerializable(PROJECT_INDICATOR, mNewProject);
            outState.putSerializable(TASK_STATE, mEditingTaskIndex);
            if(mEditingTaskIndex != DEFAULT_TASK_INDEX)
                outState.putSerializable(TASK_CONTENT, mEditingTaskContents);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save_project) {
            update();
            if(mNewProject) {
                Intent intent = new Intent(this, ActivityShow.class);
                intent.putExtra(ProjectManager.PROJECT_ID, mTmpID);
                mTmpID = null;
                mInSwitching = true;
                startActivity(intent);
                finish();
            }
            else {
                mProjectManager.destroyTmpProject(mTmpID, mProjectID);
                mTmpID = null;
                mInSwitching = true;
                Intent intent = new Intent(this, ActivityShow.class);
                intent.putExtra(ProjectManager.PROJECT_ID, mProjectID);
                startActivity(intent);
                finish();
            }
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        if(mNewProject) {
            mProjectManager.deleteProject(mTmpID);
            mTmpID = null;
            mInSwitching = true;
            return new Intent(this, ActivityList.class);
        }
        else {
            Intent intent = new Intent(this, ActivityShow.class);
            intent.putExtra(ProjectManager.PROJECT_ID, mProjectID);
            mProjectManager.deleteProject(mTmpID);
            mTmpID = null;
            mInSwitching = true;
            return intent;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(getSupportParentActivityIntent());
        finish();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "On restart.");
        startService(new Intent(this, ServiceMusic.class));
        super.onRestart();
    }

    @Override
    protected void onStop() {
        if(mTmpID != null) {
            Log.d(TAG, "Activity switched out, discard changes");
            mProjectManager.deleteProject(mTmpID);
        }
        if(!mInSwitching)
            stopService(new Intent(this, ServiceMusic.class));
        super.onStop();
    }

    private class TimeSetter implements View.OnClickListener {
        private EditText mEditText;
        public TimeSetter(EditText editText) {
            mEditText = editText;
        }

        @Override
        public void onClick(View v) {
            FragmentPicker newFragment = new FragmentPicker();
            if(mEditText == mView_ProjectDueTime) {
                // Trying to modify due time
                newFragment.setListener(
                        mEditText,
                        FragmentPicker.PICK_TIME,
                        mView_ProjectDueDate.getText().toString(),
                        getProjectDueUpperBound(),
                        getProjectDueLowerBound(),
                        null,
                        null);
            }
            else if(mEditText == mView_ProjectStartTime){
                // Trying to modify start time
                newFragment.setListener(
                        mEditText,
                        FragmentPicker.PICK_TIME,
                        mView_ProjectStartDate.getText().toString(),
                        getProjectStartUpperBound(),
                        getProjectStartLowerBound(),
                        mView_ProjectDueDate,
                        mView_ProjectDueTime);
            }
            else if(mEditText == mView_TaskDueTime){
                // Trying to modify start time
                newFragment.setListener(
                        mEditText,
                        FragmentPicker.PICK_TIME,
                        mView_TaskDueDate.getText().toString(),
                        getTaskDueUpperBound(),
                        getTaskDueLowerBound(),
                        null,
                        null);
            }
            else if(mEditText == mView_TaskStartTime){
                // Trying to modify start time
                newFragment.setListener(
                        mEditText,
                        FragmentPicker.PICK_TIME,
                        mView_TaskStartDate.getText().toString(),
                        getTaskStartUpperBound(),
                        getTaskStartLowerBound(),
                        mView_TaskDueDate,
                        mView_TaskDueTime);
            }
            newFragment.show(getSupportFragmentManager(), "Choose A Time");
        }
    }

    private class DateSetter implements View.OnClickListener {
        private EditText mEditText;
        public DateSetter(EditText editText) {
            mEditText = editText;
        }

        @Override
        public void onClick(View v) {
            FragmentPicker newFragment = new FragmentPicker();
            if(mEditText == mView_ProjectDueDate) {
                // Trying to modify due time
                newFragment.setListener(
                        mEditText,
                        FragmentPicker.PICK_DATE,
                        mView_ProjectDueTime.getText().toString(),
                        getProjectDueUpperBound(),
                        getProjectDueLowerBound(),
                        null,
                        null);
            }
            else if(mEditText == mView_ProjectStartDate){
                // Trying to modify start time
                newFragment.setListener(
                        mEditText,
                        FragmentPicker.PICK_DATE,
                        mView_ProjectStartTime.getText().toString(),
                        getProjectStartUpperBound(),
                        getProjectStartLowerBound(),
                        mView_ProjectDueDate,
                        mView_ProjectDueTime);
            }
            else if(mEditText == mView_TaskDueDate){
                // Trying to modify start time
                newFragment.setListener(
                        mEditText,
                        FragmentPicker.PICK_DATE,
                        mView_TaskDueTime.getText().toString(),
                        getTaskDueUpperBound(),
                        getTaskDueLowerBound(),
                        null,
                        null);
            }
            else if(mEditText == mView_TaskStartDate){
                // Trying to modify start time
                newFragment.setListener(
                        mEditText,
                        FragmentPicker.PICK_DATE,
                        mView_TaskStartTime.getText().toString(),
                        getTaskStartUpperBound(),
                        getTaskStartLowerBound(),
                        mView_TaskDueDate,
                        mView_TaskDueTime);
            }
            newFragment.show(getSupportFragmentManager(), "Choose A Date");
        }
    }

    private Date getEarliestTaskStartTime() {
        Date earliest = null;
        int taskNumber = mProjectManager.getTaskNumber(mTmpID);
        for(int i = 0; i < taskNumber; i++) {
            String [] task = mProjectManager.getTask(mTmpID, i);
            Date startTime = Project.getDate(task[ProjectManager.POS_START_DATE]+task[ProjectManager.POS_START_TIME]);
            if(earliest == null)
                earliest = startTime;
            else if(earliest.after(startTime))
                earliest = startTime;
        }
        return earliest;
    }

    private Date getLatestTaskDueTime() {
        Date latest = null;
        int taskNumber = mProjectManager.getTaskNumber(mTmpID);
        for(int i = 0; i < taskNumber; i++) {
            String [] task = mProjectManager.getTask(mTmpID, i);
            Date dueDate = Project.getDate(task[ProjectManager.POS_DUE_DATE]+task[ProjectManager.POS_DUE_TIME]);
            if(latest == null)
                latest = dueDate;
            else if(latest.before(dueDate))
                latest = dueDate;
        }
        return latest;
    }

    private Date getProjectStartUpperBound() {
        Date upperBound = new Date(2020-1900, 1, 1, 0, 0);
        Date taskBound = getEarliestTaskStartTime();
        if(taskBound != null && taskBound.before(upperBound)) {
            return taskBound;
        }
        else {
            return upperBound;
        }
    }

    private Date getProjectStartLowerBound() {
        return new Date(2010-1900, 1, 1, 0, 0);
    }

    private Date getProjectDueUpperBound() {
        return new Date(2020-1900, 1, 1, 0, 0);
    }

    private Date getProjectDueLowerBound() {
        Date lowerBound = Project.getDate(
                mView_ProjectStartDate.getText().toString()+ mView_ProjectStartTime.getText().toString());
        Date taskBound = getLatestTaskDueTime();
        if(taskBound != null && taskBound.after(lowerBound)) {
            return taskBound;
        }
        else {
            return lowerBound;
        }
    }

    private Date getTaskStartUpperBound() {
        Date upperBound = Project.getDate(
                mView_ProjectDueDate.getText().toString()+ mView_ProjectDueTime.getText().toString());
        return upperBound;
    }

    private Date getTaskStartLowerBound() {
        Date lowerBound = Project.getDate(
                mView_ProjectStartDate.getText().toString()+ mView_ProjectStartTime.getText().toString());
        return lowerBound;
    }

    private Date getTaskDueUpperBound() {
        Date upperBound = Project.getDate(
                mView_ProjectDueDate.getText().toString()+ mView_ProjectDueTime.getText().toString());
        return upperBound;
    }

    private Date getTaskDueLowerBound() {
        Date lowerBound = Project.getDate(
                mView_TaskStartDate.getText().toString()+ mView_TaskStartTime.getText().toString());
        return lowerBound;
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if(view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
