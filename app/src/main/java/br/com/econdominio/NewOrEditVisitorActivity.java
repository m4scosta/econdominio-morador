package br.com.econdominio;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
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

@SuppressWarnings("ConstantConditions")
public class NewOrEditVisitorActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private CircleImageView photoView;
    private TextView nameText;
    private TextView rgText;
    private Button saveBtn;
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
        saveBtn = (Button) findViewById(R.id.visitor_form_save_btn);
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
        // TODO: validar campos
        // TODO: salvar imagem assincronamente
        // TODO: voltar para a listagem apos salvar
        // TODO: exibir loading enquanto salva visitante
        // TODO: melhorar mensagens de erro
        try {
            ParseFile photo = savePhoto();
            saveVisitor(photo);
        } catch (ParseException e) {
            Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private ParseFile savePhoto() throws ParseException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        photoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bout);
        byte[] bytes = bout.toByteArray();
        ParseFile file = new ParseFile(bytes);
        file.save();
        return file;
    }

    private void saveVisitor(ParseFile photo) {
        final Activity self = this;
        ParseObject visitor = new ParseObject("Visitor");
        visitor.put("photo", photo);
        visitor.put("name", nameText.getText().toString());
        visitor.put("rg", rgText.getText().toString());
        visitor.put("residence", ParseUser.getCurrentUser().getUsername());
        visitor.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(self, "Salvo*", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(self, "Erro ao salvar visitante", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
