package com.palindromicstudios.visionmail;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Natan on 2/24/2015.
 */
public class Message implements Parcelable {
    private String content, name, phone, date;
    private int person, thread, type;

    public Message() {
        this("", 0, "");
    }

    public Message(Parcel in) {
        this.name = in.readString();
        this.phone = in.readString();
        this.date = in.readString();
        this.content = in.readString();
        this.person = in.readInt();
        this.thread = in.readInt();
        this.type = in.readInt();
    }


    public Message(String content, int person, String date) {
        this.content = content;
        this.person = person;
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPerson() {
        return person;
    }

    public void setPerson(int person) {
        this.person = person;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getThread() {
        return thread;
    }

    public void setThread(int thread) {
        this.thread = thread;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(date);
        dest.writeString(content);
        dest.writeInt(person);
        dest.writeInt(thread);
        dest.writeInt(type);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

}
