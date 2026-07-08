package hobby.asad.mushad.mycar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
    private List<Reminder> reminders;
    private Context context;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public ReminderAdapter(Context context, List<Reminder> reminders) {
        this.context = context;
        this.reminders = reminders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.textTitle.setText(reminder.getTitle());
        holder.switchEnabled.setChecked(reminder.isEnabled());
        
        if (reminder.getLastDate() > 0) {
            String label = context.getString(R.string.reminder_last_date);
            if ("TAX".equals(reminder.getType()) || "CUSTOM".equals(reminder.getType())) {
                label = context.getString(R.string.reminder_date);
            }
            holder.textLastDate.setText(String.format(label, dateFormat.format(new Date(reminder.getLastDate()))));
            holder.textLastDate.setVisibility(View.VISIBLE);
        } else {
            holder.textLastDate.setVisibility(View.GONE);
        }

        if (reminder.getDueDate() > 0 && !"TAX".equals(reminder.getType()) && !"CUSTOM".equals(reminder.getType())) {
            holder.textDueDate.setText(String.format(context.getString(R.string.reminder_due_date), dateFormat.format(new Date(reminder.getDueDate()))));
            holder.textDueDate.setVisibility(View.VISIBLE);
        } else {
            holder.textDueDate.setVisibility(View.GONE);
        }

        // Set toggle group state
        if ("RING".equals(reminder.getNotificationMode())) {
            holder.toggleGroup.check(R.id.btn_mode_ring);
        } else if ("VIBRATE".equals(reminder.getNotificationMode())) {
            holder.toggleGroup.check(R.id.btn_mode_vibrate);
        } else if ("MUTE".equals(reminder.getNotificationMode())) {
            holder.toggleGroup.check(R.id.btn_mode_mute);
        }

        holder.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            reminder.setEnabled(isChecked);
            ReminderManager.saveReminders(context, reminders);
        });

        holder.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_mode_ring) {
                    reminder.setNotificationMode("RING");
                } else if (checkedId == R.id.btn_mode_vibrate) {
                    reminder.setNotificationMode("VIBRATE");
                } else if (checkedId == R.id.btn_mode_mute) {
                    reminder.setNotificationMode("MUTE");
                }
                ReminderManager.saveReminders(context, reminders);
            }
        });

        holder.btnSetDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (reminder.getLastDate() > 0) {
                calendar.setTimeInMillis(reminder.getLastDate());
            }
            new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                long dateMillis = selected.getTimeInMillis();
                
                reminder.setLastDate(dateMillis);
                reminder.setDueDate(ReminderManager.calculateDueDate(reminder.getType(), dateMillis));
                
                notifyItemChanged(holder.getBindingAdapterPosition());
                ReminderManager.saveReminders(context, reminders);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textLastDate, textDueDate;
        MaterialSwitch switchEnabled;
        MaterialButtonToggleGroup toggleGroup;
        MaterialButton btnSetDate;
        ImageView imgIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_reminder_title);
            textLastDate = itemView.findViewById(R.id.text_last_date);
            textDueDate = itemView.findViewById(R.id.text_due_date);
            switchEnabled = itemView.findViewById(R.id.switch_reminder_enabled);
            toggleGroup = itemView.findViewById(R.id.toggle_group_mode);
            btnSetDate = itemView.findViewById(R.id.btn_set_date);
            imgIcon = itemView.findViewById(R.id.img_reminder_icon);
        }
    }
}
