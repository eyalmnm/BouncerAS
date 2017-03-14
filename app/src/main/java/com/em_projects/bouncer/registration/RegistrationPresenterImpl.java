package com.em_projects.bouncer.registration;

import android.util.Log;

import com.em_projects.bouncer.config.Dynamics;
import com.em_projects.bouncer.network.comm.CommListener;
import com.em_projects.bouncer.network.comm.Communicator;
import com.em_projects.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by eyalmuchtar on 14/03/2017.
 */

public class RegistrationPresenterImpl implements RegistrationPresenter, CommListener {
    private static final String TAG = "RegistrationPrsntrImpl";

    private RegistrationView view;
    private Communicator communicator;

    public RegistrationPresenterImpl(RegistrationView view) {
        this.view = view;
        communicator = Communicator.getInstance();
    }

    @Override
    public void register(String name, String phoneNumber) {
        if (null != view) {
            view.showProgress();
        }
        try {
            communicator.registration(name, phoneNumber, this);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "register", e);
            exceptionThrown(e);
        }
    }

    @Override
    public void onDestroy() {
        view = null;
    }

    @Override
    public void newDataArrived(String newData) {
        if (null != view) {
            view.hideProgress();
            String nonce = null;
            try {
                nonce = getNonce(newData);
                if (false == StringUtil.isNullOrEmpty(nonce)) {
                    Dynamics.SERVER.nonce = nonce;
                    view.navigateToOTP();
                }
            } catch (JSONException e) {
                Log.e(TAG, "newDataArrived", e);
                exceptionThrown(e);
            }
        }
    }

    @Override
    public void exceptionThrown(Throwable throwable) {
        if (null != view) {
            view.hideProgress();
            view.showError(throwable.getMessage());
        }
    }

    private String getNonce(String data) throws JSONException {
        JSONObject jsonObject = new JSONObject(data);
        return jsonObject.getString("nonce");
    }
}
