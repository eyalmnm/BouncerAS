package com.em_projects.bouncer.views.builders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.em_projects.bouncer.R;
import com.em_projects.bouncer.views.controllers.TextScreenViewController;
import com.em_projects.bouncer.views.model.TextScreenViewModel;
import com.em_projects.infra.views.builders.ViewBuilder;
import com.em_projects.infra.views.controllers.ViewController;


public class TextScreenViewBuilder extends ViewBuilder<TextScreenViewModel> {
    private static final String TAG = "TextScreenViewBuilder";

    public TextScreenViewBuilder(TextScreenViewModel model) {
        super(model);
        Log.d(TAG, "TextScreenViewBuilder");
    }

    @Override
    public View getMainLayout(LayoutInflater inflater) {
        Log.d(TAG, "getMainLayout");
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.text_screen_layout, null);
        TextView title = (TextView) layout.findViewById(R.id.title);
        TextView text = (TextView) layout.findViewById(R.id.text);

        // set the suitable title & text
        title.setText(getModel().getTitle());
        text.setText(getModel().getText());

        return layout;
    }

    @Override
    public void refreshView(View mainlayout, Object newModel) {
    }

    @Override
    public ViewController getViewController() {
        Log.d(TAG, "getViewController");
        return new TextScreenViewController();
    }

    @Override
    public void addToOptionsMenu(Menu menu) {
        Log.d(TAG, "addToOptionsMenu");
        menu.clear();
    }
}
