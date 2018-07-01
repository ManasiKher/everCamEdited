package io.evercam.androidapp.addeditcamera;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.Vendor;
import io.evercam.androidapp.EditCameraActivity;
import io.evercam.androidapp.EvercamPlayApplication;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.utils.Commons;

public class ModelSelectorFragment extends Fragment {
    private final String TAG = "ModelSelectorFragment";

    private final String VENDOR_SPINNER_KEY = "vendorSpinnerSelectedItem";
    private final String MODEL_SPINNER_KEY = "modelSpinnerSelectedItem";
    private int vendorSavedSelectedPosition = 0;
    private int modelSavedSelectedPosition = 0;

    private ImageView modelExplanationImageButton;
    private Spinner vendorSpinner;
    private Spinner modelSpinner;
    private TreeMap<String, String> vendorMap;
    private TreeMap<String, String> vendorMapIdAsKey;
    private TreeMap<String, String> modelMap;
    private ArrayList<Model> modelListGlobal;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_model_selector, container, false);
        vendorSpinner = (Spinner) rootView.findViewById(R.id.vendor_spinner);
        modelSpinner = (Spinner) rootView.findViewById(R.id.model_spinner);
        modelExplanationImageButton = (ImageView) rootView.findViewById(R.id.model_explanation_btn);

        final ImageView vendorLogoImageView = (ImageView) rootView.findViewById(R.id.vendor_logo_image_view);
        final ImageView modelThumbnailImageView = (ImageView) rootView.findViewById(R.id.model_thumbnail_image_view);

        buildVendorSpinner(null, null);
        buildModelSpinner(null, null);

        loadVendors();

        if (savedInstanceState != null) {
            vendorSavedSelectedPosition = savedInstanceState.getInt(VENDOR_SPINNER_KEY);
            modelSavedSelectedPosition = savedInstanceState.getInt(MODEL_SPINNER_KEY);
        }

        modelExplanationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomedDialog.showMessageDialogWithTitle(getActivity(), R.string
                        .msg_model_explanation_title, R.string
                        .msg_model_explanation);
            }
        });

        vendorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int
                    position, long id) {
                if (position == 0) {
                    //User selected Unknown/Other
                    vendorLogoImageView.setImageResource(android.R.color.transparent);
                    buildModelSpinner(new ArrayList<Model>(), null);
                } else {
                    String vendorName = vendorSpinner.getSelectedItem().toString();
                    String vendorId = vendorMap.get(vendorName).toLowerCase(Locale.UK);

                    if (!vendorName.equals(getString(R.string.vendor_other))) {
                        //Update vendor logo when vendor is selected
                        Picasso.with(getActivity()).load(Vendor.getLogoUrl(vendorId)
                        ).placeholder(android.R.color.transparent).into(vendorLogoImageView);

                        new RequestModelListTask(vendorId).executeOnExecutor(AsyncTask
                                .THREAD_POOL_EXECUTOR);
                    } else {
                        //User selected Other
                        vendorLogoImageView.setImageResource(android.R.color.transparent);
                        buildModelSpinner(new ArrayList<Model>(), null);
//                        modelSpinner.setEnabled(false);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                String vendorId = getVendorIdFromSpinner();
                String modelName = getModelNameFromSpinner();
                String modelId = getModelIdFromSpinner();

                if (position == 0) {
                    if (isAddEditActivity()) {
                        getAddEditActivity().clearDefaults();
                    } else if (isAddActivity()) {
                        getAddActivity().onDefaultsLoaded(null);
                    }
                } else {
                    for (Model model : modelListGlobal) {

                            if (model.getId().equals(modelId)){
                                int objectIndex =  modelListGlobal.indexOf(model);
                                if (isAddEditActivity()) {
                                    getAddEditActivity().fillDefaults(modelListGlobal.get(objectIndex));
                                } else if (isAddActivity()) {
                                    getAddActivity().onDefaultsLoaded(modelListGlobal.get(objectIndex));
                                }

                            }
                    }
//                    new RequestDefaultsTask(vendorId, modelName).executeOnExecutor(AsyncTask
//                            .THREAD_POOL_EXECUTOR);
                }

                //For all situations, the logo & thumbnail should update when selected
                if (position == 0) {
                    modelThumbnailImageView.setImageResource(R.drawable.thumbnail_placeholder);
                } else {
                    //Update model logo when model is selected
                    Picasso.with(getActivity())
                            .load(Model.getThumbnailUrl(vendorId, modelId))
                            .placeholder(R.drawable.thumbnail_placeholder)
                            .into(modelThumbnailImageView);
                }

                if (isAddEditActivity()) {
                    getAddEditActivity().showUrlEndings(position == 0);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        /* Save selected vendor & model before screen rotating */
        outState.putInt(VENDOR_SPINNER_KEY, vendorSpinner.getSelectedItemPosition());
        outState.putInt(MODEL_SPINNER_KEY, modelSpinner.getSelectedItemPosition());
    }

    public void buildVendorSpinner(ArrayList<Vendor> vendorList, String selectedVendor) {
        if (vendorMap == null) {
            vendorMap = new TreeMap<>();
        }

        if (vendorMapIdAsKey == null) {
            vendorMapIdAsKey = new TreeMap<>();
        }

        if (vendorList != null) {
            for (Vendor vendor : vendorList) {
                try {
                    vendorMap.put(vendor.getName(), vendor.getId());
                    vendorMapIdAsKey.put(vendor.getId(), vendor.getName());
                } catch (EvercamException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }

        Set<String> set = vendorMap.keySet();
        String[] vendorArray = Commons.joinStringArray(new String[]{getResources().getString(R
                .string.select_vendor)}, set.toArray(new String[0]));
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, vendorArray);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner);

        int selectedPosition = 0;
        if (isAddActivity()) {
            if (getAddActivity().isFromDiscoverAndHasVendor()) {
                String vendorId = getAddActivity().getDiscoveredCamera().getVendor();
                String vendorName = vendorMapIdAsKey.get(vendorId);
                selectedPosition = spinnerArrayAdapter.getPosition(vendorName);
            }
        }
        if (selectedVendor != null) {
            selectedPosition = spinnerArrayAdapter.getPosition(selectedVendor);
        }
        vendorSpinner.setAdapter(spinnerArrayAdapter);

        if (selectedPosition != 0) {
            vendorSpinner.setSelection(selectedPosition);
        }
        /* If vendor state are saved but haven't been selected */
        else if (vendorSavedSelectedPosition != 0
                && vendorSpinner.getCount() > 1
                && vendorSavedSelectedPosition < vendorSpinner.getCount()) {
            vendorSpinner.setSelection(vendorSavedSelectedPosition);
            vendorSavedSelectedPosition = 0; //Then reset it
        }
    }

    public void buildModelSpinner(ArrayList<Model> modelList, String selectedModel) {
        if (modelMap == null) {
            modelMap = new TreeMap<>();
        }
        modelMap.clear();

        if (modelList == null) {
            modelSpinner.setEnabled(false);
        } else {
            if (modelList.size() == 0) {
                modelSpinner.setEnabled(false);
            } else {
                modelSpinner.setEnabled(true);

                for (Model model : modelList) {
                    try {
                        modelMap.put(model.getId(), model.getName());
                    } catch (EvercamException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        }

        Collection<String> modelNameCollection = modelMap.values();

        String[] fullModelArray = Commons.joinStringArray(new String[]{getResources().getString(R
                .string.select_model)}, modelNameCollection.toArray(new String[0]));

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, fullModelArray);

        int defaultObjectIndex = spinnerArrayAdapter.getPosition(getString(R.string
                .model_default));

        if (defaultObjectIndex >= 0){

            String defaultObject = fullModelArray[defaultObjectIndex];

            for (int i = (defaultObjectIndex - 1); i >= 0; i--) {

                fullModelArray[i+1] = fullModelArray[i];
            }

            fullModelArray[1] = defaultObject;

        }
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner);
        modelSpinner.setAdapter(spinnerArrayAdapter);

        int selectedPosition = 0;
        if (selectedModel != null) {
            if (modelMap.get(selectedModel) != null) {
                String selectedModelName = modelMap.get(selectedModel);
                selectedPosition = spinnerArrayAdapter.getPosition(selectedModelName);
            }
        }
        if (selectedPosition != 0) {
            modelSpinner.setSelection(selectedPosition);
        }
        /* If vendor state are saved but haven't been selected */
        else if (modelSavedSelectedPosition != 0 && modelSpinner.getCount() > 1
                && modelSavedSelectedPosition < modelSpinner.getCount()) {
            modelSpinner.setSelection(modelSavedSelectedPosition);
            modelSavedSelectedPosition = 0; // Then reset it
        } else {
            modelSpinner.setSelection(spinnerArrayAdapter.getPosition(getString(R.string
                    .model_default)));
        }
    }

    public String getVendorIdFromSpinner() {
        if (vendorSpinner != null && vendorSpinner.getSelectedItem() != null) {
            String vendorName = vendorSpinner.getSelectedItem().toString();
            if (vendorName.equals(getString(R.string.select_vendor))) {
                return "";
            } else {
                return vendorMap.get(vendorName).toLowerCase(Locale.UK);
            }
        }
        return "";
    }

    public String getVendorNameFromSpinner() {
        if (vendorSpinner != null) {
            String vendorName = vendorSpinner.getSelectedItem().toString();
            if (vendorName.equals(getString(R.string.select_vendor))) {
                return "";
            } else {
                return vendorName;
            }
        }
        return "";
    }

    public String getModelIdFromSpinner() {
        if (modelSpinner.getSelectedItem() != null) {
            String modelName = modelSpinner.getSelectedItem().toString();
            if (modelName.equals(getString(R.string.select_model))) {
                return "";
            } else {
                for (Map.Entry<String, String> entry : modelMap.entrySet()) {
                    if (entry.getValue().equals(modelName)) {
                        return entry.getKey();
                    }
                }
            }
        }

        return "";
    }

    public String getModelNameFromSpinner() {
        if (modelSpinner != null && modelSpinner.getSelectedItem() != null) {
            String modelName = modelSpinner.getSelectedItem().toString();
            if (modelName.equals(getString(R.string.select_model))) {
                return "";
            } else {
                return modelName;
            }
        }

        return "";
    }

    public void loadVendors() {
        new RequestVendorListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void hideModelQuestionMark() {
        modelExplanationImageButton.setVisibility(View.INVISIBLE);
    }

    private boolean isAddEditActivity() {
        return getActivity() instanceof EditCameraActivity;
    }

    private EditCameraActivity getAddEditActivity() {
        return ((EditCameraActivity) getActivity());
    }

    private boolean isAddActivity() {
        return getActivity() instanceof AddCameraActivity;
    }

    private AddCameraActivity getAddActivity() {
        return ((AddCameraActivity) getActivity());
    }

    class RequestModelListTask extends AsyncTask<Void, Void, ArrayList<Model>> {
        private String vendorId;

        public RequestModelListTask(String vendorId) {
            this.vendorId = vendorId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            buildModelList(null);
        }

        @Override
        protected ArrayList<Model> doInBackground(Void... params) {
            try {
                return Model.getAllByVendorId(vendorId);
            } catch (EvercamException e) {
                EvercamPlayApplication.sendCaughtException(getActivity(),
                        e.toString() + " " + "with vendor id: " + vendorId);
                Log.e(TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Model> modelList) {
            if (modelList != null) {
                modelListGlobal = modelList;
                buildModelList(modelList);
            }
        }
    }

    class RequestVendorListTask extends AsyncTask<Void, Void, ArrayList<Vendor>> {

        @Override
        protected void onPostExecute(ArrayList<Vendor> vendorList) {
            if (vendorList != null) {
                if (isAddEditActivity()) {
                    getAddEditActivity().buildSpinnerOnVendorListResult(vendorList);
                } else if (isAddActivity()) {
                    getAddActivity().buildSpinnerOnVendorListResult(vendorList);
                }
            } else {
                Log.e(TAG, "Vendor list is null");
            }
        }

        @Override
        protected ArrayList<Vendor> doInBackground(Void... params) {
            try {
                return Vendor.getAll();
            } catch (EvercamException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }
    }

    class RequestDefaultsTask extends AsyncTask<Void, Void, Model> {
        private String vendorId;
        private String modelName;

        public RequestDefaultsTask(String vendorId, String modelName) {
            this.vendorId = vendorId;
            this.modelName = modelName;
        }

        @Override
        protected void onPreExecute() {
            if (isAddEditActivity()) {
                getAddEditActivity().clearDefaults();
            }
        }

        @Override
        protected Model doInBackground(Void... params) {
            try {
                ArrayList<Model> modelList = Model.getAll(modelName, vendorId);
                if (modelList.size() > 0) {
                    return modelList.get(0);
                }
            } catch (EvercamException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Model model) {
            if (model != null) {
                if (isAddEditActivity()) {
                    getAddEditActivity().fillDefaults(model);
                } else if (isAddActivity()) {
                    getAddActivity().onDefaultsLoaded(model);
                }
            }
        }
    }

    private void buildModelList(ArrayList<Model> modelList) {
        if (isAddEditActivity()) {
            getAddEditActivity().buildSpinnerOnModelListResult(modelList);
        } else if (isAddActivity()) {
            getAddActivity().buildSpinnerOnModelListResult(modelList);
        }
    }
}
