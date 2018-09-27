package research.type.keystrokeanalysis;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import research.type.keystrokeanalysis.services.UploadData;

/**
 * Created by Adrija on 08-06-2018.
 */

public class ContextRecordPopUp extends AppCompatActivity {


    RadioButton rb1,rb2,rb3,rb4,rb5,rb6,rb7,rb8,rb11,rb12,rb13;
    String selId1,selId2,selId3,selId4,selConId1,selConId2,selConId3,selConId4;
    int hr1,hr2;
    int time1,time2;

    private RadioGroup radioContextGroup1;
    private RadioButton radioContextButton1;


    private RadioGroup radioConfidenceGroup1;
    private RadioButton radioConfidenceButton1;

    private TextView timing;
    private TextView hour;
    private Button btnRecordContext;

    public static BufferedWriter out;
    int count=0;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        selId1="";selId2="";selId3="";selId4="";
        selConId1="";selConId2="";selConId3="";selConId4="";
        setContentView(R.layout.context_confirmation);
        Date currentTime = Calendar.getInstance().getTime();
        super.onCreate(savedInstanceState);
        int hr= currentTime.getHours();
        timing=(TextView)findViewById(R.id.textView);
        hour=(TextView)findViewById(R.id.textView1);
      /*  String hr2=String.valueOf(currentTime.getHours())+":"+String.valueOf(currentTime.getMinutes())+":"+String.valueOf(currentTime.getSeconds());
       String hr1=(hr-4)+":"+String.valueOf(currentTime.getMinutes())+":"+String.valueOf(currentTime.getSeconds());*/

        if(hr%4==0)
            hr2=hr;
        else
            hr2 = (((24-hr)%4)+hr-4)%24;
        hr1=hr2-4;
        if(hr2==0)
            hr1=20;

        timing.setText("What was your context between\n" +
                hr1+":00:00  and "+hr2+":00:00");
        time1=hr1;
        time2=hr1+1;
        hour.setText("Between "+time1+":00:00  and "+time2+":00:00"+" -");
        count=0;

        rb1 = findViewById(R.id.radioContext11);
        rb2 =findViewById(R.id.radioContext12);
        rb3 = findViewById(R.id.radioContext13);
        rb4 = findViewById(R.id.radioContext14);
        rb5 = findViewById(R.id.radioContext15);
        rb6 = findViewById(R.id.radioContext16);
        rb7 = findViewById(R.id.radioContext17);
        rb8 = findViewById(R.id.radioContext18);
        rb11 =findViewById(R.id.radioConfidence11);
        rb12 = findViewById(R.id.radioConfidence12);
        rb13 = findViewById(R.id.radioConfidence13);

        rb1.setId(1);
        rb2.setId(2);
        rb3.setId(3);
        rb4.setId(4);
        rb5.setId(5);
        rb6.setId(6);
        rb7.setId(7);
        rb8.setId(8);
        rb11.setId(11);
        rb11.setId(12);
        rb11.setId(13);


        this.setFinishOnTouchOutside(false);

        radioContextGroup1 = (RadioGroup) findViewById(R.id.radioContextGroup1);

        radioConfidenceGroup1 = (RadioGroup) findViewById(R.id.radioConfidenceGroup1);

        btnRecordContext = (Button) findViewById(R.id.btnDone1);

        btnRecordContext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                count=count+1;
                int selectedId = radioContextGroup1.getCheckedRadioButtonId();

                int selectedConfidenceId = radioConfidenceGroup1.getCheckedRadioButtonId();

                try {
                    radioContextButton1 = (RadioButton) findViewById(selectedId);
                    String Context1 = getContextc(radioContextButton1);

                    radioConfidenceButton1 = (RadioButton) findViewById(selectedConfidenceId);
                    String Confidence1 = getContextp(radioConfidenceButton1);


                    Intent intent=new Intent();
                    intent.putExtra("Context",Context1);

                    intent.putExtra("Confidence",Confidence1);

                    setResult(2,intent);

                    if(count==1)
                    {
                        selId1=Context1;
                        selConId1=Confidence1;
                        time1=hr1+1;
                        time2=hr1+2;
                        hour.setText("Between "+time1+":00:00  and "+time2+":00:00"+"   ---");
                        Toast.makeText(ContextRecordPopUp.this,"Hour 1 Context Taken", Toast.LENGTH_SHORT).show();
                    }
                    if(count==2) {
                        selId2=Context1;
                        selConId2=Confidence1;
                        time1=hr1+2;
                        time2=hr1+3;
                        hour.setText("Between "+time1+":00:00  and "+time2+":00:00"+"   ---");
                        Toast.makeText(ContextRecordPopUp.this,"Hour 2 Context Taken", Toast.LENGTH_SHORT).show();
                    }
                    if(count==3)
                    {
                        selId3=Context1;
                        selConId3=Confidence1;
                        time1=hr1+3;
                        time2=hr1+4;
                        if(hr1==20)
                            time2=00;
                        hour.setText("Between "+time1+":00:00  and "+time2+":00:00"+"   ---");
                        Toast.makeText(ContextRecordPopUp.this,"Hour 3 Context Taken", Toast.LENGTH_SHORT).show();
                    }

                    if(count==4)
                    {
                        selId4=Context1;
                        selConId4=Confidence1;
                        Date date=new Date();
                        writeToFile(date +","+hr1+","+hr2+ "," + selId1 + "," + selConId1 +","+ selId2 + "," + selConId2 +","+ selId3 + "," + selConId3 +","+ selId4 + "," + selConId4);
                        Toast.makeText(ContextRecordPopUp.this,"All Choices Recorded", Toast.LENGTH_SHORT).show();
                        //change begin
                        Intent intent1 = new Intent(ContextRecordPopUp.this,UploadData.class);
                        startService(intent1);
                        //change end
                        finishAndRemoveTask();
                    }
                }
                catch(Exception e) {
                    Toast.makeText(ContextRecordPopUp.this,"Please select your context", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public String getContextc(RadioButton radioContextButton) {
        String Context="";
        String Context_string = (String)radioContextButton.getText();

        switch(Context_string) {
            case "Static (Formal gathering)": Context="Static (Formal gathering)";
                break;
            case "Static (Informal gathering)": Context="Static (Informal gathering)";
                break;
            case "Walking Alone": Context="Walking Alone";
                break;
            case "Walking in a Group": Context="Walking in a Group";
                break;
            case "In vehicle (3-wheeler)": Context="In vehicle (3-wheeler)";
                break;
            case "In vehicle (4-wheeler)": Context="In vehicle (4-wheeler)";
                break;
            case "In vehicle (Train)": Context="In vehicle (Train)";
                break;
            case "No Activity": Context="No Activity";
                break;
        }
        return Context;
    }

    public String getContextp(RadioButton radioContextButton) {
        String Context="";
        String Context_string = (String)radioContextButton.getText();

        switch(Context_string) {
            case "0-50": Context="0-50";
                break;
            case "50-70": Context="50-70";
                break;
            case "70-100": Context="70-100";
                break;
        }
        return Context;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(ContextRecordPopUp.this,"Please select your context", Toast.LENGTH_SHORT).show();
    }

    public void writeToFile(String message) {
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File intervalcontextDir = new File(sdCardRoot, "/KeystrokeAnalysis/DataFiles/");
        String IntervalContextFileName = "IntervalContext.txt";
        File LogFile = new File(intervalcontextDir, IntervalContextFileName);
        FileWriter LogWriter = null;
        try {
            LogWriter = new FileWriter(LogFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        out = new BufferedWriter(LogWriter);
        try {
            out.write(message + "\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}