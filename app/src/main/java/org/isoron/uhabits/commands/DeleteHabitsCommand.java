/* Copyright (C) 2016 Alinson Santos Xavier
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied  warranty of MERCHANTABILITY or
 * FITNESS  FOR  A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You  should  have  received  a  copy  of the GNU General Public License
 * along  with  this  program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.commands;

import org.isoron.helpers.Command;
import org.isoron.uhabits.R;
import org.isoron.uhabits.models.Habit;

import java.util.List;

public class DeleteHabitsCommand extends Command
{
    private List<Habit> habits;

    public DeleteHabitsCommand(List<Habit> habits)
    {
        this.habits = habits;
    }

    @Override
    public void execute()
    {
        for(Habit h : habits)
            h.cascadeDelete();
    }

    @Override
    public void undo()
    {

    }

    public Integer getExecuteStringId()
    {
        return R.string.toast_habit_deleted;
    }

    public Integer getUndoStringId()
    {
        return R.string.toast_habit_restored;
    }
}
