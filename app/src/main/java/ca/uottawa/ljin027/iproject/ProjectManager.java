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
import java.util.Calendar;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.TreeMap;


/**
 * This class manages projects and their tasks. It manages project file read and write, generates
 * project reports, and provide interfaces for the Activities in the system.
 *
 * @author Ling Jin
 * @version 1.0
 * @since 15/04/2015
 */
public class ProjectManager {

    public static final String PROJECT_ID = "id";
    public static final String ERROR_MSG = "";
    public static final String BOOLEAN_MSG = "true";
    public static final String NO_WARNING = "There is no project due in two days.";
    // POS_ defines the items in the string array which contain information of project/task
    // An array is used to conveniently get the project/task contents
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
    // Max length of the name string in the report
    private final int NAME_LENGTH = 30;

    private ArrayList<Project> mProjects;
    // A map is used here to manage the sorted projects
    private TreeMap<Integer, Integer> mMap;
    private Context mContext;

    Project findProject(String id) {
        if (id == null) {
            Log.d(TAG, "Received a wrong id");
            return null;
        }
        for (Project project : mProjects) {
            if (project.isMe(id))
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
        if (project != null) {
            project.setName(name);
            project.setDescription(description);
            project.setCourseName(courseName);
            project.setInstructor(instructor);
            project.setStartTime(Project.getDate(startTime));
            project.setDueDate(Project.getDate(dueDate));
        } else {
            Log.d(TAG, "Found a wrong project ID in setProject!");
        }
        saveProjects();
    }

    public void setProjectState(String id, int importance, boolean completed) {
        Project project = findProject(id);
        if (project != null || (importance != Project.UNIMPORTANT && importance != Project.IMPORTANT)) {
            project.setCompletion(completed);
            project.setImportance(importance);
        } else {
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
        Calendar now = Calendar.getInstance();
        long currentTime = now.getTimeInMillis();
        ;
        now.add(Calendar.HOUR, 1);
        long anHourLater = now.getTimeInMillis();
        project.setStartTime(new Date(currentTime));
        project.setDueDate(new Date(anHourLater));
        project.setCompletion(false);
        project.setImportance(Project.UNIMPORTANT);
        mProjects.add(project);
        saveProjects();
        Log.d(TAG, "Created a new project.");
        return project.getId();
    }

    public String addProjectFromExternal(Project exProject) {
        Project project = new Project();
        project.copy(exProject);
        mProjects.add(project);
        saveProjects();
        Log.d(TAG, "Imported a new project.");
        return project.getId();
    }

    private void loadProject(Project project, String[] contents) {
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
        if (project.getCompletion()) {
            contents[POS_COMPLETION] = BOOLEAN_MSG;
        } else {
            contents[POS_COMPLETION] = ERROR_MSG;
        }
        if (project.getImportance() == Project.IMPORTANT) {
            contents[POS_IMPORTANCE] = BOOLEAN_MSG;
        } else {
            contents[POS_IMPORTANCE] = ERROR_MSG;
        }
    }

    public void getProjectByID(String id, String[] contents) {
        Project project = findProject(id);
        if (project != null) {
            loadProject(project, contents);
        } else {
            for (int i = 0; i < POS_MAX; i++)
                contents[i] = ERROR_MSG;
            Log.d(TAG, "Found a wrong project ID in getProjectByID!");
        }
    }

    public void getProjectByIndex(int index, String[] contents) {
        if (mMap.containsKey(index)) {
            Project project = mProjects.get(mMap.get(index));
            loadProject(project, contents);
        } else {
            for (int i = 0; i < POS_MAX; i++)
                contents[i] = ERROR_MSG;
            Log.d(TAG, "Found a wrong project ID in getProjectByIndex!");
        }
    }

    public String getIdByIndex(int index) {
        if (mMap.containsKey(index)) {
            Project project = mProjects.get(mMap.get(index));
            return project.getId();
        } else {
            Log.d(TAG, "Found a wrong project ID in getIdByIndex!");
            return ERROR_MSG;
        }
    }

    public int getProjectNumber() {
        return mProjects.size();
    }

    public int getTaskNumber(String projectID) {
        Project project = findProject(projectID);
        if (project != null) {
            return project.getTaskNumber();
        } else {
            Log.d(TAG, "Found a wrong project ID in getTaskNumber!");
            return -1;
        }
    }

    public String[] getTask(String projectID, int taskIndex) {
        Project project = findProject(projectID);
        if (project != null) {
            if (taskIndex >= project.getTaskNumber() || taskIndex < 0) {
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
            if (project.getTaskCompletion(taskIndex)) {
                contents[POS_COMPLETION] = BOOLEAN_MSG;
            } else {
                contents[POS_COMPLETION] = ERROR_MSG;
            }

            return contents;
        } else {
            Log.d(TAG, "Found a wrong project ID in getTask!");
            return null;
        }
    }

    public void addTask(String projectId, String startTime, String dueDate) {
        Project project = findProject(projectId);
        if (project != null) {
            project.addTask("", "", "", Project.getDate(startTime), Project.getDate(dueDate));
        } else {
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
        if (project != null) {
            if (taskIndex >= project.getTaskNumber() || taskIndex < 0) {
                Log.d(TAG, "Found a wrong task index in setTask!");
            }
            project.setTask(taskIndex, name, description, members, Project.getDate(startTime), Project.getDate(dueDate));
        } else {
            Log.d(TAG, "Found a wrong project ID in setTask!");
        }
        saveProjects();
    }

    public void setTaskState(String projectId, int taskIndex, boolean completed) {
        Project project = findProject(projectId);
        if (project != null) {
            if (taskIndex >= project.getTaskNumber() || taskIndex < 0) {
                Log.d(TAG, "Found a wrong task index in setTask!");
            }
            project.setTaskCompletion(taskIndex, completed);
        } else {
            Log.d(TAG, "Found a wrong project ID in setTask!");
        }
        saveProjects();
    }

    public void deleteTask(String projectId, int taskIndex) {
        Project project = findProject(projectId);
        if (project != null) {
            if (taskIndex >= project.getTaskNumber() || taskIndex < 0) {
                Log.d(TAG, "Found a wrong task index in deleteTask!");
            }
            project.deleteTask(taskIndex);
        } else {
            Log.d(TAG, "Found a wrong project ID in deleteTask!");
        }
        saveProjects();
    }

    public void setDefaultMap() {
        // Default sorting method, do nothing
        mMap = new TreeMap<Integer, Integer>();
        for (int i = 0; i < mProjects.size(); i++) {
            mMap.put(i, i);
        }
    }

    public void setPriorityMap() {
        // Sort by importance
        // The project has a same importance but has a earlier due date is consider more important
        setDateDescendingMap();
        TreeMap<Integer, Integer> sortedMap = mMap;
        mMap = new TreeMap<Integer, Integer>();
        int index = 0;
        for (int i = 0; i < mProjects.size(); i++) {
            if (mProjects.get(sortedMap.get(i)).getImportance() == Project.IMPORTANT) {
                mMap.put(index, sortedMap.get(i));
                index++;
            }
        }
        for (int i = 0; i < mProjects.size(); i++) {
            if (mProjects.get(sortedMap.get(i)).getImportance() != Project.IMPORTANT) {
                mMap.put(index, sortedMap.get(i));
                index++;
            }
        }
    }

    public void setDateDescendingMap() {
        // Sort by due date
        PriorityQueue<Project> sorted = new PriorityQueue<Project>(mProjects);
        mMap = new TreeMap<Integer, Integer>();
        int i = 0;
        for (Project project = sorted.poll(); project != null; project = sorted.poll()) {
            mMap.put(i, mProjects.indexOf(project));
            i++;
        }
    }

    private void saveProjects() {
        if (mProjects == null)
            return;
        try {
            FileOutputStream fos = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mProjects);
            oos.close();
            Log.d(TAG, "Projects saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Projects saved unsuccessfully!");
        }
    }

    public String saveOneProject(String id) {
        // Save a project for exporting
        // Only use external storage can the file be read by the mail app
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(mContext, "Cannot Access to External Storage!", Toast.LENGTH_SHORT).show();
            return ERROR_MSG;
        }
        Project project = findProject(id);
        if (project == null)
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
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Project export unsuccessfully!");
        }
        return ERROR_MSG;
    }

    public void deleteDuplicatedProjects() {
        ArrayList<Project> toDelete = new ArrayList<Project>();
        for(Project project: mProjects) {
            if(project.isDuplicated()) {
                Log.d(TAG, "Remove duplicated project " + project.getId());
                toDelete.add(project);
            }
        }
        if(toDelete.size() != 0) {
            mProjects.removeAll(toDelete);
            saveProjects();
            setDefaultMap();
        }
    }

    private void readProjects() {
        try {
            FileInputStream fis = mContext.openFileInput(FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            mProjects = (ArrayList<Project>) ois.readObject();
            ois.close();
            if (mProjects != null) {
                for (Project project : mProjects) {
                    Log.d(TAG, "Recovered project " + project.getId());
                }
            }
            int projectNumber = mProjects.size();
            if (projectNumber > 1) {
                for (int i = 0; i < projectNumber - 1; i++) {
                    if (mProjects.get(i).getId().compareTo(mProjects.get(projectNumber - 1).getId()) == 0) {
                        mProjects.remove(projectNumber - 1);
                        Log.d(TAG, "Recovered project " + mProjects.get(i).getId());
                        break;
                    }
                }
            }
            setDefaultMap();
        } catch (ClassNotFoundException | IOException | ClassCastException e) {
            Log.d(TAG, "Projects recovered unsuccessfully!!");
            e.printStackTrace();
            setDefaultMap();
        }
    }

    public void destroyTmpProject(String tmpID, String projectID) {
        // Copy the temporary project to the original project and delete the temporary one
        Project orgProject = findProject(projectID);
        Project tmpProject = findProject(tmpID);
        if (orgProject == null || tmpProject == null) {
            Log.d(TAG, "Projects destroy unsuccessfully!!");
        } else {
            orgProject.copy(tmpProject);
            mProjects.remove(tmpProject);
            saveProjects();
        }
    }

    public void copyProject(String srcID, String dstID) {
        Project srcProject = findProject(srcID);
        Project dstProject = findProject(dstID);
        if (srcProject == null || dstProject == null) {
            Log.d(TAG, "Projects copy unsuccessfully!!");
        } else {
            dstProject.copy(srcProject);
            dstProject.setDuplicated(true);
            saveProjects();
        }
    }

    public void deleteProject(String projectID) {
        Project project = findProject(projectID);
        if (project == null) {
            Log.d(TAG, "Projects delete unsuccessfully!!");
        } else {
            mProjects.remove(project);
            saveProjects();
        }
    }

    public String getOngoingReport() {
        // Generate report
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < mProjects.size(); i++) {
            if (mProjects.get(i).getCompletion())
                continue;
            builder.append("\nProject : ");
            if (mProjects.get(i).getName().length() >= NAME_LENGTH)
                builder.append(mProjects.get(i).getName().substring(0, NAME_LENGTH));
            else
                builder.append(mProjects.get(i).getName());
            builder.append("\nContains: ");
            if (mProjects.get(i).getTaskNumber() == 0)
                builder.append("No task.");
            else if (mProjects.get(i).getTaskNumber() == 1)
                builder.append("1 task.");
            else {
                builder.append(mProjects.get(i).getTaskNumber());
                builder.append(" tasks.");
            }
            builder.append("\nDue on  : ");
            builder.append(Project.getDateString(mProjects.get(i).getDueDate()));
            builder.append(" @ ");
            builder.append(Project.getTimeString(mProjects.get(i).getDueDate()));
            builder.append("\n");
        }
        if (builder.length() == 0)
            builder.append("There is no ongoing project.");
        else
            builder.insert(0, "Ongoing projects:\n");
        return builder.toString();
    }

    public String getWarning() {
        // Generate warning
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < mProjects.size(); i++) {
            if (mProjects.get(i).getCompletion())
                continue;
            Long currentTime = System.currentTimeMillis();
            Long dueTime = mProjects.get(i).getDueDate().getTime();
            if (dueTime - currentTime >= 2 * 24 * 60 * 60 * 1000)
                continue;
            else if (dueTime - currentTime < 0) {
                builder.append("\nProject : ");
                if (mProjects.get(i).getName().length() >= NAME_LENGTH)
                    builder.append(mProjects.get(i).getName().substring(0, NAME_LENGTH));
                else
                    builder.append(mProjects.get(i).getName());
                builder.append("\nHas been due on : ");
                builder.append(Project.getDateString(mProjects.get(i).getDueDate()));
                builder.append(" @ ");
                builder.append(Project.getTimeString(mProjects.get(i).getDueDate()));
                builder.append("!\n\n");
            } else {
                builder.append("\nProject : ");
                if (mProjects.get(i).getName().length() >= NAME_LENGTH)
                    builder.append(mProjects.get(i).getName().substring(0, NAME_LENGTH));
                else
                    builder.append(mProjects.get(i).getName());
                builder.append("\nWill be due on  : ");
                builder.append(Project.getDateString(mProjects.get(i).getDueDate()));
                builder.append(" @ ");
                builder.append(Project.getTimeString(mProjects.get(i).getDueDate()));
                builder.append("!\n");
            }
        }
        if (builder.length() == 0)
            builder.append(NO_WARNING);
        else
            builder.insert(0, "Projects due in two days:\n");
        return builder.toString();
    }
}
