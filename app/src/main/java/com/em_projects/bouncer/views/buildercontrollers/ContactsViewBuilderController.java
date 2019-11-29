package com.em_projects.bouncer.views.buildercontrollers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.em_projects.bouncer.BouncerActivity;
import com.em_projects.bouncer.BouncerApplication;
import com.em_projects.bouncer.BouncerChatActivity;
import com.em_projects.bouncer.BouncerUserSession;
import com.em_projects.bouncer.R;
import com.em_projects.bouncer.model.Contact;
import com.em_projects.bouncer.model.Phone;
import com.em_projects.bouncer.repositories.ContactsRepository;
import com.em_projects.bouncer.views.model.ContactViewModel;
import com.em_projects.bouncer.views.model.ContactsViewModel;
import com.em_projects.infra.activity.BasicActivity;
import com.em_projects.infra.views.buildercontrollers.TabViewBuilderController;
import com.em_projects.infra.views.controllers.ViewController;
import com.em_projects.utils.Utils;

import java.util.Collections;
import java.util.Vector;

public class ContactsViewBuilderController extends TabViewBuilderController<ContactsViewModel> {
    public final static ContactsListViewAdapter m_adapter = new ContactsListViewAdapter();
    private static final String TAG = "ContactsViewiderCntrlr";

    public ContactsViewBuilderController(ContactsViewModel model) {
        super(model);
        Log.d(TAG, "ContactsViewBuilderController");

        m_adapter.setNewModel(model);
    }

    /**
     * Gets contact's distinct phone numbers.
     *
     * @param contactId (String != null) Contact's id.
     * @return (Vector<String>) Contact's distinct phone numbers collection. May be empty.
     */
    public static Vector<String> getContactsDistinctPhones(String contactId) {
        Log.d(TAG, "getContactsDistinctPhones");
        //get the list of contact's phone numbers
        Vector<Phone> phones = ContactsRepository.getInstance().getPhones(contactId);

        //holds distinct phone numbers collection
        Vector<String> distinctPhones = new Vector<String>(1, 1);

        //get distinct phone numbers from the phones collection
        for (Phone phone : phones) {
            //get the phone number
            String number = phone.getNumber();

            //add to distinct phones collection only in case it is distinct
            if (!distinctPhones.contains(number))
                distinctPhones.add(number);
        }

        //return the items array
        return distinctPhones;
    }

    @Override
    public View getMainLayout(LayoutInflater inflater) {
        Log.d(TAG, "getMainLayout");
        LinearLayout mainLayout = (LinearLayout) inflater.inflate(R.layout.tab_content_container, null);
        return mainLayout;
    }

    @Override
    public void refreshView(View mainlayout, Object newModel) {
        Log.d(TAG, "refreshView");
        //set new model to the adapter
        m_adapter.setNewModel((ContactsViewModel) newModel);

        //notify that data has changed - triggers repaint
        m_adapter.notifyDataSetChanged();

        //stop gauge
        BasicActivity.stopGauge();
    }

    @Override
    public ViewController getViewController() {
        Log.d(TAG, "getViewController");
        return this;
    }

    @Override
    public void attachController(final View view) {
        Log.d(TAG, "attachController");
        //get the search textfield and attach its listener
        final EditText searchTxt = view.findViewById(R.id.search_txt);
        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString(), view);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //get the search button and set it's listener
        Button searchButton = view.findViewById(R.id.search_btn);
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                filterContacts(searchTxt.getText().toString(), view);
            }
        });

        //get the list view and set its adapter
        ListView lv = view.findViewById(R.id.list_view);
        lv.setAdapter(m_adapter);
    }

    private void filterContacts(String filterStr, View mainLayout) {
        Log.d(TAG, "filterContacts");
        //get the filtered contacts
        Vector<Contact> contacts = ContactsRepository.getInstance().getFilteredContacts(filterStr);

        //sort the contacts
        Collections.sort(contacts, Contact.COMPARE_BY_DISPLAY_NAME);

        //refresh the screen with filtered contacts
        refreshView(mainLayout, new ContactsViewModel(contacts));
    }

    @Override
    public boolean onBackKeyPressed() {
        Log.d(TAG, "onBackKeyPressed");
        Utils.showExitMessage(BouncerApplication.getApplication().getActivityInForeground());

        return true;
    }

    public static class ContactsListViewAdapter extends BaseAdapter {
        public ContactsViewModel m_model;
        //holds the action that is performed when user long-press a contact item in the contacts list.
        private View.OnClickListener contactItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get contact's view model
                final ContactViewModel cvm = (ContactViewModel) v.getTag();

                //hold application resources
                Resources res = BouncerApplication.getApplication().getResources();

                String[] items = new String[4];
                items[0] = res.getString(R.string.contact_context_menu_call);
                items[1] = res.getString(R.string.contact_context_menu_sms);
                items[2] = res.getString(R.string.contact_context_menu_edit);
                items[3] = cvm.IsHidden ?
                        res.getString(R.string.contact_context_menu_unhide) :
                        res.getString(R.string.contact_context_menu_hide);

                //create alert dialog for contact's options
                AlertDialog.Builder builder = new AlertDialog.Builder(BouncerApplication.getApplication().getActivityInForeground());
                builder.setTitle(cvm.FullName);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: //Call
                                showCallNumberSelectionDialog(cvm);
                                break;

                            case 1: //Send SMS
                                showSendSmsNumberSelectionDialog(cvm);
                                break;

                            case 2: //Edit Contact
                                editContact(cvm);
                                break;

                            case 3: //Hide/Unhide
                                hideOrUnhideContact(cvm);
                                break;

                            default:
                                break;
                        }
                    }
                });

                //build and display the dialog
                AlertDialog alert = builder.create();
                alert.show();
            }
        };
        private OnClickListener contactButtonsListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                //get contact's view model
                final ContactViewModel cvm = (ContactViewModel) v.getTag();

                //handle click according to view's id
                int viewId = v.getId();
                switch (viewId) {
                    case R.id.hide_btn:
                        hideOrUnhideContact(cvm);
                        break;

                    case R.id.make_call_btn:
                        showCallNumberSelectionDialog(cvm);
                        break;

                    case R.id.send_sms_btn:
                        showSendSmsNumberSelectionDialog(cvm);
                        break;

                    default:
                        break;
                }
            }
        };

        /**
         * Shows a dialog with contact's phone numbers for selection.
         *
         * @param items           (CharSequence[] != null) A list of contact's phone numbers.
         * @param titleResourceId (int) Dialog's title string resource id.
         * @param onClickListener (DialogInterface.OnClickListener != null) OnClickListener that defines
         *                        what will happen when user selects a number from the list.
         */
        private static void showPhonesDialog(CharSequence[] items, int titleResourceId, DialogInterface.OnClickListener onClickListener) {
            //create alert dialog for number selection
            AlertDialog.Builder builder = new AlertDialog.Builder(BouncerApplication.getApplication().getActivityInForeground());
            builder.setTitle(titleResourceId);
            builder.setItems(items, onClickListener);

            //build and display the dialog
            AlertDialog alert = builder.create();
            alert.setCanceledOnTouchOutside(true);
            alert.show();
        }

        public void setNewModel(ContactsViewModel model) {
            m_model = model;
        }

        @Override
        public int getCount() {
            return m_model.getContactsViewModels().size();
        }

        @Override
        public Object getItem(int position) {
            return m_model.getContactsViewModels().elementAt(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //get layout inflater
            LayoutInflater inflater = BouncerApplication.getApplication().getActivityInForeground().getLayoutInflater();

            //hold current contact's view model
            ContactViewModel cvm = m_model.getContactsViewModels().elementAt(position);

            //get the contact
            Contact c = ContactsRepository.getInstance().getByUID(cvm.UID);

            //set is hidden
            if (c.isHidden()) {
                cvm.IsHidden = true;
            } else {
                BouncerUserSession userSession = BouncerApplication.getApplication().getUserSession();
                cvm.IsHidden = userSession.MarkedAsHiddenContactIds.contains(cvm.UID);
            }

            // init the object that holds list-view item's data
            if (convertView == null)
                convertView = inflater.inflate(R.layout.contact_list_item, null);

            //set contact's photo view params
            ImageView photo = convertView.findViewById(R.id.photo_bg);
            byte[] photoData = c.getPhotoData();
            if (photoData != null) {
                Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
                BitmapDrawable drawable = new BitmapDrawable(BouncerApplication.getApplication().getResources(), photoBitmap);
                photo.setBackgroundDrawable(drawable);
            } else {
                photo.setBackgroundResource(R.drawable.default_photo);
            }
            photo.setTag(cvm);
            photo.setOnClickListener(contactItemClickListener);

            //set list-view item params
            TextView fullName = convertView.findViewById(R.id.display_name);
            fullName.setText(c.getDisplayName());
            ImageButton hideStatusBtn = convertView.findViewById(R.id.hide_btn);
            hideStatusBtn.setBackgroundResource(cvm.IsHidden ? R.drawable.hidden_contact_on : R.drawable.hidden_contact_off);

            //get display name layout container, set it as clickable and set on-click listener
            LinearLayout displayName = convertView.findViewById(R.id.display_name_container);
            displayName.setClickable(true);
            displayName.setTag(cvm);
            displayName.setOnClickListener(contactItemClickListener);

            //set item's on-click listeners
            LinearLayout buttons = convertView.findViewById(R.id.buttons);
            int numOfButtons = buttons.getChildCount();
            for (int i = 0; i < numOfButtons; ++i) {
                ImageButton button = (ImageButton) buttons.getChildAt(i);
                button.setTag(cvm);
                button.setOnClickListener(contactButtonsListener);
            }

            //return the view
            return convertView;
        }

        /**
         * Hides or Unhides a contact according to contact's hidden state.
         *
         * @param cvm (ContactViewModel != null) Contact's view model.
         */
        private void hideOrUnhideContact(final ContactViewModel cvm) {
            //hold user session
            BouncerUserSession userSession = BouncerApplication.getApplication().getUserSession();

            //hold contact id
            String contactId = cvm.UID;

            //in case the contact is hidden
            if (cvm.IsHidden) {
                //start gauge
                BasicActivity.startGauge(BouncerApplication.getApplication().getActivityInForeground());

                //remove the contact from marked-as-hidden list
                userSession.MarkedAsHiddenContactIds.remove(contactId);
                userSession.storeMarkedContactIds();

                //in case privacy mode is ON
                if (userSession.IsPrivacyOn) {
                    //lock all observers from updating lists
                    BouncerActivity.lockObservers();

                    //reveal contact with all its data
                    ContactsRepository.getInstance().reveal(contactId);

                    //unlock all observers
                    BouncerActivity.unlockObservers();

                    //start all services
                    BouncerActivity.startAllServices();
                }
                //in case privacy mode os OFF
                else {
                    //mark the contact as not-hidden
                    ContactsRepository.getInstance().getByUID(contactId).setHidden(false);
                    BouncerActivity.s_contactsService.startService();
                }
            }
            //in case the contact is not hidden
            else {
                //in case privacy mode is ON
                if (userSession.IsPrivacyOn) {
                    //start gauge
                    BasicActivity.startGauge(BouncerApplication.getApplication().getActivityInForeground());

                    //lock all observers from updating lists
                    BouncerActivity.lockObservers();

                    //hide contact with all its data
                    String newId = ContactsRepository.getInstance().hide(contactId);

                    //unlock all observers
                    BouncerActivity.unlockObservers();

                    //add new contact's id to marked-as-hidden list
                    userSession.MarkedAsHiddenContactIds.add(newId);

                    //start all services
                    BouncerActivity.startAllServices();
                }
                //in case privacy mode os OFF
                else {
                    //show dialog that explains privacy off mode
                    Utils.showMessage(R.string.warning, R.string.hide_contact_privacy_off,
                            BouncerApplication.getApplication().getActivityInForeground());

                    //add contact's id to marked-as-hidden list
                    userSession.MarkedAsHiddenContactIds.add(contactId);

                    //mark the contact as hidden
                    ContactsRepository.getInstance().getByUID(cvm.UID).setHidden(true);
                    BouncerActivity.s_contactsService.startService();
                }

                //store marked contacts ids
                userSession.storeMarkedContactIds();
            }
        }

        /**
         * Displays phone number selection dialog that the user can call to.
         *
         * @param cvm (ContactViewModel != null) Contact's view model.
         */
        private void showCallNumberSelectionDialog(ContactViewModel cvm) {
            final Context context = BouncerApplication.getApplication().getActivityInForeground().getBaseContext();

            //get contact's phone numbers
            final Vector<String> phones = getContactsDistinctPhones(cvm.UID);

            //in case we have phone numbers
            if (!phones.isEmpty()) {
                //set onClickListener for phones list dialog
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        //get item's string
                        String phoneNumber = phones.elementAt(item);

                        //create the intent for establishing the phone call
                        Intent intent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phoneNumber, null));

                        //start the phone call activity
                        BouncerApplication.getApplication().getActivityInForeground().startActivityForResult(intent, BouncerActivity.CALL_FROM_APP_REQUEST_CODE);
                    }
                };

                //show phone selection dialog
                showPhonesDialog(phones.toArray(new CharSequence[phones.size()]), R.string.dial_dialog_title, onClickListener);
            } else {
                //indicate that contact has no numbers
                Toast.makeText(context, "Contact has no numbers!", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Displays phone numbers selection dialog that the user can send SMS to.
         *
         * @param cvm (ContactViewModel != null) Contact's view model.
         */
        private void showSendSmsNumberSelectionDialog(ContactViewModel cvm) {
            final Context context = BouncerApplication.getApplication().getActivityInForeground().getBaseContext();

            //get contact's phone numbers
            final Vector<String> phones = getContactsDistinctPhones(cvm.UID);

            //in case we have phone numbers
            if (!phones.isEmpty()) {
                //set onClickListener for phones list dialog
                DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        // start chat screen activity
                        Intent intent = new Intent(context, BouncerChatActivity.class);
                        intent.putExtra("address", phones.elementAt(item));
                        BouncerApplication.getApplication().getActivityInForeground().startNewActivity(intent, false);
                    }
                };

                //show phone selection dialog
                showPhonesDialog(phones.toArray(new CharSequence[phones.size()]), R.string.send_sms_dialog_title, onClickListener);
            } else {
                //indicate that contact has no numbers
                Toast.makeText(context, "Contact has no numbers!", Toast.LENGTH_SHORT).show();
            }
        }

        private void editContact(ContactViewModel cvm) {
            Context context = BouncerApplication.getApplication().getActivityInForeground();

            if (cvm.IsHidden) {
                Utils.showMessage(R.string.warning, R.string.visible_contact_warning, context);
            } else {
                //save contact's view model
                BouncerActivity.s_lastEditContactVM = cvm;

//				Uri uri = Uri.parse("content://com.android.contacts/raw_contacts/" + cvm.UID);
                Uri uri = Uri.parse("content://com.android.contacts/contacts/" + cvm.UID);
                Intent editIntent = new Intent(Intent.ACTION_EDIT, uri);
                BouncerApplication.getApplication().getActivityInForeground().startActivityForResult(editIntent, BouncerActivity.EDIT_CONTACT_REQUEST_CODE);
            }
        }
    }
}
