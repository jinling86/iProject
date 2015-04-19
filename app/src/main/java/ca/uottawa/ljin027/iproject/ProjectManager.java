package ca.uottawa.ljin027.iproject;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

/**
 * Created by ljin027 on 15/04/2015.
 */

public class ProjectManager {

    public static final String PROJECT_ID = "id";
    public static final String ERROR_MSG = "";
    public static final String BOOLEAN_MSG = "true";
    public static final int POS_NAME = 0;
    public static final int POS_DESCRIPTION = 1;
    public static final int POS_START_TIME = 2;
    public static final int POS_START_DATE = 3;
    public static final int POS_DUE_TIME = 4;
    public static final int POS_DUE_DATE = 5;
    public static final int POS_INSTRUCTOR = 6;
    public static final int POS_MEMBERS = 6;
    public static final int POS_COMPLETION = 7;
    public static final int POS_IMPORTANCE = 8;
    public static final int POS_COURSE_NAME = 9;
    public static final int POS_MAX = 10;

    private final String TAG = "<<< Project Manager >>>";
    private final String FILE_NAME = "projects";
    private final String FILE_NAME_PREFIX = "iProject_";
    private final String FILE_NAME_SUFFIX = ".ipj";

    private ArrayList<Project> mProjects;
    private TreeMap<Integer, Integer> mMap;
    private Context mContext;

    Project findProject(String id) {
        if(id == null) {
            Log.d(TAG, "Received a wrong id");
            return null;
        }
        for(Project project: mProjects) {
            if(project.isMe(id))
                return project;
        }
        return null;
    }

    public ProjectManager(Context context) {
        mContext = context;
        mProjects = new ArrayList<Project>();
        readProjects();
    }

    public void setProject(
            String id,
            String name,
            String description,
            String instructor,
            String courseName,
            String startTime,
            String dueDate) {
        Project project = findProject(id);
        if(project != null) {
            project.setName(name);
            project.setDescription(description);
            project.setCourseName(courseName);
            project.setInstructor(instructor);
            project.setStartTime(Project.getDate(startTime));
            project.setDueDate(Project.getDate(dueDate));
        }
        else {
            Log.d(TAG, "Found a wrong project ID in setProject!");
        }
        saveProjects();
    }

    public void setProjectState(String id, int importance, boolean completed) {
        Project project = findProject(id);
        if(project != null || (importance != Project.UNIMPORTANT && importance != Project.IMPORTANT)) {
            project.setCompletion(completed);
            project.setImportance(importance);
        }
        else {
            Log.d(TAG, "Found a wrong project ID in setProject!");
        }
        saveProjects();
    }

    public String addProject() {
        Project project = new Project();
        project.setName(ERROR_MSG);
        project.setDescription(ERROR_MSG);
        project.setInstructor(ERROR_MSG);
        project.setCourseName(ERROR_MSG);
        long currentTime = System.currentTimeMillis();
        long anHourLater = currentTime + 60*60*1000;
        project.setStartTime(new Date(currentTime));
        project.setDueDate(new Date(anHourLater));
        project.setCompletion(false);
        project.setImportance(Project.UNIMPORTANT);
        mProjects.add(project);
        saveProjects();
        Log.d(TAG, "Created a new project.");
        return project.getId();
    }

    private void loadProject(Project project, String [] contents) {
        contents[POS_NAME] = project.getName();
        contents[POS_DESCRIPTION] = project.getDescription();
        contents[POS_COURSE_NAME] = project.getCourseName();
        contents[POS_INSTRUCTOR] = project.getInstructor();
        Date data = null;
        data = project.getStartTime();
        contents[POS_START_TIME] = Project.getTimeString(data);
        contents[POS_START_DATE] = Project.getDateString(data);
        data = project.getDueDate();
        contents[POS_DUE_TIME] = Project.getTimeString(data);
        contents[POS_DUE_DATE] = Project.getDateString(data);
        if(project.getCompletion()) {
            contents[POS_COMPLETION] = BOOLEAN_MSG;
        }
        else {
            contents[POS_COMPLETION] = ERROR_MSG;
        }
        if(project.getImportance() == Project.IMPORTANT) {
            contents[POS_IMPORTANCE] = BOOLEAN_MSG;
        }
        else {
            contents[POS_IMPORTANCE] = ERROR_MSG;
        }
    }

    public void getProjectByID(String id, String [] contents) {
        Project project = findProject(id);
        if(project != null) {
            loadProject(project, contents);
        }
        else {
            for(int i = 0; i < POS_MAX; i++)
                contents[i] = ERROR_MSG;
            Log.d(TAG, "Found a wrong project ID in getProjectByID!");
        }
    }

    public void getProjectByIndex(int index, String [] contents) {
        if(mMap.containsKey(index)) {
            Project project = mProjects.get(mMap.get(index));
            loadProject(project, contents);
        }
        else {
            for(int i = 0; i < POS_MAX; i++)
                contents[i] = ERROR_MSG;
            Log.d(TAG, "Found a wrong project ID in getProjectByIndex!");
        }
    }

    public String getIdByIndex(int index) {
        if(mMap.containsKey(index)) {
            Project project = mProjects.get(mMap.get(index));
            return project.getId();
        }
        else {
            Log.d(TAG, "Found a wrong project ID in getIdByIndex!");
            return ERROR_MSG;
        }
    }

    public int getProjectNumber() {
        return mProjects.size();
    }
    public int getTaskNumber(String projectID) {
        Project project = findProject(projectID);
        if(project != null) {
            return project.getTaskNumber();
        }
        else {
            Log.d(TAG, "Found a wrong project ID in getTaskNumber!");
            return -1;
        }
    }

    public String[] getTask(String projectID, int taskIndex) {
        Project project = findProject(projectID);
        if(project != null) {
            if(taskIndex >= project.getTaskNumber() || taskIndex < 0) {
                Log.d(TAG, "Found a wrong project ID in getTask!");
                return null;
            }
            String[] contents = new String[POS_MAX];
            contents[POS_NAME] = project.getTaskName(taskIndex);
            contents[POS_DESCRIPTION] = project.getTaskDescription(taskIndex);
            contents[POS_MEMBERS] = project.getTaskMembers(taskIndex);
            contents[POS_START_TIME] = Project.getTimeString(project.getTaskStartTime(taskIndex));
            contents[POS_START_DATE] = Project.getDateString(project.getTaskStartTime(taskIndex));
            contents[POS_DUE_TIME] = Project.getTimeString(project.getTaskDueDate(taskIndex));
            contents[POS_DUE_DATE] = Project.getDateString(project.getTaskDueDate(taskIndex));
            if(project.getTaskCompletion(taskIndex)) {
                contents[POS_COMPLETION] = BOOLEAN_MSG;
            }
            else {
                contents[POS_COMPLETION] = ERROR_MSG;
            }

            return contents;
        }
        else {
            Log.d(TAG, "Found a wrong project ID in getTask!");
            return null;
        }
    }

    public void addTask(String projectId, String startTime, String dueDate ) {
        Project project = findProject(projectId);
        if(project != null) {
            project.addTask("", "", "", Project.getDate(startTime), Project.getDate(dueDate));
        }
        else {
            Log.d(TAG, "Found a wrong project ID in addTask!");
        }
        saveProjects();
    }

    public void setTask(
            String projectId,
            int taskIndex,
            String name,
            String description,
            String members,
            String startTime,
            String dueDate) {
        Project project = findProject(projectId);
        if(project != null) {
            if(taskIndex >= project.getTaskNumber() || taskIndex < 0) {
                Log.d(TAG, "Found a wrong task index in setTask!");
            }
            project.setTask(taskIndex, name, description, members, Project.getDate(startTime), Project.getDate(dueDate));
        }
        else {
            Log.d(TAG, "Found a wrong project ID in setTask!");
        }
        saveProjects();
    }

    public void setTaskState(String projectId, int taskIndex, boolean completed) {
        Project project = findProject(projectId);
        if(project != null) {
            if(taskIndex >= project.getTaskNumber() || taskIndex < 0) {
                Log.d(TAG, "Found a wrong task index in setTask!");
            }
            project.setTaskCompletion(taskIndex, completed);
        }
        else {
            Log.d(TAG, "Found a wrong project ID in setTask!");
        }
        saveProjects();
    }

    public void deleteTask(String projectId, int taskIndex) {
        Project project = findProject(projectId);
        if(project != null) {
            if(taskIndex >= project.getTaskNumber() || taskIndex < 0) {
                Log.d(TAG, "Found a wrong task index in deleteTask!");
            }
            project.deleteTask(taskIndex);
        }
        else {
            Log.d(TAG, "Found a wrong project ID in deleteTask!");
        }
        saveProjects();
    }

    void setDefaultMap() {
        mMap = new TreeMap<Integer, Integer>();
        for(int i = 0; i < mProjects.size(); i++) {
            mMap.put(i, i);
        }
    }

    private void saveProjects() {
        if(mProjects == null)
            return;
        try {
            FileOutputStream fos = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mProjects);
            oos.close();
            Log.d(TAG, "Projects saved successfully.");
        } catch( IOException e ) {
            e.printStackTrace();
            Log.d(TAG, "Projects saved unsuccessfully!");
        }
    }

    public String saveOneProject(String id) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(mContext, "Cannot Access to External Storage!", Toast.LENGTH_SHORT).show();
            return ERROR_MSG;
        }
        Project project = findProject(id);
        if(project == null)
            return ERROR_MSG;
        try {
            String fileName = mContext.getExternalFilesDir(null)
                    + "/"
                    + FILE_NAME_PREFIX
                    + project.getName().replaceAll("[^a-zA-Z0-9.-]", "_")
                    + FILE_NAME_SUFFIX;
            File file = new File(fileName);
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(project);
            oos.close();
            Log.d(TAG, "Project export successfully.");
            return fileName;
        } catch( IOException e ) {
            e.printStackTrace();
            Log.d(TAG, "Project export unsuccessfully!");
        }
        return ERROR_MSG;
    }

    private void readProjects() {
        try {
            FileInputStream fis = mContext.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            mProjects = (ArrayList<Project>)ois.readObject();
            ois.close();
            if(mProjects != null) {
                for (Project project : mProjects) {
                    Log.d(TAG, "Recovered project " + project.getName());
                }
            }
            int projectNumber = mProjects.size();
            if(projectNumber > 1) {
                for(int i = 0; i < projectNumber - 1; i++) {
                    if(mProjects.get(i).getId().compareTo(mProjects.get(projectNumber - 1).getId()) == 0) {
                        mProjects.remove(projectNumber - 1);
                        Log.d(TAG, "Recovered project " + mProjects.get(i).getId());
                        break;
                    }
                }
            }
            setDefaultMap();
        } catch( ClassNotFoundException | IOException | ClassCastException e ) {
            Log.d(TAG, "Projects recovered unsuccessfully!!");
            e.printStackTrace();
            setDefaultMap();
        }
    }

    public void destroyTmpProject(String tmpID, String projectID) {
        Project orgProject = findProject(projectID);
        Project tmpProject = findProject(tmpID);
        if(orgProject == null || tmpProject == null) {
            Log.d(TAG, "Projects destroy unsuccessfully!!");
        }
        else {
            orgProject.copy(tmpProject);
            mProjects.remove(tmpProject);
            saveProjects();
        }
    }

    public void copyProject(String srcID, String dstID) {
        Project srcProject = findProject(srcID);
        Project dstProject = findProject(dstID);
        if(srcProject == null || dstProject == null) {
            Log.d(TAG, "Projects copy unsuccessfully!!");
        }
        else {
            dstProject.copy(srcProject);
            saveProjects();
        }
    }

    public void deleteProject(String projectID) {
        Project project = findProject(projectID);
        if(project == null) {
            Log.d(TAG, "Projects delete unsuccessfully!!");
        }
        else {
            mProjects.remove(project);
            saveProjects();
        }
    }
}
