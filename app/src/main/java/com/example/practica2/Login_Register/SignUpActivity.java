package com.example.practica2.Login_Register;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.practica2.ClassObjects.CircleImage;
import com.example.practica2.ClassObjects.ImgurUploader.ImgurUploader;
import com.example.practica2.Login_Register.MainActivity;
import com.example.practica2.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private TextView signInLink;
    private EditText NameBox;
    private EditText LastNameBox;
    private EditText EmailOrPhoneBox;
    private EditText PasswordBox;
    private ImageView AvatarBox;
    private Button SignUp;
    private RequestQueue requestQueue;
    private static final int REQUEST_CODE_GALLERY = 1;
    private Uri imageUri;
    private String UrlImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Inicialización de la cola de solicitudes
        requestQueue = Volley.newRequestQueue(SignUpActivity.this);

        // Inicialización de variables y referencias a los elementos de la interfaz de usuario
        UrlImage = "https://balandrau.salle.url.edu/i3/repositoryimages/photo/47601a8b-dc7f-41a2-a53b-19d2e8f54cd0.png";
        NameBox = findViewById(R.id.SU_NameBox);
        LastNameBox = findViewById(R.id.SU_LastNameBox);
        EmailOrPhoneBox = findViewById(R.id.SU_EmailOrPhoneBox);
        PasswordBox = findViewById(R.id.SU_PasswordBox);
        AvatarBox = findViewById(R.id.SU_AvatarImageView);
        AvatarBox.setImageResource(R.drawable.default_avatar);

        // Configuración del evento de clic para seleccionar una imagen
        AvatarBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        // Configuración del evento de clic para el botón de registro
        SignUp = findViewById(R.id.SU_SignUp_Button);
        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Se realiza la solicitud de registro
                postSignUpData();
            }
        });

        // Configuración del evento de clic para el enlace de inicio de sesión
        signInLink = findViewById(R.id.SU_SignInLink);
        signInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Acción a realizar cuando se hace clic en el enlace
                // Se inicia la actividad de inicio de sesión y se finaliza la actividad actual
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void selectImage() {
        // Abre la galería para seleccionar una imagen
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            // La imagen se seleccionó exitosamente desde la galería
            // Obtener la URI de la imagen seleccionada
            imageUri = data.getData();
            // Cargar y mostrar la imagen en el ImageView utilizando Picasso
            Picasso.get().load(imageUri).transform(new CircleImage()).into(AvatarBox);
        }
    }

    private void UploadImage(){
        try {
            // Obtener el Bitmap de la imagen seleccionada a través de la URI
            Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // Subir la imagen a Imgur utilizando el método uploadImage()
            ImgurUploader.uploadImage(imageBitmap, new ImgurUploader.ImgurUploadListener() {
                @Override
                public void onSuccess(String imageUrl) {
                    // La imagen se ha subido correctamente, se obtiene la URL de la imagen
                    UrlImage = imageUrl;
                    // Se crea la cuenta de usuario con la URL de la imagen
                    newAccount();
                }

                @Override
                public void onError(String error) {
                    // Manejo del error al subir la imagen
                    // Puedes mostrar un mensaje de error al usuario o realizar acciones adicionales
                    Log.e("error","error");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void newAccount (){
        // Crear un objeto JSON con los datos del usuario
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("name", NameBox.getText().toString());
            jsonParams.put("last_name", LastNameBox.getText().toString());
            jsonParams.put("email", EmailOrPhoneBox.getText().toString());
            jsonParams.put("password", PasswordBox.getText().toString());
            jsonParams.put("image", UrlImage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "https://balandrau.salle.url.edu/i3/socialgift/api/v1/users";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonParams,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // La solicitud de registro fue exitosa
                        Toast.makeText(getApplicationContext(), "Sign up successful", Toast.LENGTH_SHORT).show();
                        // Se inicia la actividad principal y se finaliza la actividad actual
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Manejo de errores en la respuesta de la solicitud
                        if (error.networkResponse != null) {
                            if(error.networkResponse.statusCode == 400) {
                                Toast.makeText(getApplicationContext(), R.string.Error_400, Toast.LENGTH_SHORT).show();
                            } else if(error.networkResponse.statusCode == 406) {
                                Toast.makeText(getApplicationContext(), R.string.Error_406, Toast.LENGTH_SHORT).show();
                            } else if(error.networkResponse.statusCode == 409) {
                                Toast.makeText(getApplicationContext(), R.string.Error_409, Toast.LENGTH_SHORT).show();
                            } else if(error.networkResponse.statusCode == 500) {
                                Toast.makeText(getApplicationContext(), R.string.Error_500, Toast.LENGTH_SHORT).show();
                            } else if(error.networkResponse.statusCode == 502) {
                                Toast.makeText(getApplicationContext(), R.string.Error_502, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.Error_Default, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.Error_Network, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        ) {
        };
        requestQueue.add(request);
    }

    private void postSignUpData() {
        if (imageUri != null){
            // Si se seleccionó una imagen, se sube a Imgur y luego se crea la cuenta de usuario
            UploadImage();
        } else {
            // Si no se seleccionó una imagen, se crea la cuenta de usuario con la imagen predeterminada
            newAccount();
        }
    }

    @Override
    public void onBackPressed() {
        // No hace nada
    }
}
