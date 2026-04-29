package com.chupchia.dialogs;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.chupchia.R;
import com.chupchia.models.Member;

import java.util.ArrayList;
import java.util.List;

public class AddMemberDialog extends BottomSheetDialog {

    private Context context;
    private String groupId;
    private OnMemberAddedListener listener;
    private List<Contact> contacts = new ArrayList<>();
    private List<Contact> filteredContacts = new ArrayList<>();
    private List<Contact> selectedContacts = new ArrayList<>();
    private ContactAdapter adapter;
    
    private EditText etSearch;
    private RecyclerView rvContacts;
    private TextView tvEmptyContacts;
    private LinearLayout llPermissionRequired;
    private MaterialButton btnRequestPermission;
    private TextView tvSelectedCount;
    private MaterialButton btnCancel;
    private MaterialButton btnAdd;
    
    public static final int PERMISSION_REQUEST_READ_CONTACTS = 200;
    
    public interface OnMemberAddedListener {
        void onMembersAdded(List<Member> members);
    }
    
    public AddMemberDialog(@NonNull Context context, String groupId, OnMemberAddedListener listener) {
        super(context);
        this.context = context;
        this.groupId = groupId;
        this.listener = listener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_member);
        
        initViews();
        setupListeners();
        checkContactPermission();
    }
    
    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        rvContacts = findViewById(R.id.rv_contacts);
        tvEmptyContacts = findViewById(R.id.tv_empty_contacts);
        llPermissionRequired = findViewById(R.id.ll_permission_required);
        btnRequestPermission = findViewById(R.id.btn_request_permission);
        tvSelectedCount = findViewById(R.id.tv_selected_count);
        btnCancel = findViewById(R.id.btn_cancel);
        btnAdd = findViewById(R.id.btn_add);
        
        rvContacts.setLayoutManager(new LinearLayoutManager(context));
        adapter = new ContactAdapter();
        rvContacts.setAdapter(adapter);
    }
    
    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        
        btnAdd.setOnClickListener(v -> {
            if (!selectedContacts.isEmpty()) {
                List<Member> members = new ArrayList<>();
                for (Contact contact : selectedContacts) {
                    Member member = new Member();
                    member.setId(contact.getPhoneNumber()); // Use phone as temp ID
                    member.setName(contact.getName());
                    member.setPhoneNumber(contact.getPhoneNumber());
                    member.setRole("member");
                    member.setSelected(true);
                    members.add(member);
                }
                if (listener != null) {
                    listener.onMembersAdded(members);
                }
                dismiss();
            } else {
                Toast.makeText(context, "Vui lòng chọn ít nhất một thành viên", Toast.LENGTH_SHORT).show();
            }
        });
        
        if (btnRequestPermission != null) {
            btnRequestPermission.setOnClickListener(v -> {
                ActivityCompat.requestPermissions((Activity) context, 
                    new String[]{Manifest.permission.READ_CONTACTS}, 
                    PERMISSION_REQUEST_READ_CONTACTS);
            });
        }
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void checkContactPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) 
                == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
            llPermissionRequired.setVisibility(View.GONE);
            rvContacts.setVisibility(View.VISIBLE);
        } else {
            llPermissionRequired.setVisibility(View.VISIBLE);
            rvContacts.setVisibility(View.GONE);
        }
    }
    
    private void loadContacts() {
        contacts.clear();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );
        
        if (cursor != null && cursor.getCount() > 0) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                String phoneNumber = cursor.getString(numberIndex);
                
                // Clean phone number
                phoneNumber = phoneNumber.replaceAll("[^0-9+]", "");
                
                Contact contact = new Contact(name, phoneNumber);
                contacts.add(contact);
            }
            cursor.close();
        }
        
        filteredContacts.clear();
        filteredContacts.addAll(contacts);
        adapter.notifyDataSetChanged();
        
        if (filteredContacts.isEmpty()) {
            tvEmptyContacts.setVisibility(View.VISIBLE);
            rvContacts.setVisibility(View.GONE);
        } else {
            tvEmptyContacts.setVisibility(View.GONE);
            rvContacts.setVisibility(View.VISIBLE);
        }
    }
    
    private void filterContacts(String query) {
        filteredContacts.clear();
        if (query.isEmpty()) {
            filteredContacts.addAll(contacts);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Contact contact : contacts) {
                if (contact.getName().toLowerCase().contains(lowerQuery) ||
                    contact.getPhoneNumber().contains(query)) {
                    filteredContacts.add(contact);
                }
            }
        }
        adapter.notifyDataSetChanged();
        
        if (filteredContacts.isEmpty()) {
            tvEmptyContacts.setVisibility(View.VISIBLE);
            rvContacts.setVisibility(View.GONE);
        } else {
            tvEmptyContacts.setVisibility(View.GONE);
            rvContacts.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateSelectedCount() {
        tvSelectedCount.setText(String.format("Đã chọn %d thành viên", selectedContacts.size()));
        btnAdd.setText(String.format("Thêm (%d)", selectedContacts.size()));
        btnAdd.setEnabled(!selectedContacts.isEmpty());
    }
    
    private class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
        
        @NonNull
        @Override
        public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_contact_select, parent, false);
            return new ContactViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            Contact contact = filteredContacts.get(position);
            boolean isSelected = selectedContacts.contains(contact);
            
            holder.tvName.setText(contact.getName());
            holder.tvPhone.setText(contact.getPhoneNumber());
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setChecked(isSelected);
            
            holder.itemView.setOnClickListener(v -> {
                if (selectedContacts.contains(contact)) {
                    selectedContacts.remove(contact);
                } else {
                    selectedContacts.add(contact);
                }
                notifyItemChanged(position);
                updateSelectedCount();
            });
            
            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !selectedContacts.contains(contact)) {
                    selectedContacts.add(contact);
                } else if (!isChecked && selectedContacts.contains(contact)) {
                    selectedContacts.remove(contact);
                }
                updateSelectedCount();
            });
        }
        
        @Override
        public int getItemCount() {
            return filteredContacts.size();
        }
        
        class ContactViewHolder extends RecyclerView.ViewHolder {
            CheckBox cbSelect;
            TextView tvName;
            TextView tvPhone;
            
            ContactViewHolder(@NonNull View itemView) {
                super(itemView);
                cbSelect = itemView.findViewById(R.id.cb_select);
                tvName = itemView.findViewById(R.id.tv_name);
                tvPhone = itemView.findViewById(R.id.tv_phone);
            }
        }
    }
    
    private static class Contact {
        private String name;
        private String phoneNumber;
        
        Contact(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
        }
        
        String getName() { return name; }
        String getPhoneNumber() { return phoneNumber; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Contact contact = (Contact) o;
            return phoneNumber.equals(contact.phoneNumber);
        }
        
        @Override
        public int hashCode() {
            return phoneNumber.hashCode();
        }
    }
}
