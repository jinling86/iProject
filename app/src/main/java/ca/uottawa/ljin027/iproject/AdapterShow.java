package ca.uottawa.ljin027.iproject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * This class is implemented for CSI5175 Assignment 3.
 * This class defines the data structure used in the following array adapter.
 *
 * @author Ling Jin
 * @version 1.0
 * @since 19/04/2015
 */
class ShowList {
    private String task;
    private String members;
    private String description;
    private String startTime;
    private String dueDate;
    private boolean completed;
    private boolean allDone;

    public ShowList(
            String taskName,
            String taskMembers,
            String taskDescription,
            String taskStartTime,
            String taskDueDate,
            boolean taskCompleted,
            boolean projectCompleted) {
        task = taskName;
        members = taskMembers;
        description = taskDescription;
        startTime = taskStartTime;
        dueDate = taskDueDate;
        completed = taskCompleted;
        allDone = projectCompleted;
    }

    public String getTask() {
        return task;
    }

    public String getMembers() {
        return members;
    }

    public String getDescription() {
        return description;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isAllDone() {
        return completed;
    }

}

/**
 * This class is implemented for CSI5175 Assignment 3.
 * This class implements an adapter for populating the fields of the list view. The adapter supports
 * the check box in the list view.
 *
 * @author Ling Jin
 * @version 1.0
 * @since 18/04/2015
 */
public class AdapterShow extends ArrayAdapter<ShowList> {
    private List<ShowList> mShowList;
    private Context mContext;

    public AdapterShow(List<ShowList> showList, Context context) {
        super(context, R.layout.list_task_digest, showList);
        mShowList = showList;
        mContext = context;
    }

    private static class ViewHolder {
        public TextView name;
        public TextView members;
        public TextView description;
        public TextView from;
        public TextView to;
        public CheckBox completed;
        public TextView hint;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View aRow = convertView;
        ViewHolder viewHolder = new ViewHolder();
        if (aRow == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            aRow = inflater.inflate(R.layout.list_task_digest, null);

            viewHolder.name = (TextView) aRow.findViewById(R.id.showList_text_taskName);
            viewHolder.members = (TextView) aRow.findViewById(R.id.showList_text_taskMembers);
            viewHolder.description = (TextView) aRow.findViewById(R.id.showList_text_taskDescription);
            viewHolder.from = (TextView) aRow.findViewById(R.id.showList_text_taskStartTime);
            viewHolder.to = (TextView) aRow.findViewById(R.id.showList_text_taskDueDate);
            viewHolder.completed = (CheckBox) aRow.findViewById(R.id.showList_check_complete);
            viewHolder.completed.setTag(position);
            viewHolder.hint = (TextView) aRow.findViewById(R.id.showList_text_completionHint);

            aRow.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ShowList listItem = mShowList.get(position);
        viewHolder.name.setText(listItem.getTask());
        viewHolder.members.setText(listItem.getMembers());
        viewHolder.description.setText(listItem.getDescription());
        viewHolder.from.setText(listItem.getStartTime());
        viewHolder.to.setText(listItem.getDueDate());
        if (listItem.isAllDone() || listItem.isCompleted()) {
            viewHolder.completed.setClickable(false);
        }
        viewHolder.completed.setChecked(listItem.isCompleted());
        if (listItem.isCompleted())
            viewHolder.hint.setText("Done");
        else
            viewHolder.hint.setText("Doing");


        return aRow;
    }
}
