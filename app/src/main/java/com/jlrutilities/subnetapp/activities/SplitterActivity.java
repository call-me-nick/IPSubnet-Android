package com.jlrutilities.subnetapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jlrutilities.subnetapp.R;
import com.jlrutilities.subnetapp.adapters.IpArrayAdapter;
import com.jlrutilities.subnetapp.models.BinaryTree;
import com.jlrutilities.subnetapp.models.Node;
import com.jlrutilities.subnetapp.models.SubnetCalculator;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirection;

public class SplitterActivity extends AppCompatActivity {

  protected static final String BINARY_IP_MESSAGE = "com.example.BINARYIP.Message";
  protected static final String ADDRESS_MESSAGE = "com.example.ADDRESS.MESSAGE";
  protected static final String CIDR_MESSAGE = "com.example.CIDR.MESSAGE";

  SubnetCalculator subnetCalc;
  BinaryTree tree;

  private Node[] nodes;
  private String[] nodeIps;
  private int[] nodeLocations = null;
  private int[] cidrArr;
  private int[] numOfHostsArr;
  private int pos = -1;

  private ListView list;
  SwipeActionAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splitter);

    subnetCalc = new SubnetCalculator();

    Intent intent = getIntent();
    String ipFormatted = intent.getStringExtra(MainActivity.IP_STRING_MESSAGE);
    String cidrString = intent.getStringExtra(MainActivity.CIDR_NETMASK_MESSAGE);

    int cidr = Integer.parseInt(cidrString);
    String ipBinary = subnetCalc.ipFormatToBinary(ipFormatted);
    String cutBinary = subnetCalc.trimCidrIp(ipBinary, cidr);
    ipFormatted = subnetCalc.ipBinaryToFormat(cutBinary);
    int numOfHosts = subnetCalc.numberOfHosts(cidr);

    tree = new BinaryTree();
    tree.setRoot(cidr, cutBinary, ipFormatted, numOfHosts);

    //tree list implementation
    list = findViewById(R.id.android_list);

    refreshList();

    //On click
    list.setOnItemClickListener((parent, view, position, id) -> {
      Intent intent1 = new Intent(getApplicationContext(), DescriptionActivity.class);

      int refPosition = nodeLocations[position];
      Node node = nodes[refPosition];
      int cidrIntent = node.getCidr();
      String address = node.getIpAddress();
      String binaryNum = node.getIpBinary();

      intent1.putExtra(CIDR_MESSAGE, cidrIntent);
      intent1.putExtra(ADDRESS_MESSAGE, address);
      intent1.putExtra(BINARY_IP_MESSAGE, binaryNum);

      startActivity(intent1);
    });
  }

  //finds parent node and tells parent remove
  private void merge(){
    int refPosition = nodeLocations[pos];
    Node node = nodes[refPosition];

    if ( node == tree.getRoot() ){
      Toast.makeText(getApplicationContext(),"Cannot merge root",
          Toast.LENGTH_SHORT).show();
      return;
    }

    Node parent = tree.findParent(node);

    if (parent == null){
      Toast.makeText(getApplicationContext(),"Unable to merge",
          Toast.LENGTH_SHORT).show();
      return;
    } else {
      tree.merge(parent);
      refreshList();
      Toast.makeText(getApplicationContext(),"Merged",
          Toast.LENGTH_SHORT).show();
    }
  }

  private void split(){
    int refPosition = nodeLocations[pos];
    Node node = nodes[refPosition];

    if (node.getLeft() == null && node.getRight() == null && node.cidr != 32) {
      String splitIp = subnetCalc.ipSplit(node.ipBinary, node.cidr);
      String formatIp = subnetCalc.ipBinaryToFormat(splitIp);
      int numOfHosts = subnetCalc.numberOfHosts(node.cidr+1);
      node.setLeft(node.cidr+1, node.ipBinary, node.ipAddress, numOfHosts);
      node.setRight(node.cidr+1, splitIp, formatIp, numOfHosts);

      refreshList();
      Toast.makeText(getApplicationContext(),"Split",
          Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(getApplicationContext(),"Cannot Split /32",
          Toast.LENGTH_SHORT).show();
    }
  }

  private void refreshList(){
    //Bottom Layer Nodes
    nodes = new Node[tree.size()];
    nodeIps = new String[tree.sizeBottomLayer()];
    nodeLocations = new int[nodeIps.length];
    cidrArr = new int[nodeIps.length];
    numOfHostsArr = new int[nodeIps.length];

    for(int i = 0; i < nodes.length; i++){
      nodes[i] = tree.nthPreordernode(i+1);
    }

    //Bottom Layer Nodes
    int counter = 0;
    for(int i = 0; i < nodes.length; i++) {
      Node node = nodes[i];
      if(node.getLeft() == null && node.getRight() == null){
        nodeIps[counter] = node.getIpAddress();
        cidrArr[counter] = node.getCidr();
        numOfHostsArr[counter] = node.getNumberOfHosts();
        nodeLocations[counter] = i;
        counter++;
      }
    }

    ArrayAdapter aa = new IpArrayAdapter(this, nodeIps, cidrArr, numOfHostsArr);
    mAdapter = new SwipeActionAdapter(aa);
    mAdapter.setListView(list);
    list.setAdapter(mAdapter);

    setSwipeFunctionality();
  }

  private void setSwipeFunctionality(){
    // Set backgrounds for the swipe directions
    mAdapter.addBackground(SwipeDirection.DIRECTION_NORMAL_LEFT,R.layout.row_bg_left)
        .addBackground(SwipeDirection.DIRECTION_NORMAL_RIGHT,R.layout.row_bg_right);

    mAdapter.setSwipeActionListener(new SwipeActionAdapter.SwipeActionListener(){
      @Override
      public boolean hasActions(int position, SwipeDirection direction){
        if(direction.isLeft()) return true;
        if(direction.isRight()) return true;
        return false;
      }

      @Override
      public boolean shouldDismiss(int position, SwipeDirection direction) {
        switch (direction) {
          case DIRECTION_NORMAL_LEFT:
          case DIRECTION_FAR_LEFT:
          case DIRECTION_NORMAL_RIGHT:
          case DIRECTION_FAR_RIGHT:
            return true;
          case DIRECTION_NEUTRAL:
            break;
        }
        return false;
      }

      @Override
      public void onSwipe(int[] positionList, SwipeDirection[] directionList) {
        for(int i=0;i<positionList.length;i++) {
          SwipeDirection direction = directionList[i];
          int position = positionList[i];

          switch (direction) {
            case DIRECTION_NORMAL_LEFT:
            case DIRECTION_FAR_LEFT:
              pos = position;
              merge();
              break;
            case DIRECTION_NORMAL_RIGHT:
            case DIRECTION_FAR_RIGHT:
              pos = position;
              split();
              break;
          }
        }
      }
    });
  }
}
