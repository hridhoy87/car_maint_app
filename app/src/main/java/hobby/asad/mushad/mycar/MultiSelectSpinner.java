package hobby.asad.mushad.mycar;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import java.util.Arrays;

public class MultiSelectSpinner extends AppCompatTextView {

    private String[] items;
    private boolean[] selection;
    private OnSelectionChangedListener listener;

    public interface OnSelectionChangedListener {
        void onSelectionChanged(boolean[] selection);
    }

    public MultiSelectSpinner(Context context) {
        super(context);
        init();
    }

    public MultiSelectSpinner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setClickable(true);
        setFocusable(true);
        setBackgroundResource(R.drawable.bg_car_selector); // Reuse existing style
        setPadding(32, 16, 32, 16);
        setOnClickListener(v -> showDialog());
    }

    public void setItems(String[] items) {
        this.items = items;
        this.selection = new boolean[items.length];
        Arrays.fill(selection, false);
        updateText();
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.listener = listener;
    }

    public void setSelection(String[] selectedItems) {
        if (items == null) return;
        Arrays.fill(selection, false);
        for (String selected : selectedItems) {
            for (int i = 0; i < items.length; i++) {
                if (items[i].equalsIgnoreCase(selected)) {
                    selection[i] = true;
                }
            }
        }
        updateText();
    }

    public String getSelectedItemsFormatted() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < items.length; i++) {
            if (selection[i] && !items[i].equalsIgnoreCase("All")) {
                if (!first) sb.append("|");
                sb.append(items[i]);
                first = false;
            }
        }
        return sb.toString();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.title_select_categories);
        builder.setMultiChoiceItems(items, selection, (dialog, which, isChecked) -> {
            selection[which] = isChecked;
            if (items[which].equals("All") || items[which].equals(getContext().getString(R.string.head_all))) {
                Arrays.fill(selection, isChecked);
                ((AlertDialog)dialog).getListView().setItemChecked(which, isChecked);
                for(int i=0; i<items.length; i++) {
                    ((AlertDialog)dialog).getListView().setItemChecked(i, isChecked);
                }
            }
        });
        builder.setPositiveButton(R.string.btn_ok, (dialog, which) -> {
            updateText();
            if (listener != null) listener.onSelectionChanged(selection);
        });
        builder.show();
    }

    private void updateText() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = 0; i < items.length; i++) {
            if (selection[i]) {
                if (count > 0) sb.append(", ");
                sb.append(items[i]);
                count++;
            }
        }
        if (count == 0) setText(R.string.select_placeholder);
        else if (count == items.length) setText(R.string.all_selected);
        else setText(sb.toString());
    }
}
