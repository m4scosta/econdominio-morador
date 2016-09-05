package br.com.econdominio;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public final class VisitorsActivity extends AppBaseActivity implements FindCallback<ParseObject> {

    private static final String TAG = VisitorsActivity.class.getName();

    private ListView visitorList;
    private View emptyListView;
    private View progressView;
    private FloatingActionButton fab;
    private ArrayAdapter<ParseObject> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.content_visitor);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1);
        visitorList = (ListView) findViewById(R.id.visitor_list);
        visitorList.setAdapter(adapter);
        emptyListView = findViewById(R.id.visitor_list_empty_view);
        progressView = findViewById(R.id.visitors_progress);
        setupFab();
        fetchVisitors();
    }

    private void setupFab() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateTo(NewOrEditVisitorActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        });
    }

    private void fetchVisitors() {
        showProgress(true);

        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Visitor");
        query.whereEqualTo("residence", currentUser.getUsername()); // TODO: adicionar esta informacao ao cadastro de usuarios
        query.findInBackground(this);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        progressView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public void done(List<ParseObject> visitors, ParseException e) {
        if (e == null) {
            hideProgressAndShowVisitorList(visitors);
        } else {
            Log.e(TAG, "Error while loading visitors", e);
            Toast.makeText(this, R.string.error_loading, Toast.LENGTH_SHORT).show();
        }
    }

    private void hideProgressAndShowVisitorList(List<ParseObject> visitors) {
        showProgress(false);
        adapter.clear();
        adapter.addAll(visitors);
        if (visitors.isEmpty()) {
            visitorList.setVisibility(View.GONE);
            emptyListView.setVisibility(View.VISIBLE);
        } else {
            visitorList.setVisibility(View.VISIBLE);
            emptyListView.setVisibility(View.GONE);
        }
    }
}
