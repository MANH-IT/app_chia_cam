package com.chupchia.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chupchia.R;
import com.chupchia.adapters.MemberSelectionAdapter;
import com.chupchia.models.Member;
import com.chupchia.utils.BitmapUtils;
import com.chupchia.utils.OcrUtils;
import com.chupchia.utils.PermissionUtils;
import com.chupchia.utils.SharedPrefManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private static final int REQUEST_CODE_GALLERY = 200;
    private static final String[] REQUIRED_PERMISSIONS = PermissionUtils.getRequiredPermissions();
    
    // ===== CAMERA VARIABLES =====
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Camera camera;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private boolean flashEnabled = false;
    
    // ===== VIEWS =====
    private ImageView ivFlash;
    private ImageView ivClose;
    private ImageView ivGallery;
    private ImageView ivCapture;
    private ImageView ivSwitchCamera;
    private View gridLines;
    private LinearLayout bottomSheet;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    
    // Bill info views
    private ImageView ivCapturedPreview;
    private TextInputEditText etAmount;
    private TextInputEditText etProductName;
    private TextView tvOcr;
    private MaterialButton btnSplitShared;
    private MaterialButton btnSplitHelp;
    private MaterialButton btnSplitAlone;
    private LinearLayout llMembersContainer;
    private RecyclerView rvMembers;
    private MaterialButton btnSubmit;
    
    // ===== VARIABLES =====
    private Uri capturedImageUri;
    private Bitmap capturedBitmap;
    private String selectedSplitType = "shared";
    private MemberSelectionAdapter memberAdapter;
    private List<Member> members = new ArrayList<>();
    private boolean isSubmitting = false;
    private boolean isCameraInitialized = false;
    private ProcessCameraProvider cameraProvider;
    private Vibrator vibrator;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        
        initViews();
        setupBottomSheet();
        setupMembers();
        setupListeners();
        
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Delay camera start slightly to ensure UI is stable on MIUI
        previewView.postDelayed(() -> {
            if (PermissionUtils.hasCameraPermission(this)) {
                if (!isCameraInitialized) {
                    startCamera();
                }
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSIONS);
            }
        }, 500);
    }
    
    /**
     * Initialize views
     */
    private void initViews() {
        previewView = findViewById(R.id.preview_view);
        ivFlash = findViewById(R.id.iv_flash);
        ivClose = findViewById(R.id.iv_close);
        ivGallery = findViewById(R.id.iv_gallery);
        ivCapture = findViewById(R.id.iv_capture);
        ivSwitchCamera = findViewById(R.id.iv_switch_camera);
        gridLines = findViewById(R.id.grid_lines);
        bottomSheet = findViewById(R.id.bottom_sheet);
        
        ivCapturedPreview = findViewById(R.id.iv_captured_preview);
        etAmount = findViewById(R.id.et_amount);
        etProductName = findViewById(R.id.et_product_name);
        tvOcr = findViewById(R.id.tv_ocr);
        btnSplitShared = findViewById(R.id.btn_split_shared);
        btnSplitHelp = findViewById(R.id.btn_split_help);
        btnSplitAlone = findViewById(R.id.btn_split_alone);
        llMembersContainer = findViewById(R.id.ll_members_container);
        rvMembers = findViewById(R.id.rv_members);
        btnSubmit = findViewById(R.id.btn_submit);
    }
    
    /**
     * Setup bottom sheet behavior
     */
    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setSkipCollapsed(true);
    }
    
    /**
     * Setup fake members data
     */
    private void setupMembers() {
        // Clear hardcoded data - a new account should be empty or load from API/DB
        members.clear();
        
        // TODO: Load real members from database or API
        // For now, if empty, the UI will show an empty list or we can add a placeholder
        
        memberAdapter = new MemberSelectionAdapter(members);
        memberAdapter.setOnMemberSelectedListener(selectedIds -> {
            // Update UI if needed
        });
        
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        rvMembers.setAdapter(memberAdapter);
    }
    
    /**
     * Setup button listeners
     */
    private void setupListeners() {
        ivClose.setOnClickListener(v -> finish());
        
        ivFlash.setOnClickListener(v -> toggleFlash());
        
        ivGallery.setOnClickListener(v -> openGallery());
        
        ivCapture.setOnClickListener(v -> takePhoto());
        
        ivSwitchCamera.setOnClickListener(v -> switchCamera());
        
        tvOcr.setOnClickListener(v -> performOcr());
        
        btnSplitShared.setOnClickListener(v -> selectSplitType("shared"));
        btnSplitHelp.setOnClickListener(v -> selectSplitType("help"));
        btnSplitAlone.setOnClickListener(v -> selectSplitType("alone"));
        
        btnSubmit.setOnClickListener(v -> submitBill());
        
        ivCapture.setOnLongClickListener(v -> {
            showGridLinesTemporarily();
            return true;
        });
    }
    
    /**
     * Start camera using CameraX
     */
    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                if (cameraProvider != null) {
                    cameraProvider.unbindAll();
                }
                cameraProvider = cameraProviderFuture.get();
                
                // Set implementation mode to COMPATIBLE (uses TextureView) for better compatibility
                previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
                
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                
                imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setFlashMode(flashEnabled ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF)
                    .build();
                
                CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build();
                
                try {
                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                    // ✅ Mark as initialized ONLY after successful bind
                    isCameraInitialized = true;
                } catch (Exception e) {
                    // Fallback to any available camera if back camera fails
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                    isCameraInitialized = true;
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khởi tạo Camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    /**
     * Toggle flash
     */
    private void toggleFlash() {
        flashEnabled = !flashEnabled;
        ivFlash.setImageResource(flashEnabled ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
        
        if (imageCapture != null) {
            imageCapture.setFlashMode(flashEnabled ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF);
        }
    }
    
    /**
     * Switch camera
     */
    private void switchCamera() {
        lensFacing = lensFacing == CameraSelector.LENS_FACING_BACK 
            ? CameraSelector.LENS_FACING_FRONT 
            : CameraSelector.LENS_FACING_BACK;
        startCamera();
    }
    
    /**
     * Open gallery
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }
    
    /**
     * Take photo
     */
    private void takePhoto() {
        if (imageCapture == null || !isCameraInitialized) {
            Toast.makeText(this, "Camera chưa sẵn sàng, vui lòng chờ...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for storage permission before saving
        if (!PermissionUtils.hasStoragePermission(this)) {
            PermissionUtils.requestStoragePermission(this);
            return;
        }
        
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
        
        ivCapture.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .withEndAction(() -> {
                ivCapture.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();
        
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "CHIACAM_" + timestamp + ".jpg";
        
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ChiaCam");
        }
        
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
            getContentResolver(),
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build();
        
        imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                capturedImageUri = outputFileResults.getSavedUri();
                runOnUiThread(() -> {
                    try {
                        loadCapturedImage();
                        showBillInfoBottomSheet();
                        // Auto-trigger OCR for better UX
                        performOcr();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(CameraActivity.this, R.string.camera_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            
            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> {
                    Toast.makeText(CameraActivity.this, "Chụp ảnh thất bại", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * Load captured image
     */
    private void loadCapturedImage() throws IOException {
        if (capturedImageUri == null) return;
        capturedBitmap = BitmapUtils.decodeBitmapFromUri(getContentResolver(), capturedImageUri, 300, 300);
        
        runOnUiThread(() -> {
            Glide.with(this)
                .load(capturedImageUri)
                .centerCrop()
                .into(ivCapturedPreview);
        });
    }
    
    /**
     * Show bottom sheet
     */
    private void showBillInfoBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        selectSplitType("shared");
    }
    
    /**
     * Select split type
     */
    private void selectSplitType(String type) {
        selectedSplitType = type;
        
        btnSplitShared.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_light));
        btnSplitHelp.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_light));
        btnSplitAlone.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_light));
        
        switch (type) {
            case "shared":
                btnSplitShared.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
                llMembersContainer.setVisibility(View.VISIBLE);
                memberAdapter.setSelectionMode("multiple");
                break;
            case "help":
                btnSplitHelp.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
                llMembersContainer.setVisibility(View.VISIBLE);
                memberAdapter.setSelectionMode("single");
                break;
            case "alone":
                btnSplitAlone.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
                llMembersContainer.setVisibility(View.GONE);
                break;
        }
    }
    
    /**
     * Perform OCR
     */
    private void performOcr() {
        if (capturedBitmap == null) {
            Toast.makeText(this, R.string.camera_ocr_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        
        tvOcr.setEnabled(false);
        tvOcr.setText("Đang đọc...");
        
        OcrUtils.extractAmountFromBitmap(capturedBitmap, new OcrUtils.OcrCallback() {
            @Override
            public void onSuccess(String amount) {
                runOnUiThread(() -> {
                    etAmount.setText(amount);
                    String message = String.format(getString(R.string.camera_ocr_success), amount);
                    Toast.makeText(CameraActivity.this, message, Toast.LENGTH_SHORT).show();
                    tvOcr.setEnabled(true);
                    tvOcr.setText(R.string.camera_ocr);
                });
            }
            
            @Override
            public void onFailure() {
                runOnUiThread(() -> {
                    Toast.makeText(CameraActivity.this, R.string.camera_ocr_failed, Toast.LENGTH_SHORT).show();
                    tvOcr.setEnabled(true);
                    tvOcr.setText(R.string.camera_ocr);
                });
            }
        });
    }
    
    /**
     * Show grid lines temporarily
     */
    private void showGridLinesTemporarily() {
        gridLines.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            gridLines.setVisibility(View.GONE);
        }, 2000);
    }
    
    /**
     * Submit bill
     */
    private void submitBill() {
        if (isSubmitting) return;
        
        final String amountValue = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountValue)) {
            Toast.makeText(this, R.string.camera_amount_error, Toast.LENGTH_SHORT).show();
            etAmount.requestFocus();
            return;
        }
        
        String tempProductName = etProductName.getText().toString().trim();
        if (TextUtils.isEmpty(tempProductName)) {
            tempProductName = "Hóa đơn";
        }
        final String finalProductName = tempProductName;
        
        List<String> selectedMemberIds = memberAdapter.getSelectedIds();
        if ((selectedSplitType.equals("shared") || selectedSplitType.equals("help")) && selectedMemberIds.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn thành viên", Toast.LENGTH_SHORT).show();
            return;
        }
        
        isSubmitting = true;
        btnSubmit.setText(R.string.camera_submitting);
        btnSubmit.setEnabled(false);
        btnSubmit.setAlpha(0.6f);
        
        // Real submission to Room Database
        new Thread(() -> {
            try {
                long billAmount = Long.parseLong(amountValue.replace(".", "").replace(",", ""));
                String billId = "BILL_" + System.currentTimeMillis();
                SharedPrefManager prefs = SharedPrefManager.getInstance(this);
                
                com.chupchia.models.Bill newBill = new com.chupchia.models.Bill(
                    billId,
                    capturedImageUri != null ? capturedImageUri.toString() : "",
                    finalProductName,
                    billAmount,
                    prefs.getUserName(),
                    prefs.getUserId(),
                    prefs.getUserId(),
                    prefs.getUserId(),
                    prefs.getCurrentGroupId(),
                    selectedMemberIds.size(),
                    selectedSplitType,
                    System.currentTimeMillis(),
                    ""
                );
                
                com.chupchia.database.AppDatabase.getInstance(this).billDao().insertBill(newBill);
                
                // Create local notification
                com.chupchia.models.Notification notification = new com.chupchia.models.Notification(
                    "NOTIF_" + System.currentTimeMillis(),
                    com.chupchia.models.Notification.TYPE_NEW_BILL,
                    "Hóa đơn mới",
                    prefs.getUserName() + " vừa thêm hóa đơn: " + finalProductName,
                    billId,
                    prefs.getCurrentGroupId(),
                    "",
                    prefs.getUserId(),
                    prefs.getUserName(),
                    System.currentTimeMillis(),
                    false
                );
                com.chupchia.database.AppDatabase.getInstance(this).notificationDao().insert(notification);
                
                runOnUiThread(() -> {
                    isSubmitting = false;
                    btnSubmit.setText(R.string.camera_submit);
                    btnSubmit.setEnabled(true);
                    btnSubmit.setAlpha(1f);
                    
                    Toast.makeText(CameraActivity.this, R.string.camera_submit_success, Toast.LENGTH_SHORT).show();
                    
                    // Trigger refresh in MainActivity/FeedFragment if possible
                    Intent intent = new Intent("com.chupchia.ACTION_NEW_BILL");
                    sendBroadcast(intent);
                    
                    finish();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    isSubmitting = false;
                    btnSubmit.setText(R.string.camera_submit);
                    btnSubmit.setEnabled(true);
                    btnSubmit.setAlpha(1f);
                    Toast.makeText(CameraActivity.this, "Lỗi khi lưu hóa đơn", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null) {
            capturedImageUri = data.getData();
            if (capturedImageUri != null) {
                try {
                    loadCapturedImage();
                    showBillInfoBottomSheet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private boolean allPermissionsGranted() {
        return PermissionUtils.hasAllPermissions(this);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!isCameraInitialized) {
                    startCamera();
                    isCameraInitialized = true;
                }
            } else {
                // Check if user denied permanently (Never ask again)
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Yêu cầu quyền Camera")
                        .setMessage("Bạn đã từ chối quyền Camera. Vui lòng vào Cài đặt để bật quyền này thủ công cho ứng dụng.")
                        .setPositiveButton("Vào Cài đặt", (dialog, which) -> {
                            PermissionUtils.openAppSettings(this);
                        })
                        .setNegativeButton("Thoát", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                } else {
                    Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            cameraProvider = null;
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
