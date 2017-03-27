/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package as.tra.brayden.blesim;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppBarActivity {

  // TODO(g-ortuno): Implement heart rate monitor peripheral
  public static final String[] PERIPHERALS_NAMES = new String[]{"Battery", "Heart Rate Monitor", "Color Picker", "UVIZIO LED"};
  public final static String EXTRA_PERIPHERAL_INDEX = "PERIPHERAL_INDEX";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_peripherals_list);


    JSONObject url = new JSONObject();
    try {
      url.put("m", "checkupdates");
      WebPostTask task = new WebPostTask(this, url) {
        @Override
        protected void callback(JSONObject result) {
        }
      };
      task.suppressErrors = false;
      task.execute();

    } catch (JSONException e) {

    }

    MyFragment fragment = new MyFragment();
    getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
  }



  public static class MyFragment extends ListFragment {

    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(),
        /* layout for the list item */ android.R.layout.simple_list_item_1,
        /* id of the TextView to use */ android.R.id.text1,
        /* values for the list */ PERIPHERALS_NAMES);
      setListAdapter(adapter);

    }

    public void onListItemClick(ListView listView, View view, int position, long id) {
      super.onListItemClick(listView, view, position, id);

      Intent intent = new Intent(this.getActivity(), PeripheralActivity.class);
      intent.putExtra(EXTRA_PERIPHERAL_INDEX, position);
      startActivity(intent);
    }
  }


}
