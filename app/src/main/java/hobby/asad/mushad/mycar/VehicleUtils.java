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
                Toast.makeText(context, R.string.error_model_reg_required, Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Transition to Loading
                formContainer.setVisibility(View.GONE);
                buttonContainer.setVisibility(View.GONE);
                dialogTitle.setVisibility(View.GONE);
                loadingContainer.setVisibility(View.VISIBLE);

                String modelVal = editModel.getText().toString();
                String regVal = editReg.getText().toString();
                String colorVal = editColor.getText().toString();
                String odoVal = editOdo.getText().toString();
                String transVal = spinnerTrans.getSelectedItem().toString();
                String chassisVal = editChassis.getText().toString();
                String tankVal = editTank.getText().toString();
                String oilVal = editOil.getText().toString();

                new Thread(() -> {
                    try {
                        hobby.asad.mushad.mycar.database.AppDatabase db = hobby.asad.mushad.mycar.database.AppDatabase.getDatabase(context);
                        hobby.asad.mushad.mycar.database.Vehicle vehicle = new hobby.asad.mushad.mycar.database.Vehicle();
                        vehicle.model = modelVal;
                        vehicle.registrationNumber = regVal;
                        vehicle.color = colorVal;
                        try {
                            vehicle.currentOdometer = Integer.parseInt(odoVal);
                        } catch (NumberFormatException e) {
                            vehicle.currentOdometer = 0;
                        }
                        vehicle.transmissionType = transVal;
                        vehicle.chassisNumber = chassisVal;
                        try {
                            vehicle.tankCapacity = Double.parseDouble(tankVal);
                        } catch (NumberFormatException e) {
                            vehicle.tankCapacity = 0.0;
                        }
                        try {
                            vehicle.engineOilCapacity = Double.parseDouble(oilVal);
                        } catch (NumberFormatException e) {
                            vehicle.engineOilCapacity = 0.0;
                        }

                        db.vehicleDao().insert(vehicle);

                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            loadingContainer.setVisibility(View.GONE);
                            successContainer.setVisibility(View.VISIBLE);

                            // Dismiss dialog after success
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                dialog.dismiss();
                                // Trigger refresh if context is BaseActivity
                                if (context instanceof BaseActivity) {
                                    ((BaseActivity) context).recreate();
                                }
                            }, 1500);
                        }, 1000);
                    } catch (Exception e) {
                        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                            Toast.makeText(context, context.getString(R.string.error_saving_vehicle, e.getMessage()), Toast.LENGTH_SHORT).show();
                            formContainer.setVisibility(View.VISIBLE);
                            buttonContainer.setVisibility(View.VISIBLE);
                            dialogTitle.setVisibility(View.VISIBLE);
                            loadingContainer.setVisibility(View.GONE);
                        });
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, R.string.error_processing, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
