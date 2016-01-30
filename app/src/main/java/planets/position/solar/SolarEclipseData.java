package planets.position.solar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import planets.position.FragmentListener;
import planets.position.R;
import planets.position.database.PlanetsDatabase;
import planets.position.database.SolarEclipseTable;
import planets.position.util.JDUTC;
import planets.position.util.PositionFormat;

public class SolarEclipseData extends Fragment {

    private TextView seDateText, seTypeText, seStartText, seTStartText,
            seMaxText, seTEndText, seEndText, seAzText, seAltText, seLocalText, seLocalTypeText,
            seCoverText, seMagText, seSarosText, seSarosMText, seSunRiseText, seSunSetText;
    private LinearLayout seLocalVisible, seLocalLayout, seSunriseLayout;
    private String eclType, eclLocalType;
    private int local;
    private long solarNum = 0, eclDate, eclStart, eclEnd;
    private double offset, latitude, longitude, centerBegin, centerEnd, cover,
            mag;
    private static DateFormat mDateFormat, mTimeFormat;
    private PositionFormat pf;
    private PlanetsDatabase planetsDB;
    private FragmentListener mCallbacks;
    private JDUTC jdUTC;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_solar_data, container,
                false);
        seDateText = (TextView) v.findViewById(R.id.se_date);
        seTypeText = (TextView) v.findViewById(R.id.se_type);
        seStartText = (TextView) v.findViewById(R.id.se_start_text);
        seTStartText = (TextView) v.findViewById(R.id.se_tstart_text);
        seMaxText = (TextView) v.findViewById(R.id.se_max_text);
        seTEndText = (TextView) v.findViewById(R.id.se_tend_text);
        seEndText = (TextView) v.findViewById(R.id.se_end_text);
        seSunRiseText = (TextView) v.findViewById(R.id.se_sunrise_text);
        seSunSetText = (TextView) v.findViewById(R.id.se_sunset_text);
        seAzText = (TextView) v.findViewById(R.id.se_sun_az_text);
        seAltText = (TextView) v.findViewById(R.id.se_sun_alt_text);
        seLocalText = (TextView) v.findViewById(R.id.ecl_local);
        seLocalTypeText = (TextView) v.findViewById(R.id.ecl_local_type);
        seCoverText = (TextView) v.findViewById(R.id.se_cover_text);
        seMagText = (TextView) v.findViewById(R.id.se_mag_text);
        seSarosText = (TextView) v.findViewById(R.id.se_saros_text);
        seSarosMText = (TextView) v.findViewById(R.id.se_sarosm_text);
        seLocalVisible = (LinearLayout) v.findViewById(R.id.se_local_visible);
        seLocalLayout = (LinearLayout) v.findViewById(R.id.se_local_layout);
        seSunriseLayout = (LinearLayout) v.findViewById(R.id.sunrise_layout);
        jdUTC = new JDUTC();
        pf = new PositionFormat(getActivity().getApplicationContext());

        mDateFormat = android.text.format.DateFormat
                .getDateFormat(getActivity().getApplicationContext());
        mTimeFormat = android.text.format.DateFormat
                .getTimeFormat(getActivity().getApplicationContext());

        planetsDB = new PlanetsDatabase(getActivity().getApplicationContext());

        if (mCallbacks != null) {
            mCallbacks.onToolbarTitleChange("Solar Eclipse", 1);
        }

        if (savedInstanceState != null) {
            // load data from config change
            solarNum = savedInstanceState.getLong("solarNum");
            offset = savedInstanceState.getDouble("offset");
            latitude = savedInstanceState.getDouble("latitude");
            longitude = savedInstanceState.getDouble("longitude");
        } else {
            // load bundle from previous activity
            Bundle bundle = getArguments();
            if (bundle != null) {
                solarNum = bundle.getLong("solarNum");
                offset = bundle.getDouble("offset", 0);
                latitude = bundle.getDouble("latitude", 0);
                longitude = bundle.getDouble("longitude", 0);
            }
        }
        loadEclipse();
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getTargetFragment() == null) {
            // attach to PlanetsMain
            if (!(context instanceof FragmentListener)) {
                throw new IllegalStateException(
                        "Activity must implement the FragmentListener interface.");
            }
            mCallbacks = (FragmentListener) context;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("solarNum", solarNum);
        outState.putDouble("offset", offset);
        outState.putDouble("latitude", latitude);
        outState.putDouble("longitude", longitude);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        loadEclipse();
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.calendar_menu, menu);
        inflater.inflate(R.menu.map_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_calendar:
                // add eclipse to calendar
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setData(Events.CONTENT_URI);
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eclStart);
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, eclEnd);
                intent.putExtra(Events.TITLE, eclType + " Solar Eclipse");
                if (local > 0) {
                    String desc = "Local Type: " + eclLocalType;
                    desc += "\nSun Coverage: "
                            + String.format("%3.0f%%", cover * 100);
                    desc += "\nMagnitude: " + String.format("%.2f", mag);
                    intent.putExtra(Events.DESCRIPTION, desc);
                } else {
                    intent.putExtra(Events.DESCRIPTION,
                            "This eclipse is not visible locally.");
                }
                startActivity(intent);
                return true;
            case R.id.action_map:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                SolarEclipseMap solarMap = new SolarEclipseMap();
                Bundle args = new Bundle();
                args.putDouble("latitude", latitude);
                args.putDouble("longitude", longitude);
                args.putDouble("start", centerBegin);
                args.putDouble("end", centerEnd);
                args.putLong("date", eclDate);
                args.putString("type", eclType);
                solarMap.setArguments(args);
                ft.replace(R.id.content_frame, solarMap, "eclipseMap");
                ft.addToBackStack(null);
                ft.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadEclipse() {
        int planetColor;
        boolean lTotal, gTotal;
        double sunrise, sunset, temp;
        Calendar gc = new GregorianCalendar();

        planetsDB.open();
        Bundle b = planetsDB.getSolarEclipse(solarNum);
        planetsDB.close();

        planetColor = ContextCompat.getColor
                (getActivity().getApplicationContext(), R.color.planet_set_color);

        gc.setTimeInMillis(jdUTC.jdmills(b.getLong(SolarEclipseTable.COLUMN_ECLIPSE_DATE)));
        eclDate = gc.getTimeInMillis();
        seDateText.setText(mDateFormat.format(gc.getTime()));

        String type = b.getString(SolarEclipseTable.COLUMN_ECLIPSE_TYPE, "");
        if (type.contains("|")) {
            gTotal = type.split("\\|")[0].equals("Total");
            lTotal = type.split("\\|")[1].equals("Total");
            eclType = type.split("\\|")[0];
            eclLocalType = type.split("\\|")[1];
            seTypeText.setText(type.split("\\|")[0]);
            seLocalTypeText.setText(type.split("\\|")[1]);
        } else {
            gTotal = type.equals("Total");
            lTotal = false;
            eclType = type;
            eclLocalType = "";
            seTypeText.setText(type);
        }
        centerBegin = b.getDouble(SolarEclipseTable.COLUMN_GLOBAL_CENTER_BEGIN, 0);
        centerEnd = b.getDouble(SolarEclipseTable.COLUMN_GLOBAL_CENTER_END, 0);
        // partial eclipse path start and end
        if (centerBegin == 0 || centerEnd == 0) {
            centerBegin = b.getDouble(SolarEclipseTable.COLUMN_GLOBAL_BEGIN, 0);
            centerEnd = b.getDouble(SolarEclipseTable.COLUMN_GLOBAL_END, 0);
        }
        local = b.getInt(SolarEclipseTable.COLUMN_LOCAL, -1);
        if (local > 0) {
            // local eclipse
            sunrise = b.getDouble(SolarEclipseTable.COLUMN_SUNRISE, 0);
            if (sunrise > 0) {
                gc.setTimeInMillis(jdUTC.jdmills(sunrise, offset));
                seSunRiseText.setText(mTimeFormat.format(gc.getTime()));
            } else {
                seSunRiseText.setText("");
            }
            sunset = b.getDouble(SolarEclipseTable.COLUMN_SUNSET, 0);
            if (sunset > 0) {
                gc.setTimeInMillis(jdUTC.jdmills(sunset, offset));
                seSunSetText.setText(mTimeFormat.format(gc.getTime()));
            } else {
                seSunSetText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_LOCAL_FIRST, 0);
            if (temp > 0) {
                eclStart = jdUTC.jdmills(temp, offset);
                gc.setTimeInMillis(eclStart);
                seStartText.setText(mTimeFormat.format(gc.getTime()));
                if (temp < sunrise || temp > sunset)
                    seStartText.setTextColor(planetColor);
            } else {
                seStartText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_LOCAL_SECOND, 0);
            if (temp > 0 && lTotal) {
                gc.setTimeInMillis(jdUTC.jdmills(temp, offset));
                seTStartText.setText(mTimeFormat.format(gc.getTime()));
                if (temp < sunrise || temp > sunset)
                    seTStartText.setTextColor(planetColor);
            } else {
                seTStartText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_LOCAL_MAX, 0);
            if (temp > 0) {
                gc.setTimeInMillis(jdUTC.jdmills(temp, offset));
                seMaxText.setText(mTimeFormat.format(gc.getTime()));
                if (temp < sunrise || temp > sunset)
                    seMaxText.setTextColor(planetColor);
            } else {
                seMaxText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_LOCAL_THIRD, 0);
            if (temp > 0 && lTotal) {
                gc.setTimeInMillis(jdUTC.jdmills(temp, offset));
                seTEndText.setText(mTimeFormat.format(gc.getTime()));
                if (temp < sunrise || temp > sunset)
                    seTEndText.setTextColor(planetColor);
            } else {
                seTEndText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_LOCAL_FOURTH, 0);
            if (temp > 0) {
                eclEnd = jdUTC.jdmills(temp, offset);
                gc.setTimeInMillis(eclEnd);
                seEndText.setText(mTimeFormat.format(gc.getTime()));
                if (temp < sunrise || temp > sunset)
                    seEndText.setTextColor(planetColor);
            } else {
                seEndText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_SUN_AZ, 0);
            if (temp > 0) {
                seAzText.setText(pf.formatAZ(temp));
            } else {
                seAzText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_SUN_ALT, 0);
            if (temp > 0) {
                seAltText.setText(pf.formatALT(temp));
            } else {
                seAltText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_FRACTION_COVERED, 0);
            if (temp > 0) {
                cover = temp;
                seCoverText.setText(String.format("%3.0f%%", temp * 100));
            } else {
                cover = 0;
                seCoverText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_LOCAL_MAG, 0);
            if (temp > 0) {
                mag = temp;
                seMagText.setText(String.format("%.2f", temp));
            } else {
                mag = 0;
                seMagText.setText("");
            }
            seSarosText.setText(String.valueOf(b.getInt(SolarEclipseTable.COLUMN_SAROS_NUM, 0)));
            seSarosMText.setText(String.valueOf(b.getInt(SolarEclipseTable.COLUMN_SAROS_MEMBER_NUM, 0)));
        } else {
            // global eclipse
            temp = b.getDouble(SolarEclipseTable.COLUMN_GLOBAL_BEGIN, 0);
            if (temp > 0) {
                eclStart = jdUTC.jdmills(temp, offset);
                gc.setTimeInMillis(eclStart);
                seStartText.setText(mTimeFormat.format(gc.getTime()));
            } else {
                seStartText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_GLOBAL_TOTAL_BEGIN, 0);
            if (temp > 0 && gTotal) {
                gc.setTimeInMillis(jdUTC.jdmills(temp, offset));
                seTStartText.setText(mTimeFormat.format(gc.getTime()));
            } else {
                seTStartText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_GLOBAL_MAX, 0);
            if (temp > 0) {
                gc.setTimeInMillis(jdUTC.jdmills(temp, offset));
                seMaxText.setText(mTimeFormat.format(gc.getTime()));
            } else {
                seMaxText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_GLOBAL_TOTAL_END, 0);
            if (temp > 0 && gTotal) {
                gc.setTimeInMillis(jdUTC.jdmills(temp, offset));
                seTEndText.setText(mTimeFormat.format(gc.getTime()));
            } else {
                seTEndText.setText("");
            }
            temp = b.getDouble(SolarEclipseTable.COLUMN_GLOBAL_END, 0);
            if (temp > 0) {
                eclEnd = jdUTC.jdmills(temp, offset);
                gc.setTimeInMillis(eclEnd);
                seEndText.setText(mTimeFormat.format(gc.getTime()));
            } else {
                seEndText.setText("");
            }
            seLocalTypeText.setVisibility(View.GONE);
            seLocalText.setVisibility(View.GONE);
            seLocalLayout.setVisibility(View.GONE);
            seSunriseLayout.setVisibility(View.GONE);
            seLocalVisible.setVisibility(View.VISIBLE);
        }
    }
}
