package ca.uottawa.ljin027.iproject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * This class defines the data structure used in the following array adapter.
 *
 * @author Ling Jin
 * @version 1.0
 * @since 19/04/2015
 */
class ListList {
    private String projectName;
    private String courseDigest;
    private String taskDigest;
    private String timeDigest;
    private boolean completed;
    private boolean important;
    private int timeProgress;
    private int taskProgress;

    public ListList(String name,
                    String courseDigest,
                    String taskDigest,
                    String timeDigest,
                    boolean completed,
                    boolean important,
                    int taskProgress,
                    int timeProgress) {
        this.projectName = name;
        this.courseDigest = courseDigest;
        this.taskDigest = taskDigest;
        this.timeDigest = timeDigest;
        this.completed = completed;
        this.important = important;
        this.timeProgress = taskProgress;
        this.taskProgress = timeProgress;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getCourseDigest() {
        return courseDigest;
    }

    public String getTaskDigest() {
        return taskDigest;
    }

    public String getTimeDigest() {
        return timeDigest;
    }

    public void setTimeDigest(String timeDigest) {
        this.timeDigest = timeDigest;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isImportant() {
        return important;
    }

    public int getTimeProgress() {
        return timeProgress;
    }

    public int getTaskProgress() {
        return taskProgress;
    }
}

/**
 * This class implements an adapter for populating the fields of the list view. The adapter provides
 * supports for progress bars and image buttons in the list view.
 *
 * @author Ling Jin
 * @version 1.0
 * @since 19/04/2015
 */
public class AdapterList extends ArrayAdapter<ListList> {
    private List<ListList> mListList;
    private Context mContext;

    public AdapterList(List<ListList> listList, Context context) {
        super(context, R.layout.list_project_digest, listList);
        mListList = listList;
        mContext = context;
    }

    private static class ViewHolder {
        public TextView name;
        public TextView course;
        public TextView task;
        public TextView remainder;
        public ImageView importance;
        public ImageView completion;
        public ProgressBar timeDomain;
        public ProgressBar taskDomain;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View aRow = convertView;
        ViewHolder viewHolder = new ViewHolder();
        if (aRow == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            aRow = inflater.inflate(R.layout.list_project_digest, null);

            viewHolder.name = (TextView) aRow.findViewById(R.id.list_text_name);
            viewHolder.course = (TextView) aRow.findViewById(R.id.list_text_courseDigest);
            viewHolder.task = (TextView) aRow.findViewById(R.id.list_text_taskDigest);
            viewHolder.remainder = (TextView) aRow.findViewById(R.id.list_text_timeDigest);
            viewHolder.importance = (ImageView) aRow.findViewById(R.id.list_image_importance);
            viewHolder.completion = (ImageView) aRow.findViewById(R.id.list_image_completion);
            viewHolder.timeDomain = (ProgressBar) aRow.findViewById(R.id.list_progress_time);
            viewHolder.taskDomain = (ProgressBar) aRow.findViewById(R.id.list_progress_task);

            aRow.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ListList listItem = mListList.get(position);

        viewHolder.name.setText(listItem.getProjectName());
        viewHolder.course.setText(listItem.getCourseDigest());
        viewHolder.task.setText(listItem.getTaskDigest());
        viewHolder.remainder.setText(listItem.getTimeDigest());
        // Select different icons
        if (listItem.isCompleted())
            viewHolder.completion.setBackgroundResource(R.drawable.ic_done);
        else
            viewHolder.completion.setBackgroundResource(R.drawable.ic_doing);
        if (listItem.isImportant())
            viewHolder.importance.setBackgroundResource(R.drawable.ic_important);
        else
            viewHolder.importance.setBackgroundResource(R.drawable.ic_unimportant);

        viewHolder.timeDomain.setProgress(listItem.getTimeProgress());
        viewHolder.taskDomain.setProgress(listItem.getTaskProgress());

        return aRow;
    }
}