package com.example.rapha.teste1;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.DistanceSinceCCCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.PercentageObdCommand;
import com.github.pires.obd.commands.engine.RPMCommand;

import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.control.DistanceMILOnCommand;

import com.github.pires.obd.enums.ObdProtocols;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;



public class MainActivity extends AppCompatActivity {
    boolean button = true;
    BluetoothSocket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList deviceStrs = new ArrayList();
        final ArrayList devices = new ArrayList();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device: pairedDevices) {
                    deviceStrs.add(device.getName() + "\n" + device.getAddress());
                    devices.add(device.getAddress());
                }
            }
        }
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                String deviceAddress = devices.get(position).toString();
                // TODO save deviceAddress
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                try {
                    BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                    socket.connect();
                    try {

                        new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                        new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                        new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                        new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
                        TextView echo = (TextView)findViewById(R.id.echo);
                        echo.setText("echo off = OK Select Protocol AUTO = OK");

                        FuelLevelCommand fuel = new FuelLevelCommand();
                            fuel.run(socket.getInputStream(), socket.getOutputStream());

                    } catch (Exception e) {
                        // handle errors
                    }

                }catch (IOException e){

                }

            }
        });

        alertDialog.setTitle("Escolha o Dispositivo para conectar");
        alertDialog.show();

        final TextView horafinal = findViewById(R.id.HoraFinal);
        final TextView horainicial = findViewById(R.id.HoraInicial);
        final TextView kminicial = findViewById((R.id.kminicial));
        final Button clickButton = findViewById(R.id.IniciarTerminar);
        clickButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(button) {
                    //Iniciar Viagem
                    horainicial.setText(Calendar.getInstance().getTime().toString());
                    clickButton.setBackgroundColor(Color.parseColor("#f6c800"));
                    clickButton.setText("Finalizar");
                    kminicial.setText("Km inicial = "+ new DistanceMILOnCommand().getKm() + "KM");
                    button = false;

                    TextView fueltext = findViewById(R.id.fuel);
                    FuelLevelCommand fuel = new FuelLevelCommand();
                    DistanceSinceCCCommand km = new DistanceSinceCCCommand();

                    RPMCommand engineRpmCommand = new RPMCommand();
                    SpeedCommand speedCommand = new SpeedCommand();

                    while (!Thread.currentThread().isInterrupted())
                    {

                        try {
                            engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                            speedCommand.run(socket.getInputStream(), socket.getOutputStream());

                            fueltext.setText(engineRpmCommand.getFormattedResult());
                            speedCommand.getFormattedResult();


                            fueltext.setText(fuel.getFormattedResult());

                            /*fueltext.setText(Float.toString(fuel.getFuelLevel()));
                            kminicial.setText(km.getKm());*/

                        } catch (Exception e) {
                            fueltext.setText("NADA");
                        }
                    }

                }else{
                    //Terminar Viagem
                    horafinal.setText(Calendar.getInstance().getTime().toString());
                    clickButton.setBackgroundColor(Color.parseColor("#0f8f00"));
                    clickButton.setText("Iniciar");

                }
            }
        });
    }

    public void getInformacoes(){
        try {

            new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
            new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
            new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());

            TextView fuelText = (TextView)findViewById(R.id.fuel);
            fuelText.setText("Cheguei AQUI");
            new AmbientAirTemperatureCommand().run(socket.getInputStream(), socket.getOutputStream());
            fuelText.setText("OKAY");
            FuelLevelCommand fuel = new FuelLevelCommand();
            fuel.run(socket.getInputStream(), socket.getOutputStream());
            fuelText.setText("nivel do combustivel = "+ fuel.getResult()+fuel.getPercentage());

            int a = new DistanceMILOnCommand().getKm();

        } catch (Exception e) {
            // handle errors
        }
    }


    public void teste(){
        ArrayList deviceStrs = new ArrayList();
        final ArrayList devices = new ArrayList();

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device: pairedDevices) {
                    deviceStrs.add(device.getName() + "\n" + device.getAddress());
                    devices.add(device.getAddress());
                }
            }
        }

        // show list
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs.toArray(new String[deviceStrs.size()]));

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                String deviceAddress = devices.get(position).toString();
                // TODO save deviceAddress
                BluetoothSocket socket;
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                try {
                    BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                    socket.connect();

                    try {

                        new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                        new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                        new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                        new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());

                        TextView fuelText = (TextView)findViewById(R.id.fuel);
                        fuelText.setText("Cheguei AQUI");
                        new AmbientAirTemperatureCommand().run(socket.getInputStream(), socket.getOutputStream());
                        fuelText.setText("OKAY");
                        FuelLevelCommand fuel = new FuelLevelCommand();
                        fuel.run(socket.getInputStream(), socket.getOutputStream());
                        fuelText.setText("nivel do combustivel = "+ fuel.getResult()+fuel.getPercentage());

                        int a = new DistanceMILOnCommand().getKm();

                    } catch (Exception e) {
                        // handle errors
                    }


                }catch (IOException e){

                }

            }
        });
        alertDialog.setTitle("Escolha o Dispositivo para conectar");
        alertDialog.show();

    }


}
