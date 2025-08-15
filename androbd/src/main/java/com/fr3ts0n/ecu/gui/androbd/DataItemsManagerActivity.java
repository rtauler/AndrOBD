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
    private Button btnSelectAll;
    private Button btnDeselectAll;
    private Button btnSave;
    private Button btnCancel;
    
    private List<EcuDataItem> allItems;
    private List<EcuDataItem> orderedItems;
    private Map<String, Boolean> itemEnabledState;
    private DataItemsAdapter adapter;
    private SharedPreferences prefs;
    
    // Standard OBD-II PIDs (SAE J1979) - most commonly supported
    private static final String[] STANDARD_OBD_PIDS = {
        "PID_00", "PID_01", "PID_02", "PID_03", "PID_04", "PID_05", "PID_06", "PID_07", "PID_08", "PID_09",
        "PID_0A", "PID_0B", "PID_0C", "PID_0D", "PID_0E", "PID_0F", "PID_10", "PID_11", "PID_12", "PID_13",
        "PID_14", "PID_15", "PID_16", "PID_17", "PID_18", "PID_19", "PID_1A", "PID_1B", "PID_1C", "PID_1D",
        "PID_1E", "PID_1F", "PID_20", "PID_21", "PID_22", "PID_23", "PID_24", "PID_25", "PID_26", "PID_27",
        "PID_28", "PID_29", "PID_2A", "PID_2B", "PID_2C", "PID_2D", "PID_2E", "PID_2F", "PID_30", "PID_31",
        "PID_32", "PID_33", "PID_34", "PID_35", "PID_36", "PID_37", "PID_38", "PID_39", "PID_3A", "PID_3B",
        "PID_3C", "PID_3D", "PID_3E", "PID_3F", "PID_40", "PID_41", "PID_42", "PID_43", "PID_44", "PID_45",
        "PID_46", "PID_47", "PID_48", "PID_49", "PID_4A", "PID_4B", "PID_4C", "PID_4D", "PID_4E", "PID_4F",
        "PID_50", "PID_51", "PID_52", "PID_53", "PID_54", "PID_55", "PID_56", "PID_57", "PID_58", "PID_59",
        "PID_5A", "PID_5B", "PID_5C", "PID_5D", "PID_5E", "PID_60", "PID_61", "PID_62", "PID_63", "PID_64",
        "PID_65", "PID_66", "PID_67", "PID_68", "PID_69", "PID_6A", "PID_6B", "PID_6C", "PID_6D", "PID_6E",
        "PID_6F", "PID_70", "PID_71", "PID_72", "PID_73", "PID_74", "PID_75", "PID_76", "PID_77", "PID_78",
        "PID_79", "PID_7A", "PID_7B", "PID_7C", "PID_7D", "PID_7E", "PID_7F"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_items_manager);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Initialize views
        listView = findViewById(R.id.list_data_items);
        btnStandardPids = findViewById(R.id.btn_standard_pids);
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
        btnSelectAll.setOnClickListener(v -> selectAll());
        btnDeselectAll.setOnClickListener(v -> deselectAll());
        btnSave.setOnClickListener(v -> showSaveConfirmation());
        btnCancel.setOnClickListener(v -> finish());
        
        // Update title with selection count
        updateTitle();
        
        // Set up long press to show reorder options
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            showReorderDialog(position);
            return true;
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
            String itemId = item.toString();
            for (String standardPid : STANDARD_OBD_PIDS) {
                if (itemId.equals(standardPid)) {
                    itemEnabledState.put(itemId, true);
                    break;
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        updateTitle();
        Toast.makeText(this, getString(R.string.standard_obd_pids_selected), Toast.LENGTH_SHORT).show();
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
    
    private void showReorderDialog(int position) {
        String[] options = {getString(R.string.move_to_top), getString(R.string.move_to_bottom), getString(R.string.move_up), getString(R.string.move_down)};
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.reorder_item))
            .setItems(options, (dialog, which) -> {
                EcuDataItem item = orderedItems.get(position);
                switch (which) {
                    case 0: // Move to Top
                        orderedItems.remove(position);
                        orderedItems.add(0, item);
                        break;
                    case 1: // Move to Bottom
                        orderedItems.remove(position);
                        orderedItems.add(item);
                        break;
                    case 2: // Move Up
                        if (position > 0) {
                            orderedItems.remove(position);
                            orderedItems.add(position - 1, item);
                        }
                        break;
                    case 3: // Move Down
                        if (position < orderedItems.size() - 1) {
                            orderedItems.remove(position);
                            orderedItems.add(position + 1, item);
                        }
                        break;
                }
                adapter.notifyDataSetChanged();
                updateTitle();
            })
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
            
            return convertView;
        }
    }
}
