package com.chupchia.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.chupchia.models.Member;

import java.util.ArrayList;
import java.util.List;

public class ContactUtils {

    /**
     * Lấy danh bạ từ điện thoại và chuyển đổi thành đối tượng Member
     */
    public static List<Member> getPhoneContacts(Context context) {
        List<Member> contacts = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        
        try {
            Cursor cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    },
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            );

            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                int count = 0;
                while (cursor.moveToNext() && count < 50) { // Limit to 50 for performance
                    String name = cursor.getString(nameIndex);
                    String phone = cursor.getString(phoneIndex);

                    if (!TextUtils.isEmpty(phone)) {
                        // Tạo ID duy nhất từ số điện thoại
                        String id = "contact_" + phone.replaceAll("[^0-9]", "");
                        contacts.add(new Member(id, name, "", "Danh bạ"));
                        count++;
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contacts;
    }
}
