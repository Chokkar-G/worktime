/*
 * Copyright 2012 Dirk Vranckaert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.vranckaert.worktime.activities.reporting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.inject.Inject;
import eu.vranckaert.worktime.R;
import eu.vranckaert.worktime.constants.Constants;
import eu.vranckaert.worktime.enums.export.ExportCsvSeparator;
import eu.vranckaert.worktime.enums.export.ExportData;
import eu.vranckaert.worktime.enums.export.ExportType;
import eu.vranckaert.worktime.exceptions.export.GeneralExportException;
import eu.vranckaert.worktime.model.TimeRegistration;
import eu.vranckaert.worktime.model.dto.export.ExportDTO;
import eu.vranckaert.worktime.model.dto.reporting.ReportingTableRecord;
import eu.vranckaert.worktime.service.ExportService;
import eu.vranckaert.worktime.utils.context.ContextUtils;
import eu.vranckaert.worktime.utils.context.IntentUtil;
import eu.vranckaert.worktime.utils.date.DateFormat;
import eu.vranckaert.worktime.utils.date.DateUtils;
import eu.vranckaert.worktime.utils.date.TimeFormat;
import eu.vranckaert.worktime.utils.preferences.Preferences;
import eu.vranckaert.worktime.utils.string.StringUtils;
import roboguice.activity.GuiceActivity;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: DIRK VRANCKAERT
 * Date: 15/02/11
 * Time: 00:15
 */
public class ReportingExportActivity extends GuiceActivity {
    private static final String LOG_TAG = ReportingExportActivity.class.getSimpleName();

    @InjectView(R.id.reporting_export_type)
    private Spinner reportingTypeSpinner;
    @InjectView(R.id.reporting_export_filename)
    private EditText fileNameInput;
    @InjectView(R.id.reporting_export_filename_required)
    private TextView fileNameInputRequired;
    @InjectView(R.id.reporting_export_csv_separator_container)
    private View reportingCsvSeparatorContainer;
    @InjectView(R.id.reporting_export_csv_separator)
    private Spinner reportingCsvSeparatorSpinner;
    @InjectView(R.id.reporting_export_data_container)
    private View reportingDataContainer;
    @InjectView(R.id.reporting_export_data)
    private Spinner reportingDataSpinner;

    @Inject
    private SharedPreferences preferences;

    @Inject
    private ExportService exportService;

    @InjectExtra(value = Constants.Extras.EXPORT_DTO)
    private ExportDTO exportDto;

    private File exportedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reporting_export);

        initForm(ReportingExportActivity.this);
    }

    /**
     * Updates the entire form when launching this activity.
     *
     * @param ctx The context of the activity.
     */
    private void initForm(Context ctx) {
        // Type
        ArrayAdapter reportingTypeAdapter = ArrayAdapter.createFromResource(
                this, R.array.array_reporting_export_type_options, android.R.layout.simple_spinner_item);
        reportingTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportingTypeSpinner.setAdapter(reportingTypeAdapter);
        reportingTypeSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ExportType exportType = ExportType.getByIndex(position);
                updateViewsForExportType(exportType);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // NA
            }
        });

        // Filename
        fileNameInput.setText(Preferences.getReportingExportFileName(ctx));

        // CSV Separator
        ArrayAdapter reportingCsvSeparatorAdapter = ArrayAdapter.createFromResource(
                this, R.array.array_reporting_export_csv_separator_options, android.R.layout.simple_spinner_item);
        reportingCsvSeparatorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportingCsvSeparatorSpinner.setAdapter(reportingCsvSeparatorAdapter);

        // Data
        ArrayAdapter reportingDataAdapter = ArrayAdapter.createFromResource(
                this, R.array.array_reporting_export_data_options, android.R.layout.simple_spinner_item);
        reportingDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportingDataSpinner.setAdapter(reportingDataAdapter);

        // Set initial data
        updateExportType(ctx);
        updateExportCsvSeparator(ctx);
        updateExportData(ctx);
        updateViewsForExportType(ExportType.getByIndex(reportingTypeSpinner.getSelectedItemPosition()));
    }

    /**
     * Updates the entire view for the selected export type.
     * @param exportType The selected {@link ExportType}.
     */
    private void updateViewsForExportType(ExportType exportType) {
        switch (exportType) {
            case CSV: {
                reportingCsvSeparatorContainer.setVisibility(View.VISIBLE);
                reportingDataContainer.setVisibility(View.VISIBLE);
                break;
            }
            case XLS: {
                reportingCsvSeparatorContainer.setVisibility(View.GONE);
                reportingDataContainer.setVisibility(View.GONE);
                break;
            }
        }
    }

    /**
     * Updates the view elements specified for showing the export type with the default settings.
     * @param ctx The context.
     */
    private void updateExportType(Context ctx) {
        ExportType exportType = Preferences.getPreferredExportType(ctx);
        reportingTypeSpinner.setSelection(exportType.getPosition());
    }

    /**
     * Updates the view elements specified for showing the export csv separator with the default settings.
     * @param ctx The context.
     */
    private void updateExportCsvSeparator(Context ctx) {
        ExportCsvSeparator exportCsvSeparator = Preferences.getPreferredExportCSVSeparator(ctx);
        reportingCsvSeparatorSpinner.setSelection(exportCsvSeparator.getPosition());
    }

    /**
     * Updates the view elements specified for showing the export data with the default settings.
     * @param ctx The context.
     */
    private void updateExportData(Context ctx) {
        ExportData exportData = Preferences.getPreferredExportData(ctx);
        reportingDataSpinner.setSelection(exportData.getPosition());
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Log.d(LOG_TAG, "Received request to create loading dialog with id " + id);
        Dialog dialog = null;
        switch (id) {
            case Constants.Dialog.REPORTING_EXPORT_UNAVAILABLE: {
                AlertDialog.Builder alertExportUnavailable = new AlertDialog.Builder(this);
                alertExportUnavailable.setTitle(R.string.msg_reporting_export_sd_unavailable)
                        .setMessage(R.string.msg_reporting_export_sd_unavailable_detail)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                dialog = alertExportUnavailable.create();
                break;
            }
            case Constants.Dialog.REPORTING_EXPORT_LOADING: {
                dialog = ProgressDialog.show(
                        ReportingExportActivity.this,
                        "",
                        getString(R.string.msg_reporting_export_saving_sd),
                        true,
                        false
                );
                break;
            }
            case Constants.Dialog.REPORTING_EXPORT_DONE: {
                AlertDialog.Builder alertExportDone = new AlertDialog.Builder(this);
                alertExportDone
                        .setMessage(getString(R.string.msg_reporting_export_done, exportedFile.getAbsolutePath()))
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(Constants.Dialog.REPORTING_EXPORT_DONE);
                            }
                        })
                        .setNegativeButton(R.string.msg_reporting_export_share_file, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(Constants.Dialog.REPORTING_EXPORT_DONE);
                                sendExportedFileByMail();
                            }
                        });
                dialog = alertExportDone.create();
                break;
            }
            case Constants.Dialog.REPORTING_EXPORT_ERROR: {
                AlertDialog.Builder alertExportError = new AlertDialog.Builder(this);
                alertExportError
                        .setTitle(R.string.dialog_title_error)
                        .setMessage(R.string.msg_reporting_export_error)
                        .setCancelable(true)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                removeDialog(Constants.Dialog.REPORTING_EXPORT_ERROR);
                            }
                        });
                dialog = alertExportError.create();
                break;
            }
            default:
                Log.d(LOG_TAG, "Dialog id " + id + " is not supported in this activity!");
        }
        return dialog;
    }

    /**
     * Go Home.
     *
     * @param view The view.
     */
    public void onHomeClick(View view) {
        IntentUtil.goHome(this);
    }

    /**
     * Save the file name to the preferences in case it has changed.
     * Afterwards Disk the time registrations.
     *
     * @param view The view.
     */
    public void onExportClick(View view) {
        Log.d(LOG_TAG, "Export button clicked!");
        Log.d(LOG_TAG, "Validate input...");
        if (!validate()) {
            return;
        }

        Log.d(LOG_TAG, "Update the preferences...");
        updatePreferences();

        Log.d(LOG_TAG, "Hide the soft keyboard if visible");
        ContextUtils.hideKeyboard(ReportingExportActivity.this, fileNameInput);

        if (ContextUtils.isSdCardAvailable() && ContextUtils.isSdCardWritable()) {
            startExport();
        } else {
            showDialog(Constants.Dialog.REPORTING_EXPORT_UNAVAILABLE);
        }
    }

    private boolean validate() {
        boolean valid = true;
        if (fileNameInput.getText().toString().length() < 3) {
            Log.d(LOG_TAG, "Validation failed! Showing applicable error messages...");
            fileNameInputRequired.setVisibility(View.VISIBLE);
            valid = false;
        }

        if (valid) {
            Log.d(LOG_TAG, "Validation successful. Hiding all error messages...");
            fileNameInputRequired.setVisibility(View.GONE);
        }
        return valid;
    }

    private void updatePreferences() {
        ExportType exportType = ExportType.getByIndex(reportingTypeSpinner.getSelectedItemPosition());
        String filename = fileNameInput.getText().toString();
        Log.d(LOG_TAG, "Save the (changed) filename and export type in the preferences");
        Preferences.setReportingExportFileName(ReportingExportActivity.this, fileNameInput.getText().toString());
        Preferences.setPreferredExportType(ReportingExportActivity.this, exportType);

        if (reportingCsvSeparatorSpinner.getVisibility() == View.VISIBLE) {
            Log.d(LOG_TAG, "Save the (changed) CSV separator in the preferences");
            ExportCsvSeparator separatorExport = ExportCsvSeparator.getByIndex(reportingCsvSeparatorSpinner.getSelectedItemPosition());
            Preferences.setPreferredExportCSVSeparator(ReportingExportActivity.this, separatorExport);
        }

        if (reportingCsvSeparatorSpinner.getVisibility() == View.VISIBLE) {
            Log.d(LOG_TAG, "Save the (changed) export data in the preferences");
            ExportData exportData = ExportData.getByIndex(reportingDataSpinner.getSelectedItemPosition());
            Preferences.setPreferredExportData(ReportingExportActivity.this, exportData);
        }
    }

    private void startExport() {
        AsyncTask task = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                Log.d(LOG_TAG, "About to show loading dialog for export");
                showDialog(Constants.Dialog.REPORTING_EXPORT_LOADING);
                Log.d(LOG_TAG, "Loading dialog for export showing!");
            }

            @Override
            protected Object doInBackground(Object... objects) {
                Log.d(LOG_TAG, "Starting export background process...");
                ExportType exportType = ExportType.getByIndex(reportingTypeSpinner.getSelectedItemPosition());
                String filename = fileNameInput.getText().toString();

                File file = null;
                try {
                    switch (exportType) {
                        case CSV: {
                            ExportCsvSeparator separatorExport = ExportCsvSeparator.getByIndex(reportingCsvSeparatorSpinner.getSelectedItemPosition());
                            ExportData exportData = ExportData.getByIndex(reportingDataSpinner.getSelectedItemPosition());
                            file = doCSVExport(filename, separatorExport, exportData);
                            break;
                        }
                        case XLS: {
                            file = doExcelExport(filename);
                            break;
                        }
                    }
                } catch (GeneralExportException e) {
                    Log.e(LOG_TAG, "A general exception occured during export!", e);
                }
                Log.d(LOG_TAG, "Export in background process finished!");
                return file;
            }

            @Override
            protected void onPostExecute(Object o) {
                Log.d(LOG_TAG, "About to remove loading dialog for export");
                removeDialog(Constants.Dialog.REPORTING_EXPORT_LOADING);
                Log.d(LOG_TAG, "Loading dialog for export removed!");

                if (o == null) {
                    showDialog(Constants.Dialog.REPORTING_EXPORT_ERROR);
                    return;
                }

                exportedFile = (File) o;
                showDialog(Constants.Dialog.REPORTING_EXPORT_DONE);
            }
        }.execute();
    }

    private File doCSVExport(String filename, ExportCsvSeparator separatorExport, ExportData exportData) throws GeneralExportException {
        List<String> headers = new ArrayList<String>();
        List<String[]> values = new ArrayList<String[]>();

        switch (exportData) {
            case RAW_DATA: {
                //Construct headers
                headers.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_startdate));
                headers.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_starttime));
                headers.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_enddate));
                headers.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_endtime));
                headers.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_comment));
                headers.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_project));
                headers.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_task));
                headers.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_projectcomment));
                //Construct body
                for (TimeRegistration timeRegistration : exportDto.getTimeRegistrations()) {
                    String startDate = DateUtils.DateTimeConverter.convertDateToString(timeRegistration.getStartTime(), DateFormat.SHORT, ReportingExportActivity.this);
                    String startTime = DateUtils.DateTimeConverter.convertTimeToString(timeRegistration.getStartTime(), TimeFormat.MEDIUM, ReportingExportActivity.this);
                    String endDate = "";
                    String endTime = "";
                    String trComment = "";
                    String projectName = timeRegistration.getTask().getProject().getName();
                    String taskName = timeRegistration.getTask().getName();
                    String projectComment = "";

                    if(timeRegistration.getEndTime() != null) {
                        endDate = DateUtils.DateTimeConverter.convertDateToString(timeRegistration.getEndTime(), DateFormat.SHORT, ReportingExportActivity.this);
                        endTime = DateUtils.DateTimeConverter.convertTimeToString(timeRegistration.getEndTime(), TimeFormat.MEDIUM, ReportingExportActivity.this);
                    } else {
                        endDate = getString(R.string.now);
                        endTime = "";
                    }
                    if (StringUtils.isNotBlank(timeRegistration.getComment())) {
                        trComment = timeRegistration.getComment();
                    }
                    if (StringUtils.isNotBlank(timeRegistration.getTask().getProject().getComment())) {
                        projectComment = timeRegistration.getTask().getProject().getComment();
                    }

                    String[] exportLine = {
                            startDate, startTime, endDate, endTime, trComment,
                            projectName, taskName, projectComment
                    };
                    values.add(exportLine);
                }
                break;
            }
            case REPORT: {
                //Construct headers
                headers = null;
                //Construct body
                for (ReportingTableRecord tableRecord : exportDto.getTableRecords()) {
                    String[] exportLine = {
                            tableRecord.getColumn1(),
                            tableRecord.getColumn2(),
                            tableRecord.getColumn3(),
                            tableRecord.getColumnTotal()
                    };
                    values.add(exportLine);
                }
                break;
            }
        }

        return exportService.exportCsvFile(ReportingExportActivity.this, filename, headers, values, separatorExport);
    }

    private File doExcelExport(String filename) throws GeneralExportException {
        List<String> reportHeaders = new ArrayList<String>();
        List<Object[]> reportValues = new ArrayList<Object[]>();

        List<String> rawHeaders = new ArrayList<String>();
        List<Object[]> rawValues = new ArrayList<Object[]>();

        //Construct report body
        for (ReportingTableRecord tableRecord : exportDto.getTableRecords()) {
            String[] exportLine = {
                    tableRecord.getColumn1(),
                    tableRecord.getColumn2(),
                    tableRecord.getColumn3(),
                    tableRecord.getColumnTotal()
            };
            reportValues.add(exportLine);
        }
        //Construct report headers
        if (exportDto.getTableRecords().size() > 0) {
            reportHeaders.add(exportDto.getTableRecords().get(0).getColumn1());
            reportHeaders.add(exportDto.getTableRecords().get(0).getColumn2());
            reportHeaders.add(exportDto.getTableRecords().get(0).getColumn3());
            reportHeaders.add(exportDto.getTableRecords().get(0).getColumnTotal());

            reportValues.remove(0);
        }
        //Construct raw headers
        rawHeaders.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_startdate));
        rawHeaders.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_starttime));
        rawHeaders.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_enddate));
        rawHeaders.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_endtime));
        rawHeaders.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_comment));
        rawHeaders.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_project));
        rawHeaders.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_task));
        rawHeaders.add(getString(R.string.lbl_reporting_results_export_raw_data_csv_projectcomment));
        //Construct raw body
        for (TimeRegistration timeRegistration : exportDto.getTimeRegistrations()) {
            String startDate = DateUtils.DateTimeConverter.convertDateToString(timeRegistration.getStartTime(), DateFormat.SHORT, ReportingExportActivity.this);
            String startTime = DateUtils.DateTimeConverter.convertTimeToString(timeRegistration.getStartTime(), TimeFormat.MEDIUM, ReportingExportActivity.this);
            String endDate = "";
            String endTime = "";
            String trComment = "";
            String projectName = timeRegistration.getTask().getProject().getName();
            String taskName = timeRegistration.getTask().getName();
            String projectComment = "";

            if(timeRegistration.getEndTime() != null) {
                endDate = DateUtils.DateTimeConverter.convertDateToString(timeRegistration.getEndTime(), DateFormat.SHORT, ReportingExportActivity.this);
                endTime = DateUtils.DateTimeConverter.convertTimeToString(timeRegistration.getEndTime(), TimeFormat.MEDIUM, ReportingExportActivity.this);
            } else {
                endDate = getString(R.string.now);
                endTime = "";
            }
            if (StringUtils.isNotBlank(timeRegistration.getComment())) {
                trComment = timeRegistration.getComment();
            }
            if (StringUtils.isNotBlank(timeRegistration.getTask().getProject().getComment())) {
                projectComment = timeRegistration.getTask().getProject().getComment();
            }

            String[] exportLine = {
                    startDate, startTime, endDate, endTime, trComment,
                    projectName, taskName, projectComment
            };
            rawValues.add(exportLine);
        }

        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Report", reportHeaders);
        headers.put("Data", rawHeaders);

        Map<String, List<Object[]>> values = new HashMap<String, List<Object[]>>();
        values.put("Report", reportValues);
        values.put("Data", rawValues);

        try {
            return exportService.exportXlsFile(ReportingExportActivity.this, filename, headers, values);
        } catch (GeneralExportException e) {
            throw e;
        }
    }

    private void sendExportedFileByMail() {
        IntentUtil.sendSomething(
                ReportingExportActivity.this,
                R.string.lbl_reporting_export_share_subject,
                R.string.lbl_reporting_export_share_body,
                exportedFile,
                R.string.lbl_reporting_export_share_file_app_chooser_title
        );
    }
}
