package com.gpp.dmr_print.ConfiguracionS;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bixolon.commonlib.BXLCommonConst;
import com.bixolon.labelprinter.BixolonLabelPrinter;


import com.bixolon.commonlib.emul.image.LabelImage;
import com.bixolon.labelprinter.service.ServiceManager;



import com.example.tscdll.TSCActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.gpp.dmr_print.GRUB.MenuGRUB;
import com.gpp.dmr_print.InicioFash;
import com.gpp.dmr_print.MainActivity;
import com.gpp.dmr_print.R;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.setting.BitmapSetting;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.internal.CompressedBitmapOutputStreamCpcl;
import com.zebra.sdk.graphics.internal.DitheredImageProvider;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.internal.PrinterConnectionOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import honeywell.connection.ConnectionBase;
import honeywell.connection.Connection_Bluetooth;
import honeywell.printer.DocumentDPL;
import honeywell.printer.DocumentExPCL_LP;
import honeywell.printer.ParametersDPL;
import honeywell.printer.ParametersExPCL_LP;

public class Configuracion_Impresora extends AppCompatActivity {

    static final String CONNECTION_MODE_KEY = "com.gpp.dmr_print.ConfiguracionS.Connection_Mode_Key";
    static final String PRINTER_IPADDRESS_KEY = "com.gpp.dmr_print.ConfiguracionS.PRINTER_IPAddress_Key";

    static final String PRINTER_TCPIPPORT_KEY = "com.gpp.dmr_print.ConfiguracionS.PRINTER_TCPIPPort_Key";
    static final String BLUETOOTH_DEVICE_NAME_KEY = "com.gpp.dmr_print.ConfiguracionS.PRINTER_Bluetooth_Device_Name_Key";
    static final String BLUETOOTH_DEVICE_ADDR_KEY = "com.gpp.dmr_print.ConfiguracionS.PRINTER_Bluetooth_Device_Addr_Key";
    static final String USB_DEVICENAME = "com.gpp.dmr_print.ConfiguracionS.USB_DEVICENAME";
    static final String USB_PRODUCTOID = "com.gpp.dmr_print.ConfiguracionS.USB_PRODUCTOID";


    private Context mContext;
    private byte[] printData;
    private String m_printerIP = null;
    private String m_printerMAC = null;
    private int m_printerPort = 515;
    private String connectionType;
    private int m_printHeadWidth = 384; //572
    private int densidad = 10; //temperatura
    private int selectedItemIndex = 0;
    private FirebaseAuth firebaseauth;
    private FirebaseFirestore firebasefirestore;
    private FirebaseUser firebaseuser;
    private String UID;
    private String UIDEMAIL;
    ConnectionBase conn = null;
    static final int CONFIG_CONNECTION_REQUEST = 0; // for Connection Settings
    private ProgressDialog dialog;
    //Document and Parameter Objects

    private DocumentDPL docDPL;
    private DocumentExPCL_LP docExPCL_LP;


    private ParametersDPL paramDPL;
    private ParametersExPCL_LP paramExPCL_LP;


    private Handler m_handler = new Handler(); // Main thread

    private String m_printerMode = "";
    private int selectedItemHojas = 0;
    //====UI Controls========//
    //Buttons
    Button m_saveButton;

    String m_devicename;
    String m_productoid;

    Button m_configConnectionButton;
    RadioGroup grupohojas;
    RadioGroup grupomini;
    RadioButton m_unahoja;
    RadioButton m_doshojas;

    RadioButton mini_uno;
    RadioButton mini_dos;
    RadioButton mini_tres;
    private EditText txtdensidad;
    RadioGroup grupoauto;
    RadioButton auto_no;
    RadioButton auto_si;
        EditText txttimeout;

    //EditText
    TextView m_connectionInfoStatus;

    Spinner m_connectionSpinner;
    Spinner m_printerModeSpinner;
    Spinner m_printHeadSpinner;
    Boolean estado_configuracion;
    private int mini = 0;

    private int automatico = 0;

    private String direcciontestprint = "";

    String ApplicationConfigFilename = "applicationconfigg.dat";

    DMRPrintSettings g_appSettings = new DMRPrintSettings("", "", 0, "/", "", 0, 0, 0, 0, 0, 0, 0, "", "", false,0,10000);
    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1;
    final Context c = this;



    private Bitmap mBitmap = null;
    private File Filetemp = null;
    int heigth_calculator, wigth_calculator;
    TSCActivity TscDll = new TSCActivity();

    private ServiceManager mServiceManager;
private int timeout = 10000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pedir_permiso_escritura();

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowHomeEnabled(true);


        mContext = this.getApplicationContext();


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        DMRPrintSettings appSettings = ReadApplicationSettingFromFile();
        if (appSettings != null) {
            g_appSettings = appSettings;
            m_printerIP = g_appSettings.getPrinterIP();
            m_printerMAC = g_appSettings.getPrinterMAC();
            m_printerPort = g_appSettings.getPrinterPort();
            m_printerMode = g_appSettings.getSelectedPrintMode();//2018 PH
            m_printHeadWidth = g_appSettings.getPrintheadWidth();//2018 PH
            connectionType = g_appSettings.getCommunicationType();//2018 PH
            selectedItemHojas = g_appSettings.getSelecciondehojas();
            selectedItemIndex = g_appSettings.getSelectedItemIndex();
            mini = g_appSettings.getMinimizar();
            automatico = g_appSettings.getAutomatico();
            m_devicename = g_appSettings.getDevicename();
            m_productoid = g_appSettings.getProductoid();
            estado_configuracion = g_appSettings.getEstadoconfiguracion();
            densidad = g_appSettings.getDensidad();
            timeout = g_appSettings.getTimeout();
        } else {
            m_printerMAC = "0";
        }

        //Inicializar firebase
        initFireBase();




        //======Mapping UI controls from our activity xml===========//
        m_connectionInfoStatus = (TextView) findViewById(R.id.communication_status_information);
        m_connectionSpinner = (Spinner) findViewById(R.id.connection_spinner);
        m_configConnectionButton = (Button) findViewById(R.id.configConn_button);
        m_printerModeSpinner = (Spinner) findViewById(R.id.printer_mode_spinner);

        txtdensidad = findViewById(R.id.txtdensidad);
        txttimeout = findViewById(R.id.edit_timeout);

        txttimeout.setText(""+ timeout);
        txtdensidad.setText(""+densidad);



        grupohojas = (RadioGroup) findViewById(R.id.grupoimpresion);
        m_unahoja = findViewById(R.id.unahoja);
        m_doshojas = findViewById(R.id.doshojas);

        grupomini = (RadioGroup) findViewById(R.id.grupominimizar);
        mini_uno = findViewById(R.id.mini_uno);
        mini_dos = findViewById(R.id.mini_dos);
        mini_tres = findViewById(R.id.mini_tres);

        grupoauto = (RadioGroup) findViewById(R.id.grupoauto);
        auto_no = findViewById(R.id.auto_no);
        auto_si = findViewById(R.id.auto_si);


        m_saveButton = (Button) findViewById(R.id.saveSettings_button);
        m_printHeadSpinner = (Spinner) findViewById(R.id.printHeadSpinner);


        m_connectionSpinner.setSelection(g_appSettings.getCommunicationMethod());
        m_printerModeSpinner.setSelection(g_appSettings.getSelectedModeIndex());
        m_printHeadSpinner.setSelection(g_appSettings.getPrintheadWidthIndex());


        // ------------------------------------------------
        // Event handler when user select communication method
        // -------------------------------------------------
        m_connectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                connectionType = m_connectionSpinner.getSelectedItem().toString();
                g_appSettings.setCommunicationType(connectionType);

                //=======Display correct connection information when user selects connection=====//
                if (connectionType.equals("TCP/IP")) {
                    if (m_printerIP.length() == 0) {
                        m_connectionInfoStatus.setText(R.string.connection_not_configured);
                    } else {
                        String printerInfo = "Printer's IP Address/Port: " + m_printerIP + ":" + Integer.toString(m_printerPort);
                        m_connectionInfoStatus.setText(printerInfo);
                    }
                } else if (connectionType.equals("Bluetooth")) {
                    if (m_printerMAC.length() == 0) {
                        m_connectionInfoStatus.setText(R.string.connection_not_configured);
                    } else {
                        String printerInfo = "Printer's MAC Address: " + m_printerMAC;
                        m_connectionInfoStatus.setText(printerInfo);
                    }
                }
                g_appSettings.setCommunicationMethod(m_connectionSpinner.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        // ------------------------------------------------
        // Handles when user presses connection config button
        // -------------------------------------------------
        m_configConnectionButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //==================Open Connection Configuration Activity=======================================//
                Intent connSettingsIntent = new Intent("com.gpp.dmr_print.ConfiguracionS.ConnectionSettingsActivity");

                Spinner connectionSpinner = (Spinner) findViewById(R.id.connection_spinner);
                String connectionType = connectionSpinner.getSelectedItem().toString();
                connSettingsIntent.putExtra(CONNECTION_MODE_KEY, connectionType);
                connSettingsIntent.putExtra(PRINTER_IPADDRESS_KEY, m_printerIP);
                connSettingsIntent.putExtra(PRINTER_TCPIPPORT_KEY, m_printerPort);
                connSettingsIntent.putExtra(BLUETOOTH_DEVICE_ADDR_KEY, m_printerMAC);
                connSettingsIntent.putExtra(USB_DEVICENAME, m_devicename);
                connSettingsIntent.putExtra(USB_PRODUCTOID, m_productoid);
                startActivityForResult(connSettingsIntent, CONFIG_CONNECTION_REQUEST);
            }
        });

        // ------------------------------------------------
        // Event handler when user select printer mode
        // -------------------------------------------------
        m_printerModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                //=======Display correct printer mode information when user selects connection=====//
                m_printerMode = m_printerModeSpinner.getSelectedItem().toString();
                g_appSettings.setSelectedModeIndex(m_printerModeSpinner.getSelectedItemPosition());
                g_appSettings.setSelectedPrintMode(m_printerModeSpinner.getSelectedItem().toString());

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                m_printerMode = m_printerModeSpinner.getSelectedItem().toString();
                g_appSettings.setSelectedModeIndex(m_printerModeSpinner.getSelectedItemPosition());

            }
        });
        // --------------------------------------------------
        // Event handler when user selects print head width
        // --------------------------------------------------
        m_printHeadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String value = (String) m_printHeadSpinner.getSelectedItem();
                m_printHeadWidth = Integer.parseInt(value.substring(0, 3));
                g_appSettings.setPrintheadWidthIndex(m_printHeadSpinner.getSelectedItemPosition());
                g_appSettings.setPrintheadWidth(m_printHeadWidth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                String value = (String) m_printHeadSpinner.getSelectedItem();
                m_printHeadWidth = Integer.parseInt(value.substring(0, 3));
                g_appSettings.setPrintheadWidth(m_printHeadWidth);
            }
        });


        grupoauto.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (auto_no.isChecked()) {

                    g_appSettings.setAutomatico(0);
                } else if (auto_si.isChecked()) {

                    g_appSettings.setAutomatico(1);
                }


            }
        });


        grupomini.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mini_uno.isChecked()) {

                    g_appSettings.setMinimizar(0);
                } else if (mini_dos.isChecked()) {

                    g_appSettings.setMinimizar(1);
                } else if (mini_tres.isChecked()) {

                    g_appSettings.setMinimizar(2);
                }
            }
        });

        grupohojas.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (m_unahoja.isChecked()) {
                    g_appSettings.setSelecciondehojas(0);
                } else if (m_doshojas.isChecked()) {
                    g_appSettings.setSelecciondehojas(1);
                }
            }
        });


        m_saveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                docExPCL_LP = new DocumentExPCL_LP(3);
                paramExPCL_LP = new ParametersExPCL_LP();


                if (!txtdensidad.getText().toString().isEmpty()){

                    timeout = Integer.parseInt(txttimeout.getText().toString());
                    densidad = Integer.parseInt(txtdensidad.getText().toString());

                    if (timeout<1500){
                        timeout=5000;
                    }

                    if (densidad>= 0 && timeout>0){
                        g_appSettings.setDensidad(densidad);
                        g_appSettings.setTimeout(timeout);
                        ValidarSerialMac();
                    }else{
                        showDialog("ERROR : La densidad  es de 0 a 15 o timeout entre 5000 y 30000");
                    }

                }else{
                    showDialog("No puede dejar la densidad en blanco");
                }

            }

        });


        if (automatico == 0) {
            auto_no.setChecked(true);
        } else {
            auto_si.setChecked(true);
        }


        if (mini == 0) {
            mini_uno.setChecked(true);
        } else if (mini == 1) {
            mini_dos.setChecked(true);
        } else {
            mini_tres.setChecked(true);
        }


        if (selectedItemHojas == 0) {
            m_unahoja.setChecked(true);
        } else {
            m_doshojas.setChecked(true);
        }

    }


    private void initFireBase() {

        firebaseauth = FirebaseAuth.getInstance();
        firebasefirestore = FirebaseFirestore.getInstance();
        firebaseuser = firebaseauth.getCurrentUser();

        mAuth = FirebaseAuth.getInstance();

        if (firebaseuser != null) {
            UIDEMAIL = firebaseuser.getEmail();
            UID = firebaseauth.getUid();
        } else {
            Log.w("ERROR:", "No existe un UID");
        }


    }

    private void ValidarSerialMac() {

        //TODO HACER UN TEST PRINT -> SI IMPRIME -> PEDIR SERIAL DEL EQUIPO ->
        // tesimpresion();
        preparardocumento();

        //TODO VALIDAR SERIAL INGRESADO EN FIREBASE TABLASERIALESGLOBAL   -> SI EXISTE ->

        //TODO VALIDAR  "MAC Y SERIAL" INGRESADO CONRTA REGISTRO EN FIREBASE TABLA_SERIAL_MAC_USUARIO ->

        // TODO SI EXISTEN -> GUARDA LA CONFIGURACION

        //TODO SI NO EXISTE -> REGISTRAR EN FIREBASE FIREBASE TABLA_SERIAL_MAC_USUARIO -> GUARDA LA CONFIGURACION

        //TODO SI NO COINCIDEN -> MOSTRAR MENSAJE Y VOLVER A REALIZAR TEST DE IMPRESION.

        // SaveApplicationSettingToFile();

    }

    private void pedir_permiso_escritura() {

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        int readExternalPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeExternalPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (writeExternalPermission != PackageManager.PERMISSION_GRANTED || readExternalPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_PERMISSION);
        }

    }
    private FirebaseAuth mAuth;



    private void pedirserialequipo() {

        m_handler.post(new Runnable() {
            public void run() {

                AlertDialog.Builder dialogo1 = new AlertDialog.Builder(Configuracion_Impresora.this);
                dialogo1.setTitle("Importante");
                dialogo1.setMessage("¿La impresora imprimió correctamente ?");
                dialogo1.setCancelable(false);
                dialogo1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {

                        if (mAuth.getCurrentUser().getEmail().equals("desarrollo@dmr.com.uy")){
                            SaveApplicationSettingToFile();
                        }else{
                            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
                            final View mView = layoutInflaterAndroid.inflate(R.layout.dialog_pedir_serial, null);
                            final AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
                            alertDialogBuilderUserInput.setView(mView);
                            final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);

                            alertDialogBuilderUserInput
                                    .setCancelable(false)
                                    .setPositiveButton("Validar", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogBox, int id) {


                                            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(Configuracion_Impresora.this);
                                            dialogo1.setTitle("Importante");
                                            dialogo1.setMessage("Confirme el numero de S/N  Con su impresora: " + userInputDialogEditText.getText().toString());
                                            dialogo1.setCancelable(false);
                                            dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialogo1, int id) {

                                                    EnableDialog(true, "Validando", "Validando número de serie");
                                                    validarSerialTablaFirebase(userInputDialogEditText.getText().toString());

                                                }
                                            });
                                            dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialogo1, int id) {


                                                }
                                            });
                                            dialogo1.show();


                                        }
                                    })

                                    .setNegativeButton("No",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialogBox, int id) {

                                                    dialogBox.cancel();


                                                }
                                            });

                            AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                            alertDialogAndroid.show();
                        }



                    }
                });
                dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogo1, int id) {


                    }
                });
                dialogo1.show();

            }// run()
        });


    }

    private void validarSerialTablaFirebase(final String nserie) {

        firebasefirestore.collection("TABLASERIES")
                .whereEqualTo("nserie", nserie.toUpperCase())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> resultado) {

                        if (resultado.getResult().size() > 0) {

                            validarserieymac(nserie);

                        } else {
                            showDialog("El numero de serie no coincide con nuestros equipos");
                            EnableDialog(false, "", "");
                        }
                    }
                });
    }

    private void validarserieymac(final String nserie) {

        firebasefirestore.collection("TABLASERIALMAC")
                .whereEqualTo("nserie", nserie)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> resultado) {

                        if (resultado.getResult().size() > 0) {

                            int existe = 0;

                            for (DocumentSnapshot tablaseialmac : resultado.getResult().getDocuments()) {

                                if (tablaseialmac.get("nmac").equals(m_printerMAC)) {

                                    if (tablaseialmac.get("idusuario").equals(UID)) {

                                        existe = 1;
                                        break;
                                    } else {

                                        existe = 2;

                                    }
                                }

                            }

                            if (existe == 1) {
                                SaveApplicationSettingToFile();
                            } else if (existe == 2) {
                                registrarTabla_Serial_Mac(nserie);
                                SaveApplicationSettingToFile();
                            } else {

                                showDialog("El numero de mac no coincide con ningun numero de serie registrado");
                            }

                            EnableDialog(false, "Enviando terminando...", "");

                        } else {
                            //TODO REGISTRAR
                            registrarTabla_Serial_Mac(nserie);

                        }

                    }
                });

    }

    private void registrarTabla_Serial_Mac(String nserie) {

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateOnly = new SimpleDateFormat("MM/dd/yyyy");

        Map<String, Object> docData = new HashMap<>();
        docData.put("idusuario", UID);
        docData.put("email", UIDEMAIL.toUpperCase());
        docData.put("nmac", m_printerMAC.toUpperCase());
        docData.put("nserie", nserie.toUpperCase());
        docData.put("fecha", dateOnly.format(cal.getTime()));

        // docData.put("listExample", Arrays.asList(1, 2, 3));
        // docData.put("nullExample", null);
/*
        Map<String, Object> nestedData = new HashMap<>();
        nestedData.put("a", 5);
        nestedData.put("b", true);
        docData.put("objectExample", nestedData);
*/


        firebasefirestore.collection("TABLASERIALMAC")
                .add(docData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        showToast("Se ha registrado exitosamente");
                        SaveApplicationSettingToFile();
                        EnableDialog(false, "", "");

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                showToast("Intentelo nuevamente");

                EnableDialog(false, "", "");
            }
        });

    }

    private void preparardocumento() {

        try {


            docDPL = new DocumentDPL();
            docExPCL_LP = new DocumentExPCL_LP(3);
            paramDPL = new ParametersDPL();
            paramExPCL_LP = new ParametersExPCL_LP();

            printData = new byte[]{0};
            //ICommandBuilder builder;
            File f = new File(getCacheDir() + "/temp.pdf");
            if (!f.exists()) {

                InputStream is = getAssets().open("ejemplo.pdf");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                FileOutputStream fos = new FileOutputStream(f);
                fos.write(buffer);
                fos.close();

            }


            switch (m_printerMode) {

                case "BIXOLON":

                    mBitmap = generateImageFromPdf(f.getPath(), 0, m_printHeadWidth, m_printerMode);
                    byte[] bitmapData = convertTo1BPP(mBitmap, 128);
                    Bitmap bitt = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
                    printBixolon(bitt);
                    break;

                case "ESCPOS":


                    printtscrun(f);


                    break;

                case "Apex":

                    mBitmap = generateImageFromPdf(f.getPath(), 0, m_printHeadWidth, m_printerMode);
                    if (mBitmap != null) {
                        byte[] bitmapData3 = convertTo1BPP(mBitmap, 128);
                        Bitmap bitt3 = BitmapFactory.decodeByteArray(bitmapData3, 0, bitmapData3.length);
                        docExPCL_LP.writeImage(bitt3,m_printHeadWidth);
                        docExPCL_LP.writeText("  ", paramExPCL_LP);
                        docExPCL_LP.writeText("  ", paramExPCL_LP);
                        docExPCL_LP.writeText("  ", paramExPCL_LP);
                        docExPCL_LP.writeText("  ", paramExPCL_LP);
                        docExPCL_LP.writeText("  ", paramExPCL_LP);
                        printData = docExPCL_LP.getDocumentData();
                    }

                    break;

                case "DPL":

                    docDPL.writePDF(f.getPath(), m_printHeadWidth, 0, 0);
                    docDPL.writeLine(0, 0, 10, 25);
                    docDPL.writeLine(0, 0, 10, 25);
                    docDPL.writeLine(0, 0, 10, 25);
                    printData = docDPL.getDocumentData();
                    break;

                case "CPCL":

                    mBitmap = generateImageFromPdf(f.getPath(), 0, 500, m_printerMode);
                    if (mBitmap != null) {
                        byte[] bitmapData3 = convertTo1BPP(mBitmap, 128);
                        Bitmap bitt3 = BitmapFactory.decodeByteArray(bitmapData3, 0, bitmapData3.length);
                        printPhotoFromExternal(bitt3);
                    }


                    break;

                case "TSC":

               try {
                        mBitmap = generateImageFromPdf(f.getPath(), 0, m_printHeadWidth,m_printerMode);
                        heigth_calculator = (int) (mBitmap.getHeight() / 8);
                        wigth_calculator = (int) (mBitmap.getWidth() / 8);

                        File f2 = new File(Environment.getExternalStorageDirectory().getPath() + "/Download/" + "/temp2.BMP");
                        byte[] bitmapData2 = convertTo1BPP(mBitmap, 128);
                        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapData2);

                        InputStream is = bs;
                        int size = is.available();
                        byte[] buffer = new byte[size];
                        is.read(buffer);
                        is.close();
                        FileOutputStream fos = new FileOutputStream(f2);
                        fos.write(buffer);
                        fos.close();

                        PrintBmpTsc();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;

            }

            if (m_printerMode.equals("Apex") || (m_printerMode.equals("DPL") )) {
                tesimpresion();
            }

        } catch (Exception e) {

            e.printStackTrace();
            String error = e.getMessage();
            if (error.contains("Unable to open file")) {
                String textoError = error.replace("Unable to open file", "Error: No se puede abrir el archivo");
                showToast(textoError);
            } else {
                showToast(e.getMessage());
            }

        }

    }

    private void printtscrun(File f) {


        Thread thread = new Thread() {
            @Override
            public void run() {
                try {

                    EnableDialog(true, "Enviando Documento...","ESCPOS");


                        mBitmap = generateImageFromPdf(f.getPath(), 0, m_printHeadWidth, m_printerMode);
                        if (mBitmap != null) {
                            byte[] bitmapData2 = convertTo1BPP(mBitmap, 128);
                            Bitmap bitt2 = BitmapFactory.decodeByteArray(bitmapData2, 0, bitmapData2.length);
                            CmdFactory cmdFactory = new EscFactory();
                            Cmd cmd = cmdFactory.create();
                            BitmapSetting bitmapSetting = new BitmapSetting();
                            bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);
                            bitmapSetting.setBimtapLimitWidth(72 * 8);
                            cmd.append(cmd.getBitmapCmd(bitmapSetting, bitt2));
                            printData = cmd.getAppendCmds();

                            TscDll.openport(m_printerMAC);
                            TscDll.sendcommand(printData);
                            TscDll.closeport(timeout);
                            pedirserialequipo();
                        }

                    EnableDialog(false, "Enviando terminando...","ESCPOS");

                } catch (Exception e) {
                    e.printStackTrace();
                    EnableDialog(false, "Imprimiendo test de prueba", "Imprimiendo");
                }
            }
        };
        thread.start();
    }

    private final Handler mHandlerer = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BixolonLabelPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BixolonLabelPrinter.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "conectado", Toast.LENGTH_SHORT).show();
                            break;

                        case BixolonLabelPrinter.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(), "conectando", Toast.LENGTH_SHORT).show();
                            break;

                        case BixolonLabelPrinter.STATE_NONE:
                            Toast.makeText(getApplicationContext(), "error conexion", Toast.LENGTH_SHORT).show();

                            break;
                    }
                    break;
            }
        }
    };

    private void printBixolon(Bitmap bitmap) {

        mServiceManager = new ServiceManager(getApplicationContext(), mHandlerer,  Looper.getMainLooper());

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {

                    EnableDialog(true, "Imprimiendo test de prueba", "Imprimiendo");
                    if (bitmap != null) {
                        LabelImage image = new LabelImage();
                        if (image.Load(bitmap)) {
                            image.MakeLD(0, 0, m_printHeadWidth,  0, 0, 50, 30);

                            mServiceManager.connect(m_printerMAC, BXLCommonConst._PORT_BLUETOOTH);
                            mServiceManager.Write(image.PopAll());
                            mServiceManager.executeCommand("P" + 1 + "," + 1, false);
                             pedirserialequipo();
                            //SaveApplicationSettingToFile();
                        }

                    }

                    EnableDialog(false, "Imprimiendo test de prueba", "Imprimiendo");

                } catch (Exception e) {
                    e.printStackTrace();
                    EnableDialog(false, "Imprimiendo test de prueba", "Imprimiendo");
                }
            }
        };
        thread.start();
    }

    private void PrintBmpTsc() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {

                    EnableDialog(true, "Imprimiendo test de prueba", "Imprimiendo");
                    TscDll.openport(m_printerMAC);
                    TscDll.setup(wigth_calculator, heigth_calculator, 4, densidad, 0, 0, 0);
                    TscDll.downloadbmp("temp2.BMP");



                    TscDll.clearbuffer();
                    TscDll.sendcommand("PUTBMP 10,10,\"temp2.BMP\"\n");
                    TscDll.printlabel(1, 1);
                    TscDll.closeport(timeout);
                     pedirserialequipo();
                    //SaveApplicationSettingToFile();
                    EnableDialog(false, "Imprimiendo test de prueba", "Imprimiendo");

                } catch (Exception e) {
                    e.printStackTrace();
                    EnableDialog(false, "Imprimiendo test de prueba", "Imprimiendo");
                }

                Log.e("DATOS CONFIGURACION:","Mac: " + m_printerMAC + " Ancho: " + wigth_calculator + " Largo: " + heigth_calculator + "Densidad: " + densidad );
            }
        };

        thread.start();


    }



    private void printPhotoFromExternal(final Bitmap bbr) {
        new Thread(new Runnable() {
            public void run() {
                try {

                    EnableDialog(true, "Imprimiendo test de prueba", "Imprimiendo");
                    Looper.prepare();
                    Connection connection = new BluetoothConnection(m_printerMAC);
                    connection.open();


                    ZebraImageAndroid imagenandroid = new ZebraImageAndroid(bbr);

                    int var2 = 0;
                    int var3 = 0;
                    int iamodificado = (imagenandroid.getWidth() + 7) / 8;

                    try {

                        ByteArrayOutputStream var9 = new ByteArrayOutputStream();
                        String var10 = "! 0 200 200 " + imagenandroid.getHeight() + " 1\r\n";
                        String var11 = "PRINT\r\n";

                        var9.write(var10.getBytes());
                        var9.write("CG ".getBytes());
                        var9.write(String.valueOf(iamodificado).getBytes());
                        var9.write(" ".getBytes());
                        var9.write(String.valueOf(imagenandroid.getHeight()).getBytes());
                        var9.write(" ".getBytes());
                        var9.write(String.valueOf(var2).getBytes());
                        var9.write(" ".getBytes());
                        var9.write(String.valueOf(var3).getBytes());
                        var9.write(" ".getBytes());

                        connection.write(var9.toByteArray());

                        PrinterConnectionOutputStream var12 = new PrinterConnectionOutputStream(connection);
                        CompressedBitmapOutputStreamCpcl var13 = new CompressedBitmapOutputStreamCpcl(var12);
                        DitheredImageProvider.getDitheredImage(imagenandroid, var13);

                        var13.close();
                        var12.close();

                        connection.write("\r\n".getBytes());
                        connection.write(var11.getBytes());

                         pedirserialequipo();

                    } catch (Exception var14) {
                        throw new ConnectionException(var14.getMessage());
                    }

                    connection.close();
                    EnableDialog(false, "Imprimiendo test de prueba", "Imprimiendo");

                } catch (ConnectionException e) {

                } finally {
                    bbr.recycle();
                    Looper.myLooper().quit();
                }
            }
        }).start();

    }

    private Bitmap generateImageFromPdf(String assetFileName, int pageNumber, int printHeadWidth, String lenguaje) {

        PdfiumCore pdfiumCore = new PdfiumCore(Configuracion_Impresora.this);
        try {
            File f = new File(assetFileName);
            ParcelFileDescriptor fd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
            pdfiumCore.openPage(pdfDocument, pageNumber);
            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);

            float scale = (float) printHeadWidth / width;
            float scaledWidth = width * scale;
            float scaledHeight = height * scale;
            Bitmap bmp = null;
            bmp = Bitmap.createBitmap((int) scaledWidth, (int) scaledHeight, Bitmap.Config.ARGB_8888);
            int width2 = (int) scaledWidth;
            int height2 = (int) scaledHeight;

            pdfiumCore.getClass();
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width2, height2);
            pdfiumCore.closeDocument(pdfDocument);

            return bmp;

        } catch (Exception e) {
            //todo with exception
        }
        return null;
    }

    private byte[] intToDWord(int parValue) {
        byte[] retValue = new byte[]{(byte) (parValue & 255), (byte) (parValue >> 8 & 255), (byte) (parValue >> 16 & 255), (byte) (parValue >> 24 & 255)};
        return retValue;
    }

    private byte[] intToWord(int parValue) {
        byte[] retValue = new byte[]{(byte) (parValue & 255), (byte) (parValue >> 8 & 255)};
        return retValue;
    }

    private byte[] convertTo1BPP(Bitmap inputBitmap, int darknessThreshold) {
        int width = inputBitmap.getWidth();
        int height = inputBitmap.getHeight();
        ByteArrayOutputStream mImageStream = new ByteArrayOutputStream();
        int BITMAPFILEHEADER_SIZE = 14;
        int BITMAPINFOHEADER_SIZE = 40;
        short biPlanes = 1;
        short biBitCount = 1;
        int biCompression = 0;
        int biSizeImage = (width * biBitCount + 31 & -32) / 8 * height;
        int biXPelsPerMeter = 0;
        int biYPelsPerMeter = 0;
        int biClrUsed = 2;
        int biClrImportant = 2;
        byte[] bfType = new byte[]{66, 77};
        short bfReserved1 = 0;
        short bfReserved2 = 0;
        int bfOffBits = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE + 8;
        int bfSize = bfOffBits + biSizeImage;
        byte[] colorPalette = new byte[]{0, 0, 0, -1, -1, -1, -1, -1};
        int monoBitmapStride = (width + 31 & -32) / 8;
        byte[] newBitmapData = new byte[biSizeImage];

        try {
            mImageStream.write(bfType);
            mImageStream.write(this.intToDWord(bfSize));
            mImageStream.write(this.intToWord(bfReserved1));
            mImageStream.write(this.intToWord(bfReserved2));
            mImageStream.write(this.intToDWord(bfOffBits));
            mImageStream.write(this.intToDWord(BITMAPINFOHEADER_SIZE));
            mImageStream.write(this.intToDWord(width));
            mImageStream.write(this.intToDWord(height));
            mImageStream.write(this.intToWord(biPlanes));
            mImageStream.write(this.intToWord(biBitCount));
            mImageStream.write(this.intToDWord(biCompression));
            mImageStream.write(this.intToDWord(biSizeImage));
            mImageStream.write(this.intToDWord(biXPelsPerMeter));
            mImageStream.write(this.intToDWord(biYPelsPerMeter));
            mImageStream.write(this.intToDWord(biClrUsed));
            mImageStream.write(this.intToDWord(biClrImportant));
            mImageStream.write(colorPalette);
            int[] imageData = new int[height * width];
            inputBitmap.getPixels(imageData, 0, width, 0, 0, width, height);

            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    int pixelIndex = y * width + x;
                    int mask = 128 >> (x & 7);
                    int pixel = imageData[pixelIndex];
                    int R = Color.red(pixel);
                    int G = Color.green(pixel);
                    int B = Color.blue(pixel);
                    int A = Color.alpha(pixel);
                    boolean set = A < darknessThreshold || R + G + B > darknessThreshold * 3;
                    if (set) {
                        int index = (height - y - 1) * monoBitmapStride + (x >>> 3);
                        newBitmapData[index] = (byte) (newBitmapData[index] | mask);
                    }
                }
            }

            mImageStream.write(newBitmapData);
        } catch (Exception var36) {
            var36.printStackTrace();
        }

        return mImageStream.toByteArray();
    }

    private void tesimpresion() {

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {

                    EnableDialog(true, "Imprimiendo test de prueba", "Imprimiendo");

                    conn = null;
                    Looper.prepare();
                    conn = Connection_Bluetooth.createClient(m_printerMAC, false);
                    if (!conn.getIsOpen()) {
                        if (conn.open()) {
                            int bytesWritten = 0;
                            int bytesToWrite = 1024;
                            int totalBytes = printData.length;
                            int remainingBytes = totalBytes;
                            while (bytesWritten < totalBytes) {
                                if (remainingBytes < bytesToWrite)
                                    bytesToWrite = remainingBytes;
                                //Send data, 1024 bytes at a time until all data sent
                                conn.write(printData, bytesWritten, bytesToWrite);
                                bytesWritten += bytesToWrite;
                                remainingBytes = remainingBytes - bytesToWrite;
                                Thread.sleep(100);
                            }

                            conn.close();
                           pedirserialequipo();
                            //SaveApplicationSettingToFile();
                        }
                    }
                    EnableDialog(false, "Enviando terminando...", "");
                    DisplayPrintingStatusMessage("Impresión Exitosa.");

                } catch (Exception e) {

                    e.printStackTrace();
                    EnableDialog(false, "Enviando terminando...", "");
                    DisplayPrintingStatusMessage("Fallo conexion Bluetooth.");
                }

            }
        };
        thread.start();

    }


    public void DisplayPrintingStatusMessage(final String MsgStr) {

        m_handler.post(new Runnable() {
            public void run() {
                showToast(MsgStr);//2018 PH
            }// run()
        });

    }


    private void createCancelProgressDialog(String title, String message) {
        dialog = new ProgressDialog(this);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void EnableDialog(final boolean value, final String mensaje, final String titulo) {
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                if (value) {
                    createCancelProgressDialog(titulo, mensaje);
                } else {
                    if (dialog != null)
                        dialog.dismiss();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case CONFIG_CONNECTION_REQUEST: {
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();

                    if (extras != null) {
                        //===============Get data from Bluetooth configuration=================//
                        if (connectionType.equals("Bluetooth")) {
                            String btAddressString = extras.getString(BLUETOOTH_DEVICE_ADDR_KEY);
                            if (btAddressString != null)
                                m_printerMAC = btAddressString.toUpperCase(Locale.US);
                            if (!m_printerMAC.matches("[0-9A-fa-f:]{17}")) {
                                m_printerMAC = formatBluetoothAddress(m_printerMAC);
                            }

                            String printerInfo = "Printer's MAC Address: " + m_printerMAC;
                            m_connectionInfoStatus.setText(printerInfo);
                        }
                        //==============Get data from TCP/IP configuration===================//
                        else if (connectionType.equals("TCP/IP")) {

                            m_printerIP = extras.getString(PRINTER_IPADDRESS_KEY);
                            m_printerPort = extras.getInt(PRINTER_TCPIPPORT_KEY);

                            String printerInfo = "Printer's IP Address/Port: " + m_printerIP + ":" + Integer.toString(m_printerPort);
                            m_connectionInfoStatus.setText(printerInfo); //valid values are 3inch

                        }

                        g_appSettings.setDevicename(m_devicename);
                        g_appSettings.setProductoid(m_productoid);
                        g_appSettings.setPrinterMAC(m_printerMAC);
                        g_appSettings.setPrinterIP(m_printerIP);
                        g_appSettings.setPrinterPort(m_printerPort);

                    }
                }
                break;
            }
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //reload your ScrollBars by checking the newConfig

    }

    // -------------------------------------------------
    // Read the application configuration information from a file.
    // -------------------------------------------------
    DMRPrintSettings ReadApplicationSettingFromFile() {
        DMRPrintSettings ret = null;
        InputStream instream;
        try {
            //showToast("Loading configuration");
            showToast("Cargando configuración...");
            instream = openFileInput(ApplicationConfigFilename);
        } catch (FileNotFoundException e) {

            Log.e("DOPrint", e.getMessage(), e);
            //showToast("No configuration loaded");
            showToast("No hay configuración para cargar...");
            return null;
        }

        try {
            ObjectInputStream ois = new ObjectInputStream(instream);

            try {
                ret = (DMRPrintSettings) ois.readObject();
            } catch (ClassNotFoundException e) {
                Log.e("DOPrint", e.getMessage(), e);
                ret = null;
            }
        } catch (Exception e) {
            Log.e("DOPrint", e.getMessage(), e);
            ret = null;
        } finally {
            try {
                if (instream != null)
                    instream.close();
            } catch (IOException ignored) {
            }
        }
        return ret;
    }


    public boolean SaveApplicationSettingToFile() {

        boolean bRet = true;
        FileOutputStream fos = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            // write the object to the output stream object.
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(g_appSettings);

            // convert the output stream object to array of bytes
            byte[] buf = bos.toByteArray();

            // write the array of bytes to file output stream
            fos = openFileOutput(ApplicationConfigFilename,
                    Context.MODE_PRIVATE);
            fos.write(buf);

            File f = getDir(ApplicationConfigFilename, 0);
            Log.e("DOPrint", "Save Application settings to file: " + f.getName());
            //showToast("Application Settings saved");
           // showToast("Configuración guardada correctamente.");

            finish();
        } catch (IOException ioe) {
            Log.e("DOPrint", "error", ioe);
            showToast(ioe.getMessage());
            bRet = false;
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException ioe) {

                showToast(ioe.getMessage());
            }
        }
        return bRet;
    }// SaveApplicationSettingToFile()

    public void showToast(final String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }

    public void showDialog(final String toast) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(toast);
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }


    public String formatBluetoothAddress(String bluetoothAddr) {
        //Format MAC address string
        StringBuilder formattedBTAddress = new StringBuilder(bluetoothAddr);
        for (int bluetoothAddrPosition = 2; bluetoothAddrPosition <= formattedBTAddress.length() - 2; bluetoothAddrPosition += 3)
            formattedBTAddress.insert(bluetoothAddrPosition, ":");
        return formattedBTAddress.toString();
    }

}
