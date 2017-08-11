package com.americavoice.backup.main.ui.dialog;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.americavoice.backup.R;


/**
 * Created by Lesther on 3/4/2017.
 */

public class DialogError extends DialogFragment {
    private static final String ARGUMENT_MESSAGE = "gt.kilo.kiloapp.ARGUMENT_MESSAGE";

    public static DialogError newInstance(String message)
    {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_MESSAGE, message);

        DialogError dialogHelp = new DialogError();
        dialogHelp.setArguments(bundle);

        return dialogHelp;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_error, container,
                false);
        ((TextView)rootView.findViewById(R.id.tv_text)).setText(getArguments().getString(ARGUMENT_MESSAGE));
        getDialog().setTitle("Kilo");
        // Do something else
        return rootView;
    }
}