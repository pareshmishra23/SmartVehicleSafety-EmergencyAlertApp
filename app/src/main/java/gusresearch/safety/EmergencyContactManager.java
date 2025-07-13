package gusresearch.safety;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class EmergencyContactManager {

    private static final String PREF_NAME = "emergency_contacts";
    private static final String KEY_CONTACTS = "contacts";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public EmergencyContactManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addContact(String name, String phoneNumber) {
        List<Contact> contacts = getContacts();
        contacts.add(new Contact(name, phoneNumber));
        saveContacts(contacts);
    }

    public List<Contact> getContacts() {
        String json = sharedPreferences.getString(KEY_CONTACTS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Contact>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveContacts(List<Contact> contacts) {
        String json = gson.toJson(contacts);
        sharedPreferences.edit().putString(KEY_CONTACTS, json).apply();
    }

    public static class Contact {
        public String name;
        public String phoneNumber;

        public Contact(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
        }
    }
}

