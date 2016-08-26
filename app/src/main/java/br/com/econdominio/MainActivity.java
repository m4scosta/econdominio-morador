package br.com.econdominio;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.econdominio.notifications.NotificationAdapter;

public class MainActivity extends AppBaseActivity implements NotifiableActivity {

    private NotificationAdapter notificationAdapter;

    private final List<ParseObject> notifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.content_main);

        setupFab();
        setupListView();
        fetchNotifications();
    }

    private void setupFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void setupListView() {
        final ListView notificationsView = (ListView) findViewById(R.id.notification_list);
        notificationAdapter = new NotificationAdapter(this, notifications);
        notificationsView.setAdapter(notificationAdapter);
        notificationsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject itemValue = (ParseObject) notificationsView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), itemValue.getObjectId(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchNotifications() {
        List<ParseQuery<ParseObject>> queries = new ArrayList<>();

        ParseQuery<ParseObject> userQuery = ParseQuery.getQuery("Notification");
        userQuery.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        queries.add(userQuery);

        ParseQuery<ParseObject> condoQuery = ParseQuery.getQuery("Notification");
        userQuery.whereEqualTo("condo", ParseUser.getCurrentUser().get("condo"));
        queries.add(condoQuery);

        ParseQuery<ParseObject> mainQuery = ParseQuery.or(queries);
        mainQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> results, ParseException e) {
                if (e == null) {
                    notifications.clear();
                    notifications.addAll(results);
                    notificationAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getApplicationContext(), "Erro ao carregar notificacioes",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        eCondominioApp.setNotifiableActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        eCondominioApp.setNotifiableActivity(null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }

    // TODO: refatorar este metodo ou todo o processo de recebimento de push
    @Override
    public boolean notify(JSONObject notification) {
        Log.d("TEST", notification.toString());
        String type = notification.optString("type");
        String notificationId = notification.optString("notificationId");
        if ((type.equals("mail") || type.equals("visitor_arrived") || type.equals("visitor_left")
                || type.equals("condo_notice")) && notificationId != null) {
            ParseQuery<ParseObject> query = new ParseQuery<>("Notification");
            query.getInBackground(notificationId, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        notifications.add(0, object);
                        alertNewNotification();
                        notificationAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("TEST", "ERROR", e);
                    }
                }
            });
            return true;
        }
        return false;
    }

    private void alertNewNotification() {
        Toast.makeText(this, "Nova notificação", Toast.LENGTH_SHORT).show();
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(new long[] {0, 100L, 100L, 100L}, -1);
    }
}