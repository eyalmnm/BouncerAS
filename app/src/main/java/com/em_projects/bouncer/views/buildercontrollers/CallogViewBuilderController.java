package com.em_projects.bouncer.views.buildercontrollers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.em_projects.bouncer.BouncerActivity;
import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerChatActivity;
import com.em_projects.bouncer.R;
import com.em_projects.bouncer.model.CallLogElement;
import com.em_projects.bouncer.repositories.CallLogsRepository;
import com.em_projects.bouncer.utils.Utils;
import com.em_projects.bouncer.views.model.CallogListViewModel;
import com.em_projects.bouncer.views.model.CallogViewModel;
import com.em_projects.infra.views.buildercontrollers.TabViewBuilderController;
import com.em_projects.infra.views.controllers.ViewController;
import com.em_projects.utils.StringUtil;

import java.util.Collections;
import java.util.Date;
import java.util.Vector;

public class CallogViewBuilderController extends TabViewBuilderController<CallogListViewModel> {
    private static final String TAG = "CallogViewBuilderCntrlr";

    public static CallogListViewAdapter m_adapter = new CallogListViewAdapter();

    public CallogViewBuilderController(CallogListViewModel model) {
        super(model);
        Log.d(TAG, "CallogViewBuilderController");

        m_adapter.setNewModel(model);
    }

    @Override
    public void attachController(final View view) {
        Log.d(TAG, "attachController");
        //get the search textfield and attach its listener
        EditText searchTxt = (EditText) view.findViewById(R.id.search_txt);
        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged");
                //get the filtered call-logs
                Vector<CallLogElement> callLogs = CallLogsRepository.getInstance().getFilteredCallLogs(s.toString());

                //sort the call-logs
                Collections.sort(callLogs, CallLogElement.COMPARE_BY_CALL_TIME);

                //refresh the screen with filtered call-logs
                refreshView(view, new CallogListViewModel(callLogs));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //get callogs list view and set its adapter
        ListView lv = (ListView) view.findViewById(R.id.list_view);
        lv.setAdapter(m_adapter);
    }

    @Override
    public View getMainLayout(LayoutInflater inflater) {
        Log.d(TAG, "getMainLayout");
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.tab_content_container, null);
        return layout;
    }

    @Override
    public void refreshView(View mainlayout, Object newModel) {
        Log.d(TAG, "refreshView");
        //set new model to the adapter
        m_adapter.setNewModel((CallogListViewModel) newModel);

        //notify that data has changed - triggers repaint
        m_adapter.notifyDataSetChanged();
    }

    @Override
    public ViewController getViewController() {
        Log.d(TAG, "getViewController");
        return this;
    }

    @Override
    public boolean onBackKeyPressed() {
        Log.d(TAG, "onBackKeyPressed");
        Utils.showExitMessage(BouncerApplication.getApplication().getActivityInForeground());

        return true;
    }

    public static class CallogListViewAdapter extends BaseAdapter {
        public CallogListViewModel m_model;
        private OnClickListener callogViewItemListener = new OnClickListener() {
            private Context context = BouncerApplication.getApplication().getActivityInForeground().getBaseContext();

            @Override
            public void onClick(View v) {
                //get callog's view model
                final CallogViewModel cvm = (CallogViewModel) v.getTag();

                //handle click according to view's id
                int viewId = v.getId();
                switch (viewId) {
                    case R.id.make_call_btn: {
                        //create the intent for establishing the phone call
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", cvm.CallerNumber, null));

                        //start the phone call activity
                        BouncerApplication.getApplication().getActivityInForeground().startActivityForResult(intent, BouncerActivity.CALL_FROM_APP_REQUEST_CODE);

                        break;
                    }

                    case R.id.send_sms_btn: {
                        // start chat screen activity
                        Intent intent = new Intent(context, BouncerChatActivity.class);
                        intent.putExtra("address", cvm.CallerNumber);

                        //start chat activity
                        BouncerApplication.getApplication().getActivityInForeground().startNewActivity(intent, false);

                        break;
                    }

                    default:
                        break;
                }
            }
        };

        public void setNewModel(CallogListViewModel model) {
            m_model = model;
        }

        @Override
        public int getCount() {
            return m_model.getCallLogsViewModels().size();
        }

        @Override
        public Object getItem(int position) {
            return m_model.getCallLogsViewModels().elementAt(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //get layout inflater
            LayoutInflater inflater = BouncerApplication.getApplication().getActivityInForeground().getLayoutInflater();

            // init the object that holds list-view item's data
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.callog_list_item, null);
            }

            //hold current callog's view model
            CallogViewModel cvm = m_model.getCallLogsViewModels().elementAt(position);

            //get the callog
            CallLogElement call = CallLogsRepository.getInstance().getCallLogById(cvm.UID);

            if (call == null) {
                Log.d(getClass().getSimpleName(), "CallogListViewAdapter.getView() - failed to get call with id: " + cvm.UID);

                return convertView;
            }

            //holds call indicator resource id
            int callIndicatorResourceId = 0;

            //set call indicator background resource
            switch (call.CallType) {
                case CallLogElement.CALL_TYPE.INCOMING: {
                    callIndicatorResourceId = android.R.drawable.sym_call_incoming;

                    break;
                }
                case CallLogElement.CALL_TYPE.OUTGOING: {
                    callIndicatorResourceId = android.R.drawable.sym_call_outgoing;

                    break;
                }
                case CallLogElement.CALL_TYPE.MISSED: {
                    callIndicatorResourceId = android.R.drawable.sym_call_missed;

                    break;
                }
            }

            //set call indicator image
            ImageView callIndicator = (ImageView) convertView.findViewById(R.id.call_indicator);
            callIndicator.setBackgroundResource(callIndicatorResourceId);

            //hold caller name and number textfields
            TextView callerName = (TextView) convertView.findViewById(R.id.display_name);
            TextView phoneNumber = (TextView) convertView.findViewById(R.id.phone_number);

            //holds caller name and phone number
            String name = cvm.CallerName;
            String number = cvm.CallerNumber;

            //holds caller name and number views visibility state
            int nameVisibility = View.VISIBLE;
            int numberVisibility = View.VISIBLE;

            //in case this is a "private number"
            if (number.equals("-2")) {
                numberVisibility = View.GONE;
                name = BouncerApplication.getApplication().getResources().getString(R.string.private_number);
            }

            //in case name doesn't exist
            if (StringUtil.isNullOrEmpty(name)) {
                numberVisibility = View.GONE;
                name = number;
            }

            //set name and number views
            callerName.setVisibility(nameVisibility);
            callerName.setText(name);
            phoneNumber.setVisibility(numberVisibility);
            phoneNumber.setText(number);

            //set call date-time
            TextView date = (TextView) convertView.findViewById(R.id.date);
            date.setText(String.valueOf(new Date(call.CallTime).toLocaleString()));

            //get buttons layout
            LinearLayout buttons = (LinearLayout) convertView.findViewById(R.id.buttons);

            //display buttons only in case we have a phone number
            if (!StringUtil.isNullOrEmpty(call.CallerNumber) && !call.CallerNumber.equals("-2")) {
                //make sure buttons layout is visible
                buttons.setVisibility(View.VISIBLE);

                //set item's on-click listeners
                int numOfButtons = buttons.getChildCount();
                for (int i = 0; i < numOfButtons; ++i) {
                    ImageButton button = (ImageButton) buttons.getChildAt(i);
                    button.setTag(cvm);
                    button.setOnClickListener(callogViewItemListener);
                }
            } else {
                //don't display buttons layout
                buttons.setVisibility(View.GONE);
            }

            //return the view
            return convertView;
        }
    }
}
