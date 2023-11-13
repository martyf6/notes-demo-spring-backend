package com.jfahey.notesdemo.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;

public class NoteRequest {

    @NotNull 
    @Length(max = 500)
    private String title;

    private String content;

    public NoteRequest() {}

    public NoteRequest(String title, String content){
        this.title = title;
        this.content = content;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getContent(){
        return this.content;
    }

    public void setContent(String content){
        this.content = content;
    }
    
}
