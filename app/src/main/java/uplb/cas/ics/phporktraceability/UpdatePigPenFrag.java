package uplb.cas.ics.phporktraceability;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import helper.SQLiteHandler;

/**
 * Created by marmagno on 12/11/2015.
 */
public class UpdatePigPenFrag extends Fragment {

    private static final String LOGCAT = UpdatePigPenFrag.class.getSimpleName();
    ViewPager frag_viewPager;
    PagerAdapter frag_adapter;
    LinearLayout ll;
    LinearLayout bl;
    TextView tv_item;
    TextView tv_title;

    SQLiteHandler db;

    public final static String KEY_PIGID = "pig_id";
    public final static String KEY_BREED = "breed_name";
    public final static String KEY_FOSTER = "foster_sow";
    String[] lists = {};
    String[] lists2 = {};
    String[] lists3 = {};
    String[] ids = {};

    String pig_id = "";
    String foster_sow = "";
    String foster_disp = "Current Foster Parent: ";
    String title = "Update Foster Sow Parent";

    public UpdatePigPenFrag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View theView = inflater.inflate(R.layout.fragment_view, container, false);
        frag_viewPager = (ViewPager) theView.findViewById(R.id.frag_viewpager);

        return theView;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Intent i = getActivity().getIntent();
        pig_id = i.getStringExtra("pig_id");

        db = new SQLiteHandler(getActivity());

        loadLists();

        tv_item = (TextView) view.findViewById(R.id.tv_item);
        tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_title.setText(title);

        HashMap<String, String> b = db.getThePig(pig_id);
        if(!b.get(KEY_FOSTER).equals("null")) {
            foster_disp += getLabel(b.get(KEY_FOSTER));
        }
        else
            foster_disp += "None";
        tv_item.setText(foster_disp);

        bl = (LinearLayout) view.findViewById(R.id.bottom_container);

        bl.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent e) {
                int action = e.getAction();
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        Log.d(LOGCAT, "Drag event started");
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        Log.d(LOGCAT, "Drag event entered into " + v.toString());
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        Log.d(LOGCAT, "Drag event exited from " + v.toString());
                        break;
                    case DragEvent.ACTION_DROP:
                        TextView tv_drag = (TextView) view.findViewById(R.id.tv_dragHere);
                        View view2 = (View) e.getLocalState();
                        ViewGroup from = (ViewGroup) view2.getParent();
                        from.removeView(view2);
                        view2.invalidate();
                        LinearLayout to = (LinearLayout) v;
                        to.addView(view2);
                        to.removeView(tv_drag);
                        view2.setVisibility(View.VISIBLE);

                        int id = view2.getId();
                        foster_sow = view.findViewById(id).getTag().toString();

                        int vid = to.getId();
                        if (view.findViewById(vid) == view.findViewById(R.id.bottom_container)) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Updating Foster Sow Parent...")
                                    .setMessage("Confirm update?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int id) {
                                            db.updateSowParent(pig_id, foster_sow);
                                            Intent i = new Intent(getActivity(),
                                                    UpdatePigActivity.class);
                                            i.putExtra("pig_id", pig_id);
                                            i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            getActivity().finish();
                                            getActivity().startActivity(i);

                                            dialog.cancel();
                                        }

                                    })
                                    .setNegativeButton("No", null);

                            AlertDialog alert = builder.create();
                            alert.show();
                        }

                        Log.d(LOGCAT, "Dropped " + foster_sow);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        Log.d(LOGCAT, "Drag ended");
                        break;
                    default:
                        break;
                }
                return true;

            }
        });
    }

    private String getLabel(String _id){
        String result = "";

        int size = _id.length();
        String s = "0";
        size = 6 - size;
        for(int i = 0; i < size;i++){
            s = s + "0";
        }
        s = s + _id;
        String temp1 = s.substring(0,2);
        String temp2 = s.substring(3, 7);
        result = temp1 + "-" + temp2;
        return result;
    }

    public void loadLists(){

        ArrayList<HashMap<String, String>> theList = db.getSows();

        lists = new String[theList.size()];
        lists2 = new String[theList.size()];
        lists3 = new String[theList.size()];
        ids = new String[theList.size()];
        for(int i = 0;i < theList.size();i++)
        {
            HashMap<String, String> c = theList.get(i);
            String _id = getLabel(c.get(KEY_PIGID));
            lists[i] = "Sow: " + _id;
            lists2[i] = "Breed: " + c.get(KEY_BREED);
            lists3[i] = "";
            ids[i] = c.get(KEY_PIGID);
        }

        frag_adapter = new FragCustomPagerAdapter(getActivity(),
                lists, lists2, lists3, ids);
        frag_viewPager.setAdapter(frag_adapter);

    }

    @Override
    public void onStart(){
        super.onStart();

        loadLists();
    }

}