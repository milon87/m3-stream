package org.m3.view;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.m3.R;
import org.m3.model.Video;
import org.m3.xml.DataHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
 

public class EmailView extends Activity {
	private Video video;
	private Button send;
	private Button cancel;
    private EditText address, subject, emailtext;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email);
        send=(Button) findViewById(R.id.btnSend);
        cancel=(Button) findViewById(R.id.btnCancel);
        address=(EditText) findViewById(R.id.address);
        subject=(EditText) findViewById(R.id.subject);
        emailtext=(EditText) findViewById(R.id.text);
       
        final int categoryId = this.getIntent().getExtras().getInt("categoryId");
        final int videoId = this.getIntent().getExtras().getInt("videoId");
        video = DataHandler.getCategories().get(categoryId).getVideos().get(videoId);
        
        subject.setText(video.getName());
        
        StringBuilder text = new StringBuilder(128);
        text.append(this.getResources().getString(R.string.emailSignature)).append("\n\n");
        text.append(video.getUrl());
        emailtext.setText(text.toString());
        send.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");//"[a-zA-Z]*[0-9]*@[a-zA-Z]*.[a-zA-Z]*");//
        		Matcher m = p.matcher(address.getText().toString());
	    		if (m.matches()) {
	    	    	  final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	                  emailIntent.setType("plain/text");
	                  emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ address.getText().toString()});
	                  emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject.getText());
	                  emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, emailtext.getText());
	                  EmailView.this.startActivity(Intent.createChooser(emailIntent, EmailView.this.getResources().getString(R.string.sendMail)));
	                  finish();	  
	    	    } else {
	    	    	AlertDialog.Builder alertbox = new AlertDialog.Builder(EmailView.this);
	                alertbox.setMessage(EmailView.this.getResources().getString(R.string.invalidEmail));
	                alertbox.setNeutralButton(EmailView.this.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface arg0, int arg1) {
	                    }
	                });
	                alertbox.show();
	    	    }
        	}
        });
        
        cancel.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		finish();	  
        	}
        });
    }
}