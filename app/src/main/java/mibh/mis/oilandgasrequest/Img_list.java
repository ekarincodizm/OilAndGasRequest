package mibh.mis.oilandgasrequest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import mibh.mis.oilandgasrequest.Camera.CameraMain;
import mibh.mis.oilandgasrequest.Data.PreferencesManager;
import mibh.mis.oilandgasrequest.Database.IMG_OILANDGAS;

/**
 * Created by ponlakiss on 07/16/2015.
 */
public class Img_list extends AppCompatActivity {

    RecyclerView recyclerView;
    Adapter adapter;
    CardView listItem;
    String[] arrName;
    static SparseBooleanArray mCheckStates;
    ArrayList<IMG_OILANDGAS.Image_tms> cursor;
    IMG_OILANDGAS ImgTms;
    PreferencesManager prefManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_list);
        listItem = (CardView) findViewById(R.id.cardimage_item);
        recyclerView = (RecyclerView) findViewById(R.id.image_recycleview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(Img_list.this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        ImgTms = new IMG_OILANDGAS(Img_list.this);

        prefManage = PreferencesManager.getInstance();

        setUpToolbar();

        arrName = getResources().getStringArray(R.array.img_list);
        mCheckStates = new SparseBooleanArray(arrName.length);

        cursor = ImgTms.Img_GetImageByGroupType(prefManage.getValue(prefManage.FUELID), ImgTms.IMG_FUEL);
        for (int i = 0; i < cursor.size(); i++) {
            int x = Integer.parseInt(cursor.get(i).Type_img);
            mCheckStates.put(x - 10, true);
        }
        ImgTms.close();

        if (adapter == null) {
            adapter = new Adapter(Img_list.this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

    }

    private void setUpToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    Img_list.this.finish();
                }
            });
        }
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.VersionViewHolder> {

        Context context;

        public Adapter(Context context) {
            this.context = context;
        }

        @Override
        public VersionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.img_item, viewGroup, false);
            VersionViewHolder viewHolder = new VersionViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final VersionViewHolder versionViewHolder, final int position) {

            versionViewHolder.imageTxt.setText(arrName[position]);
            versionViewHolder.imageMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Img_list.this, GridPic.class);
                    intent.putExtra("FUEL_DOCID", prefManage.getValue(prefManage.FUELID));
                    intent.putExtra("Type_Img", String.valueOf(10 + position));
                    startActivity(intent);
                }
            });
            versionViewHolder.imageClick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (prefManage.getValue(prefManage.latitude).equals("") || prefManage.getValue(prefManage.longtitude).equals("")) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Img_list.this);
                        alertDialogBuilder.setMessage("กรุณารอข้อมูล GPS สักครู่");
                        alertDialogBuilder.setNegativeButton("ตกลง",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        Intent intent = new Intent(Img_list.this, CameraMain.class);
                        intent.putExtra("FUEL_DOCID", prefManage.getValue(prefManage.FUELID));
                        intent.putExtra("MODE", ImgTms.Doc_name(String.valueOf(10 + position)));
                        intent.putExtra("From", ImgTms.IMG_FUEL);
                        intent.putExtra("Type_Img", String.valueOf(10 + position));
                        startActivityForResult(intent, position);
                    }
                }
            });
            if (!mCheckStates.get(position, false)) {
                versionViewHolder.imageStat.setImageResource(R.drawable.close_circle);
            } else versionViewHolder.imageStat.setImageResource(R.drawable.marked_circle_green);
        }

        @Override
        public int getItemCount() {
            return arrName == null ? 0 : arrName.length;
        }

        public class VersionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            CardView cardItemLayout;
            TextView imageTxt;
            ImageView imageStat, imageMore;
            View imageClick;

            public VersionViewHolder(View itemView) {
                super(itemView);
                cardItemLayout = (CardView) itemView.findViewById(R.id.cardimage_item);
                imageTxt = (TextView) itemView.findViewById(R.id.imageTxt);
                imageStat = (ImageView) itemView.findViewById(R.id.imagestat);
                imageMore = (ImageView) itemView.findViewById(R.id.imagemore);
                imageClick = itemView.findViewById(R.id.imageClick);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            mCheckStates.put(requestCode, true);
            adapter.notifyDataSetChanged();
        }
    }
}