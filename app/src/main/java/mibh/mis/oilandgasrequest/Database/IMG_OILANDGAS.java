package mibh.mis.oilandgasrequest.Database;

import android.content.Context;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ponlakiss on 07/16/2015.
 */
public class IMG_OILANDGAS {

    private DB_MAIN db_main;

    public static final String IMG_FUEL = "FUEL";

    public static final String IMG_LOCATION = "20";
    public static final String IMG_DOC_FUEL = "21";
    public static final String IMG_DOC_RECEIPT = "22";
    public static final String IMG_CAR = "23";
    /*public static final String IMG_CAR_LICENSE = "24";*/
    public static final String IMG_PRICE = "24";
    public static final String IMG_FUELING = "25";
    public static final String IMG_OTHER = "26";

    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";


    public String Doc_name(String img_type) {
        String Dname = "";

        if (img_type.equalsIgnoreCase(this.IMG_LOCATION)) {
            Dname = "สถานี";
        }
        if (img_type.equalsIgnoreCase(this.IMG_DOC_FUEL)) {
            Dname = "ใบเบิกเชื้อเพลิง";
        }
        if (img_type.equalsIgnoreCase(this.IMG_DOC_RECEIPT)) {
            Dname = "ใบเสร็จ";
        }
        if (img_type.equalsIgnoreCase(this.IMG_CAR)) {
            Dname = "รถ";
        }
        /*if (img_type.equalsIgnoreCase(this.IMG_CAR_LICENSE)) {
            Dname = "ป้ายทะเบียน";
        }*/
        if (img_type.equalsIgnoreCase(this.IMG_PRICE)) {
            Dname = "ยอดการเติมที่หน้าตู้เติม";
        }
        if (img_type.equalsIgnoreCase(this.IMG_FUELING)) {
            Dname = "รูปขณะเติม";
        }
        if (img_type.equalsIgnoreCase(this.IMG_OTHER)) {
            Dname = "อื่นๆ";
        }

        return Dname;
    }

    public IMG_OILANDGAS(Context context) {

        db_main = new DB_MAIN(context);
        db_main.getWritableDatabase();

    }


    /****************************< Function comment >*************************/
    /** NAME		 : -			                                      	**/
    /** PARAMETERS	 : none		                                           	**/
    /** RETURN VALUE : none                                                	**/
    /** DESCRIPTION  : -					                               	**/
    /**
     * *********************************************************************
     */
    public static class Image_tms {
        public String WorkHid = "";
        public String Group_Type = "";
        public String Type_img = "";
        public String Filename = "";
        public String Doc_item = "";
        public String Lat_img = "";
        public String Lng_img = "";
        public String Date_img = "";
        public String Stat_Upd = "";
        public String Comment = "";

        Image_tms() {
        }
    }


    /****************************< Function comment >*************************/
    /** NAME		 : -			                                      	**/
    /** PARAMETERS	 : none		                                           	**/
    /** RETURN VALUE : none                                                	**/
    /** DESCRIPTION  : -					                               	**/
    /**
     * *********************************************************************
     */
    public ArrayList<Image_tms> Img_GetImageByDoc_item(String vWorkHid, String vDoc_item) {
        ArrayList<Image_tms> Images = new ArrayList<Image_tms>();

        Cursor c = db_main.GetImageByDoc_item(vWorkHid, vDoc_item);
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    Image_tms m = new Image_tms();

                    m.WorkHid = db_main.GetString(c, db_main.Im_WorkHid, "");
                    m.Group_Type = db_main.GetString(c, db_main.Im_Group_Type, "");
                    m.Type_img = db_main.GetString(c, db_main.Im_Type_img, "");
                    m.Filename = db_main.GetString(c, db_main.Im_Filename, "");
                    m.Doc_item = db_main.GetString(c, db_main.Im_Doc_item, "");
                    m.Lat_img = db_main.GetString(c, db_main.Im_Lat_img, "");
                    m.Lng_img = db_main.GetString(c, db_main.Im_Lng_img, "");
                    m.Date_img = db_main.GetString(c, db_main.Im_Date_img, "");
                    m.Stat_Upd = db_main.GetString(c, db_main.Im_Status_Update, "");
                    m.Comment = db_main.GetString(c, db_main.Im_Comment, "");

                    Images.add(m);

                } while (c.moveToNext());
            }
        }
        c.close();
        return Images;
    }

    /****************************< Function comment >*************************/
    /** NAME		 : -			                                      	**/
    /** PARAMETERS	 : none		                                           	**/
    /** RETURN VALUE : none                                                	**/
    /** DESCRIPTION  : -					                               	**/
    /**
     * *********************************************************************
     */
    public ArrayList<Image_tms> Img_GetImageByDoc_itemAndGroupType(String vWorkHid, String vGroup_Type, String vImage_Type) {
        ArrayList<Image_tms> Images = new ArrayList<Image_tms>();

        Cursor c = db_main.GetImageByGroupTypeAndItem(vWorkHid, vGroup_Type, vImage_Type);
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    Image_tms m = new Image_tms();

                    m.WorkHid = db_main.GetString(c, db_main.Im_WorkHid, "");
                    m.Group_Type = db_main.GetString(c, db_main.Im_Group_Type, "");
                    m.Type_img = db_main.GetString(c, db_main.Im_Type_img, "");
                    m.Filename = db_main.GetString(c, db_main.Im_Filename, "");
                    m.Doc_item = db_main.GetString(c, db_main.Im_Doc_item, "");
                    m.Lat_img = db_main.GetString(c, db_main.Im_Lat_img, "");
                    m.Lng_img = db_main.GetString(c, db_main.Im_Lng_img, "");
                    m.Date_img = db_main.GetString(c, db_main.Im_Date_img, "");
                    m.Stat_Upd = db_main.GetString(c, db_main.Im_Status_Update, "");
                    m.Comment = db_main.GetString(c, db_main.Im_Comment, "");

                    Images.add(m);

                } while (c.moveToNext());
            }
        }
        c.close();
        return Images;
    }

    /****************************< Function comment >*************************/
    /** NAME		 : -			                                      	**/
    /** PARAMETERS	 : none		                                           	**/
    /** RETURN VALUE : none                                                	**/
    /** DESCRIPTION  : -					                               	**/
    /**
     * *********************************************************************
     */
    public ArrayList<Image_tms> Img_GetImageByDoc_itemAndGroupTypeAndImg_Type(String vWorkHid, String vGroup_Type, String vDoc_item, String vImg_Type) {
        ArrayList<Image_tms> Images = new ArrayList<Image_tms>();

        Cursor c = db_main.GetImageByGroupTypeAndItemAndImgType(vWorkHid, vGroup_Type, vDoc_item, vImg_Type);
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    Image_tms m = new Image_tms();

                    m.WorkHid = db_main.GetString(c, db_main.Im_WorkHid, "");
                    m.Group_Type = db_main.GetString(c, db_main.Im_Group_Type, "");
                    m.Type_img = db_main.GetString(c, db_main.Im_Type_img, "");
                    m.Filename = db_main.GetString(c, db_main.Im_Filename, "");
                    m.Doc_item = db_main.GetString(c, db_main.Im_Doc_item, "");
                    m.Lat_img = db_main.GetString(c, db_main.Im_Lat_img, "");
                    m.Lng_img = db_main.GetString(c, db_main.Im_Lng_img, "");
                    m.Date_img = db_main.GetString(c, db_main.Im_Date_img, "");
                    m.Stat_Upd = db_main.GetString(c, db_main.Im_Status_Update, "");
                    m.Comment = db_main.GetString(c, db_main.Im_Comment, "");

                    Images.add(m);

                } while (c.moveToNext());
            }
        }
        c.close();
        return Images;
    }

    /****************************< Function comment >*************************/
    /** NAME		 : -			                                      	**/
    /** PARAMETERS	 : none		                                           	**/
    /** RETURN VALUE : none                                                	**/
    /** DESCRIPTION  : -					                               	**/
    /**
     * *********************************************************************
     */
    public ArrayList<Image_tms> Img_GetImageByGroupType(String vWorkHid, String vGroup_Type) {
        ArrayList<Image_tms> Images = new ArrayList<Image_tms>();

        Cursor c = db_main.GetImageByGroupType(vWorkHid, vGroup_Type);
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    Image_tms m = new Image_tms();

                    m.WorkHid = db_main.GetString(c, db_main.Im_WorkHid, "");
                    m.Group_Type = db_main.GetString(c, db_main.Im_Group_Type, "");
                    m.Type_img = db_main.GetString(c, db_main.Im_Type_img, "");
                    m.Filename = db_main.GetString(c, db_main.Im_Filename, "");
                    m.Doc_item = db_main.GetString(c, db_main.Im_Doc_item, "");
                    m.Lat_img = db_main.GetString(c, db_main.Im_Lat_img, "");
                    m.Lng_img = db_main.GetString(c, db_main.Im_Lng_img, "");
                    m.Date_img = db_main.GetString(c, db_main.Im_Date_img, "");
                    m.Stat_Upd = db_main.GetString(c, db_main.Im_Status_Update, "");
                    m.Comment = db_main.GetString(c, db_main.Im_Comment, "");

                    Images.add(m);

                } while (c.moveToNext());
            }
        }
        c.close();
        return Images;
    }

    /****************************< Function comment >*************************/
    /** NAME		 : -			                                      	**/
    /** PARAMETERS	 : none		                                           	**/
    /** RETURN VALUE : none                                                	**/
    /** DESCRIPTION  : -					                               	**/
    /**
     * *********************************************************************
     */
    public ArrayList<Image_tms> Img_GetImageFromFeild(String vFeild, String vFeildFilter) {
        ArrayList<Image_tms> Images = new ArrayList<Image_tms>();

        Cursor c = db_main.GetImageFromFeild(vFeild, vFeildFilter);
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    Image_tms m = new Image_tms();

                    m.WorkHid = db_main.GetString(c, db_main.Im_WorkHid, "");
                    m.Group_Type = db_main.GetString(c, db_main.Im_Group_Type, "");
                    m.Type_img = db_main.GetString(c, db_main.Im_Type_img, "");
                    m.Filename = db_main.GetString(c, db_main.Im_Filename, "");
                    m.Doc_item = db_main.GetString(c, db_main.Im_Doc_item, "");
                    m.Lat_img = db_main.GetString(c, db_main.Im_Lat_img, "");
                    m.Lng_img = db_main.GetString(c, db_main.Im_Lng_img, "");
                    m.Date_img = db_main.GetString(c, db_main.Im_Date_img, "");
                    m.Stat_Upd = db_main.GetString(c, db_main.Im_Status_Update, "");
                    m.Comment = db_main.GetString(c, db_main.Im_Comment, "");

                    Images.add(m);

                } while (c.moveToNext());
            }
        }
        c.close();
        return Images;
    }

    public ArrayList<Image_tms> Img_GetAllInactive() {
        ArrayList<Image_tms> Images = new ArrayList<Image_tms>();

        Cursor c = db_main.GetAllInactive();
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    Image_tms m = new Image_tms();

                    m.WorkHid = db_main.GetString(c, db_main.Im_WorkHid, "");
                    m.Group_Type = db_main.GetString(c, db_main.Im_Group_Type, "");
                    m.Type_img = db_main.GetString(c, db_main.Im_Type_img, "");
                    m.Filename = db_main.GetString(c, db_main.Im_Filename, "");
                    m.Doc_item = db_main.GetString(c, db_main.Im_Doc_item, "");
                    m.Lat_img = db_main.GetString(c, db_main.Im_Lat_img, "");
                    m.Lng_img = db_main.GetString(c, db_main.Im_Lng_img, "");
                    m.Date_img = db_main.GetString(c, db_main.Im_Date_img, "");
                    m.Stat_Upd = db_main.GetString(c, db_main.Im_Status_Update, "");
                    m.Comment = db_main.GetString(c, db_main.Im_Comment, "");

                    Images.add(m);

                } while (c.moveToNext());
            }
        }
        c.close();
        return Images;
    }

    public ArrayList<Image_tms> Img_GetActiveAndWoheader(String vWorkHid) {
        ArrayList<Image_tms> Images = new ArrayList<Image_tms>();

        Cursor c = db_main.GetActiveAndWoheader(vWorkHid);
        if (c.getCount() > 0) {
            if (c.moveToFirst()) {
                do {
                    Image_tms m = new Image_tms();

                    m.WorkHid = db_main.GetString(c, db_main.Im_WorkHid, "");
                    m.Group_Type = db_main.GetString(c, db_main.Im_Group_Type, "");
                    m.Type_img = db_main.GetString(c, db_main.Im_Type_img, "");
                    m.Filename = db_main.GetString(c, db_main.Im_Filename, "");
                    m.Doc_item = db_main.GetString(c, db_main.Im_Doc_item, "");
                    m.Lat_img = db_main.GetString(c, db_main.Im_Lat_img, "");
                    m.Lng_img = db_main.GetString(c, db_main.Im_Lng_img, "");
                    m.Date_img = db_main.GetString(c, db_main.Im_Date_img, "");
                    m.Stat_Upd = db_main.GetString(c, db_main.Im_Status_Update, "");
                    m.Comment = db_main.GetString(c, db_main.Im_Comment, "");

                    Images.add(m);

                } while (c.moveToNext());
            }
        }
        c.close();
        return Images;
    }

    public void deleteFilename(String vFilename) {
        db_main.deleteFilename(vFilename);
    }


    /****************************< Function comment >*************************/
    /** NAME		 : -			                                      	**/
    /** PARAMETERS	 : none		                                           	**/
    /** RETURN VALUE : none                                                	**/
    /** DESCRIPTION  : -					                               	**/
    /**
     * *********************************************************************
     */
    public void UpdateByFilename(String vFilename) {
         /* Create new img data */
        db_main.Update_status_Ct_Upload_req(vFilename);
    }


    /****************************< Function comment >*************************/
    /** NAME		 : -			                                      	**/
    /** PARAMETERS	 : none		                                           	**/
    /** RETURN VALUE : none                                                	**/
    /** DESCRIPTION  : -					                               	**/
    /**
     * *********************************************************************
     */
    public void SaveImg(String vWorkHid, String vGroup_Type, String vType_img, String vFilename, String vDoc_item, String vLat_img, String vLng_img, String vComment) {
         /* Create new img data */
        db_main.InsertImage(vWorkHid, vGroup_Type, vType_img, vFilename, vDoc_item, vLat_img, vLng_img, this.INACTIVE, vComment);

    }


    /****************************< Function comment >*************************/
    /** NAME		 : -			                                      	**/
    /** PARAMETERS	 : none		                                           	**/
    /** RETURN VALUE : BT+yymm+Ck_id + runing 2 digit                                               	**/
    /** DESCRIPTION  : -					                               	**/
    /**
     * *********************************************************************
     */
    public String Gen_imgName(String vTruck, String vGroup_type) {

		/* Add image capture time */
        Calendar c = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDate = date.format(c.getTime());
        return "I" + vGroup_type + "_" + formattedDate + "_" + vTruck + ".jpg";

    }

    public void close() {
        db_main.close();
    }

}
