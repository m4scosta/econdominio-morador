package br.com.econdominio;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;


// TODO: organizar o código desta classe
@SuppressWarnings("ConstantConditions")
public class NewOrEditVisitorActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ProgressDialog progressDialog;

    private CircleImageView photoView;
    private TextView nameText;
    private TextView rgText;
    private Bitmap photoBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_or_edit_visitor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameText = (TextView) findViewById(R.id.visitor_form_name);
        rgText = (TextView) findViewById(R.id.visitor_form_rg);
        photoView = (CircleImageView) findViewById(R.id.visitor_form_photo);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            photoBitmap = (Bitmap) extras.get("data");
            photoView.setImageBitmap(photoBitmap);
        }
    }

    private void requestPhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

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
        final ParseObject visitor = new ParseObject("Visitor");
        visitor.put("photo", photo);
        visitor.put("name", nameText.getText().toString());
        visitor.put("rg", rgText.getText().toString());
        visitor.put("residence", ParseUser.getCurrentUser().getUsername());

        showSaveLoading();

        photo.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    visitor.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(self, "Salvo", Toast.LENGTH_SHORT).show();
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("asd", "asdsa");
                                setResult(RESULT_OK, returnIntent);
                                dismissSaveLoading();
                                finish();
                            } else {
                                Toast.makeText(self, "Erro ao salvar visitante", Toast.LENGTH_SHORT).show();
                            }
                            dismissSaveLoading();
                        }
                    });
                } else {
                    Toast.makeText(self, "Erro ao salvar foto", Toast.LENGTH_SHORT).show();
                    dismissSaveLoading();
                }
            }
        });
    }

    private void showSaveLoading() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Salvando...");
        progressDialog.show();
    }

    private void dismissSaveLoading() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
