package ca.uottawa.ljin027.iproject;

import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This class is implemented for CSI5175 Assignment 3.
 * This class defines the project fields and their corresponding formats. It also provide a
 * comparator for the generic sort method. The compare is perform by comparing due date.
 *
 * @author Ling Jin
 * @version 1.0
 * @since 15/04/2015
 */
public class Project implements Serializable, Comparable<Object> {
    public static final long TIME_OFFSET = 60 * 60 * 1000;
    public static final int IMPORTANT = 1;
    public static final int UNIMPORTANT = 0;
    private static SimpleDateFormat mTimeFormat = new SimpleDateFormat(" kk : mm");
    private static SimpleDateFormat mDateFormat = new SimpleDateFormat("MMM dd, yyyy");
    private static SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("MMM dd, yyyy kk : mm");
    private static final String TAG = "<<< Project >>>";

    private String mId;
    private String mName;
    private String mDescription;
    private String mCourseName;
    private String mCourseInstructor;
    private Date mStartTime;
    private Date mDueDate;
    private int mImportance;
    private boolean mCompletion;
    private ArrayList<Task> mTaskList;

    public int compareTo(Object object) {
        Project anotherProject = (Project) object;
        if (anotherProject.mDueDate.before(mDueDate))
            return 1;
        else if (anotherProject.mDueDate.after(mDueDate))
            return -1;
        else
            return 0;
    }

    private class Task implements Serializable {
        public String id;
        public String name;
        public String members;
        public String description;
        public Date startTime;
        public Date dueDate;
        public boolean completion;
    }

    public Project() {
        Long currentTime = System.currentTimeMillis();
        mId = String.valueOf(currentTime);
        mTaskList = new ArrayList<Task>();
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setCourseName(String courseName) {
        mCourseName = courseName;
    }

    public String getCourseName() {
        return mCourseName;
    }

    public void setInstructor(String instructor) {
        mCourseInstructor = instructor;
    }

    public String getInstructor() {
        return mCourseInstructor;
    }

    public void setStartTime(Date startTime) {
        mStartTime = startTime;
    }

    public Date getStartTime() {
        return mStartTime;
    }

    public void setDueDate(Date dueDate) {
        mDueDate = dueDate;
    }

    public Date getDueDate() {
        return mDueDate;
    }

    public void setImportance(int importance) {
        mImportance = importance;
    }

    public int getImportance() {
        return mImportance;
    }

    public void setCompletion(boolean completion) {
        mCompletion = completion;
    }

    public boolean getCompletion() {
        return mCompletion;
    }

    public boolean isMe(String id) {
        return mId.compareTo(id) == 0;
    }

    public String getId() {
        return mId;
    }

    public int getTaskNumber() {
        return mTaskList.size();
    }

    public String getTaskName(int index) {
        if (index < mTaskList.size()) {
            return mTaskList.get(index).name;
        } else {
            Log.d(TAG, "Index error!");
            return null;
        }
    }

    public String getTaskDescription(int index) {
        if (index < mTaskList.size()) {
            return mTaskList.get(index).description;
        } else {
            Log.d(TAG, "Index error!");
            return null;
        }
    }

    public String getTaskMembers(int index) {
        if (index < mTaskList.size()) {
            return mTaskList.get(index).members;
        } else {
            Log.d(TAG, "Index error!");
            return null;
        }
    }

    public Date getTaskStartTime(int index) {
        if (index < mTaskList.size()) {
            return mTaskList.get(index).startTime;
        } else {
            Log.d(TAG, "Index error!");
            return null;
        }
    }

    public Date getTaskDueDate(int index) {
        if (index < mTaskList.size()) {
            return mTaskList.get(index).dueDate;
        } else {
            Log.d(TAG, "Index error!");
            return null;
        }
    }

    public boolean getTaskCompletion(int index) {
        if (index < mTaskList.size()) {
            return mTaskList.get(index).completion;
        } else {
            Log.d(TAG, "Index error!");
            return false;
        }
    }

    public void setTaskCompletion(int index, boolean completion) {
        if (index < mTaskList.size()) {
            mTaskList.get(index).completion = completion;
        } else {
            Log.d(TAG, "Index error!");
        }
    }

    public void addTask(String name, String description, String members, Date startTime, Date dueDate) {
        Task task = new Task();
        Long currentTime = System.currentTimeMillis();
        task.id = String.valueOf(currentTime);
        task.name = name;
        task.members = members;
        task.description = description;
        task.startTime = startTime;
        task.dueDate = dueDate;
        mTaskList.add(task);
        Log.d(TAG, "Added a task " + task.id);
    }

    public void setTask(int index, String name, String description, String members, Date startTime, Date dueDate) {
        if (index < mTaskList.size()) {
            Task task = mTaskList.get(index);
            task.name = name;
            task.members = members;
            task.description = description;
            task.startTime = startTime;
            task.dueDate = dueDate;
            Log.d(TAG, "Modified a task " + task.id);
        } else {
            Log.d(TAG, "Used a wrong index to modify a task!");
        }
    }

    public void deleteTask(int index) {
        if (index < mTaskList.size()) {
            String taskId = mTaskList.get(index).id;
            mTaskList.remove(index);
            Log.d(TAG, "Deleted a task " + taskId);
        } else {
            Log.d(TAG, "Used a wrong index to delete a task!");
        }
    }

    void copy(Project aProject) {
        mName = aProject.mName;
        mDescription = aProject.mDescription;
        mCourseName = aProject.mCourseName;
        mCourseInstructor = aProject.mCourseInstructor;
        mStartTime = aProject.mStartTime;
        mDueDate = aProject.mDueDate;
        mTaskList = aProject.mTaskList;
        mImportance = aProject.mImportance;
        mCompletion = aProject.mCompletion;
    }

    public static String getTimeString(int hour, int minute) {
        return String.format(" %02d : %02d", hour, minute);
    }

    public static String getDateString(int year, int month, int day) {
        return mDateFormat.format(new Date(year - 1900, month, day));
    }

    public static String getTimeString(Date date) {
        return mTimeFormat.format(date);
    }

    public static String getDateString(Date date) {
        return mDateFormat.format(date);
    }

    public static Date getDate(String dateAndTime) {
        try {
            return mDateTimeFormat.parse(dateAndTime);
        } catch (ParseException e) {
            Log.d(TAG, "Parse date and time failed!");
            return new Date();
        }
    }
}
