package research.type.keystrokeanalysis;

import java.util.Date;

import research.type.keystrokeanalysis.services.UploadData;

/**
 * Created by Adrija on 18-06-2018.
 */

public class LastUploaded extends UploadData {
    public int ludata;
    public Date cur_time;

    public int getLupdata() {
        return ludata;
    }

    public void setLupdata(int input) {
        this.ludata = input;
    }

    public Date getCurrTime() {
        return cur_time;
    }

    public void setCurrTime(Date cur_time) {
        this.cur_time=cur_time;
    }




}
