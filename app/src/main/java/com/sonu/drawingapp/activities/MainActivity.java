package com.sonu.drawingapp.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sonu.drawingapp.R;
import com.sonu.drawingapp.adapters.ToolsAdapter;
import com.sonu.drawingapp.common.Common;
import com.sonu.drawingapp.interfaces.ToolsListener;
import com.sonu.drawingapp.model.ToolsItem;
import com.sonu.drawingapp.widget.PaintView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ToolsListener {
    private FirebaseUser mCurrentUser;
    private PaintView paintView;
    private int canvasBgColor, brushColor;
    private StorageReference mStorageRef;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        initTools();
    }

    private void initTools() {
        mStorageRef = FirebaseStorage.getInstance().getReference();
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_more_vert_24);
        toolbar.setOverflowIcon(drawable);
        canvasBgColor = Color.WHITE;
        brushColor = Color.BLACK;
        paintView = findViewById(R.id.paint_view);
        paintView.setBrushColor(brushColor);
        paintView.setCanvasBackground(canvasBgColor);
        paintView.setEraserSize(12);
        paintView.setBrushSize(12);
        RecyclerView recyclerView = findViewById(R.id.recycle_view_tools);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        ToolsAdapter toolsAdapter = new ToolsAdapter(getTools(), this);
        recyclerView.setAdapter(toolsAdapter);
    }

    private List<ToolsItem> getTools() {
        List<ToolsItem> list = new ArrayList<>();
        list.add(new ToolsItem(R.drawable.ic_baseline_brush_24, Common.BRUSH));
        list.add(new ToolsItem(R.drawable.eraser, Common.ERASER));
        list.add(new ToolsItem(R.drawable.ic_baseline_palette_24, Common.COLORS));
        list.add(new ToolsItem(R.drawable.paint, Common.BACKGROUND));
        list.add(new ToolsItem(R.drawable.ic_baseline_undo_24, Common.UNDO));
        list.add(new ToolsItem(R.drawable.ic_baseline_redo_24, Common.REDO));
        list.add(new ToolsItem(R.drawable.ic_baseline_clear_24, Common.CLEAR_ALL));
        list.add(new ToolsItem(R.drawable.ic_baseline_cloud_upload_24, Common.UPLOAD));
        return list;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCurrentUser == null) {
            sendUserToLogin();
        }
    }

    private void sendUserToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.all_drawings) {
            startActivity(new Intent(MainActivity.this, AllDrawingsActivity.class));
        }
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            sendUserToLogin();
        }
        return super.onOptionsItemSelected(item);
    }

    public void uploadImage() {
        if (paintView.allActions.size() == 0) {
            Toast.makeText(this, "Nothing to upload", Toast.LENGTH_SHORT).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Uploading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        Bitmap bitmap = paintView.getBitmap();
        String imageName = UUID.randomUUID() + ".png";
        Log.d("TAG", "uploadImage: " + bitmap);
        StorageReference storageReference = mStorageRef.child("images/" + imageName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MainActivity.this, " ERROR " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl()
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    Map<String, String> map = new HashMap<>();
                                    map.put("imageUrl", downloadUri.toString());
                                    db.collection("Images").add(map)
                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(MainActivity.this, "Drawing uploaded successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Log.d("MainActivity", "onFailure: " + e.getMessage());

                                                }
                                            });
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Log.d("MainActivity", "onFailure: " + e.getMessage());
                            }
                        });
            }
        });

    }

    @Override
    public void onSelected(String name) {
        switch (name) {
            case Common.BRUSH:
                showDialogSize(false);
                break;
            case Common.ERASER:
                showDialogSize(true);
                break;
            case Common.UNDO:
                paintView.undo();
                break;
            case Common.BACKGROUND:
                updateColor(name);
                break;
            case Common.COLORS:
                updateColor(name);
                break;
            case Common.REDO:
                paintView.redo();
                break;
            case Common.CLEAR_ALL:
                paintView.clear();
                break;
            case Common.UPLOAD:
                uploadImage();
                break;
        }
    }

    private void updateColor(final String name) {
        int color;

        if (name.equals(Common.BACKGROUND)) {
            color = canvasBgColor;
        } else {
            color = brushColor;
        }

        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(int selectedColor) {
                    }
                })
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        if (name.equals(Common.BACKGROUND)) {
                            canvasBgColor = selectedColor;
                            paintView.setCanvasBackground(canvasBgColor);
                            paintView.setBrushColor(brushColor);
                        } else {
                            brushColor = selectedColor;
                            paintView.setBrushColor(brushColor);
                        }
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .build()
                .show();

    }

    private void showDialogSize(final boolean isEraser) {
        final int[] temp = {0};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog, null, false);
        TextView toolSelected = view.findViewById(R.id.status_tool_selected);
        final TextView statusSize = view.findViewById(R.id.status_size);
        ImageView ivTools = view.findViewById(R.id.iv_tools);
        SeekBar seekBar = view.findViewById(R.id.seekbar_size);

        seekBar.setMax(100);

        if (isEraser) {
            seekBar.setProgress(paintView.getEraserSize());
            toolSelected.setText("Eraser Size");
            ivTools.setImageResource(R.drawable.eraser_black);
            statusSize.setText("Selcted Size : " + paintView.getEraserSize());
        } else {
            seekBar.setProgress(paintView.getBrushSize());
            toolSelected.setText("Brush  Size");
            ivTools.setImageResource(R.drawable.brush);
            statusSize.setText("Selcted Size : " + paintView.getBrushSize());
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isEraser) {
                    temp[0] = progress;
                    statusSize.setText("Selected Size : " + temp[0]);
                } else {
                    temp[0] = progress;
                    statusSize.setText("Selected Size : " + temp[0]);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!isEraser) {
                    if (temp[0] != 0) {
                        paintView.setBrushSize(temp[0]);
                    }
                    paintView.disableEraser();
                } else {
                    paintView.enableEraser();
                    if (temp[0] != 0) {
                        paintView.setEraserSize(temp[0]);

                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(view);
        builder.show();
    }
}
