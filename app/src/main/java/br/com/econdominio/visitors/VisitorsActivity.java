package br.com.econdominio.visitors;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import br.com.econdominio.AppBaseActivity;
import br.com.econdominio.R;

public final class VisitorsActivity extends AppBaseActivity implements FindCallback<ParseObject> {

    private static final String TAG = VisitorsActivity.class.getName();

    private static final int CREATE_VISITOR = 1;
    private static final int EDIT_VISITOR = 2;

    private ProgressDialog progressDialog;
    private ListView visitorList;
    private View emptyListView;
    private View progressView;
    private VisitorListAdapter visitorListAdapter;
    private List<ParseObject> visitors = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.content_visitor);

        visitorListAdapter = new VisitorListAdapter(this, visitors);
        visitorList = (ListView) findViewById(R.id.visitor_list);
        visitorList.setAdapter(visitorListAdapter);
        emptyListView = findViewById(R.id.visitor_list_empty_view);
        progressView = findViewById(R.id.visitors_progress);
        setupFab();
        fetchVisitors();
    }

    private void setupFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VisitorsActivity.this, NewOrEditVisitorActivity.class);
                startActivityForResult(intent, CREATE_VISITOR);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean visitorsUpdated = requestCode == CREATE_VISITOR || requestCode == EDIT_VISITOR;
        if (visitorsUpdated && resultCode == RESULT_OK) {
            fetchVisitors();
        }
    }

    private void fetchVisitors() {
        showProgress(true);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Visitor");
        query.orderByDescending("createdAt");
        query.whereEqualTo("residence", ParseUser.getCurrentUser().get("residence"));
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

    private void hideProgressAndShowVisitorList(List<ParseObject> loadedVisitors) {
        showProgress(false);
        visitors.clear();
        visitors.addAll(loadedVisitors);
        visitorListAdapter.notifyDataSetChanged();
        if (visitors.isEmpty()) {
            visitorList.setVisibility(View.GONE);
            emptyListView.setVisibility(View.VISIBLE);
        } else {
            visitorList.setVisibility(View.VISIBLE);
            emptyListView.setVisibility(View.GONE);
        }
    }

    void deleteVisitor(final ParseObject visitor) {
        showSaveLoading();
        visitor.deleteInBackground(new DeleteCallback() {

            @Override
            public void done(ParseException e) {
                String msg;
                if (e == null) {
                    msg = "Deletado";
                    visitorList.setVisibility(View.GONE);
                    emptyListView.setVisibility(View.GONE);
                    fetchVisitors();
                } else {
                    msg = "Erro ao deletar, tente novamente";
                }
                Toast.makeText(VisitorsActivity.this, msg, Toast.LENGTH_SHORT).show();
                dismissSaveLoading();
            }
        });
    }

    void editVisitor(final ParseObject visitor) {
        Intent intent = new Intent(VisitorsActivity.this, NewOrEditVisitorActivity.class);
        intent.putExtra("visitorId", visitor.getObjectId());
        startActivityForResult(intent, CREATE_VISITOR);
    }

    private void showSaveLoading() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Aguarde...");
        progressDialog.show();
    }

    private void dismissSaveLoading() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
