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

package org.isoron.uhabits;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;

import org.isoron.helpers.DateHelper;
import org.isoron.helpers.DialogHelper;
import org.isoron.helpers.ReplayableActivity;
import org.isoron.uhabits.fragments.ListHabitsFragment;
import org.isoron.uhabits.helpers.ReminderHelper;
import org.isoron.uhabits.models.Habit;
import org.isoron.uhabits.widgets.CheckmarkWidgetProvider;
import org.isoron.uhabits.widgets.HistoryWidgetProvider;
import org.isoron.uhabits.widgets.ScoreWidgetProvider;
import org.isoron.uhabits.widgets.StreakWidgetProvider;

public class MainActivity extends ReplayableActivity
        implements ListHabitsFragment.OnHabitClickListener
{
    private ListHabitsFragment listHabitsFragment;
    private SharedPreferences prefs;
    private BroadcastReceiver receiver;
    private LocalBroadcastManager localBroadcastManager;

    public static final String ACTION_REFRESH = "org.isoron.uhabits.ACTION_REFRESH";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_habits_activity);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        listHabitsFragment =
                (ListHabitsFragment) getFragmentManager().findFragmentById(R.id.fragment1);

        receiver = new Receiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(receiver, new IntentFilter(ACTION_REFRESH));

        onStartup();
    }

    private void onStartup()
    {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        DialogHelper.incrementLaunchCount(this);
        DialogHelper.updateLastAppVersion(this);
        showTutorial();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params)
            {
                ReminderHelper.createReminderAlarms(MainActivity.this);
                updateWidgets(MainActivity.this);
                return null;
            }
        }.execute();

    }

    private void showTutorial()
    {
        Boolean firstRun = prefs.getBoolean("pref_first_run", true);

        if (firstRun)
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("pref_first_run", false);
            editor.putLong("last_hint_timestamp", DateHelper.getStartOfToday()).apply();
            editor.apply();

            Intent intent = new Intent(this, IntroActivity.class);
            this.startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.list_habits_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onHabitClicked(Habit habit)
    {
        Intent intent = new Intent(this, ShowHabitActivity.class);
        intent.setData(Uri.parse("content://org.isoron.uhabits/habit/" + habit.getId()));
        startActivity(intent);
    }

    @Override
    public void onPostExecuteCommand(Long refreshKey)
    {
        listHabitsFragment.onPostExecuteCommand(refreshKey);
        updateWidgets(this);
    }

    public static void updateWidgets(Context context)
    {
        updateWidgets(context, CheckmarkWidgetProvider.class);
        updateWidgets(context, HistoryWidgetProvider.class);
        updateWidgets(context, ScoreWidgetProvider.class);
        updateWidgets(context, StreakWidgetProvider.class);
    }

    private static void updateWidgets(Context context, Class providerClass)
    {
        ComponentName provider = new ComponentName(context, providerClass);
        Intent intent = new Intent(context, providerClass);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(provider);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    @Override
    protected void onDestroy()
    {
        localBroadcastManager.unregisterReceiver(receiver);
        super.onDestroy();
    }

    class Receiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            listHabitsFragment.onPostExecuteCommand(null);
        }
    }
}
