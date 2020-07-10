import React, { useState, useEffect } from 'react';
import {
  ScrollView,
  View,
  Text,
  PermissionsAndroid,
  NativeModules
} from 'react-native';


// import nativeCalls from 'rn-device-information';
// import nativeCalls from 'react-native-my-library';

const MyLibraryBle = NativeModules.MyLibraryBle;

const App = () => {


  const [devicename, setDevieName] = useState("");
  const [devicenMacAddress, setDevieMacAddress] = useState("");
  const [latlong, setLatLong] = useState("");
  const [connectedTo, setConnectedTo] = useState("");
  const [connectedDeviceInfo, setConnectedDeviceInfo] = useState('');
  const [availableConnection, setAvailableConnection] = useState([]);

  const persmission = async () => {
    try {
      // permission to access location to set wifi connection
      const granted = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION)
        .then(res => {
          if (res === "granted") {
            console.log(" permission!");

          } else {
            console.log("You will not able to retrieve wifi available networks list");
          }
        });
    } catch (err) {
      console.warn(err)
    }
  }

  // useEffect(() => {


  //   nativeCalls.deviceNativeCall.getDeviceName((err, name) => {
  //     setDevieName(name);
  //   });

  //   nativeCalls.deviceNativeCall.getMacAddress((err, deviceMacAddress) => {
  //     setDevieMacAddress(deviceMacAddress);
  //   });

  //   nativeCalls.deviceNativeCall.getClientList((err, clientList) => {
  //     setConnectedDeviceInfo(clientList);

  //   });

  // }, []);

  useEffect(() => {
    persmission();
    MyLibraryBle.tryToTurnBluetoothOn().then(isBleOn => {
      console.log("TCL: App -> res", isBleOn)
      if(isBleOn!==undefined){
        if(isBleOn){
         console.log("TCL: App -> isBleOn", isBleOn);
         
             }
      }

    }).catch(err => {
      console.log("TCL: App -> err", err)

    });
   
    setInterval(() => {
      MyLibraryBle.getBleDevices((err, value) => {
        console.log("TCL: App ->getBleDevices value", value)
        console.log("TCL: App ->getBleDevices err", err)

      });
    }, 15000);
  }, [MyLibraryBle]);

  useEffect(() => {

    MyLibraryBle.getListOfPairedDevices((err, value) => {
      console.log("TCL: App -> value", value)
    });

    // MyLibraryBle.getBleDevices((err, value) => {
    //   console.log("TCL: App -> getBleDevices value", value)
    //   console.log("TCL: App ->getBleDevices err", err)

    // });
  }, []);

  return (
    <View>
      <Text>Working with ble</Text>
    </View>
  )
  // return (
  //   <ScrollView>
  //     <View style={{ margin: 10 }}>

  //       <Text>
  //         {"Device name: " + devicename + "\n"}
  //       </Text>

  //       <Text>
  //         {"Device mac address: " + devicenMacAddress + "\n"}
  //       </Text>
  //       {latlong.location !== undefined
  //         ? <Text>
  //           {"latitude: " + latlong.location.latitude + " \nlongitude: " + latlong.location.longitude + " \ntime: " + latlong.location.time + "\n"}
  //         </Text>
  //         : <Text>Wait</Text>}

  //       {connectedTo !== "" && connectedTo.type === 'wifi'
  //         ? <View>
  //           <Text>{"connected type:   " + connectedTo.type}</Text>
  //           <Text>{"isConnected:   " + connectedTo.isConnected}</Text>
  //           <Text>{"isInternetReachable:   " + connectedTo.isInternetReachable}</Text>
  //           <Text>{"isWifiEnabled:   " + connectedTo.isWifiEnabled}</Text>

  //           <Text>{"bssid:   " + connectedTo.details.bssid}</Text>
  //           <Text>{"frequency:   " + connectedTo.details.frequency}</Text>
  //           <Text>{"ipAddress:   " + connectedTo.details.ipAddress}</Text>
  //           <Text>{"isConnectionExpensive:   " + connectedTo.details.isConnectionExpensive}</Text>
  //           <Text>{"ssid:   " + connectedTo.details.ssid}</Text>
  //           <Text>{"strength:   " + connectedTo.details.strength}</Text>
  //           <Text>{"subnet:   " + connectedTo.details.bssid + "\n"}</Text>
  //         </View>

  //         : connectedTo !== "" && connectedTo.type === 'cellular'
  //           ? <View>
  //             <Text>{"connected to:   " + connectedTo.type}</Text>

  //             <Text>{"carrier:   " + connectedTo.details.carrier}</Text>
  //             <Text>{"cellularGeneration:   " + connectedTo.details.cellularGeneration}</Text>
  //             <Text>{"isConnectionExpensive:   " + connectedTo.details.isConnectionExpensive + "\n"}</Text>


  //             <Text>{"List of device connected to mobile hotspot:\n"}</Text>
  //             <Text style={{ fontSize: 12 }}>{connectedDeviceInfo}</Text>
  //           </View>
  //           : <Text>no connection found</Text>}

  //       <Text >{"Available wifi Connection:\n"}</Text>
  //       {availableConnection.length > 0
  //         ? availableConnection.map((list, i) => {
  //           return (
  //             <View key={i}>
  //               <Text>{"BSSID:  " + list.BSSID}</Text>
  //               <Text>{"SSID:   " + list.SSID}</Text>
  //               <Text>{"capabilities:   " + list.capabilities}</Text>
  //               <Text>{"frequency:  " + list.frequency}</Text>
  //               <Text>{"level:  " + list.level}</Text>
  //               <Text>{"timestamp:  " + list.timestamp + "\n"}</Text>
  //             </View>
  //           )
  //         })

  //         : <Text>No connection available</Text>}

  //     </View>
  //   </ScrollView>

  // );
};

export default App;
