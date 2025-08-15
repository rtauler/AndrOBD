package com.fr3ts0n.ecu.gui.androbd;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;
import android.app.AlertDialog;

import com.fr3ts0n.ecu.EcuDataItem;
import com.fr3ts0n.ecu.EcuDataPv;
import com.fr3ts0n.ecu.prot.obd.ObdProt;
import com.fr3ts0n.ecu.gui.androbd.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity for managing OBD data items with reorderable list and activate/deactivate functionality
 */
public class DataItemsManagerActivity extends Activity {
    
    private static final String KEY_DATA_ITEMS = "data_items";
    private static final String KEY_DATA_ITEMS_ORDER = "data_items_order";
    
    private ListView listView;
    private Button btnStandardPids;
    private Button btnKtmHusqvarna;
    private Button btnSelectAll;
    private Button btnDeselectAll;
    private Button btnSave;
    private Button btnCancel;
    
    private List<EcuDataItem> allItems;
    private List<EcuDataItem> orderedItems;
    private Map<String, Boolean> itemEnabledState;
    private DataItemsAdapter adapter;
    private SharedPreferences prefs;
    
    // Standard OBD-II PIDs (SAE J1979) mapped to actual mnemonics in the system
    // This maps the standard PIDs to the actual mnemonics used in AndrOBD
    private static final String[] STANDARD_OBD_PIDS = {
        // PID 00-0F: Basic engine data
        "number_fault_codes",      // PID 01 - Number of Fault Codes
        "status_mil",             // PID 01 - MIL status
        "status_misfires",        // PID 01 - Misfire status
        "status_fuel_system",     // PID 01 - Fuel system test status
        "status_component_test",  // PID 01 - Component test status
        "status_ignition_monitoring", // PID 01 - Ignition monitoring status
        "status_catalyst_test",   // PID 01 - Catalyst test status
        "status_catalyst_test_nox_monitor", // PID 01 - Heated Cat / Nox Monitor test status
        "status_evaporative_system_test", // PID 01 - Evaporative system test status
        "status_secondary_air_system_test", // PID 01 - Secondary air sys test status
        "status_ac_refrigerant_test", // PID 01 - A/C refrigerant test status
        "status_oxygen_sensor_test", // PID 01 - Oxygen Sensor test status
        "status_oxygen_sensor_heater_test", // PID 01 - Oxygen Sensor Heater test status
        "status_egr_system_test", // PID 01 - EGR System test status
        "location_fault_code",    // PID 02 - DTC location
        "status_fuel_system_1",  // PID 03 - Fuel System 1 Status
        "status_fuel_system_2",  // PID 03 - Fuel System 2 Status
        "engine_load_calculated", // PID 04 - Calculated Load Value
        "engine_coolant_temperature", // PID 05 - Coolant Temperature
        "fuel_trim_short_b1",    // PID 06 - Short Term Fuel Trim (Bank 1)
        "fuel_trim_long_b1",     // PID 07 - Long Term Fuel Trim (Bank 1)
        "fuel_trim_short_b2",    // PID 08 - Short Term Fuel Trim (Bank 2)
        "fuel_trim_long_b2",     // PID 09 - Long Term Fuel Trim (Bank 2)
        "fuel_pressure",         // PID 0A - Fuel Pressure (gauge)
        "intake_manifold_pressure", // PID 0B - Intake Manifold Pressure
        "engine_speed",          // PID 0C - Engine RPM
        "vehicle_speed",         // PID 0D - Vehicle Speed
        "ignition_timing_advance_cyl1", // PID 0E - Timing Advance (Cyl. #1)
        "intake_air_temperature", // PID 0F - Intake Air Temperature
        "mass_airflow",          // PID 10 - Air Flow Rate (MAF sensor)
        "throttle_position_abs", // PID 11 - Absolute Throttle Position
        "status_secondary_air_system", // PID 12 - Secondary air status
        "number_oxygen_sensors", // PID 13 - Oxygen sensors present
        
        // PID 14-1B: O2 Sensor voltages and fuel trims (Bank 1-4)
        "o2_sensor_voltage_b1s1", // PID 14 - O2 Sensor B1S1
        "o2_sensor_fuel_trim_b1s1", // PID 14 - O2 Sensor fuel trim B1S1
        "o2_sensor_voltage_b1s2", // PID 15 - O2 Sensor B1S2
        "o2_sensor_fuel_trim_b1s2", // PID 15 - O2 Sensor fuel trim B1S2
        "o2_sensor_voltage_b1s3", // PID 16 - O2 Sensor B1S3
        "o2_sensor_fuel_trim_b1s3", // PID 16 - O2 Sensor fuel trim B1S3
        "o2_sensor_voltage_b1s4", // PID 17 - O2 Sensor B1S4
        "o2_sensor_fuel_trim_b1s4", // PID 17 - O2 Sensor fuel trim B1S4
        "o2_sensor_voltage_b2s1", // PID 18 - O2 Sensor B2S1
        "o2_sensor_fuel_trim_b2s1", // PID 18 - O2 Sensor fuel trim B2S1
        "o2_sensor_voltage_b2s2", // PID 19 - O2 Sensor B2S2
        "o2_sensor_fuel_trim_b2s2", // PID 19 - O2 Sensor fuel trim B2S2
        "o2_sensor_voltage_b2s3", // PID 1A - O2 Sensor B2S3
        "o2_sensor_fuel_trim_b2s3", // PID 1A - O2 Sensor fuel trim B2S3
        "o2_sensor_voltage_b2s4", // PID 1B - O2 Sensor B2S4
        "o2_sensor_fuel_trim_b2s4", // PID 1B - O2 Sensor fuel trim B2S4
        
        // PID 1C-1F: Additional engine data
        "obd_type",              // PID 1C - OBD conforms to
        "map_oxygen_sensors_present", // PID 1D - Oxygen sensors present
        "status_power_take_off", // PID 1E - Power Take-Off Status
        "running_time",          // PID 1F - Time Since Engine Start
        
        // PID 21-2F: Extended engine data
        "distance_sine_mil_active", // PID 21 - Distance since MIL activated
        "fuel_pressure_rel",     // PID 22 - Fuel Rail Pressure (rel. to manifold vacuum)
        "fuel_pressure_wr",      // PID 23 - Fuel Pressure (gauge) wide range
        "o2_wr_lambda_b1s1",    // PID 24 - O2 Sensor B1S1 lambda (Bank 1 WR)
        "o2_wr_voltage_b1s1",   // PID 24 - O2 Sensor B1S1 (Bank 1 WR)
        "o2_wr_lambda_b1s2",    // PID 25 - O2 Sensor B1S2 lambda (Bank 1 WR)
        "o2_wr_voltage_b1s2",   // PID 25 - O2 Sensor B1S2 (Bank 1 WR)
        "o2_wr_lambda_b1s3",    // PID 26 - O2 Sensor B1S3 lambda (Bank 1 WR)
        "o2_wr_voltage_b1s3",   // PID 26 - O2 Sensor B1S3 (Bank 1 WR)
        "o2_wr_lambda_b1s4",    // PID 27 - O2 Sensor B1S4 lambda (Bank 1 WR)
        "o2_wr_voltage_b1s4",   // PID 27 - O2 Sensor B1S4 (Bank 1 WR)
        "o2_wr_lambda_b2s1",    // PID 28 - O2 Sensor B2S1 lambda (Bank 2 WR)
        "o2_wr_voltage_b2s1",   // PID 28 - O2 Sensor B2S1 (Bank 2 WR)
        "o2_wr_lambda_b2s2",    // PID 29 - O2 Sensor B2S2 lambda (Bank 2 WR)
        "o2_wr_voltage_b2s2",   // PID 29 - O2 Sensor B2S2 (Bank 2 WR)
        "o2_wr_lambda_b2s3",    // PID 2A - O2 Sensor B2S3 lambda (Bank 2 WR)
        "o2_wr_voltage_b2s3",   // PID 2A - O2 Sensor B2S3 (Bank 2 WR)
        "o2_wr_lambda_b2s4",    // PID 2B - O2 Sensor B2S4 lambda (Bank 2 WR)
        "o2_wr_voltage_b2s4",   // PID 2B - O2 Sensor B2S4 (Bank 2 WR)
        "egr_ratio_commanded",  // PID 2C - Commanded EGR
        "egr_error",            // PID 2D - EGR Error
        "evaporative_purge_ratio", // PID 2E - Commanded Evaporative Purge
        "fuel_level",           // PID 2F - Fuel Level Input
        
        // PID 30-3F: Additional engine parameters
        "counts_warmups_since_ecu_reset", // PID 30 - Warm-ups since ECU reset
        "distance_since_ecu_reset", // PID 31 - Distance since ECU reset
        "pressure_vapor_evaporative_purge", // PID 32 - Evap System Vapor Pressure
        "barometric_pressure",  // PID 33 - Barometric Pressure (absolute)
        "o2_wr_lambda_s1",     // PID 34 - O2 Sensor 1 lambda (Bank 1 WR)
        "o2_wr_current_s1",    // PID 34 - O2 Sensor 1 current (Bank 1 WR)
        "o2_wr_lambda_s2",     // PID 35 - O2 Sensor 2 lambda (Bank 1 WR)
        "o2_wr_current_s2",    // PID 35 - O2 Sensor 2 current (Bank 1 WR)
        "o2_wr_lambda_s3",     // PID 36 - O2 Sensor 3 lambda (Bank 1 WR)
        "o2_wr_current_s3",    // PID 36 - O2 Sensor 3 current (Bank 1 WR)
        "o2_wr_lambda_s4",     // PID 37 - O2 Sensor 4 lambda (Bank 1 WR)
        "o2_wr_current_s4",    // PID 37 - O2 Sensor 4 current (Bank 1 WR)
        "o2_wr_lambda_s5",     // PID 38 - O2 Sensor 5 lambda (Bank 2 WR)
        "o2_wr_current_s5",    // PID 38 - O2 Sensor 5 current (Bank 2 WR)
        "o2_wr_lambda_s6",     // PID 39 - O2 Sensor 6 lambda (Bank 2 WR)
        "o2_wr_current_s6",    // PID 39 - O2 Sensor 6 current (Bank 2 WR)
        "o2_wr_lambda_s7",     // PID 3A - O2 Sensor 7 lambda (Bank 2 WR)
        "o2_wr_current_s7",    // PID 3A - O2 Sensor 7 current (Bank 2 WR)
        "o2_wr_lambda_s8",     // PID 3B - O2 Sensor 8 lambda (Bank 2 WR)
        "o2_wr_current_s8",    // PID 3B - O2 Sensor 8 current (Bank 2 WR)
        "cat_temperature_b1s1", // PID 3C - CAT Temperature B1S1
        "cat_temperature_b2s1", // PID 3D - CAT Temperature B2S1
        "cat_temperature_b1s2", // PID 3E - CAT Temperature B1S2
        "cat_temperature_b2s2"  // PID 3F - CAT Temperature B2S2
    };
    
    // KTM/Husqvarna motorcycle-specific PIDs (Mode 01) - commonly supported
    private static final String[] KTM_HUSQVARNA_PIDS = {
        // PID 01: Monitor status since DTCs cleared
        "status_mil",             // PID 01 - MIL status
        "status_misfires",        // PID 01 - Misfire status
        "status_fuel_system",     // PID 01 - Fuel system test status
        "status_component_test",  // PID 01 - Component test status
        "status_ignition_monitoring", // PID 01 - Ignition monitoring status
        "status_catalyst_test",   // PID 01 - Catalyst test status
        "status_evaporative_system_test", // PID 01 - Evaporative system test status
        "status_secondary_air_system_test", // PID 01 - Secondary air sys test status
        "status_ac_refrigerant_test", // PID 01 - A/C refrigerant test status
        "status_oxygen_sensor_test", // PID 01 - Oxygen Sensor test status
        "status_oxygen_sensor_heater_test", // PID 01 - Oxygen Sensor Heater test status
        "status_egr_system_test", // PID 01 - EGR System test status
        
        // PID 03: Fuel system status
        "status_fuel_system_1",  // PID 03 - Fuel System 1 Status
        "status_fuel_system_2",  // PID 03 - Fuel System 2 Status
        
        // PID 04-0F: Basic engine data
        "engine_load_calculated", // PID 04 - Calculated Load Value
        "engine_coolant_temperature", // PID 05 - Coolant Temperature
        "intake_manifold_pressure", // PID 0B - Intake Manifold Pressure
        "engine_speed",          // PID 0C - Engine RPM
        "vehicle_speed",         // PID 0D - Vehicle Speed
        "ignition_timing_advance_cyl1", // PID 0E - Timing Advance (Cyl. #1)
        "intake_air_temperature", // PID 0F - Intake Air Temperature
        "mass_airflow",          // PID 10 - Air Flow Rate (MAF sensor)
        "throttle_position_abs", // PID 11 - Absolute Throttle Position
        
        // PID 1F: Run time since engine start
        "running_time",          // PID 1F - Time Since Engine Start
        
        // PID 21: Distance traveled with MIL on
        "distance_sine_mil_active", // PID 21 - Distance since MIL activated
        
        // PID 2F: Fuel tank level input
        "fuel_level",           // PID 2F - Fuel Level Input
        
        // PID 31: Distance traveled since codes cleared
        "distance_since_ecu_reset", // PID 31 - Distance since ECU reset
        
        // PID 33: Barometric pressure
        "barometric_pressure",  // PID 33 - Barometric Pressure (absolute)
        
        // PID 42-4C: Additional engine parameters
        "control_module_voltage", // PID 42 - Control module voltage (if available)
        "absolute_load_value",  // PID 43 - Absolute load value (if available)
        "commanded_equivalence_ratio", // PID 44 - Commanded equivalence ratio (if available)
        "relative_throttle_position", // PID 45 - Relative throttle position (if available)
        "ambient_air_temperature", // PID 46 - Ambient air temperature (if available)
        "absolute_throttle_position_b", // PID 47 - Absolute throttle position B (if available)
        "accelerator_pedal_position_d", // PID 49 - Accelerator pedal position D (if available)
        "accelerator_pedal_position_e", // PID 4A - Accelerator pedal position E (if available)
        "commanded_throttle_actuator", // PID 4C - Commanded throttle actuator (if available)
        
        // PID 50-5D: Fuel and engine data
        "fuel_type",            // PID 50 - Fuel type (if available)
        "ethanol_fuel_percent", // PID 51 - Ethanol fuel % (if available)
        "engine_oil_temperature", // PID 5B - Engine oil temperature (if available)
        "fuel_injection_timing", // PID 5C - Fuel injection timing (if available)
        "engine_fuel_rate"      // PID 5D - Engine fuel rate (if available)
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_items_manager);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Initialize views
        listView = findViewById(R.id.list_data_items);
        btnStandardPids = findViewById(R.id.btn_standard_pids);
        btnKtmHusqvarna = findViewById(R.id.btn_ktm_husqvarna);
        btnSelectAll = findViewById(R.id.btn_select_all);
        btnDeselectAll = findViewById(R.id.btn_deselect_all);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        
        // Initialize itemEnabledState first
        itemEnabledState = new HashMap<>();
        
        // Load data items
        loadDataItems();
        
        // Set up adapter
        adapter = new DataItemsAdapter();
        listView.setAdapter(adapter);
        
        // Set up button click listeners
        btnStandardPids.setOnClickListener(v -> selectStandardPids());
        btnKtmHusqvarna.setOnClickListener(v -> selectKtmHusqvarnaPids());
        btnSelectAll.setOnClickListener(v -> selectAll());
        btnDeselectAll.setOnClickListener(v -> deselectAll());
        btnSave.setOnClickListener(v -> showSaveConfirmation());
        btnCancel.setOnClickListener(v -> finish());
        
        // Update title with selection count
        updateTitle();
        
        // Set up long press to show reorder options
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Log.d("DataItemsManager", "Long press detected at position: " + position);
            showReorderDialog(position);
            return true;
        });
        
        // Also add click listener for debugging
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("DataItemsManager", "Item clicked at position: " + position);
        });
    }
    
    private void loadDataItems() {
        // Get all available data items
        allItems = ObdProt.dataItems.getSvcDataItems(ObdProt.OBD_SVC_DATA);
        
        // Get current selection order
        String orderPref = prefs.getString(KEY_DATA_ITEMS_ORDER, "");
        if (!orderPref.isEmpty()) {
            // Load saved order
            String[] orderArray = orderPref.split(",");
            orderedItems = new ArrayList<>();
            
            // First add items in saved order
            for (String itemId : orderArray) {
                for (EcuDataItem item : allItems) {
                    if (item.toString().equals(itemId)) {
                        orderedItems.add(item);
                        break;
                    }
                }
            }
            
            // Add any remaining items that weren't in the saved order
            for (EcuDataItem item : allItems) {
                if (!orderedItems.contains(item)) {
                    orderedItems.add(item);
                }
            }
        } else {
            // First time - use default order
            orderedItems = new ArrayList<>(allItems);
        }
        
        // Initialize enabled state from preferences
        Set<String> enabledItems = prefs.getStringSet(KEY_DATA_ITEMS, new HashSet<>());
        if (enabledItems.isEmpty()) {
            // If no items are selected, default to all enabled
            for (EcuDataItem item : orderedItems) {
                itemEnabledState.put(item.toString(), true);
            }
        } else {
            for (EcuDataItem item : orderedItems) {
                String itemId = item.toString();
                itemEnabledState.put(itemId, enabledItems.contains(itemId));
            }
        }
    }
    
    private void selectStandardPids() {
        // Clear current selection
        for (EcuDataItem item : orderedItems) {
            itemEnabledState.put(item.toString(), false);
        }
        
        // Enable only standard OBD-II PIDs
        for (EcuDataItem item : orderedItems) {
            String itemMnemonic = (String) item.pv.get(EcuDataPv.FID_MNEMONIC);
            if (itemMnemonic != null) {
                for (String standardPid : STANDARD_OBD_PIDS) {
                    if (itemMnemonic.equals(standardPid)) {
                        itemEnabledState.put(item.toString(), true);
                        break;
                    }
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        updateTitle();
        Toast.makeText(this, getString(R.string.standard_obd_pids_selected), Toast.LENGTH_SHORT).show();
    }
    
    private void selectKtmHusqvarnaPids() {
        // Clear current selection
        for (EcuDataItem item : orderedItems) {
            itemEnabledState.put(item.toString(), false);
        }
        
        // Enable only KTM/Husqvarna-specific PIDs
        for (EcuDataItem item : orderedItems) {
            String itemMnemonic = (String) item.pv.get(EcuDataPv.FID_MNEMONIC);
            if (itemMnemonic != null) {
                for (String ktmPid : KTM_HUSQVARNA_PIDS) {
                    if (itemMnemonic.equals(ktmPid)) {
                        itemEnabledState.put(item.toString(), true);
                        break;
                    }
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        updateTitle();
        Toast.makeText(this, getString(R.string.ktm_husqvarna_pids_selected), Toast.LENGTH_SHORT).show();
    }
    
    private void selectAll() {
        for (EcuDataItem item : orderedItems) {
            itemEnabledState.put(item.toString(), true);
        }
        adapter.notifyDataSetChanged();
        updateTitle();
        Toast.makeText(this, getString(R.string.all_data_items_selected), Toast.LENGTH_SHORT).show();
    }
    
    private void deselectAll() {
        for (EcuDataItem item : orderedItems) {
            itemEnabledState.put(item.toString(), false);
        }
        adapter.notifyDataSetChanged();
        updateTitle();
        Toast.makeText(this, getString(R.string.all_data_items_deselected), Toast.LENGTH_SHORT).show();
    }
    
    private void updateTitle() {
        int enabledCount = 0;
        for (Boolean enabled : itemEnabledState.values()) {
            if (enabled) enabledCount++;
        }
        setTitle(getString(R.string.data_items_manager) + " (" + enabledCount + "/" + orderedItems.size() + ")");
    }
    
    private void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= orderedItems.size() || 
            toPosition < 0 || toPosition >= orderedItems.size()) {
            return;
        }
        
        EcuDataItem item = orderedItems.get(fromPosition);
        String itemName = item.label != null ? item.label : item.toString();
        
        // Remove item from current position and add to new position
        orderedItems.remove(fromPosition);
        orderedItems.add(toPosition, item);
        
        // Update the adapter and scroll to the new position
        adapter.notifyDataSetChanged();
        updateTitle();
        
        // Scroll to the new position to show the user where the item moved
        listView.setSelection(toPosition);
        
        // Show a toast message confirming the move
        String action = fromPosition > toPosition ? "moved up" : "moved down";
        Toast.makeText(this, itemName + " " + action, Toast.LENGTH_SHORT).show();
        
        Log.d("DataItemsManager", "Moved item from position " + fromPosition + " to " + toPosition);
    }
    
    private void showReorderDialog(int position) {
        EcuDataItem item = orderedItems.get(position);
        String itemName = item.label != null ? item.label : item.toString();
        
        Log.d("DataItemsManager", "Showing reorder dialog for item: " + itemName + " at position: " + position);
        
        String[] options = {getString(R.string.move_to_top), getString(R.string.move_to_bottom), getString(R.string.move_up), getString(R.string.move_down)};
        
        // Disable options that don't make sense
        boolean[] enabledOptions = new boolean[4];
        enabledOptions[0] = true;  // Move to Top - always enabled
        enabledOptions[1] = true;  // Move to Bottom - always enabled
        enabledOptions[2] = position > 0;  // Move Up - only if not at top
        enabledOptions[3] = position < orderedItems.size() - 1;  // Move Down - only if not at bottom
        
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.reorder_item) + ": " + itemName)
            .setItems(options, (dialog, which) -> {
                Log.d("DataItemsManager", "User selected option: " + which + " for item at position: " + position);
                
                EcuDataItem itemToMove = orderedItems.get(position);
                int newPosition = position;
                
                switch (which) {
                    case 0: // Move to Top
                        orderedItems.remove(position);
                        orderedItems.add(0, itemToMove);
                        newPosition = 0;
                        break;
                    case 1: // Move to Bottom
                        orderedItems.remove(position);
                        orderedItems.add(itemToMove);
                        newPosition = orderedItems.size() - 1;
                        break;
                    case 2: // Move Up
                        if (position > 0) {
                            orderedItems.remove(position);
                            orderedItems.add(position - 1, itemToMove);
                            newPosition = position - 1;
                        }
                        break;
                    case 3: // Move Down
                        if (position < orderedItems.size() - 1) {
                            orderedItems.remove(position);
                            orderedItems.add(position + 1, itemToMove);
                            newPosition = position + 1;
                        }
                        break;
                }
                
                // Update the adapter and scroll to the new position
                adapter.notifyDataSetChanged();
                updateTitle();
                
                // Scroll to the new position to show the user where the item moved
                listView.setSelection(newPosition);
                
                // Show a toast message confirming the move
                String action = "";
                switch (which) {
                    case 0: action = "moved to top"; break;
                    case 1: action = "moved to bottom"; break;
                    case 2: action = "moved up"; break;
                    case 3: action = "moved down"; break;
                }
                Toast.makeText(this, itemName + " " + action, Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(getString(R.string.cancel), null)
            .show();
    }
    
    private void saveSettings() {
        // Save enabled/disabled state
        Set<String> enabledItems = new HashSet<>();
        for (EcuDataItem item : orderedItems) {
            if (itemEnabledState.get(item.toString())) {
                enabledItems.add(item.toString());
            }
        }
        prefs.edit().putStringSet(KEY_DATA_ITEMS, enabledItems).apply();
        
        // Save order
        StringBuilder orderBuilder = new StringBuilder();
        for (EcuDataItem item : orderedItems) {
            if (orderBuilder.length() > 0) {
                orderBuilder.append(",");
            }
            orderBuilder.append(item.toString());
        }
        prefs.edit().putString(KEY_DATA_ITEMS_ORDER, orderBuilder.toString()).apply();
        
        Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void showSaveConfirmation() {
        int enabledCount = 0;
        for (Boolean enabled : itemEnabledState.values()) {
            if (enabled) enabledCount++;
        }
        
        String message = getString(R.string.save_configuration_message, enabledCount, orderedItems.size());
        
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_save))
            .setMessage(message)
            .setPositiveButton(getString(R.string.save), (dialog, which) -> saveSettings())
            .setNegativeButton(getString(R.string.cancel), null)
            .show();
    }
    
    private class DataItemsAdapter extends ArrayAdapter<EcuDataItem> {
        
        public DataItemsAdapter() {
            super(DataItemsManagerActivity.this, 0, orderedItems);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_data_item, parent, false);
            }
            
            EcuDataItem item = getItem(position);
            
            TextView tvLabel = convertView.findViewById(R.id.tv_label);
            TextView tvDescription = convertView.findViewById(R.id.tv_description);
            CheckBox checkBox = convertView.findViewById(R.id.cb_enabled);
            
            tvLabel.setText(item.label);
            tvDescription.setText(String.format("PID %s - %s", item.toString(), item.pv.get(EcuDataPv.FID_MNEMONIC)));
            
            // Set checkbox state
            Boolean enabled = itemEnabledState.get(item.toString());
            if (enabled == null) {
                // Default to enabled if not set
                enabled = true;
                itemEnabledState.put(item.toString(), enabled);
            }
            checkBox.setChecked(enabled);
            
            // Set checkbox listener
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                itemEnabledState.put(item.toString(), isChecked);
                updateTitle();
            });
            
            // Set up reorder buttons
            Button btnMoveUp = convertView.findViewById(R.id.btn_move_up);
            Button btnMoveDown = convertView.findViewById(R.id.btn_move_down);
            
            // Disable up button if at top, disable down button if at bottom
            btnMoveUp.setEnabled(position > 0);
            btnMoveDown.setEnabled(position < orderedItems.size() - 1);
            
            // Set button click listeners
            btnMoveUp.setOnClickListener(v -> {
                if (position > 0) {
                    moveItem(position, position - 1);
                }
            });
            
            btnMoveDown.setOnClickListener(v -> {
                if (position < orderedItems.size() - 1) {
                    moveItem(position, position + 1);
                }
            });
            
            return convertView;
        }
    }
}
