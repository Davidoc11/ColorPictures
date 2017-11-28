package com.example.david.colorpictures;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int PETICION_FOTO = 1;
    public static final int PETICION_VIDEO = 2;
    public static final int PETICION_GALERIA_FOTOS = 3;
    public static final int PETICION_GALERIA_VIDEOS = 4;
    private static final int MAX_DURATION = 15;
    private static final int WRITE_STORAGE_CAMERA_PICTURE = 11;
    private static final int WRITE_STORAGE_CAMERA_VIDEO = 12;
    private Uri uri;

    public static final int MEDIA_FOTO = 5;
    public static final int MEDIA_VIDEO = 6;
    @BindView(R.id.imageImageView)
    ImageView imageImageView;
    @BindView(R.id.videoImageView)
    ImageView videoImageView;
    @BindView(R.id.imageGalleryImageView)
    ImageView imageGalleryImageView;
    @BindView(R.id.videoGalleryImageView)
    ImageView videoGalleryImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        imageImageView.setOnClickListener((view) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        mostrarDialogo(WRITE_STORAGE_CAMERA_PICTURE);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_CAMERA_PICTURE);
                    }

                } else {
                    crearMedio(MediaStore.ACTION_IMAGE_CAPTURE, MEDIA_FOTO, PETICION_FOTO);
                }
            else {
                crearMedio(MediaStore.ACTION_IMAGE_CAPTURE, MEDIA_FOTO, PETICION_FOTO);
            }
        });


        videoImageView.setOnClickListener((view) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        mostrarDialogo(WRITE_STORAGE_CAMERA_VIDEO);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_CAMERA_VIDEO);
                    }

                } else {
                    crearMedio(MediaStore.ACTION_VIDEO_CAPTURE, MEDIA_VIDEO, PETICION_VIDEO);
                }
            else {
                crearMedio(MediaStore.ACTION_VIDEO_CAPTURE, MEDIA_VIDEO, PETICION_VIDEO);
            }
        });


    }

    @SuppressLint("NewApi")
    private void mostrarDialogo(final int param) {

        new AlertDialog.Builder(this).setTitle("Permiso").setMessage("Necesito el permiso para guardar fotos").setPositiveButton("Aceptar", (view, which) -> {
            int request = param == WRITE_STORAGE_CAMERA_PICTURE ? WRITE_STORAGE_CAMERA_PICTURE : WRITE_STORAGE_CAMERA_VIDEO;
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, request);

        }).setNegativeButton("Cancelar", (view, which) -> Toast.makeText(this, ":(", Toast.LENGTH_SHORT).show()).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            
            case WRITE_STORAGE_CAMERA_PICTURE:
                crearMedio(MediaStore.ACTION_IMAGE_CAPTURE, MEDIA_FOTO, PETICION_FOTO);
                break;
            case WRITE_STORAGE_CAMERA_VIDEO:
                crearMedio(MediaStore.ACTION_VIDEO_CAPTURE, MEDIA_VIDEO, PETICION_VIDEO);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PETICION_FOTO) {
                Intent intent = new Intent(this, ImageActivity.class);
                intent.setDataAndType(uri, "video/*");
                startActivity(intent);
            }
            if (requestCode == PETICION_VIDEO) {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setData(uri);
                startActivity(intent);
            }

            if (requestCode == PETICION_GALERIA_FOTOS) {
                Intent intent = new Intent(this, ImageActivity.class);
                intent.setData(data.getData());
                startActivity(intent);
            }
            if (requestCode == PETICION_GALERIA_VIDEOS) {
                Intent intent = new Intent(this, VideoActivity.class);
                intent.setData(data.getData());
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "Ocurrio un error", Toast.LENGTH_SHORT).show();
        }
    }

    public void crearMedio(String action, int tipo, int peticion) {
        try {
            uri = crearArchivo(tipo);
            if (uri == null) {
                Toast.makeText(this, "Error almacenamiento", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(action);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                if (action.equals(MediaStore.ACTION_VIDEO_CAPTURE))
                    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, MAX_DURATION);
                startActivityForResult(intent, peticion);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Uri crearArchivo(int tipo) throws IOException {
        if (!verificarAlmacenamiento())
            return null;


        String fecha = new SimpleDateFormat("yyyy-MM-dd_HH:mm").format(new Date());
        String nombre;
        File archivo;
        File directorio;
        switch (tipo)

        {
            case MEDIA_FOTO:
                nombre = "IMG_" + fecha;
                directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                archivo = File.createTempFile(nombre, ".jpg", directorio);
                Log.d(TAG, archivo.getAbsolutePath() + "");
                MediaScannerConnection.scanFile(this, new String[]{archivo.getPath()}, new String[]{"image/jpeg", "video/mp4"}, null);
                return Uri.fromFile(archivo);
            case MEDIA_VIDEO:
                nombre = "VID_" + fecha;
                directorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                archivo = File.createTempFile(nombre, ".mp4", directorio);
                Log.d(TAG, archivo.getAbsolutePath() + "");
                return Uri.fromFile(archivo);
            default:
                return null;
        }


    }

    private boolean verificarAlmacenamiento() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public void verFotos(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PETICION_GALERIA_FOTOS);
    }

    public void verVideo(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, PETICION_GALERIA_VIDEOS);
    }
}
