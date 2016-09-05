package br.com.econdominio.visitors;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;

import br.com.econdominio.R;
import br.com.econdominio.utils.RoundRectTransformation;


@SuppressWarnings("ConstantConditions")
public class NewOrEditVisitorActivity
        extends AppCompatActivity
        implements GetCallback<ParseObject> {

    private static final String TAG = NewOrEditVisitorActivity.class.getName();
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ParseObject editVisitor;
    private ProgressDialog progressDialog;

    private ImageView photoView;
    private TextView nameText;
    private TextView rgText;
    private Bitmap photoBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_or_edit_visitor);
        setupView();
        loadVisitorIfNecessary();
    }

    private void setupView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameText = (TextView) findViewById(R.id.visitor_form_name);
        rgText = (TextView) findViewById(R.id.visitor_form_rg);
        photoView = (ImageView) findViewById(R.id.visitor_form_photo);
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPhoto();
            }
        });
        Button saveBtn = (Button) findViewById(R.id.visitor_form_save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateAndSaveVisitor();
            }
        });
    }

    // Edit visitor
    private void loadVisitorIfNecessary() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String visitorId = extras.getString("visitorId");
            if (visitorId != null) {
                showLoading();
                new ParseQuery<>("Visitor").getInBackground(visitorId, this);
            }
        }
    }

    @Override
    public void done(ParseObject visitor, ParseException e) {
        if (e == null) {
            fillFormWithVisitorData(visitor);
        } else {
            cancelActivity();
        }
        dismissLoading();
    }

    private void fillFormWithVisitorData(ParseObject visitor) {
        editVisitor = visitor;
        nameText.setText(visitor.getString("name"));
        rgText.setText(visitor.getString("rg"));

        Uri photoUri = Uri.parse(visitor.getParseFile("photo").getUrl());

        Picasso.with(this)
                .load(photoUri.toString())
                .placeholder(android.R.drawable.progress_indeterminate_horizontal)
                .into(new Target() {
                    @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        setPhotoBitmap(bitmap);
                    }

                    @Override public void onBitmapFailed(Drawable errorDrawable) {
                        cancelActivity();
                    }

                    @Override public void onPrepareLoad(Drawable placeHolderDrawable) {}
                });
    }

    private void setPhotoBitmap(Bitmap bitmap) {
        photoBitmap = bitmap;
        Bitmap copy = photoBitmap.copy(photoBitmap.getConfig(), true);
        photoView.setImageBitmap(new RoundRectTransformation(15).transform(copy));
    }
    // Edit visitor

    // Camera request
    private void requestPhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPhotoBitmap((Bitmap) data.getExtras().get("data"));
        }
    }
    // Camera request

    // Form submit
    private void validateAndSaveVisitor() {
        if (validate()) {
            ParseFile photo = buildPhoto();
            saveVisitor(photo);
        }
    }

    private boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String rg = rgText.getText().toString();

        if (name.isEmpty()) {
            nameText.setError("Obrigatório");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (rg.isEmpty()) {
            rgText.setError("Obrigatório");
            valid = false;
        } else {
            rgText.setError(null);
        }

        if (photoBitmap == null) {
            new AlertDialog.Builder(this)
                    .setMessage("A foto do visitante é obrigatória.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {}
                    })
                    .show();
        }

        return valid;
    }

    private ParseFile buildPhoto(){
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bout);
        byte[] bytes = bout.toByteArray();
        return new ParseFile(bytes);
    }

    private void saveVisitor(ParseFile photo) {
        final Activity self = this;
        final ParseObject visitor;
        if (editVisitor == null) {
            visitor = new ParseObject("Visitor");
        } else {
            visitor = editVisitor;
        }
        visitor.put("photo", photo);
        visitor.put("name", nameText.getText().toString());
        visitor.put("rg", rgText.getText().toString());
        visitor.put("residence", ParseUser.getCurrentUser().get("residence"));

        showLoading();

        photo.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    visitor.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(self, "Salvo", Toast.LENGTH_SHORT).show();
                                finishWithResponse();
                            } else {
                                Log.e(TAG, "Error while saving visitor", e);
                                Toast.makeText(self, "Erro ao salvar visitante", Toast.LENGTH_SHORT).show();
                            }
                            dismissLoading();
                        }
                    });
                } else {
                    Toast.makeText(self, "Erro ao salvar foto", Toast.LENGTH_SHORT).show();
                    dismissLoading();
                }
            }
        });
    }
    // Form submit

    private void showLoading() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Aguarde...");
        progressDialog.show();
    }

    private void dismissLoading() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void finishWithResponse() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    private void cancelActivity() {
        Toast.makeText(NewOrEditVisitorActivity.this,
                "Erro ao carregar visitante", Toast.LENGTH_SHORT).show();
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }
}
