/*
 * Copyright 2016 Gabor Somogyi (gabor.g.somogyi@gmail.com)
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
package org.traccar.manager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Button;

public class ConfirmationDialogFragment extends DialogFragment {
    private static final String LONG_PARAM_NAME = "LongParam";

    private Context context;
    private long longParam;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public void setLongParam(long longParam) {
        this.longParam = longParam;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            longParam = savedInstanceState.getLong(LONG_PARAM_NAME);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.confirmation_title);
        builder.setPositiveButton(android.R.string.yes, (DialogInterface.OnClickListener)context);
        builder.setNegativeButton(android.R.string.no, (DialogInterface.OnClickListener)context);
        AlertDialog result = builder.create();
        result.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog = ((AlertDialog)dialog);
                Button positive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if(positive != null) {
                    positive.setTag(longParam);
                }
                Button negative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                if(negative != null) {
                    negative.setTag(longParam);
                }
                Button neutral = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                if(neutral != null) {
                    neutral.setTag(longParam);
                }
            }
        });
        return result;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(LONG_PARAM_NAME, longParam);
    }
}
