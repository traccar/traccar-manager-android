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
package org.traccar.manager.commandhandler;

import android.content.res.Resources;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Spinner;

import org.traccar.manager.R;
import org.traccar.manager.model.Command;

public class PositionPeriodicCommandHandler implements CommandHandler {
    private Resources resources;
    private NumberPicker frequency;
    private Spinner unitSpinner;

    public PositionPeriodicCommandHandler(View view, Resources resources) {
        frequency = (NumberPicker) view.findViewById(R.id.frequency);
        unitSpinner = (Spinner) view.findViewById(R.id.unit);
    }

    @Override
    public void onCommandSelected(String commandType) {
        if(commandType.equals(Command.TYPE_POSITION_PERIODIC)) {
            frequency.setVisibility(View.VISIBLE);
            unitSpinner.setVisibility(View.VISIBLE);
        } else {
            frequency.setVisibility(View.GONE);
            unitSpinner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCommandNotingSelected() {
        frequency.setVisibility(View.GONE);
        unitSpinner.setVisibility(View.GONE);
    }

    @Override
    public void onCommandAddParameters(Command command) {
        if (command.getType().equals(Command.TYPE_POSITION_PERIODIC)) {
            int value = frequency.getValue();
            int multiplier = resources.getIntArray(R.array.unit_values)[unitSpinner.getSelectedItemPosition()];
            command.set(Command.KEY_FREQUENCY, value * multiplier);
        }
    }
}
