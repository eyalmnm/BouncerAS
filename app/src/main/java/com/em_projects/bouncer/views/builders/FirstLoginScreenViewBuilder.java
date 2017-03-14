package com.em_projects.bouncer.views.builders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.em_projects.bouncer.R;
import com.em_projects.bouncer.views.controllers.FirstLoginScreenViewController;
import com.em_projects.bouncer.views.model.FirstLoginScreenViewModel;
import com.em_projects.infra.views.builders.ViewBuilder;
import com.em_projects.infra.views.controllers.ViewController;

public class FirstLoginScreenViewBuilder extends ViewBuilder<FirstLoginScreenViewModel> {
    private static final String TAG = "FirstLoginScreenVwBldr";

    /**
     * Ctor.
     *
     * @param model (FirstLoginScreenViewModel) the model for this view.
     */
    public FirstLoginScreenViewBuilder(FirstLoginScreenViewModel model) {
        super(model);
        Log.d(TAG, "FirstLoginScreenViewBuilder");
    }

    @Override
    public View getMainLayout(LayoutInflater inflater) {
        Log.d(TAG, "getMainLayout");
        ScrollView layout = (ScrollView) inflater.inflate(R.layout.password_screen_layout, null);

        // set titles and button's text
        ((TextView) layout.findViewById(R.id.title)).setText(getModel().Title);
        ((TextView) layout.findViewById(R.id.textView1)).setText(getModel().CreatePasswordLabel);
        ((TextView) layout.findViewById(R.id.textView2)).setText(getModel().ConfirmPasswordLabel);
        ((Button) layout.findViewById(R.id.button)).setText(getModel().ButtonText);

        // set third edit view as invisible (it is for the change-password screen)
        layout.findViewById(R.id.linearLayout3).setVisibility(View.GONE);

        return layout;
    }

    @Override
    public void refreshView(View mainlayout, Object newModel) {
    }

    @Override
    public ViewController getViewController() {
        Log.d(TAG, "getViewController");
        return new FirstLoginScreenViewController();
    }
}
