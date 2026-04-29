package com.chupchia.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.chupchia.R;
import com.chupchia.adapters.ContactInviteAdapter;
import com.chupchia.models.Contact;
import com.chupchia.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class InviteMembersActivity extends AppCompatActivity {

    // ===== CONSTANTS =====
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 100;
    private static final int PERMISSION_REQUEST_SEND_SMS = 101;

    // ===== VIEWS =====
    private Toolbar toolbar;
    private TextView tvInviteLink;
    private View ivCopyLink;
    private LinearLayout llShareZalo;
    private LinearLayout llShareMessenger;
    private LinearLayout llShareSms;
    private LinearLayout llShareTelegram;
    private MaterialButton btnSyncContacts;
    private RecyclerView rvContacts;
    private TextView tvNoContacts;
    private EditText etPhoneNumber;
    private MaterialButton btnInvitePhone;

    // ===== VARIABLES =====
    private String groupId;
    private String groupName;
    private String inviteCode;
    private String fullInviteLink;
    private String currentUserName;
    private List<Contact> contactList = new ArrayList<>();
    private ContactInviteAdapter contactAdapter;
    private boolean isSyncing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_members);

        initViews();
        setupToolbar();
        loadGroupData();
        setupInviteLink();
        setupListeners();
    }

    /**
     * Initialize views
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvInviteLink = findViewById(R.id.tv_invite_link);
        ivCopyLink = findViewById(R.id.iv_copy_link);
        llShareZalo = findViewById(R.id.ll_share_zalo);
        llShareMessenger = findViewById(R.id.ll_share_messenger);
        llShareSms = findViewById(R.id.ll_share_sms);
        llShareTelegram = findViewById(R.id.ll_share_telegram);
        btnSyncContacts = findViewById(R.id.btn_sync_contacts);
        rvContacts = findViewById(R.id.rv_contacts);
        tvNoContacts = findViewById(R.id.tv_no_contacts);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        btnInvitePhone = findViewById(R.id.btn_invite_phone);
    }

    /**
     * Setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Load group data from intent
     */
    private void loadGroupData() {
        groupId = getIntent().getStringExtra("group_id");
        groupName = getIntent().getStringExtra("group_name");
        inviteCode = getIntent().getStringExtra("invite_code");

        if (groupId == null) {
            // Demo data
            groupId = "group_1";
            groupName = "Nhà mình";
            inviteCode = "ABC123";
        }

        currentUserName = SharedPrefManager.getInstance(this).getUserName();
        if (TextUtils.isEmpty(currentUserName)) {
            currentUserName = "Một người bạn";
        }

        fullInviteLink = "https://chia.cam/join/" + inviteCode;
    }

    /**
     * Setup invite link display
     */
    private void setupInviteLink() {
        tvInviteLink.setText(fullInviteLink);
    }

    /**
     * Setup button listeners
     */
    private void setupListeners() {
        ivCopyLink.setOnClickListener(v -> copyInviteLink());

        llShareZalo.setOnClickListener(v -> shareViaZalo());
        llShareMessenger.setOnClickListener(v -> shareViaMessenger());
        llShareSms.setOnClickListener(v -> shareViaSms());
        llShareTelegram.setOnClickListener(v -> shareViaTelegram());

        btnSyncContacts.setOnClickListener(v -> syncContacts());

        btnInvitePhone.setOnClickListener(v -> inviteByPhoneNumber());
    }

    /**
     * Copy invite link to clipboard
     */
    private void copyInviteLink() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("invite_link", fullInviteLink);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.invite_copy_success, Toast.LENGTH_SHORT).show();
    }

    /**
     * Get invite message text
     */
    private String getInviteMessage() {
        return String.format(getString(R.string.invite_message_template),
                currentUserName, groupName, fullInviteLink, inviteCode);
    }

    /**
     * Share via Zalo
     */
    private void shareViaZalo() {
        String message = getInviteMessage();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://zalo.me/share?text=" + Uri.encode(message)));
        
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.invite_app_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Share via Messenger
     */
    private void shareViaMessenger() {
        String message = getInviteMessage();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setPackage("com.facebook.orca");
        
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.invite_app_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Share via SMS
     */
    private void shareViaSms() {
        String message = getInviteMessage();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("sms:"));
        intent.putExtra("sms_body", message);
        startActivity(intent);
    }

    /**
     * Share via Telegram
     */
    private void shareViaTelegram() {
        String message = getInviteMessage();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, message);
        intent.setPackage("org.telegram.messenger");
        
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.invite_app_not_installed, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sync contacts from phone
     */
    private void syncContacts() {
        if (isSyncing) return;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_REQUEST_READ_CONTACTS);
        } else {
            performSyncContacts();
        }
    }

    /**
     * Perform actual contact syncing
     */
    private void performSyncContacts() {
        isSyncing = true;
        btnSyncContacts.setText(R.string.invite_syncing);
        btnSyncContacts.setEnabled(false);

        // Simulate loading delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            List<Contact> contacts = readContactsFromPhone();
            filterExistingUsers(contacts);
            
            isSyncing = false;
            btnSyncContacts.setText(R.string.invite_sync_contacts);
            btnSyncContacts.setEnabled(true);
        }, 500);
    }

    /**
     * Read contacts from phone
     */
    private List<Contact> readContactsFromPhone() {
        List<Contact> contacts = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null && cursor.getCount() > 0) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                String phone = cursor.getString(phoneIndex);

                if (!TextUtils.isEmpty(phone)) {
                    // Clean phone number
                    phone = phone.replaceAll("[^0-9]", "");
                    // Only get Vietnamese phone numbers (starting with 0, length 10-11)
                    if (phone.startsWith("0") && (phone.length() == 10 || phone.length() == 11)) {
                        contacts.add(new Contact(name, phone, false));
                    }
                }
            }
            cursor.close();
        }

        return contacts;
    }

    /**
     * Filter contacts to show only those not in group yet
     */
    private void filterExistingUsers(List<Contact> contacts) {
        contactList.clear();
        
        // TODO: Call API to check which contacts are already members or using Chia Cam
        // For demo, simulate with random status
        for (int i = 0; i < contacts.size() && i < 20; i++) {
            Contact contact = contacts.get(i);
            // Simulate: 70% are not members, 30% are already members
            boolean isMember = (i % 3 == 0);
            contact.setMember(isMember);
            contact.setExisting(!isMember);
            contactList.add(contact);
        }

        setupContactAdapter();
        
        if (contactList.isEmpty()) {
            tvNoContacts.setVisibility(View.VISIBLE);
            rvContacts.setVisibility(View.GONE);
        } else {
            tvNoContacts.setVisibility(View.GONE);
            rvContacts.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Setup contact adapter
     */
    private void setupContactAdapter() {
        contactAdapter = new ContactInviteAdapter(this);
        contactAdapter.setContacts(contactList);
        contactAdapter.setOnInviteClickListener((contact, position) -> {
            inviteByPhone(contact.getPhoneNumber(), contact.getName(), position);
        });

        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(contactAdapter);
    }

    /**
     * Invite by phone number from manual input
     */
    private void inviteByPhoneNumber() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, R.string.invite_phone_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Clean phone number
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
        if (!phoneNumber.startsWith("0") || (phoneNumber.length() != 10 && phoneNumber.length() != 11)) {
            Toast.makeText(this, R.string.invite_phone_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        
        inviteByPhone(phoneNumber, phoneNumber, -1);
        etPhoneNumber.setText("");
    }

    /**
     * Invite by phone number
     */
    private void inviteByPhone(String phoneNumber, String name, int position) {
        String message = getInviteMessage();
        
        try {
            // Try to send SMS directly
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(this, String.format(getString(R.string.invite_send_success), name), Toast.LENGTH_SHORT).show();
                
                if (position >= 0 && contactAdapter != null) {
                    contactAdapter.updateInviteStatus(position, true);
                }
            } else {
                // Request permission or fallback to SMS app
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_SEND_SMS);
                } else {
                    // Fallback: open SMS app
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("sms:" + phoneNumber));
                    intent.putExtra("sms_body", message);
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            // Fallback: open SMS app
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("sms:" + phoneNumber));
            intent.putExtra("sms_body", message);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performSyncContacts();
            } else {
                showPermissionDeniedDialog();
            }
        } else if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Retry sending SMS
                String phoneNumber = etPhoneNumber.getText().toString().trim();
                if (!TextUtils.isEmpty(phoneNumber)) {
                    inviteByPhoneNumber();
                }
            } else {
                Toast.makeText(this, R.string.invite_sms_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Show permission denied dialog
     */
    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_contacts_title)
                .setMessage(R.string.permission_contacts_message)
                .setPositiveButton(R.string.permission_settings, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
