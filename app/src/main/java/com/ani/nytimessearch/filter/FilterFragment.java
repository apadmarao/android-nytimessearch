package com.ani.nytimessearch.filter;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.ani.nytimessearch.nytclient.Filter;
import com.ani.nytimessearch.R;

import java.text.DateFormat;
import java.util.Calendar;

public class FilterFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private static final String FILTER_EXTRA = "filter";

    private Spinner spSort;
    private EditText etSelectDate;

    private CheckBox cbArts;
    private CheckBox cbStyle;
    private CheckBox cbSports;

    private Button btnReset;
    private Button btnCancel;
    private Button btnSave;

    private Filter lastFilter;
    private Filter filter;

    public FilterFragment() {
        super();
    }

    public static FilterFragment newInstance(Filter filter) {
        FilterFragment filterFragment = new FilterFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(FILTER_EXTRA, filter);
        filterFragment.setArguments(bundle);
        return filterFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        filter = (Filter) getArguments().getSerializable(FILTER_EXTRA);
        lastFilter = filter.copy(); // deep copy object, because it may be mutated by user actions

        spSort = (Spinner) view.findViewById(R.id.spSort);
        spSort.setOnItemSelectedListener(new SpSortItemSelectedListener());

        etSelectDate = (EditText) view.findViewById(R.id.etSelectDate);
        etSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        cbArts = (CheckBox) view.findViewById(R.id.cbArts);
        cbStyle = (CheckBox) view.findViewById(R.id.cbStyle);
        cbSports = (CheckBox) view.findViewById(R.id.cbSports);
        CbNewsDeskOnClickListener cbNewsDeskOnClickListener = new CbNewsDeskOnClickListener();
        cbArts.setOnClickListener(cbNewsDeskOnClickListener);
        cbStyle.setOnClickListener(cbNewsDeskOnClickListener);
        cbSports.setOnClickListener(cbNewsDeskOnClickListener);

        btnReset = (Button) view.findViewById(R.id.btnReset);
        btnCancel = (Button) view.findViewById(R.id.btnCancel);
        btnSave = (Button) view.findViewById(R.id.btnSave);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = new Filter();
                // reinitialize view with updated filter
                initializeViews();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter = lastFilter;

                Listener listener = (Listener) getActivity();
                listener.onFinishFilterDialog(filter);
                dismiss();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Listener listener = (Listener) getActivity();
                listener.onFinishFilterDialog(filter);
                dismiss();
            }
        });

        initializeViews();
    }

    private void initializeViews() {
        // Initialize sort spinner
        switch (filter.getSort()) {
            case OLDEST:
                spSort.setSelection(0);
                break;
            case NEWEST:
                spSort.setSelection(1);
                break;
            case RELEVANCE:
            default:
                spSort.setSelection(2);
                break;
        }

        // Initialize date edit text
        if (filter.getBeginDate() == null) {
            etSelectDate.setText("");
        } else {
            etSelectDate.setText(formatDate(filter.getBeginDate()));
        }

        // Initialize news desk checkboxes
        cbArts.setChecked(filter.getNewsDesks().contains("Arts"));
        cbStyle.setChecked(filter.getNewsDesks().contains("Fashion & Style"));
        cbSports.setChecked(filter.getNewsDesks().contains("Sports"));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.MONTH, month);

        filter.setBeginDate(cal);
        etSelectDate.setText(formatDate(cal));
    }

    private String formatDate(Calendar cal) {
        DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
        format.setTimeZone(cal.getTimeZone());
        return format.format(cal.getTime());
    }

    private void showDatePickerDialog() {
        FragmentManager fm = getFragmentManager();
        DatePickerFragment datePicker = DatePickerFragment.instance();

        // SETS the target fragment for use later when sending results
        datePicker.setTargetFragment(this, 300);
        datePicker.show(fm, "fragment_date_picker");
    }

    private class SpSortItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedItem = spSort.getSelectedItem().toString();
            switch (selectedItem) {
                case "Oldest":
                    filter.setSort(Filter.Sort.OLDEST);
                    break;
                case "Newest":
                    filter.setSort(Filter.Sort.NEWEST);
                    break;
                case "Relevance":
                    filter.setSort(Filter.Sort.RELEVANCE);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // do nothing
        }
    }

    private class CbNewsDeskOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            boolean checked = ((CheckBox) view).isChecked();

            // Check which checkbox was clicked
            switch(view.getId()) {
                case R.id.cbArts:
                    if (checked) {
                        filter.getNewsDesks().add("Arts");
                    } else {
                        filter.getNewsDesks().remove("Arts");
                    }
                    break;
                case R.id.cbStyle:
                    if (checked) {
                        filter.getNewsDesks().add("Fashion & Style");
                    } else {
                        filter.getNewsDesks().remove("Fashion & Style");
                    }
                    break;
                case R.id.cbSports:
                    if (checked) {
                        filter.getNewsDesks().add("Sports");
                    } else {
                        filter.getNewsDesks().remove("Sports");
                    }
                    break;
            }
        }
    }

    public interface Listener {
        void onFinishFilterDialog(Filter filter);
    }
}
