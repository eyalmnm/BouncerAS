package com.em_projects.bouncer.views.builders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.em_projects.bouncer.R;
import com.em_projects.bouncer.views.controllers.ChangePasswordScreenViewController;
import com.em_projects.bouncer.views.model.ChangePasswordScreenViewModel;
import com.em_projects.infra.views.builders.ViewBuilder;
import com.em_projects.infra.views.controllers.ViewController;

public class ChangePasswordScreenViewBuilder extends ViewBuilder<ChangePasswordScreenViewModel> {
    private static final String TAG = "ChangePwdScreenViewBldr";

    /**
     * Ctor.
     *
     * @param model (ChangePasswordScreenViewModel) the model for this view.
     */
    public ChangePasswordScreenViewBuilder(ChangePasswordScreenViewModel model) {
        super(model);
        Log.d(TAG, "ChangePasswordScreenViewBuilder");
    }

    @Override
    public View getMainLayout(LayoutInflater inflater) {
        Log.d(TAG, "getMainLayout");
        ScrollView layout = (ScrollView) inflater.inflate(R.layout.password_screen_layout, null);

        // set titles and button's text
        ((TextView) layout.findViewById(R.id.title)).setText(getModel().Title);
        ((TextView) layout.findViewById(R.id.textView1)).setText(getModel().CurrentPasswordLabel);
        ((TextView) layout.findViewById(R.id.textView2)).setText(getModel().ChangePasswordLabel);
        ((TextView) layout.findViewById(R.id.textView3)).setText(getModel().ConfirmPasswordLabel);
        ((Button) layout.findViewById(R.id.button)).setText(getModel().ButtonText);

        return layout;
    }

    @Override
    public void refreshView(View mainlayout, Object newModel) {
    }

    @Override
    public ViewController getViewController() {
        Log.d(TAG, "getViewController");
        return new ChangePasswordScreenViewController();
    }
}
