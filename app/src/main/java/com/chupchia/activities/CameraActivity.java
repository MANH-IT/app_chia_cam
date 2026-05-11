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
import android.widget.ProgressBar;
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
    
    // ===== BIẾN CAMERA =====
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Camera camera;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private int flashMode = ImageCapture.FLASH_MODE_AUTO;
    
    // ===== GIAO DIỆN =====
    private ImageView ivFlash;
    private ImageView ivClose;
    private ImageView ivGallery;
    private ImageView ivCapture;
    private ImageView ivSwitchCamera;
    private View gridLines;
    private LinearLayout bottomSheet;
    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    
    // Giao diện thông tin hóa đơn
    private ImageView ivCapturedPreview;
    private TextInputEditText etAmount;
    private TextInputEditText etProductName;
    private TextView tvOcr;
    private MaterialButton btnSplitShared;
    private MaterialButton btnSplitHelp;
    private MaterialButton btnSplitAlone;
    private LinearLayout llMembersContainer;
    private RecyclerView rvMembers;
    private ProgressBar pbMembers;
    private TextView tvEmptyMembers;
    private TextInputEditText etSearchMembers;
    private MaterialButton btnSubmit;
    
    // ===== BIẾN =====
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
        // Trì hoãn khởi động camera một chút để đảm bảo giao diện ổn định trên MIUI
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
     * Khởi tạo giao diện
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
        pbMembers = findViewById(R.id.pb_members);
        tvEmptyMembers = findViewById(R.id.tv_empty_members);
        etSearchMembers = findViewById(R.id.et_search_members);
        btnSubmit = findViewById(R.id.btn_submit);

        // Đồng bộ biểu tượng flash ban đầu
        ivFlash.setImageResource(R.drawable.ic_flash_auto);
    }
    
    /**
     * Cấu hình hành vi bottom sheet
     */
    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setSkipCollapsed(true);
    }
    
    /**
     * Cấu hình dữ liệu thành viên
     */
    private void setupMembers() {
        members.clear();
        pbMembers.setVisibility(View.VISIBLE);
        tvEmptyMembers.setVisibility(View.GONE);
        
        // Nếu có quyền danh bạ, tải danh bạ thật
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) 
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            
            // Tải danh bạ trên luồng nền
            new Thread(() -> {
                List<com.chupchia.models.Member> contacts = com.chupchia.utils.ContactUtils.getPhoneContacts(this);
                runOnUiThread(() -> {
                    pbMembers.setVisibility(View.GONE);
                    members.clear();
                    if (!contacts.isEmpty()) {
                        members.addAll(contacts);
                        tvEmptyMembers.setVisibility(View.GONE);
                    } else {
                        tvEmptyMembers.setVisibility(View.VISIBLE);
                    }
                    updateMemberAdapter();
                });
            }).start();
        } else {
            pbMembers.setVisibility(View.GONE);
            tvEmptyMembers.setVisibility(View.VISIBLE);
            tvEmptyMembers.setText("Vui lòng cấp quyền danh bạ để chọn thành viên");
            updateMemberAdapter();
        }
    }

    private void updateMemberAdapter() {
        if (memberAdapter == null) {
            memberAdapter = new MemberSelectionAdapter(members);
            memberAdapter.setOnMemberSelectedListener(selectedIds -> {
                // Cập nhật giao diện nếu cần
            });
            rvMembers.setLayoutManager(new LinearLayoutManager(this));
            rvMembers.setAdapter(memberAdapter);
        } else {
            memberAdapter.notifyDataSetChanged();
        }
    }
    
    /**
     * Cấu hình các sự kiện nút bấm
     */
    private void setupListeners() {
        ivClose.setOnClickListener(v -> finish());
        
        ivFlash.setOnClickListener(v -> toggleFlash());
        
        ivGallery.setOnClickListener(v -> openGallery());
        
        etSearchMembers.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (memberAdapter != null) {
                    memberAdapter.filter(s.toString());
                    tvEmptyMembers.setVisibility(memberAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        etAmount.addTextChangedListener(new android.text.TextWatcher() {
            private String current = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    etAmount.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[^0-9]", "");
                    if (!cleanString.isEmpty()) {
                        long parsed = Long.parseLong(cleanString);
                        String formatted = com.chupchia.utils.CurrencyUtils.formatNumber(parsed);
                        current = formatted;
                        etAmount.setText(formatted);
                        etAmount.setSelection(formatted.length());
                    } else {
                        current = "";
                        etAmount.setText("");
                    }

                    etAmount.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        previewView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                focusOnPoint(event.getX(), event.getY());
                return true;
            }
            return false;
        });
        
        btnSubmit.setOnClickListener(v -> submitBill());
        
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
     * Khởi động camera sử dụng CameraX
     */
    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                if (cameraProvider != null) {
                    cameraProvider.unbindAll();
                }
                cameraProvider = cameraProviderFuture.get();
                
                // Đặt chế độ triển khai COMPATIBLE (dùng TextureView) để tương thích tốt hơn
                previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
                
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                
                imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setFlashMode(flashMode)
                    .build();
                
                CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build();
                
                try {
                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                    // ✅ Đánh dấu đã khởi tạo CHỈ SAU KHI bind thành công
                    isCameraInitialized = true;
                } catch (Exception e) {
                    // Dự phòng: dùng camera mặc định nếu camera sau thất bại
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
     * Chuyển đổi đèn flash (Vòng lặp: Tự động -> Bật -> Tắt)
     */
    private void toggleFlash() {
        flashMode = (flashMode + 1) % 3;
        
        int iconRes;
        switch (flashMode) {
            case ImageCapture.FLASH_MODE_ON:
                iconRes = R.drawable.ic_flash_on;
                Toast.makeText(this, "Đèn Flash: Bật", Toast.LENGTH_SHORT).show();
                break;
            case ImageCapture.FLASH_MODE_OFF:
                iconRes = R.drawable.ic_flash_off;
                Toast.makeText(this, "Đèn Flash: Tắt", Toast.LENGTH_SHORT).show();
                break;
            default:
                iconRes = R.drawable.ic_flash_auto;
                Toast.makeText(this, "Đèn Flash: Tự động", Toast.LENGTH_SHORT).show();
                break;
        }
        
        ivFlash.setImageResource(iconRes);
        if (imageCapture != null) {
            imageCapture.setFlashMode(flashMode);
        }
    }
    
    private void focusOnPoint(float x, float y) {
        if (camera == null) return;
        
        androidx.camera.core.CameraControl cameraControl = camera.getCameraControl();
        androidx.camera.core.CameraInfo cameraInfo = camera.getCameraInfo();
        
        androidx.camera.core.MeteringPointFactory factory = previewView.getMeteringPointFactory();
        androidx.camera.core.MeteringPoint point = factory.createPoint(x, y);
        androidx.camera.core.FocusMeteringAction action = new androidx.camera.core.FocusMeteringAction.Builder(point, androidx.camera.core.FocusMeteringAction.FLAG_AF)
                .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        
        cameraControl.startFocusAndMetering(action);
        
        // Hiệu ứng hình ảnh khi lấy nét
        showFocusCircle(x, y);
    }
    
    private void showFocusCircle(float x, float y) {
        // Hiển thị vòng tròn lấy nét đơn giản nếu cần,
        // hiện tại chỉ dùng toast hoặc logic animation đơn giản
    }
    
    /**
     * Chuyển đổi camera
     */
    private void switchCamera() {
        lensFacing = lensFacing == CameraSelector.LENS_FACING_BACK 
            ? CameraSelector.LENS_FACING_FRONT 
            : CameraSelector.LENS_FACING_BACK;
        startCamera();
    }
    
    /**
     * Mở thư viện ảnh
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }
    
    /**
     * Chụp ảnh
     */
    private void takePhoto() {
        if (imageCapture == null || !isCameraInitialized) {
            Toast.makeText(this, "Camera chưa sẵn sàng, vui lòng chờ...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra quyền lưu trữ trước khi lưu
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
                
                // Giải mã bitmap trên luồng nền để tránh ANR
                new Thread(() -> {
                    try {
                        Bitmap oldBitmap = capturedBitmap;
                        capturedBitmap = BitmapUtils.decodeBitmapFromUri(getContentResolver(), capturedImageUri, 600, 600);
                        
                        // Thu hồi bitmap cũ nếu có
                        if (oldBitmap != null && !oldBitmap.isRecycled()) {
                            oldBitmap.recycle();
                        }

                        runOnUiThread(() -> {
                            ivCapturedPreview.setImageBitmap(capturedBitmap);
                            showBillInfoBottomSheet();
                            performOcr();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show());
                    }
                }).start();
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
     * Tải ảnh đã chụp
     */
    private void loadCapturedImage() throws IOException {
        if (capturedImageUri == null) return;
        
        // Xử lý này giờ đã được thực hiện trên luồng nền trong onImageSaved và onActivityResult
        // Nhưng để hoàn chỉnh, cập nhật giao diện
        if (capturedBitmap != null) {
            ivCapturedPreview.setImageBitmap(capturedBitmap);
        }
    }
    
    /**
     * Hiển thị bottom sheet
     */
    private void showBillInfoBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        selectSplitType("shared");
    }
    
    /**
     * Chọn kiểu chia
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
     * Thực hiện nhận dạng OCR
     */
    private void performOcr() {
        if (capturedBitmap == null) return;
        
        tvOcr.setEnabled(false);
        tvOcr.setText("Đang đọc...");
        tvOcr.setAlpha(0.6f);
        
        // Hiệu ứng nhịp đập cho OCR
        tvOcr.animate().scaleX(1.1f).scaleY(1.1f).setDuration(400).setInterpolator(new android.view.animation.CycleInterpolator(2)).start();
        
        OcrUtils.extractAmountFromBitmap(capturedBitmap, new OcrUtils.OcrCallback() {
            @Override
            public void onSuccess(String amount) {
                runOnUiThread(() -> {
                    tvOcr.setEnabled(true);
                    tvOcr.setText(R.string.camera_ocr);
                    tvOcr.setAlpha(1.0f);
                    
                    if (amount != null) {
                        etAmount.setText(amount);
                        String successMsg = getString(R.string.camera_ocr_success, amount);
                        Toast.makeText(CameraActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure() {
                runOnUiThread(() -> {
                    tvOcr.setEnabled(true);
                    tvOcr.setText(R.string.camera_ocr);
                    tvOcr.setAlpha(1.0f);
                    Toast.makeText(CameraActivity.this, R.string.camera_ocr_failed, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * Hiển thị lưới tạm thời
     */
    private void showGridLinesTemporarily() {
        gridLines.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            gridLines.setVisibility(View.GONE);
        }, 2000);
    }
    
    /**
     * Gửi hóa đơn
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
        
        // Gửi thật vào cơ sở dữ liệu Room
        new Thread(() -> {
            try {
                // Phân tích an toàn số tiền
                String cleanAmount = amountValue.replaceAll("[^\\d]", "");
                if (cleanAmount.isEmpty()) cleanAmount = "0";
                long billAmount = Long.parseLong(cleanAmount);
                
                String billId = "BILL_" + System.currentTimeMillis();
                SharedPrefManager prefs = SharedPrefManager.getInstance(getApplicationContext());
                
                // Tính splitCount dựa trên chế độ
                int splitCount = 1; // Mặc định cho 'alone'
                
                if (selectedSplitType.equals("shared")) {
                    splitCount = selectedMemberIds.size() + 1; // Thành viên + người trả
                } else if (selectedSplitType.equals("help")) {
                    splitCount = selectedMemberIds.size(); // Chỉ thành viên được chọn (thường là 1)
                }
                
                if (splitCount <= 0) splitCount = 1;

                com.chupchia.models.Bill newBill = new com.chupchia.models.Bill(
                    billId,
                    capturedImageUri != null ? capturedImageUri.toString() : "",
                    finalProductName,
                    billAmount,
                    prefs.getUserName(),
                    prefs.getUserId(),
                    prefs.getUserId(),
                    prefs.getUserId(), // Tạm thời cho quản trị viên nhóm
                    prefs.getCurrentGroupId(),
                    splitCount,
                    selectedSplitType,
                    System.currentTimeMillis(),
                    ""
                );
                
                com.chupchia.database.AppDatabase.getInstance(this).billDao().insertBill(newBill);
                
                // Tạo thông báo cục bộ
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
                    
                    // Kích hoạt làm mới trong MainActivity/FeedFragment nếu có thể
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
                new Thread(() -> {
                    try {
                        Bitmap oldBitmap = capturedBitmap;
                        capturedBitmap = BitmapUtils.decodeBitmapFromUri(getContentResolver(), capturedImageUri, 600, 600);
                        
                        // Thu hồi bitmap cũ
                        if (oldBitmap != null && !oldBitmap.isRecycled()) {
                            oldBitmap.recycle();
                        }

                        runOnUiThread(() -> {
                            ivCapturedPreview.setImageBitmap(capturedBitmap);
                            showBillInfoBottomSheet();
                            performOcr();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Lỗi tải ảnh", Toast.LENGTH_SHORT).show());
                    }
                }).start();
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
                }
            } else {
                // Kiểm tra nếu người dùng từ chối vĩnh viễn (Không hỏi lại)
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
        if (capturedBitmap != null && !capturedBitmap.isRecycled()) {
            capturedBitmap.recycle();
            capturedBitmap = null;
        }
    }
}
