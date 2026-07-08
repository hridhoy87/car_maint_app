package hobby.asad.mushad.mycar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

public class VehicleUtils {

    public static void showAddVehicleDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_vehicle, null);
        
        EditText editModel = dialogView.findViewById(R.id.edit_model);
        EditText editReg = dialogView.findViewById(R.id.edit_reg_number);
        EditText editColor = dialogView.findViewById(R.id.edit_color);
        EditText editOdo = dialogView.findViewById(R.id.edit_odo);
        Spinner spinnerTrans = dialogView.findViewById(R.id.spinner_transmission);
        EditText editChassis = dialogView.findViewById(R.id.edit_chassis);
        EditText editTank = dialogView.findViewById(R.id.edit_tank_capacity);
        EditText editOil = dialogView.findViewById(R.id.edit_engine_oil);

        View formContainer = dialogView.findViewById(R.id.form_container);
        View loadingContainer = dialogView.findViewById(R.id.loading_container);
        View successContainer = dialogView.findViewById(R.id.success_container);
        View buttonContainer = dialogView.findViewById(R.id.button_container);
        View dialogTitle = dialogView.findViewById(R.id.dialog_title);

        String[] transTypes = {context.getString(R.string.trans_manual), context.getString(R.string.trans_automatic)};
        ArrayAdapter<String> transAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, android.R.id.text1, transTypes);
        transAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerTrans.setAdapter(transAdapter);

        AlertDialog dialog = new AlertDialog.Builder(context, R.style.CustomDialogTheme)
                .setView(dialogView)
                .create();

        dialog.show();

        // Make the dialog wider (95% of screen width)
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.95);
            layoutParams.width = width;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }

        dialogView.findViewById(R.id.btn_dialog_cancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btn_dialog_add).setOnClickListener(v -> {
            String model = editModel.getText().toString();
            String reg = editReg.getText().toString();
            
            if (model.isEmpty() || reg.isEmpty()) {
                Toast.makeText(context, "Model and Reg Number are required", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject json = new JSONObject();
                json.put("model", model);
                json.put("reg_number", reg);
                json.put("color", editColor.getText().toString());
                json.put("odo", editOdo.getText().toString());
                json.put("transmission", spinnerTrans.getSelectedItem().toString());
                json.put("chassis_number", editChassis.getText().toString());
                json.put("tank_capacity", editTank.getText().toString());
                json.put("engine_oil_capacity", editOil.getText().toString());

                // Log the JSON
                android.util.Log.d("AddVehicle", "Vehicle Data: " + json.toString(4));

                // Transition to Loading
                formContainer.setVisibility(View.GONE);
                buttonContainer.setVisibility(View.GONE);
                dialogTitle.setVisibility(View.GONE);
                loadingContainer.setVisibility(View.VISIBLE);

                // Simulate processing delay
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    loadingContainer.setVisibility(View.GONE);
                    successContainer.setVisibility(View.VISIBLE);

                    // Dismiss dialog after success
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(dialog::dismiss, 1500);
                }, 2000);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "Error processing data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
