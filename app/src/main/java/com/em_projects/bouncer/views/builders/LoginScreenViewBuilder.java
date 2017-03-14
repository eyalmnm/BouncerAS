package com.em_projects.bouncer.views.builders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.em_projects.bouncer.R;
import com.em_projects.bouncer.views.controllers.LoginScreenViewController;
import com.em_projects.bouncer.views.model.LoginScreenViewModel;
import com.em_projects.infra.views.builders.ViewBuilder;
import com.em_projects.infra.views.controllers.ViewController;

public class LoginScreenViewBuilder extends ViewBuilder<LoginScreenViewModel> {
    private static final String TAG = "LoginScreenViewBuilder";

    /**
     * Ctor.
     *
     * @param model (LoginScreenViewModel) the model for this view.
     */
    public LoginScreenViewBuilder(LoginScreenViewModel model) {
        super(model);
        Log.d(TAG, "LoginScreenViewBuilder");
    }

    @Override
    public View getMainLayout(LayoutInflater inflater) {
        Log.d(TAG, "getMainLayout");
        ScrollView layout = (ScrollView) inflater.inflate(R.layout.password_screen_layout, null);

        // set titles and button's text
        ((TextView) layout.findViewById(R.id.title)).setText(getModel().Title);
        ((TextView) layout.findViewById(R.id.textView1)).setText(getModel().EnterPasswordLabel);
        ((Button) layout.findViewById(R.id.button)).setText(getModel().ButtonText);

        // set second and third edit views as invisible (we don't need them in this view)
        layout.findViewById(R.id.linearLayout2).setVisibility(View.GONE);
        layout.findViewById(R.id.linearLayout3).setVisibility(View.GONE);

        return layout;
    }

    @Override
    public void refreshView(View mainlayout, Object newModel) {
    }

    @Override
    public ViewController getViewController() {
        Log.d(TAG, "getViewController");
        return new LoginScreenViewController();
    }
}
